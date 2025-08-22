package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.BlockedFileExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FileExtensionService {

    private final BlockedFileExtensionRepository blockedFileExtensionRepository;

    public boolean isAllowedExtension(String ext) {
        return blockedFileExtensionRepository.findByExtension(ext.toLowerCase()).isEmpty();
    }
}
