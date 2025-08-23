package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.*;
import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.dto.FileValidation;
import com.example.blockfileextension.dto.FixExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FileExtensionService {

    private final BlockedFileExtensionRepository blockedFileExtensionRepository;
    private final ExtensionFrequencyRepository extensionFrequencyRepository;

    public boolean isAllowedExtension(String ext) {
        return blockedFileExtensionRepository
                .findByExtension(ext.toLowerCase())
                .map(bfe -> !bfe.isBlocked())
                .orElse(true);
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

    public FileValidation validateFile(String filename) {
        String[] extensions = filename.split("\\.");

        for (int i = 1; i < extensions.length; i++) {
            String extension = String.join(".", Arrays.copyOfRange(extensions, i, extensions.length));
            if (!isAllowedExtension(extension)) {
                return new FileValidation(Result.BLOCKED, "차단된 확장자 " + extension);
            }
        }

        return new FileValidation(Result.ALLOWED, "차단되지 않은 확장자");
    }

    public void updateFixExtensions() {
        List<String> newFixExtensions = extensionFrequencyRepository.findTop7ByOrderByAddedCountDesc()
                .stream().map(ExtensionFrequency::getExtension).toList();
        List<String> currFixExtensions = blockedFileExtensionRepository.findByExtensionType(ExtensionType.FIX)
                .stream().map(BlockedFileExtension::getExtension).toList();

        // 기존과 새로 선정된 확장자의 겹치는 개수
        long sameCount = newFixExtensions.stream()
                .filter(currFixExtensions::contains)
                .count();

        // 새로 추가해야 하는 고정 확장자 수
        int newFixCount = 7 - (int) sameCount;

        List<String> currFixExtensionsDesc = extensionFrequencyRepository.findByExtensionInOrderByAddedCountDesc(currFixExtensions)
                .stream().map(ExtensionFrequency::getExtension).toList();

        List<String> toRemove = currFixExtensionsDesc
                .subList(currFixExtensionsDesc.size() - newFixCount, currFixExtensionsDesc.size());

        for (String ext : toRemove) {
            BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(ext)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 확장자입니다."));
            if (bfe.isBlocked()) {
                bfe.setExtensionType(ExtensionType.CUSTOM);
            } else {
                blockedFileExtensionRepository.delete(bfe);
            }
        }

        for (String newExt : newFixExtensions) {
            BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(newExt).orElse(null);

            if (bfe != null) {
                bfe.setExtensionType(ExtensionType.FIX);
            } else {
                BlockedFileExtension newFixedExtension = new BlockedFileExtension(newExt, ExtensionType.FIX, false);
                blockedFileExtensionRepository.save(newFixedExtension);
            }
        }
    }
}
