package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import static org.mockito.Mockito.mock;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(OAuthController.class)
public class OAuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private GitHubOAuthService service;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GitHubOAuthService gitHubOAuthService() {
            return mock(GitHubOAuthService.class);
        }
    }

    @Test
    void startReturnsSession() throws Exception {
        OAuthSession s = new OAuthSession();
        s.sessionId = "sid-123";
        s.authUrl = "https://github.com/login/oauth/authorize?dummy";
        s.status = OAuthState.WAITING;

        when(service.start()).thenReturn(s);

        mvc.perform(get("/oauth/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sid-123"))
                .andExpect(jsonPath("$.authUrl").value("https://github.com/login/oauth/authorize?dummy"));
    }

    @Test
    void statusNotFoundWhenNoSession() throws Exception {
        when(service.getStatus("nope")).thenReturn(null);
        mvc.perform(get("/oauth/status").param("session", "nope"))
                .andExpect(status().isNotFound());
    }

    @Test
    void statusReturnsSessionWhenFound() throws Exception {
        OAuthSession s = new OAuthSession();
        s.sessionId = "sid-200";
        s.status = OAuthState.SUCCESS;
        when(service.getStatus("sid-200")).thenReturn(s);

        mvc.perform(get("/oauth/status").param("session", "sid-200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sid-200"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void callbackShowsErrorHtmlWhenServiceReportsError() throws Exception {
        OAuthSession s = new OAuthSession();
        s.status = OAuthState.ERROR;
        s.error = "bad state";
        when(service.handleCallback("code-2", "state-2")).thenReturn(s);

        mvc.perform(get("/oauth/callback").param("code", "code-2").param("state", "state-2"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("OAuth failed: ")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bad state")));
    }

    @Test
    void statusMissingParamReturnsBadRequest() throws Exception {
        mvc.perform(get("/oauth/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void callbackShowsSuccessHtmlWhenServiceReportsSuccess() throws Exception {
        OAuthSession s = new OAuthSession();
        s.status = OAuthState.SUCCESS;
        when(service.handleCallback("code-1", "state-1")).thenReturn(s);

        mvc.perform(get("/oauth/callback").param("code", "code-1").param("state", "state-1"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Success! You may close this window.")));
    }
}
