package com.example.blockfileextension.controller;

import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.service.FileExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileExtensionController {

    @Autowired
    private FileExtensionService fileExtensionService;

    @GetMapping("/extensions")
    public ResponseEntity<FileExtensions> getExtensions() {
        FileExtensions fileExtensions = fileExtensionService.getExtensions();
        return new ResponseEntity<>(fileExtensions, HttpStatus.OK);
    }
}
