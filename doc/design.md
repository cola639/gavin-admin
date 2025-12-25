### 1 github third party login

1 (preLogin → frontend redirects → frontend posts code back)
2 Below is the enterprise-style “backend callback (BFF-ish) + JWT for API calls” flow:
Browser goes to backend: /oauth2/authorization/github
✅ GitHub redirects back to backend: /login/oauth2/code/github (Spring handles it)
✅ Backend creates your app JWT and redirects to frontend with a one-time code (NOT the JWT)
✅ Frontend calls backend /auth/exchange?code=... to get the JWT
✅ Frontend calls APIs with Authorization: Bearer <jwt>