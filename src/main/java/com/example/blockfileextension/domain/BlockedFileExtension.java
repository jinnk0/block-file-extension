package com.example.blockfileextension.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockedFileExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String extension;

    @Enumerated(EnumType.STRING)
    private ExtensionType extensionType;

    private boolean isBlocked;

    public BlockedFileExtension(String extension, ExtensionType type, boolean isBlocked) {
        this.extension = extension.toLowerCase();
        this.extensionType = type;
        this.isBlocked = isBlocked;
    }
}
