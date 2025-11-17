package com.mr.checker.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "llm.url=http://test-llm.com",
    "llm.model=test-model",
    "llm.timeout=60000",
    "llm.temperature=0.5",
    "llm.max-tokens=2000"
})
class LLMPropertiesTest {

    @Autowired
    private LLMProperties llmProperties;

    @Test
    void shouldLoadLLMPropertiesFromConfiguration() {
        assertThat(llmProperties).isNotNull();
        assertThat(llmProperties.getUrl()).isEqualTo("http://test-llm.com");
        assertThat(llmProperties.getModel()).isEqualTo("test-model");
        assertThat(llmProperties.getTimeout()).isEqualTo(60000);
        assertThat(llmProperties.getTemperature()).isEqualTo(0.5);
        assertThat(llmProperties.getMaxTokens()).isEqualTo(2000);
    }

    @Test
    void shouldHaveDefaultValues() {
        // Test with minimal configuration
        LLMProperties defaultProps = new LLMProperties();
        assertThat(defaultProps.getUrl()).isNull(); // No default in class
        assertThat(defaultProps.getModel()).isEqualTo("llama2"); // Has default
        assertThat(defaultProps.getTimeout()).isEqualTo(120000); // Has default
        assertThat(defaultProps.getTemperature()).isEqualTo(0.1); // Has default
        assertThat(defaultProps.getMaxTokens()).isEqualTo(4096); // Has default
    }
}
