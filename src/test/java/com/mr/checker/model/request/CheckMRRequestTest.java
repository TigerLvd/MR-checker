package com.mr.checker.model.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CheckMRRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void shouldValidateRequiredFields() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(null);
        request.setMrIid(null);
        request.setGitlabToken("test-token");

        // When
        Set<ConstraintViolation<CheckMRRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("projectId"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("mrIid"));
    }

    @Test
    void shouldPassValidationWithValidData() {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);
        request.setGitlabToken("test-token");

        // When
        Set<ConstraintViolation<CheckMRRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    void shouldSerializeToJson() throws Exception {
        // Given
        CheckMRRequest request = new CheckMRRequest();
        request.setProjectId(123L);
        request.setMrIid(456);
        request.setGitlabToken("test-token");

        // When
        String json = objectMapper.writeValueAsString(request);

        // Then
        assertThat(json).contains("\"projectId\":123");
        assertThat(json).contains("\"mrIid\":456");
        assertThat(json).contains("\"gitlabToken\":\"test-token\"");
    }

    @Test
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = "{\"projectId\":123,\"mrIid\":456,\"gitlabToken\":\"test-token\"}";

        // When
        CheckMRRequest request = objectMapper.readValue(json, CheckMRRequest.class);

        // Then
        assertThat(request.getProjectId()).isEqualTo(123L);
        assertThat(request.getMrIid()).isEqualTo(456);
        assertThat(request.getGitlabToken()).isEqualTo("test-token");
    }
}
