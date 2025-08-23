package com.example.blockfileextension.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class FileExtensions {
    private List<FixExtension> fixExtensions;
    private List<String> customExtensions;

    public FileExtensions(List<FixExtension> fixExtensions, List<String> customExtensions) {
        this.fixExtensions = fixExtensions;
        this.customExtensions = customExtensions;
    }
}
