# Testing GitHub OAuth locally

To test locally run:

```
$env:GITHUB_CLIENT_ID = "Ov23lisHO1Te8e1qKAKG"
$env:GITHUB_CLIENT_SECRET = "3861ff8af785d24fb9f282c7b1da9a1c93e7b1a3"
$env:GITHUB_AUTHORIZE_URI = "https://github.com/login/oauth/authorize"
$env:GITHUB_TOKEN_URI = "https://github.com/login/oauth/access_token"
$env:GITHUB_REDIRECT_URI = "http://localhost:8080/oauth/callback"
$env:GITHUB_SCOPE = "repo"
.\run-local.ps1
```

Then in your browser, you can go to either:
http://localhost:8080/oauth/debug for the JSON response

or

http://localhost:8080/oauth/start to go through the actual process

Currently, it goes to the github authorization and then takes you back to a page that says "Success! You may close this window"