package com.example.blockfileextension.dto;

import lombok.Getter;

@Getter
public class FixExtension {
    private String extension;
    private boolean isBlocked;

    public FixExtension(String extension, boolean isBlocked) {
        this.extension = extension;
        this.isBlocked = isBlocked;
    }
}
