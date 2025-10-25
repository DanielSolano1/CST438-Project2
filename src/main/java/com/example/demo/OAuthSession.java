package com.example.demo;

import java.time.Instant;

// OAuthState.java
enum OAuthState { WAITING, SUCCESS, ERROR }

// OAuthSession.java

public class OAuthSession {
  public String sessionId;     // random id the frontend will poll with
  public String state;         // anti-CSRF state sent to GitHub
  public OAuthState status;    // WAITING | SUCCESS | ERROR
  public String authUrl;       // where frontend should send the user
  public String error;         // error message if any
  public String accessToken;   // store securely in real apps
  public String tokenType;     // "bearer"
  public String scope;         // granted scopes
  public Instant createdAt = Instant.now();
}

