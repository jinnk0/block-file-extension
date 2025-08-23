package com.example.blockfileextension.controller;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.dto.FixExtension;
import com.example.blockfileextension.service.FileExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/extensions")
    public ResponseEntity<Void> updateFixExtension(@RequestBody FixExtension fixExtension) {
        BlockedFileExtension bfe = fileExtensionService.changeStatus(fixExtension.getExtension(), fixExtension.isBlocked());
        return ResponseEntity.noContent().build();
    }
}
