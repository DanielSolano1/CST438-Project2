package com.example.demo;

// GitHubOAuthService.java
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GitHubOAuthService {

  private final String clientId;
  private final String clientSecret;
  private final String authorizeUri;
  private final String tokenUri;
  private final String redirectUri;
  private final String scope;

  private final WebClient http = WebClient.builder().build();
  private final SecureRandom random = new SecureRandom();

  // in-memory session store; consider Redis in prod
  private final Map<String, OAuthSession> sessions = new ConcurrentHashMap<>();

  public GitHubOAuthService(
      @Value("${github.client-id}") String clientId,
      @Value("${github.client-secret}") String clientSecret,
      @Value("${github.authorize-uri}") String authorizeUri,
      @Value("${github.token-uri}") String tokenUri,
      @Value("${github.redirect-uri}") String redirectUri,
      @Value("${github.scope}") String scope
  ) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.authorizeUri = authorizeUri;
    this.tokenUri = tokenUri;
    this.redirectUri = redirectUri;
    this.scope = scope;
  }

  public OAuthSession start() {
    String sessionId = randomId();
    String state = randomId();

  // build(true) is deprecated; build then encode() to get the same result
  String authUrl = UriComponentsBuilder.fromHttpUrl(authorizeUri)
    .queryParam("client_id", clientId)
    .queryParam("redirect_uri", redirectUri)
    .queryParam("scope", scope)
    .queryParam("state", state)
    .queryParam("allow_signup", "false")
    .build().encode().toUriString();

    OAuthSession s = new OAuthSession();
    s.sessionId = sessionId;
    s.state = state;
    s.status = OAuthState.WAITING;
    s.authUrl = authUrl;
    sessions.put(sessionId, s);
    return s;
  }

  public OAuthSession getStatus(String sessionId) {
    return sessions.get(sessionId);
  }

  public OAuthSession handleCallback(String code, String returnedState) {
    // Find the session by matching state
    OAuthSession session = sessions.values().stream()
        .filter(s -> s.state.equals(returnedState))
        .findFirst()
        .orElse(null);

    if (session == null) {
      // no session found for state => error
      OAuthSession err = new OAuthSession();
      err.status = OAuthState.ERROR;
      err.error = "Invalid or expired state";
      return err;
    }

    // Exchange code -> token (server-side, includes client_secret)
    try {
      MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
      form.add("client_id", clientId);
      form.add("client_secret", clientSecret);
      form.add("code", code);
      form.add("redirect_uri", redirectUri);
      form.add("state", returnedState);

      TokenResponse token = http.post()
          .uri(tokenUri)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .header("Accept", "application/json")
          .body(BodyInserters.fromFormData(form))
          .retrieve()
          .bodyToMono(TokenResponse.class)
          .block();

      if (token == null || token.error != null || token.accessToken == null) {
        session.status = OAuthState.ERROR;
        session.error = token != null ? token.errorDescription : "Token exchange failed";
      } else {
        session.status = OAuthState.SUCCESS;
        session.accessToken = token.accessToken;
        session.tokenType = token.tokenType;
        session.scope = token.scope;
      }
      return session;
    } catch (Exception e) {
      session.status = OAuthState.ERROR;
      session.error = "Exception during token exchange: " + e.getMessage();
      return session;
    }
  }

  private String randomId() {
    byte[] bytes = new byte[16];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  // Expose non-sensitive getters to assist with debugging (do not expose clientSecret)
  public String getClientId() {
    return clientId;
  }

  public String getAuthorizeUri() {
    return authorizeUri;
  }

  public String getTokenUri() {
    return tokenUri;
  }

  public String getRedirectUri() {
    return redirectUri;
  }

  public String getScope() {
    return scope;
  }
}