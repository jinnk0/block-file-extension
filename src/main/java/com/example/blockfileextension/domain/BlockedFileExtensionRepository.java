package com.example.blockfileextension.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockedFileExtensionRepository extends JpaRepository<BlockedFileExtension, Long> {
    Optional<BlockedFileExtension> findByExtension(String extension);
    boolean existsByExtension(String extension);
}
