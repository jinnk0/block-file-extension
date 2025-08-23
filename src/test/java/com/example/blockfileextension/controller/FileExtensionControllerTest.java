package com.example.blockfileextension.controller;

import com.example.blockfileextension.domain.BlockedFileExtension;
import com.example.blockfileextension.dto.FixExtension;
import com.example.blockfileextension.service.FileExtensionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

}
