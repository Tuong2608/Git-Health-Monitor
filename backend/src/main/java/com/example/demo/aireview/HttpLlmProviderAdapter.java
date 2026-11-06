package com.example.demo.aireview;

import com.example.demo.gitanalyzer.GitIngestion;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Production {@link LlmReviewService.LLMProviderAdapter} calling an OpenAI-compatible
 * Chat Completions endpoint over bounded HTTP without tools or repository access.
 *
 * <p>Enforces remaining-deadline timeouts, cancellation checks, and streaming response
 * byte caps while reading the HTTP body so an upstream endpoint cannot buffer unbounded
 * data in memory. Never logs API keys, source excerpts, prompts, or raw model outputs.
 */
public final class HttpLlmProviderAdapter implements LlmReviewService.LLMProviderAdapter {

    public static final Duration MAX_PROVIDER_TIMEOUT = Duration.ofSeconds(30);
    public static final Duration VALIDATION_RESERVE = Duration.ofMillis(300);
    public static final int ENVELOPE_OVERHEAD_BYTES = 4 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    public record Config(
            String providerName,
            URI endpoint,
            String modelName,
            String apiKey,
            int maxModelInputTokens,
            int maxModelOutputTokens,
            Duration connectTimeout,
            Duration maxRequestTimeout,
            Duration validationReserve) {

        public Config {
            if (providerName == null || providerName.isBlank()) {
                throw new IllegalArgumentException("providerName must not be blank");
            }
            Objects.requireNonNull(endpoint, "endpoint");
            String scheme = endpoint.getScheme() == null ? "" : endpoint.getScheme().toLowerCase(Locale.ROOT);
            String host = endpoint.getHost() == null ? "" : endpoint.getHost().toLowerCase(Locale.ROOT);
            boolean loopback = "127.0.0.1".equals(host) || "localhost".equals(host) || "::1".equals(host);
            if (!"https".equals(scheme) && !("http".equals(scheme) && loopback)) {
                throw new IllegalArgumentException("Provider endpoint must use HTTPS (or loopback HTTP for tests)");
            }
            if (modelName == null || modelName.isBlank()) {
                throw new IllegalArgumentException("modelName must not be blank");
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("AI review provider API key must be configured when provider is enabled");
            }
            if (maxModelInputTokens < 1024 || maxModelOutputTokens < 128) {
                throw new IllegalArgumentException("Invalid model token budget configuration");
            }
            connectTimeout = connectTimeout == null ? Duration.ofSeconds(5) : connectTimeout;
            maxRequestTimeout = maxRequestTimeout == null ? MAX_PROVIDER_TIMEOUT : maxRequestTimeout;
            validationReserve = validationReserve == null ? VALIDATION_RESERVE : validationReserve;
        }

        @Override
        public String toString() {
            return "HttpLlmProviderAdapter.Config[provider=" + providerName
                    + ", endpoint=" + endpoint
                    + ", model=" + modelName
                    + ", apiKey=***REDACTED***]";
        }
    }

    private final Config config;
    private final HttpClient httpClient;

    public HttpLlmProviderAdapter(Config config) {
        this(
                config,
                HttpClient.newBuilder()
                        .connectTimeout(Objects.requireNonNull(config, "config").connectTimeout())
                        .followRedirects(HttpClient.Redirect.NEVER)
                        .build());
    }

