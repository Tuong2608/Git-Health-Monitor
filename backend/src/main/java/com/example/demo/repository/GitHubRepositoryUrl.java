package com.example.demo.repository;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/** Syntax validation only: no network access or assertion that a repository is public. */
public record GitHubRepositoryUrl(String url, String owner, String name) {
    private static final Pattern PATH = Pattern.compile("^/([A-Za-z0-9](?:[A-Za-z0-9-]{0,38}))/([A-Za-z0-9_.-]{1,100})/?$");

    public static GitHubRepositoryUrl parse(String input) {
        try {
            var uri = URI.create(input.strip());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !"github.com".equalsIgnoreCase(uri.getHost())
                    || uri.getPort() != -1 || uri.getUserInfo() != null
                    || uri.getQuery() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException();
            }
            var matcher = PATH.matcher(uri.getRawPath());
            if (!matcher.matches()) throw new IllegalArgumentException();
            var owner = matcher.group(1).toLowerCase(Locale.ROOT);
            var name = matcher.group(2).toLowerCase(Locale.ROOT);
            if (name.endsWith(".git")) name = name.substring(0, name.length() - 4);
            if (name.isEmpty() || name.equals(".") || name.equals("..")) throw new IllegalArgumentException();
            return new GitHubRepositoryUrl("https://github.com/" + owner + "/" + name, owner, name);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalArgumentException("Chỉ chấp nhận URL dạng https://github.com/owner/repository.");
        }
    }
}
