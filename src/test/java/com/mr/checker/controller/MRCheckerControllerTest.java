package com.mr.checker.controller;

import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import com.mr.checker.service.MRCheckerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(MRCheckerController.class)
class MRCheckerControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MRCheckerService mrCheckerService;

    @BeforeEach
    void setUp() {
        // Setup common mocks if needed
    }

    @Test
    void shouldCheckMRSuccessfully() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        CheckMRResponse response = CheckMRResponse.builder()
                .status("success")
                .summary("No issues found")
                .checkedAt(LocalDateTime.now())
                .build();

        when(mrCheckerService.checkMR(any(CheckMRRequest.class))).thenReturn(Mono.just(response));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CheckMRResponse.class)
                .value(resp -> {
                    assert resp.getStatus().equals("success");
                    assert resp.getSummary().equals("No issues found");
                    assert resp.getCheckedAt() != null;
                });
    }

    @Test
    void shouldHandleValidationErrors() {
        // Given - invalid request (null projectId)
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(null);
        request.setMrIid(456);

        // When & Then
        webTestClient.post()
                .uri("/api/v1/check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldHandleServiceErrors() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        when(mrCheckerService.checkMR(any(CheckMRRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Service error")));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void shouldCheckMRWithIssues() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);

        CheckMRResponse response = CheckMRResponse.builder()
                .status("warning")
                .summary("Found 2 issues in the code")
                .checkedAt(LocalDateTime.now())
                .build();

        when(mrCheckerService.checkMR(any(CheckMRRequest.class))).thenReturn(Mono.just(response));

        // When & Then
        webTestClient.post()
                .uri("/api/v1/check")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CheckMRResponse.class)
                .value(resp -> {
                    assert resp.getStatus().equals("warning");
                    assert resp.getSummary().contains("Found");
                    assert resp.getCheckedAt() != null;
                });
    }
}
