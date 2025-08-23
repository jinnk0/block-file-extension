package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FileExtensionServiceTest {

    @Autowired
    private FileExtensionService fileExtensionService;

    @Autowired
    private BlockedFileExtensionRepository blockedFileExtensionRepository;

    @Autowired
    private ExtensionFrequencyRepository extensionFrequencyRepository;

    @BeforeEach
    void setUp() {
        fileExtensionService = new FileExtensionService(blockedFileExtensionRepository, extensionFrequencyRepository);

        // 기존 데이터 삭제
        blockedFileExtensionRepository.deleteAll();
        extensionFrequencyRepository.deleteAll();

        // 초기 FIX 확장자 데이터: isBlocked true/false 섞음
        List<BlockedFileExtension> fixedExtensions = List.of(
                new BlockedFileExtension("bat", ExtensionType.FIX, false),
                new BlockedFileExtension("cmd", ExtensionType.FIX, true),
                new BlockedFileExtension("com", ExtensionType.FIX, false),
                new BlockedFileExtension("cpl", ExtensionType.FIX, true),
                new BlockedFileExtension("exe", ExtensionType.FIX, false),
                new BlockedFileExtension("scr", ExtensionType.FIX, true),
                new BlockedFileExtension("js", ExtensionType.FIX, false),
                new BlockedFileExtension("txt", ExtensionType.CUSTOM, true)
        );
        blockedFileExtensionRepository.saveAll(fixedExtensions);

        // 초기 ExtensionFrequency 데이터
        List<ExtensionFrequency> frequencies = List.of(
                new ExtensionFrequency("bat", 1),
                new ExtensionFrequency("cmd", 0),
                new ExtensionFrequency("com", 2),
                new ExtensionFrequency("cpl", 4),
                new ExtensionFrequency("exe", 10),
                new ExtensionFrequency("scr", 6),
                new ExtensionFrequency("js", 10),
                new ExtensionFrequency("zip", 80),
                new ExtensionFrequency("tar", 70),
                new ExtensionFrequency("gz", 60),
                new ExtensionFrequency("txt", 50),
                new ExtensionFrequency("doc", 40)
        );
        extensionFrequencyRepository.saveAll(frequencies);
    }

    @Test
    void 차단되지_않은_확장자는_허용한다() {
        boolean result_fix = fileExtensionService.isAllowedExtension("js");
        boolean result_custom = fileExtensionService.isAllowedExtension("jpg");
        assertTrue(result_fix);
        assertTrue(result_custom);
    }

    @Test
    void 커스텀_확장자를_추가한다() {
        String extension = "sh";

        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(extension);

        assertNotNull(addedExtension.getId());
        assertEquals(extension, addedExtension.getExtension());
    }

    @Test
    void 차단된_확장자는_거부한다() {
        boolean result_fix = fileExtensionService.isAllowedExtension("scr"); // 체크된 고정 확장자
        boolean result_custom = fileExtensionService.isAllowedExtension("txt"); // 추가된 커스텀 확장자
        assertFalse(result_fix);
        assertFalse(result_custom);
    }

    @Test
    void 추가된_커스텀_확장자_중복_추가_시_예외_처리() {
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension("scr") // 이미 추가되어 있는 고정 확장자
        );
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension("txt") // 이미 추가되어 있는 커스텀 확장자
        );
    }

    @Test
    void 고정_확장자_체크_시_DB에_저장된다() {
        BlockedFileExtension entity = fileExtensionService.changeStatus("bat", true);
        assertTrue(entity.isBlocked());
    }

    @Test
    void 고정_확장자_체크해제_시_DB에_저장된다() {
        BlockedFileExtension unchecked_entity = fileExtensionService.changeStatus("cmd", false);
        assertFalse(unchecked_entity.isBlocked());
    }

    @Test
    void 확장자_최대_입력_길이는_20자리() {
        String extension = "abcdefghijklmnopqrstuvwxyz";
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension(extension)
        );
    }

    @Test
    void 커스텀_확장자를_삭제한다() {
        fileExtensionService.deleteCustomExtension("txt");
        boolean result = fileExtensionService.isAllowedExtension("txt");
        assertTrue(result);
    }

    @Test
    void 커스텀_확장자는_200개까지_추가할_수_있다() {
        // 커스텀 확장자 200개 추가
        for (int i = 0; i < 199; i++) { // 이미 추가되어 있는 1개의 커스텀 확장자에 더해 총 200개의 커스텀 확장자 추가
            BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(String.valueOf(i));
        }
        // 201번째 커스텀 확장자 추가 시 예외 발생
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension("201")
        );
    }

    @Test
    void updateFixExtensions_isBlocked_테스트() {
        // 확장자 추가 빈도에 따라 고정 확장자 업데이트
        fileExtensionService.updateFixExtensions();

        // 고정 확장자 업데이트 내용 확인
        List<BlockedFileExtension> allExtensions = blockedFileExtensionRepository.findAll();

        // 새로운 top7 확장자들이 FIX로 반영되었는지 확인
        assertThat(allExtensions)
                .extracting(BlockedFileExtension::getExtension, BlockedFileExtension::getExtensionType)
                .contains(
                        tuple("zip", ExtensionType.FIX),
                        tuple("tar", ExtensionType.FIX),
                        tuple("gz", ExtensionType.FIX),
                        tuple("txt", ExtensionType.FIX),
                        tuple("doc", ExtensionType.FIX)
                );

        // 빈도수가 낮은 isBlocked=true였던 기존 FIX는 CUSTOM으로 변경되었는지 확인
        BlockedFileExtension cmd = blockedFileExtensionRepository.findByExtension("cmd").orElseThrow();
        BlockedFileExtension cpl = blockedFileExtensionRepository.findByExtension("cpl").orElseThrow();
        BlockedFileExtension scr = blockedFileExtensionRepository.findByExtension("scr").orElseThrow();

        assertThat(cmd.getExtensionType()).isEqualTo(ExtensionType.CUSTOM);
        assertThat(cpl.getExtensionType()).isEqualTo(ExtensionType.CUSTOM);
        assertThat(scr.getExtensionType()).isEqualTo(ExtensionType.CUSTOM);

        // 빈도수가 낮은 isBlocked=false였던 기존 FIX는 삭제되었는지 확인
        assertThat(blockedFileExtensionRepository.findByExtension("bat")).isEmpty();
        assertThat(blockedFileExtensionRepository.findByExtension("com")).isEmpty();
    }
}
