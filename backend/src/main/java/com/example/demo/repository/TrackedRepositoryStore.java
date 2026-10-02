package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedRepositoryStore extends JpaRepository<TrackedRepository, Long> { }
