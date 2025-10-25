package com.example.demo;

// TokenResponse.java (GitHub token exchange)
import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
  @JsonProperty("access_token") public String accessToken;
  @JsonProperty("token_type")   public String tokenType;
  public String scope;
  public String error;
  @JsonProperty("error_description") public String errorDescription;
}
