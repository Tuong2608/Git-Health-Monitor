package com.example.demo.repository;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RepositoryService {
    private final TrackedRepositoryStore store;

    public RepositoryService(TrackedRepositoryStore store) { this.store = store; }

    @Transactional
    public TrackedRepository register(String url) {
        // Let the unique constraint also protect simultaneous requests.
        return store.saveAndFlush(new TrackedRepository(GitHubRepositoryUrl.parse(url)));
    }

    @Transactional(readOnly = true)
    public TrackedRepository get(long id) {
        return store.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<TrackedRepository> list(int page, int size) {
        return store.findAll(PageRequest.of(page, size, Sort.by("id").descending())).getContent();
    }
}
