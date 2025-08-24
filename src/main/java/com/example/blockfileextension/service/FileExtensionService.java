package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.*;
import com.example.blockfileextension.dto.FileExtensions;
import com.example.blockfileextension.dto.FileValidationResponse;
import com.example.blockfileextension.dto.FixExtension;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
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

    /**
     * 주어진 확장자가 차단된 확장자인지 확인
     *
     * @param extension 확인할 확장자 이름
     * @return 허용 여부 (true: 허용, false: 차단)
     * */
    public boolean isAllowedExtension(String extension) {
        return blockedFileExtensionRepository
                .findByExtension(extension.toLowerCase())
                .map(bfe -> !bfe.isBlocked())
                .orElse(true);
    }

    /**
     * 커스텀 확장자 추가
     *
     * @param extension 추가할 커스텀 확장자 이름
     * @return 추가된 확장자 객체
     * @throws IllegalArgumentException 중복 추가, 입력 제한 조건 초과
     * */
    public BlockedFileExtension addCustomExtension(String extension) {
        if (blockedFileExtensionRepository.existsByExtension(extension)) {
            throw new IllegalArgumentException("이미 존재하는 확장자입니다.");
        }
        if (extension.length() > 20) {
            throw new IllegalArgumentException("확장자의 입력 길이가 20자를 넘었습니다.");
        }
        if (blockedFileExtensionRepository.findByExtensionType(ExtensionType.CUSTOM).size() >= 200) {
            throw new IllegalArgumentException("확장자의 개수가 200개를 넘었습니다.");
        }
        BlockedFileExtension addedExtension = new BlockedFileExtension(extension, ExtensionType.CUSTOM, true);
        addCount(extension);
        return blockedFileExtensionRepository.save(addedExtension);
    }

    /**
     * 고정 확장자의 체크 상태 변경
     *
     * @param extension 체크 상태를 변경하려는 고정 확장자 이름
     * @param isBlocked 변경할 상태 (true: 차단, false: 허용)
     * @return 체크 상태를 변경한 확장자 객체
     * @throws IllegalArgumentException 해당 확장자가 존재하지 않는 경우
     * */
    public BlockedFileExtension changeStatus(String extension, boolean isBlocked) {
        BlockedFileExtension blockedFileExtension = blockedFileExtensionRepository.findByExtension(extension)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        blockedFileExtension.setIsBlocked(isBlocked);
        if (isBlocked) {
            addCount(extension);
        }
        return blockedFileExtension;
    }

    /**
     * 커스텀 확장자 삭제
     *
     * @param extension 삭제하려는 커스텀 확장자 이름
     * @throws IllegalArgumentException 해당 확장자가 존재하지 않는 경우, 커스텀 확장자가 아닌 경우
     * */
    public void deleteCustomExtension(String extension) {
        BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(extension)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 확장자입니다."));
        if (bfe.getExtensionType().equals(ExtensionType.FIX)) {
            throw new IllegalArgumentException("고정 확장자는 삭제할 수 없습니다.");
        } else {
            blockedFileExtensionRepository.delete(bfe);
        }
    }

    /**
     * 고정 확장자와 커스텀 확장자 목록 반환
     * 고정 확장자의 체크 상태 포함
     *
     * @return FileExtension 객체
     *         <ul>
     *             <li>fixExtensions: 체크 상태를 포함한 고정 확장자 리스트</li>
     *             <li>customExtensions: 커스텀 확장자 이름 리스트</li>
     *         </ul>
     * */
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

    /**
     * 업로드하려는 파일 검증
     * 파일의 확장자를 통해 업로드 가능 여부 반환
     *
     * @param filename 업로드하려고 하는 파일의 확장자를 포함한 이름
     * @return FileValidation 객체
     *         <ul>
     *             <li>result: 검증 결과 (BLOCKED: 차단, ALLOWED: 허용)</li>
     *             <li>reason: 차단된 확장자의 이름</li>
     *         </ul>
     * */
    public FileValidationResponse validateFile(String filename) {
        String[] extensions = filename.split("\\.");

        for (int i = 1; i < extensions.length; i++) {
            String extension = String.join(".", Arrays.copyOfRange(extensions, i, extensions.length));
            if (!isAllowedExtension(extension)) {
                return new FileValidationResponse(Result.BLOCKED, "차단된 확장자 " + extension);
            }
        }

        return new FileValidationResponse(Result.ALLOWED, "차단되지 않은 확장자");
    }

    /**
     * 확장자 추가 빈도수에 따라 고정 확장자 업데이트
     * 1시간마다 주기적으로 실행
     * */
    @Scheduled(fixedRate = 60 * 60 * 1000)
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

        if (newFixCount > 0) {
            int fromIndex = Math.max(currFixExtensionsDesc.size() - newFixCount, 0);
            List<String> toRemove = currFixExtensionsDesc.subList(fromIndex, currFixExtensionsDesc.size());

            for (String extension : toRemove) {
                BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(extension)
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 확장자입니다."));
                if (bfe.isBlocked()) {
                    bfe.setExtensionType(ExtensionType.CUSTOM);
                } else {
                    blockedFileExtensionRepository.delete(bfe);
                }
            }
        }

        for (String newExtension : newFixExtensions) {
            BlockedFileExtension bfe = blockedFileExtensionRepository.findByExtension(newExtension).orElse(null);

            if (bfe != null) {
                bfe.setExtensionType(ExtensionType.FIX);
            } else {
                BlockedFileExtension newFixedExtension = new BlockedFileExtension(newExtension, ExtensionType.FIX, false);
                blockedFileExtensionRepository.save(newFixedExtension);
            }
        }
    }

    /**
     * 확장자 추가 빈도 업데이트
     * 이미 있을 경우 빈도수 + 1
     * 없을 경우 새롭게 생성
     *
     * @param extension 업데이트할 확장자 이름
     * */
    public void addCount(String extension) {
        ExtensionFrequency extensionFrequency = extensionFrequencyRepository.findByExtension(extension)
                .orElse(new ExtensionFrequency(extension, 0));
        extensionFrequency.setAddedCount();
        extensionFrequencyRepository.save(extensionFrequency);
    }
}
