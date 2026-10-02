package com.example.demo.repository;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/repositories")
public class RepositoryController {
    private final RepositoryService service;
    private final boolean registrationEnabled;

    public RepositoryController(RepositoryService service,
            @Value("${app.registration-enabled}") boolean registrationEnabled) {
        this.service = service;
        this.registrationEnabled = registrationEnabled;
    }

    public record RegisterRequest(@NotBlank @Size(max = 512) String url) { }
    public record RepositoryResponse(Long id, String url, String owner, String name, Instant createdAt) {
        static RepositoryResponse from(TrackedRepository repository) {
            return new RepositoryResponse(repository.getId(), repository.getUrl(), repository.getOwner(),
                    repository.getName(), repository.getCreatedAt());
        }
    }

    @GetMapping
    public List<RepositoryResponse> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("page ≥ 0; size từ 1 đến 100.");
        return service.list(page, size).stream().map(RepositoryResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<RepositoryResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!registrationEnabled) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Đăng ký đang tắt trên môi trường này.");
        }
        var repository = service.register(request.url());
        return ResponseEntity.created(URI.create("/api/repositories/" + repository.getId()))
                .body(RepositoryResponse.from(repository));
    }

    @GetMapping("/{id}")
    public RepositoryResponse get(@PathVariable long id) {
        return RepositoryResponse.from(service.get(id));
    }
}
