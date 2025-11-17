package com.mr.checker.config;

import com.mr.checker.config.properties.GitLabProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GitLabConfig {

    private final GitLabProperties gitLabProperties;

    @Bean("gitLabWebClient")
    public WebClient gitLabWebClient() {
        return WebClient.builder()
                .baseUrl(gitLabProperties.getUrl())
                .defaultHeader("PRIVATE-TOKEN", gitLabProperties.getToken())
                .clientConnector(createHttpClient())
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(logRequest());
                    exchangeFilterFunctions.add(logResponse());
                })
                .build();
    }

    private ReactorClientHttpConnector createHttpClient() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofMillis(gitLabProperties.getTimeout()));

        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("GitLab API Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers()
                        .forEach((name, values) -> log.debug("Request Header: {} = {}", name, values));
            }
            return Mono.empty();
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("GitLab API Response: {}", clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}
