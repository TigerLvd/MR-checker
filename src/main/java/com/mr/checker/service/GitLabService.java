package com.mr.checker.service;

import com.mr.checker.config.properties.GitLabProperties;
import com.mr.checker.model.gitlab.MRChanges;
import com.mr.checker.model.gitlab.GitLabComment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabService {

    private final WebClient gitLabWebClient;
    private final GitLabProperties gitLabProperties;

    public Mono<MRChanges> getMRChanges(Long projectId, Integer mrIid) {
        String url = String.format("/api/%s/projects/%d/merge_requests/%d/changes",
                gitLabProperties.getApiVersion(), projectId, mrIid);

        return gitLabWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(MRChanges.class)
                .doOnNext(changes -> log.info("Retrieved MR changes for project {} MR {}", projectId, mrIid))
                .doOnError(error -> log.error("Failed to get MR changes for project {} MR {}", projectId, mrIid, error));
    }

    public Mono<Void> postComment(Long projectId, Integer mrIid, GitLabComment comment) {
        String url = String.format("/api/%s/projects/%d/merge_requests/%d/notes",
                gitLabProperties.getApiVersion(), projectId, mrIid);

        return gitLabWebClient.post()
                .uri(url)
                .bodyValue(comment)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Posted comment to MR {} in project {}", mrIid, projectId))
                .doOnError(error -> log.error("Failed to post comment to MR {} in project {}", mrIid, projectId, error));
    }
}
