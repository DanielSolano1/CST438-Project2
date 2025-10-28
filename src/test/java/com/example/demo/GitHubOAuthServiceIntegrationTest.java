package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class GitHubOAuthServiceIntegrationTest {

    static MockWebServer server;

    @BeforeAll
    static void startServer() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void shutdown() throws Exception {
        server.shutdown();
    }

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("github.client-id", () -> "fake-client");
        r.add("github.client-secret", () -> "fake-secret");
        r.add("github.authorize-uri", () -> "https://example.com/login/oauth/authorize");
        // point token-uri to mock server
        r.add("github.token-uri", () -> server.url("/login/oauth/access_token").toString());
        r.add("github.redirect-uri", () -> "https://example.com/callback");
        r.add("github.scope", () -> "repo");
    }

    @Autowired
    private GitHubOAuthService service;

    @Test
    void handleCallbackExchangesTokenSuccessfully() throws Exception {
        // prepare session
        OAuthSession s = service.start();

        // enqueue token response
        String body = "{\"access_token\":\"tok-123\",\"token_type\":\"bearer\",\"scope\":\"repo\"}";
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));

        OAuthSession res = service.handleCallback("code-1", s.state);
        assertThat(res.status).isEqualTo(OAuthState.SUCCESS);
        assertThat(res.accessToken).isEqualTo("tok-123");
        assertThat(res.tokenType).isEqualTo("bearer");
    }

    @Test
    void handleCallbackHandlesTokenError() throws Exception {
        OAuthSession s = service.start();

        String body = "{\"error\":\"bad_verification\",\"error_description\":\"Invalid code\"}";
        server.enqueue(new MockResponse().setBody(body).addHeader("Content-Type", "application/json"));

        OAuthSession res = service.handleCallback("code-err", s.state);
        assertThat(res.status).isEqualTo(OAuthState.ERROR);
        assertThat(res.error).contains("Invalid code");
    }
}