    public HttpLlmProviderAdapter(Config config, HttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    public Config config() {
        return config;
    }

    @Override
    public LlmReviewService.ProviderResponse generate(
            LlmReviewService.RenderedPrompt prompt,
            LlmReviewService.ReviewBudget budget) throws Exception {
        Objects.requireNonNull(prompt, "prompt");
        Objects.requireNonNull(budget, "budget");

        checkNotCancelled(budget);

        int requestedOutputTokens = Math.min(budget.maxOutputTokens(), config.maxModelOutputTokens());
        if (prompt.estimatedInputTokens() + requestedOutputTokens > config.maxModelInputTokens()) {
            throw new GitIngestion.ExtractionException("MODEL_BUDGET_EXCEEDED");
        }

        Duration remaining = Duration.between(Instant.now(), budget.deadline())
                .minus(config.validationReserve());
        if (remaining.isZero() || remaining.isNegative()) {
            throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
        }
        Duration effectiveTimeout = remaining.compareTo(config.maxRequestTimeout()) < 0
                ? remaining
                : config.maxRequestTimeout();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", config.modelName());
        payload.put(
                "messages",
                List.of(
                        Map.of("role", "system", "content", prompt.systemMessage()),
                        Map.of("role", "user", "content", prompt.userMessage())));
        payload.put("temperature", 0.0);
        payload.put("max_tokens", requestedOutputTokens);
        payload.put("response_format", Map.of("type", "json_object"));

        String requestBody = JSON.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder(config.endpoint())
                .timeout(effectiveTimeout)
                .header("Authorization", "Bearer " + config.apiKey())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        final HttpResponse<InputStream> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (HttpTimeoutException e) {
            throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GitIngestion.ExtractionException("CANCELLED");
        }

        int maxEnvelopeBytes = Math.max(
                budget.maxOutputBytes() + ENVELOPE_OVERHEAD_BYTES,
                budget.maxOutputBytes());
        byte[] bodyBytes = readBoundedStream(response.body(), maxEnvelopeBytes, budget);

        if (response.statusCode() != 200) {
            throw new GitIngestion.ExtractionException("PROVIDER_HTTP_" + response.statusCode());
        }

        JsonNode root = JSON.readTree(new String(bodyBytes, StandardCharsets.UTF_8));
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new GitIngestion.ExtractionException("PROVIDER_INVALID_ENVELOPE");
        }
        JsonNode message = choices.get(0).get("message");
        JsonNode contentNode = message == null ? null : message.get("content");
        if (contentNode == null || !contentNode.isString()) {
            throw new GitIngestion.ExtractionException("PROVIDER_INVALID_ENVELOPE");
        }
        String rawJson = contentNode.asString();
        if (rawJson.getBytes(StandardCharsets.UTF_8).length > budget.maxOutputBytes()) {
            throw new GitIngestion.ExtractionException("PROVIDER_OUTPUT_TOO_LARGE");
        }

        String resolvedModel = root.hasNonNull("model") && root.get("model").isString()
                ? root.get("model").asString()
                : config.modelName();
        Integer inputTokens = null;
        Integer outputTokens = null;
        JsonNode usage = root.get("usage");
        if (usage != null && usage.isObject()) {
            if (usage.hasNonNull("prompt_tokens") && usage.get("prompt_tokens").isNumber()) {
                inputTokens = usage.get("prompt_tokens").intValue();
            }
            if (usage.hasNonNull("completion_tokens") && usage.get("completion_tokens").isNumber()) {
                outputTokens = usage.get("completion_tokens").intValue();
            }
        }

        return new LlmReviewService.ProviderResponse(
                rawJson, config.providerName(), resolvedModel, inputTokens, outputTokens);
    }

    private static byte[] readBoundedStream(
            InputStream stream,
            int maxBytes,
            LlmReviewService.ReviewBudget budget) throws Exception {
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int total = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                checkNotCancelled(budget);
                if (!Instant.now().isBefore(budget.deadline())) {
                    throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
                }
                total += read;
                if (total > maxBytes) {
                    throw new GitIngestion.ExtractionException("PROVIDER_RESPONSE_TOO_LARGE");
                }
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    private static void checkNotCancelled(LlmReviewService.ReviewBudget budget)
            throws GitIngestion.ExtractionException {
        if (Thread.currentThread().isInterrupted()
                || (budget.cancellation() != null && budget.cancellation().isCancelled())) {
            throw new GitIngestion.ExtractionException("CANCELLED");
        }
    }

    @Configuration
    static class ProviderConfiguration {
        @Bean
        @ConditionalOnMissingBean(LlmReviewService.LLMProviderAdapter.class)
        @ConditionalOnProperty(name = "app.ai-review.provider.enabled", havingValue = "true")
        LlmReviewService.LLMProviderAdapter httpLlmProviderAdapter(
                @Value("${app.ai-review.provider.name:mistral}") String providerName,
                @Value("${app.ai-review.provider.endpoint:https://api.mistral.ai/v1/chat/completions}") String endpoint,
                @Value("${app.ai-review.provider.model:codestral-latest}") String modelName,
                @Value("${app.ai-review.provider.api-key:}") String apiKey,
                @Value("${app.ai-review.provider.max-input-tokens:16384}") int maxInputTokens,
                @Value("${app.ai-review.provider.max-output-tokens:2048}") int maxOutputTokens,
                @Value("${app.ai-review.provider.connect-timeout-ms:5000}") long connectTimeoutMs,
                @Value("${app.ai-review.provider.request-timeout-ms:30000}") long requestTimeoutMs) {
            return new HttpLlmProviderAdapter(new Config(
                    providerName,
                    URI.create(endpoint),
                    modelName,
                    apiKey,
                    maxInputTokens,
                    maxOutputTokens,
                    Duration.ofMillis(Math.max(500L, connectTimeoutMs)),
                    Duration.ofMillis(Math.min(30_000L, Math.max(500L, requestTimeoutMs))),
                    VALIDATION_RESERVE));
        }
    }
}
