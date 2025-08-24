package com.example.blockfileextension.controller;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.dto.CustomExtension;
import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.dto.FileValidation;
import com.example.blockfileextension.dto.FixExtension;
import com.example.blockfileextension.service.FileExtensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/files")
public class FileExtensionController {

    @Autowired
    private FileExtensionService fileExtensionService;

    @GetMapping("/extensions")
    public ResponseEntity<FileExtensions> getExtensions() {
        FileExtensions fileExtensions = fileExtensionService.getExtensions();
        return ResponseEntity.ok(fileExtensions);
    }

    @PatchMapping("/extensions")
    public ResponseEntity<Void> updateFixExtension(@RequestBody FixExtension fixExtension) {
        BlockedFileExtension bfe = fileExtensionService.changeStatus(fixExtension.getExtension(), fixExtension.isBlocked());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/extensions")
    public ResponseEntity<CustomExtension> addCustomExtension(@RequestBody CustomExtension customExtension) {
        BlockedFileExtension bfe = fileExtensionService.addCustomExtension(customExtension.getExtension());
        return ResponseEntity
                .created(URI.create("/files/extensions/" + bfe.getExtension()))
                .body(new CustomExtension(bfe.getExtension(), bfe.isBlocked()));
    }

    @DeleteMapping("/extensions/{extension}")
    public ResponseEntity<Void> deleteCustomExtension(@PathVariable String extension) {
        fileExtensionService.deleteCustomExtension(extension);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check")
    public ResponseEntity<FileValidationResponse> validateFileExtension(@RequestBody FileValidationRequest request) {
        FileValidationResponse validation = fileExtensionService.validateFile(request.getFilename());
        return ResponseEntity.ok(validation);
    }
}
