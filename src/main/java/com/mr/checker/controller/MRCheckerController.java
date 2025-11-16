package com.mr.checker.controller;

import com.mr.checker.model.request.CheckMRRequest;
import com.mr.checker.model.response.CheckMRResponse;
import com.mr.checker.service.MRCheckerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MRCheckerController {

    private final MRCheckerService mrCheckerService;

    @PostMapping("/check")
    public Mono<CheckMRResponse> checkMR(@Validated @RequestBody CheckMRRequest request) {
        log.info("Received MR check request for projectId={}, mrIid={}", request.getProjectId(), request.getMrIid());
        return mrCheckerService.checkMR(request);
    }
}
