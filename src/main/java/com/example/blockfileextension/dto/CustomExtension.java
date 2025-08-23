package com.example.blockfileextension.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CustomExtension {
    private String extension;
    @JsonProperty("isBlocked")
    private boolean isBlocked;

    public CustomExtension(String extension, boolean isBlocked) {
        this.extension = extension;
        this.isBlocked = isBlocked;
    }
}
