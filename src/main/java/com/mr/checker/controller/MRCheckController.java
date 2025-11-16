package com.mr.checker.controller;

import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import com.mr.checker.service.MRCheckerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
@Slf4j
public class MRCheckController {

    private final MRCheckerService mrCheckerService;

    @PostMapping("/check-mr")
    public Mono<CheckMRResponse> checkMR(@Valid @RequestBody CheckMRRequest request) {
        log.info("Received MR check request for project {} MR {}", request.getProjectId(), request.getMrIid());

        return mrCheckerService.checkMR(request)
                .doOnNext(response -> log.info("MR check completed with status: {}", response.getStatus()))
                .doOnError(error -> log.error("Error during MR check", error));
    }
}
