package com.example.blockfileextension.service;

import com.example.blockfileextension.domain.BlockedFileExtension;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class FileExtensionServiceTest {

    @Autowired
    private FileExtensionService fileExtensionService;

    @Test
    void 차단되지_않은_확장자는_허용한다() {
        boolean result = fileExtensionService.isAllowedExtension("jpg");
        assertTrue(result);
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
        String extension = "sh";
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(extension);
        boolean result = fileExtensionService.isAllowedExtension("sh");
        assertFalse(result);
    }

    @Test
    void 추가된_커스텀_확장자_중복_추가_시_예외_처리() {
        String extension = "sh";
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(extension);
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension(extension)
        );
    }

    @Test
    void 시작_시_체크되지_않은_고정_확장자_리스트를_반환한다() {
        // DB에 사전 등록해둔 고정 확장자 리스트
        List<String> expected_value = Arrays.asList("bat", "cmd", "com", "cpl", "exe", "scr", "js");

        List<BlockedFileExtension> actual_value = fileExtensionService.getFixExtension();
        assertThat(actual_value).hasSize(expected_value.size());

        // 체크되지 않았는지 확인
        assertThat(actual_value).extracting(BlockedFileExtension::isBlocked).doesNotContain(true);
        // 고정 확장자를 제대로 반환하는지 확인
        assertThat(actual_value).extracting(BlockedFileExtension::getExtension)
                                .containsExactlyInAnyOrderElementsOf(expected_value);
    }

    @Test
    void 고정_확장자_체크_시_DB에_저장된다() {
        String extension = "bat";
        BlockedFileExtension entity = fileExtensionService.changeStatus(extension, true);
        assertTrue(entity.isBlocked());
    }

    @Test
    void 고정_확장자_체크해제_시_DB에_저장된다() {
        String extension = "bat";
        BlockedFileExtension chekced_entity = fileExtensionService.changeStatus(extension, true);
        BlockedFileExtension unchecked_entity = fileExtensionService.changeStatus(extension, false);
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
        String extension = "sh";
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(extension);
        fileExtensionService.deleteCustomExtension(extension);
        boolean result = fileExtensionService.isAllowedExtension(extension);
        assertTrue(result);
    }

    @Test
    void 커스텀_확장자는_200개까지_추가할_수_있다() {
        // 커스텀 확장자 200개 추가
        for (int i = 0; i < 200; i++) {
            BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension(String.valueOf(i));
        }
        // 201번째 커스텀 확장자 추가 시 예외 발생
        String extension = "sh";
        assertThrows(IllegalArgumentException.class, () ->
                fileExtensionService.addCustomExtension(extension)
        );
    }
}
