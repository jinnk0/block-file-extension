package com.example.blockfileextension.controller;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.dto.CustomExtension;
import com.example.blockfileextension.dto.FixExtension;
import com.example.blockfileextension.service.FileExtensionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

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
    private FileExtensionService fileExtensionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void 확장자_리스트를_반환한다() throws Exception {

        // 테스트용 커스텀 확장자 추가
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension("sh");

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
                .andExpect(jsonPath("$.customExtensions", containsInAnyOrder("sh")));
    }

    @Test
    void 고정_확장자_체크_상태를_변경한다() throws Exception {
        FixExtension extensionToUpdate = new FixExtension("exe", true);

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
        String extensionToDelete = "exe";

        mvc.perform(delete("/files/extensions/{extension}", extensionToDelete))
                .andExpect(status().isNoContent());
    }

    @Test
    void 차단된_확장자면_파일_업로드_실패() throws Exception {
        BlockedFileExtension entity = fileExtensionService.changeStatus("exe", true);

        MockMultipartFile blockedFile =
                new MockMultipartFile("file", "virus.exe", "application/octet-stream", "dummy".getBytes());

        mvc.perform(multipart("/files").file(blockedFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("BLOCKED"));
    }

    @Test
    void 차단되지_않은_확장자면_파일_업로드_성공() throws Exception {
        MockMultipartFile blockedFile =
                new MockMultipartFile("file", "safe.exe", "application/octet-stream", "dummy".getBytes());

        mvc.perform(multipart("/files").file(blockedFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("ALLOWED"));
    }

    @Test
    void 이중_확장자_처리_성공() throws Exception {
        // 테스트용 이중 확장자 추가
        BlockedFileExtension addedExtension = fileExtensionService.addCustomExtension("tar.gz");

        MockMultipartFile blockedFile =
                new MockMultipartFile("file", "virus.tar.gz", "application/octet-stream", "dummy".getBytes());

        mvc.perform(multipart("/files").file(blockedFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("BLOCKED"));
    }

}
