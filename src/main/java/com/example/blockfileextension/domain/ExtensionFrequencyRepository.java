package com.example.blockfileextension.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExtensionFrequencyRepository extends JpaRepository<ExtensionFrequency, Long> {
    List<ExtensionFrequency> findTop7ByOrderByAddedCountDesc();
    List<ExtensionFrequency> findByExtensionInOrderByAddedCountDesc(List<String> extensions);
}
