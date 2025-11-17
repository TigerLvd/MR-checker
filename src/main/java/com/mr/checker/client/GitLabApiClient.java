package com.mr.checker.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Клиент для работы с GitLab API.
 * Временная заглушка для продолжения разработки сервисов.
 */
@Slf4j
@Component
public class GitLabApiClient {

    /**
     * Создает комментарий к merge request в GitLab.
     * Временная заглушка - логирует действие.
     *
     * @param projectId ID проекта
     * @param mrIid IID merge request
     * @param comment текст комментария
     */
    public void postComment(Long projectId, Long mrIid, String comment) {
        log.info("Would post comment to GitLab MR {}/{}: {}", projectId, mrIid, comment.substring(0, Math.min(100, comment.length())));
    }
}
