package com.example.blockfileextension.dto;

import lombok.Getter;

@Getter
public class FileValidationRequest {
    private String filename;

    public FileValidationRequest(String filename) {
        this.filename = filename;
    }
}
