package com.mr.checker.service;

import com.mr.checker.config.properties.LLMProperties;
import com.mr.checker.model.AnalysisResult;
import com.mr.checker.model.llm.ChatRequest;
import com.mr.checker.model.llm.ChatResponse;
import com.mr.checker.model.llm.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LLMAnalysisService {

    private final WebClient llmWebClient;
    private final LLMProperties llmProperties;

    public Mono<AnalysisResult> analyzeCode(String codeSnippet) {
        if (codeSnippet == null || codeSnippet.trim().isEmpty()) {
            return Mono.just(new AnalysisResult());
        }

        ChatRequest chatRequest = ChatRequest.builder()
                .model(llmProperties.getModel())
                .messages(java.util.List.of(
                        Message.systemMessage("You are a code review assistant. Analyze the following code for potential issues, bugs, security vulnerabilities, performance problems, and best practices violations. Return your analysis in JSON format with categorized issues."),
                        Message.userMessage("Please analyze this code:\n\n" + codeSnippet)
                ))
                .temperature(llmProperties.getTemperature())
                .maxTokens(llmProperties.getMaxTokens())
                .build();

        return llmWebClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(chatRequest)
                .retrieve()
                .bodyToMono(ChatResponse.class)
                .map(this::parseLLMResponse)
                .doOnNext(result -> log.info("Successfully analyzed code snippet of length {}", codeSnippet.length()))
                .doOnError(error -> log.error("Failed to analyze code with LLM", error))
                .onErrorReturn(new AnalysisResult());
    }

    private AnalysisResult parseLLMResponse(ChatResponse chatResponse) {
        // For now, return empty result - parsing logic will be implemented in future iterations
        return new AnalysisResult();
    }
}