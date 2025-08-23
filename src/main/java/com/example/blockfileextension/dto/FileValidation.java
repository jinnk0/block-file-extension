package com.example.blockfileextension.dto;

import com.example.blockfileextension.domain.Result;
import lombok.Getter;

@Getter
public class FileValidation {
    private Result result;
    private String reason;

    public FileValidation(Result result, String reason) {
        this.result = result;
        this.reason = reason;
    }
}
