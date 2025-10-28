package com.example.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/oauth")
public class OAuthController {

  private final GitHubOAuthService service;

  public OAuthController(GitHubOAuthService service) {
    this.service = service;
  }

  @GetMapping("/start")
  public ResponseEntity<?> start(@RequestParam(name = "redirect", required = false, defaultValue = "false") boolean redirect,
                                 @RequestHeader(name = "Accept", required = false) String acceptHeader,
                                 @RequestHeader(name = "User-Agent", required = false) String userAgent) {
    OAuthSession s = service.start();

    // Decide whether to redirect automatically to the GitHub authorize URL.
    // Behavior:
    // - If query param redirect=true is present, always redirect (explicit override).
    // - If the client Accept header contains text/html, assume a browser and redirect.
    // - If Accept is missing but User-Agent looks like a browser (contains Mozilla/Chrome/Firefox), redirect.
    // Otherwise return the JSON OAuthSession (API clients expect JSON).
    if (redirect) {
      return ResponseEntity.status(302).header("Location", s.authUrl).build();
    }

    boolean acceptHtml = (acceptHeader != null && acceptHeader.toLowerCase().contains("text/html"));
    boolean uaLooksLikeBrowser = (userAgent != null && (userAgent.contains("Mozilla") || userAgent.contains("Chrome") || userAgent.contains("Safari") || userAgent.contains("Firefox")));

    if (acceptHtml || (acceptHeader == null && uaLooksLikeBrowser)) {
      return ResponseEntity.status(302).header("Location", s.authUrl).build();
    }

    return ResponseEntity.ok(s);
  }

  @GetMapping("/debug")
  public ResponseEntity<Map<String, String>> debug() {
    Map<String, String> m = new HashMap<>();
    // Expose non-sensitive config so we can debug redirect/authorize URL problems
    m.put("authorizeUri", service.getAuthorizeUri());
    m.put("tokenUri", service.getTokenUri());
    m.put("redirectUri", service.getRedirectUri());
    m.put("scope", service.getScope());
    m.put("clientId", service.getClientId());
    return ResponseEntity.ok(m);
  }

  @GetMapping("/status")
  public ResponseEntity<OAuthSession> status(@RequestParam("session") String sessionId) {
    OAuthSession s = service.getStatus(sessionId);
    if (s == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(s);
  }

  @GetMapping("/callback")
  public ResponseEntity<String> callback(@RequestParam("code") String code,
      @RequestParam("state") String state) {
    OAuthSession s = service.handleCallback(code, state);
    if (s.status == OAuthState.SUCCESS) {
      return ResponseEntity.ok("<html><body>Success! You may close this window.</body></html>");
    }
    return ResponseEntity.ok("<html><body>OAuth failed: " + s.error + "</body></html>");
  }
}
