package com.example.blockfileextension.dto;

import com.example.blockfileextension.domain.Result;
import lombok.Getter;

@Getter
public class FileValidationResponse {
    private Result result;
    private String reason;

    public FileValidationResponse(Result result, String reason) {
        this.result = result;
        this.reason = reason;
    }
}
