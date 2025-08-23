package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.domain.BlockedFileExtensionRepository;
import com.example.blockfileextension.domain.ExtensionType;
import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.dto.FixExtension;
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
        if (ext.length() > 20) {
            throw new IllegalArgumentException("확장자의 입력 길이가 20자를 넘었습니다.");
        }
        if (blockedFileExtensionRepository.findByExtensionType(ExtensionType.CUSTOM).size() >= 200) {
            throw new IllegalArgumentException("확장자의 개수가 200개를 넘었습니다.");
        }
        BlockedFileExtension addedExtension = new BlockedFileExtension(ext, ExtensionType.CUSTOM, true);
        return blockedFileExtensionRepository.save(addedExtension);
    }

    public List<BlockedFileExtension> getFixExtension() {
        return blockedFileExtensionRepository.findByExtensionType(ExtensionType.FIX);
    }

    public BlockedFileExtension changeStatus(String extension, boolean isBlocked) {
        BlockedFileExtension blockedFileExtension = blockedFileExtensionRepository.findByExtension(extension)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        blockedFileExtension.setIsBlocked(isBlocked);
        return blockedFileExtension;
    }

    public void deleteCustomExtension(String extension) {
        BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(extension)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        blockedFileExtensionRepository.delete(bfe);
    }

    public FileExtensions getExtensions() {
        List<BlockedFileExtension> fixedEntities = blockedFileExtensionRepository.findByExtensionType(ExtensionType.FIX);
        List<BlockedFileExtension> customEntities = blockedFileExtensionRepository.findByExtensionType(ExtensionType.CUSTOM);

        List<FixExtension> fixExtensions = fixedEntities.stream()
                .map(entity -> new FixExtension(entity.getExtension(), entity.isBlocked()))
                .toList();
        List<String> customExtensions = customEntities.stream()
                .map(BlockedFileExtension::getExtension)
                .toList();

        return new FileExtensions(fixExtensions, customExtensions);
    }
}
