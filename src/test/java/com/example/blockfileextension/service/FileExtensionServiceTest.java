package com.example.blockfileextension.service;

import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
