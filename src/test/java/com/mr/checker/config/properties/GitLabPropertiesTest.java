package com.mr.checker.config.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
    "gitlab.url=http://test-gitlab.com",
    "gitlab.token=test-token-123",
    "gitlab.api-version=v4",
    "gitlab.timeout=15000"
})
class GitLabPropertiesTest {

    @Autowired
    private GitLabProperties gitLabProperties;

    @Test
    void shouldLoadGitLabPropertiesFromConfiguration() {
        assertThat(gitLabProperties).isNotNull();
        assertThat(gitLabProperties.getUrl()).isEqualTo("http://test-gitlab.com");
        assertThat(gitLabProperties.getToken()).isEqualTo("test-token-123");
        assertThat(gitLabProperties.getApiVersion()).isEqualTo("v4");
        assertThat(gitLabProperties.getTimeout()).isEqualTo(15000);
    }

    @Test
    void shouldHaveDefaultValues() {
        // Test with minimal configuration
        GitLabProperties defaultProps = new GitLabProperties();
        assertThat(defaultProps.getUrl()).isNull(); // No default in class
        assertThat(defaultProps.getToken()).isNull(); // No default in class
        assertThat(defaultProps.getApiVersion()).isEqualTo("v4"); // Has default
        assertThat(defaultProps.getTimeout()).isEqualTo(30000); // Has default
    }
}
