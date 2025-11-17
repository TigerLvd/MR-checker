package com.mr.checker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import com.mr.checker.service.MRCheckerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MRCheckController.class)
class MRCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MRCheckerService mrCheckerService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void shouldCheckMRWithValidRequest() throws Exception {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        CheckMRResponse expectedResponse = CheckMRResponse.builder()
                .status("success")
                .summary("Analysis completed")
                .checkedAt(LocalDateTime.now())
                .build();

        when(mrCheckerService.checkMR(any(CheckMRRequest.class)))
                .thenReturn(Mono.just(expectedResponse));

        // When & Then
        mockMvc.perform(post("/api/v1/check-mr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnBadRequestForNullProjectId() throws Exception {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setMrIid(456);
        // projectId is null

        // When & Then
        mockMvc.perform(post("/api/v1/check-mr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestForNullMrIid() throws Exception {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        // mrIid is null

        // When & Then
        mockMvc.perform(post("/api/v1/check-mr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleServiceError() throws Exception {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        CheckMRResponse errorResponse = CheckMRResponse.builder()
                .status("failure")
                .summary("Service error")
                .checkedAt(LocalDateTime.now())
                .build();

        when(mrCheckerService.checkMR(any(CheckMRRequest.class)))
                .thenReturn(Mono.just(errorResponse));

        // When & Then
        mockMvc.perform(post("/api/v1/check-mr")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
