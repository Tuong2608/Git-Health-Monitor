package com.example.demo.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.*;

class GitHubRepositoryUrlTests {
    @Test
    void canonicalizesCaseSuffixAndTrailingSlash() {
        assertThat(GitHubRepositoryUrl.parse(" https://GitHub.com/Tuong2608/Git-Health-Monitor.git/ ").url())
                .isEqualTo("https://github.com/tuong2608/git-health-monitor");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " ", "file:///etc/passwd", "http://github.com/a/b", "https://github.com.evil.test/a/b",
            "https://github.com@evil.test/a/b", "https://user@github.com/a/b", "https://github.com:443/a/b",
            "https://github.com/a/b?x=y", "https://github.com/a/b#x", "https://github.com/a/b/tree/main",
            "https://github.com/a/%2e%2e", "https://github.com/a/..", "https://github.com/a/.git"})
    void rejectsUnsupportedOrAmbiguousUrls(String value) {
        assertThatThrownBy(() -> GitHubRepositoryUrl.parse(value)).isInstanceOf(IllegalArgumentException.class);
    }
}
