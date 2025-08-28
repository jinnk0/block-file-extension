package com.example.blockfileextension.controller;

import com.example.blockfileextension.domain.*;
import com.example.blockfileextension.dto.CustomExtension;
import com.example.blockfileextension.dto.FileValidationRequest;
import com.example.blockfileextension.dto.FixExtension;
import com.example.blockfileextension.service.FileExtensionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class FileExtensionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void 확장자_리스트를_반환한다() throws Exception {
        mvc.perform(get("/files/extensions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fixExtensions", containsInAnyOrder(
                        hasEntry("extension", "bat"),
                        hasEntry("extension", "cmd"),
                        hasEntry("extension", "com"),
                        hasEntry("extension", "cpl"),
                        hasEntry("extension", "exe"),
                        hasEntry("extension", "scr"),
                        hasEntry("extension", "js")

                )))
                .andExpect(jsonPath("$.customExtensions", containsInAnyOrder("txt")));
    }

    @Test
    void 고정_확장자_체크_상태를_변경한다() throws Exception {
        FixExtension extensionToUpdate = new FixExtension("cmd", true);

        mvc.perform(patch("/files/extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(extensionToUpdate)))
                .andExpect(status().isNoContent());
    }

    @Test
    void 커스텀_확장자를_추가한다() throws Exception {
        CustomExtension customExtension = new CustomExtension("sh", true);

        mvc.perform(post("/files/extensions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customExtension)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.extension").value("sh"))
                .andExpect(jsonPath("$.isBlocked").value(true));
    }

    @Test
    void 커스텀_확장자를_삭제한다() throws Exception {
        String extensionToDelete = "txt";

        mvc.perform(delete("/files/extensions/{extension}", extensionToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    void 차단된_확장자면_파일_업로드_실패() throws Exception {
        FileValidationRequest request = new FileValidationRequest("virus.cmd");

        mvc.perform(post("/files/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("BLOCKED"));
    }

    @Test
    void 차단되지_않은_확장자면_파일_업로드_성공() throws Exception {
        FileValidationRequest request = new FileValidationRequest("safe.exe");

        mvc.perform(post("/files/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALLOWED"));
    }

    @Test
    void 이중_확장자_처리_성공() throws Exception {
        // 테스트용 이중 확장자 추가
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension("tar.gz");

        FileValidationRequest request = new FileValidationRequest("test.tar.gz");

        mvc.perform(post("/files/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("BLOCKED"));
    }

}
