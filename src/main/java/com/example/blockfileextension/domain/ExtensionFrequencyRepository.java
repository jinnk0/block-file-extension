package com.example.blockfileextension.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExtensionFrequencyRepository extends JpaRepository<ExtensionFrequency, Long> {
    List<ExtensionFrequency> findTop7ByOrderByAddedCountDesc();
    List<ExtensionFrequency> findByExtensionInOrderByAddedCountDesc(List<String> extensions);
    Optional<ExtensionFrequency> findByExtension(String extension);
}
