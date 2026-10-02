package com.example.demo.repository;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tracked_repository")
public class TrackedRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 512)
    private String url;
    @Column(nullable = false, length = 100)
    private String owner;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TrackedRepository() { }

    public TrackedRepository(GitHubRepositoryUrl repositoryUrl) {
        url = repositoryUrl.url();
        owner = repositoryUrl.owner();
        name = repositoryUrl.name();
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getOwner() { return owner; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}
