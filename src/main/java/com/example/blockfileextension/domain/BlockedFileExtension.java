package com.example.blockfileextension.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedFileExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String extension;

    private ExtensionType extensionType;

    private boolean isBlocked;

    public BlockedFileExtension(String extension, ExtensionType type, boolean isBlocked) {
        this.extension = extension.toLowerCase();
        this.extensionType = type;
        this.isBlocked = isBlocked;
    }
}
