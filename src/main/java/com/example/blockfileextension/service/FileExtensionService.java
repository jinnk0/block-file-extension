package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.domain.BlockedFileExtensionRepository;
import com.example.blockfileextension.domain.ExtensionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileExtensionService {

    private final BlockedFileExtensionRepository blockedFileExtensionRepository;

    public boolean isAllowedExtension(String ext) {
        return blockedFileExtensionRepository.findByExtension(ext.toLowerCase()).isEmpty();
    }

    public BlockedFileExtension addCustomExtension(String ext) {
        if (blockedFileExtensionRepository.existsByExtension(ext)) {
            throw new IllegalArgumentException("이미 존재하는 확장자입니다.");
        }
        BlockedFileExtension addedExtension = new BlockedFileExtension(ext, ExtensionType.CUSTOM, true);
        return blockedFileExtensionRepository.save(addedExtension);
    }

    public List<BlockedFileExtension> getFixExtension() {
        return blockedFileExtensionRepository.findByExtensionType(ExtensionType.FIX);
    }

    public BlockedFileExtension changeStatus(String extension, boolean isBlocked) {
        BlockedFileExtension blockedFileExtension = blockedFileExtensionRepository
                .findByExtension(extension).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        blockedFileExtension.setIsBlocked(isBlocked);
        return blockedFileExtension;
    }
}
