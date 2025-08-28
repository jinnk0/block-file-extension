package com.example.blockfileextension.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExtensionFrequency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String extension;

    private long addedCount;

    public ExtensionFrequency(String extension, long addedCount) {
        this.extension = extension;
        this.addedCount = addedCount;
    }

    public void setAddedCount() {
        this.addedCount += 1;
    }
}
