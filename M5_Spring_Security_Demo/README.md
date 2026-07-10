# Spring Security Demo

This repository is an incremental Spring Security learning project. It starts from Spring Boot security defaults, moves through database-backed authentication, JWT-based stateless APIs, refresh tokens, OAuth2 login, session tracking, and ends with role/permission based authorization using method security.

The final application protects a small Posts API and Auth API while demonstrating how Spring Security's authentication pipeline, JWT filters, `SecurityContextHolder`, `UserDetailsService`, `AuthenticationManager`, OAuth2 login, and RBAC fit together.

## What This Project Covers

- Spring Security default behavior after adding `spring-boot-starter-security`
- `SecurityFilterChain` customization
- In-memory users and database-backed users
- `UserDetailsService` integration with `DaoAuthenticationProvider`
- Password hashing with `BCryptPasswordEncoder`
- Signup and login endpoints
- JWT access token generation and validation
- Custom `OncePerRequestFilter` for JWT authentication
- Reading authenticated user data from `SecurityContextHolder`
- Centralized handling of authentication and JWT exceptions
- Refresh token flow
- Refresh token rotation
- HttpOnly cookies for refresh tokens
- Logout flow that invalidates server-side refresh sessions
- OAuth2 login with Google
- OAuth2 success handler, failure redirect, and frontend redirect
- Refresh-token-backed session records used for refresh and logout
- Maximum active session limit with least-recently-used eviction
- Role based access control
- Permission based access control
- Mapping roles to permissions
- Method-level authorization with `@Secured` and `@PreAuthorize`
- Ownership checks through a custom Spring bean in SpEL
- Avoiding large request matcher configurations by keeping authorization near business methods

## Tech Stack

- Java 21
- Spring Boot 4.1.0
- Spring Web MVC
- Spring Security
- Spring Data JPA
- MySQL
- OAuth2 Client
- JJWT `0.13.0`
- ModelMapper
- Lombok

## Project Structure

```text
src/main/java/com/shubh/module5/Spring_Security_Demo
+-- advice
|   +-- ApiError.java
|   +-- GlobalExceptionHandler.java
+-- config
|   +-- AppConfig.java
|   +-- WebSecurityConfig.java
+-- controller
|   +-- AuthController.java
|   +-- PostController.java
+-- dto
+-- entity
|   +-- PostEntity.java
|   +-- Session.java
|   +-- UserEntity.java
|   +-- enums
|       +-- Permission.java
|       +-- Role.java
+-- filter
|   +-- JWTAuthFilter.java
+-- handlers
|   +-- Oauth2SuccessHandler.java
+-- repository
+-- service
|   +-- AuthService.java
|   +-- JWTService.java
|   +-- SessionService.java
|   +-- UserService.java
|   +-- impl
+-- utils
    +-- PostSecurityService.java
    +-- RolePermissionMapper.java
```

## Learning Timeline From Commits

### 1. Spring Security Setup

Commits:

- [`d5f0f01`](https://github.com/shubhgaur37/SpringBoot/commit/d5f0f01) Initialising Project and Adding Spring Security Dependency
- [`c10afa0`](https://github.com/shubhgaur37/SpringBoot/commit/c10afa0) Adding a single user with some roles in yaml
- [`5d1c1dc`](https://github.com/shubhgaur37/SpringBoot/commit/5d1c1dc) Adding User Entity with db authentication functionality via service layer
- [`5c85861`](https://github.com/shubhgaur37/SpringBoot/commit/5c85861) basics of security filter chain
- [`fbf4342`](https://github.com/shubhgaur37/SpringBoot/commit/fbf4342) Registering Multiple Users using InMemoryUserDetailsService bean
- [`4f8fe07`](https://github.com/shubhgaur37/SpringBoot/commit/4f8fe07) Security Filter Chain Stateless Mode

What was learned:

- Adding Spring Security immediately protects all endpoints by default.
- A default login page and generated password are provided unless overridden.
- A simple user can be configured in `application.yaml` using `spring.security.user`.
- Multiple users can be registered with an `InMemoryUserDetailsManager`.
- A custom `SecurityFilterChain` controls which routes are public, authenticated, or restricted.
- Stateless APIs should use `SessionCreationPolicy.STATELESS`.
- Database-backed authentication begins by creating a `UserEntity`, `UserRepository`, and `UserDetailsService`.

### 2. Database Authentication And Login

Commits:

- [`bed117a`](https://github.com/shubhgaur37/SpringBoot/commit/bed117a) Adding Login and SignUp Functionality
- [`02668a5`](https://github.com/shubhgaur37/SpringBoot/commit/02668a5) Sending token as cookie alongwith response in login
- [`dbd51da`](https://github.com/shubhgaur37/SpringBoot/commit/dbd51da) reformat
- [`9f2d02f`](https://github.com/shubhgaur37/SpringBoot/commit/9f2d02f) Custom JWT Auth Filter to authenticate all routes except for auth
- [`41826ff`](https://github.com/shubhgaur37/SpringBoot/commit/41826ff) Getting User Details from Security Filter Context
- [`c4717c6`](https://github.com/shubhgaur37/SpringBoot/commit/c4717c6) reformat
- [`61e3d8e`](https://github.com/shubhgaur37/SpringBoot/commit/61e3d8e) Handling Authentication and JWT Exceptions

What was learned:

- `UserService` implements `UserDetailsService`.
- Spring Security calls `loadUserByUsername()` during username/password login.
- The login principal is email, and it maps to the user entity's `email` field.
- `AuthenticationManager.authenticate(...)` delegates to a provider such as `DaoAuthenticationProvider`.
- `DaoAuthenticationProvider` loads the user and checks the raw password against the stored BCrypt hash.
- Signup must hash the raw password before saving it.
- Authentication exceptions should return clear API errors instead of default HTML/login behavior.
- A custom JWT filter must manually delegate exceptions to `HandlerExceptionResolver` because filter exceptions occur before controller advice normally runs.

### 3. JWT Authentication

Commits:

- [`76e4b96`](https://github.com/shubhgaur37/SpringBoot/commit/76e4b96) Basics Of JWT Authentication
- [`9f2d02f`](https://github.com/shubhgaur37/SpringBoot/commit/9f2d02f) Custom JWT Auth Filter to authenticate all routes except for auth
- [`61e3d8e`](https://github.com/shubhgaur37/SpringBoot/commit/61e3d8e) Handling Authentication and JWT Exceptions

What was learned:

- JWTs are signed with a secret key. For HS256-style signing, the secret must be long enough.
- The token subject stores the user id.
- Access tokens can include useful claims such as email and roles.
- A JWT filter should:
  - Read the `Authorization` header.
  - Continue the chain if the header is missing or does not start with `Bearer `.
  - Validate the token signature and expiration.
  - Load the user from the database.
  - Create a `UsernamePasswordAuthenticationToken`.
  - Include the user's authorities.
  - Store authentication in `SecurityContextHolder`.
  - Continue the filter chain.

```mermaid
flowchart TD
    A[HTTP Request] --> B[JWTAuthFilter]
    B --> C{Authorization header starts with Bearer?}
    C -- No --> H[Continue filter chain]
    C -- Yes --> D[Validate JWT signature and expiry]
    D --> E[Extract user id from subject]
    E --> F[Load UserEntity from database]
    F --> G[Create Authentication with authorities]
    G --> I[Set SecurityContextHolder]
    I --> H
    H --> J[Authorization rules and controller]
    D -- Invalid or expired --> K[HandlerExceptionResolver]
    K --> L[GlobalExceptionHandler returns 401]
```

### 4. Security Context

Commits:

- [`41826ff`](https://github.com/shubhgaur37/SpringBoot/commit/41826ff) Getting User Details from Security Filter Context
- [`557d5ba`](https://github.com/shubhgaur37/SpringBoot/commit/557d5ba) RBAC: Fine Grained Access Control and avoid request matcher bloat using `@PreAuthorize` and `@Secure`

What was learned:

- Once the JWT filter sets the authenticated principal, services and authorization helpers can read it from `SecurityContextHolder`.
- The principal is a `UserEntity` only after authentication succeeds.
- If an endpoint is public, Spring may use an anonymous principal like `"anonymousUser"`, so direct casting to `UserEntity` is only safe behind authenticated routes.
- Spring Security stores the context per request, internally using thread-local context.

### 5. Refresh Token Flow

Commits:

- [`f2c3007`](https://github.com/shubhgaur37/SpringBoot/commit/f2c3007) Adding Support for refreshing jwt token upon expiry
- [`7129f90`](https://github.com/shubhgaur37/SpringBoot/commit/7129f90) Security Measure: Refresh Token Rotation

What was learned:

- Short-lived access tokens reduce the damage of token leakage.
- Refresh tokens are longer lived and are used only to obtain new access tokens.
- Refresh endpoints should validate:
  - The refresh token signature.
  - The refresh token expiration.
  - Whether the refresh token still represents an active server-side session.
- The login response returns the access token in the response body and stores only the refresh token in an HttpOnly cookie.
- The refresh response returns a new access token in the response body and sets a new refresh token cookie.
- The old refresh token session is deleted during refresh, so every refresh token is single-use.
- The client sends the access token with `Authorization: Bearer <token>`.
- Logout deletes the server-side refresh session and expires the refresh token cookie.

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant JWTService
    participant SessionService
    participant DB

    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login(email, password)
    AuthService->>AuthService: AuthenticationManager.authenticate()
    AuthService->>SessionService: createSession(user)
    SessionService->>JWTService: createAccessToken(user)
    SessionService->>JWTService: createRefreshToken(user)
    SessionService->>DB: save session
    AuthService-->>AuthController: access token + refresh token
    AuthController-->>Client: access token JSON + refreshToken HttpOnly cookie

    Client->>AuthController: POST /auth/refresh with refreshToken cookie
    AuthController->>AuthService: refresh(refreshToken)
    AuthService->>SessionService: refreshSession(refreshToken)
    SessionService->>JWTService: validateTokenGetUserId(refreshToken)
    SessionService->>DB: verify old refresh session
    SessionService->>JWTService: createAccessToken(user)
    SessionService->>JWTService: createRefreshToken(user)
    SessionService->>DB: save new session
    SessionService->>DB: delete old session
    AuthController-->>Client: new access token JSON + rotated refreshToken cookie

    Client->>AuthController: DELETE /auth/logout with refreshToken cookie
    AuthController->>AuthService: logout(refreshToken)
    AuthService->>SessionService: deleteSession(refreshToken)
    SessionService->>DB: delete refresh session
    AuthController-->>Client: 204 No Content + expired refreshToken cookie
```

### 6. Cookie Security

Commits:

- [`02668a5`](https://github.com/shubhgaur37/SpringBoot/commit/02668a5) Sending token as cookie alongwith response in login
- [`f2c3007`](https://github.com/shubhgaur37/SpringBoot/commit/f2c3007) Adding Support for refreshing jwt token upon expiry
- [`b1b2445`](https://github.com/shubhgaur37/SpringBoot/commit/b1b2445) OAuth Flow: Failure, Success Handler, Redirects

What was learned:

- `HttpOnly` cookies prevent JavaScript from reading tokens through `document.cookie`.
- This helps reduce token theft risk during XSS incidents.
- `HttpOnly` does not mean HTTPS-only.
- `Secure` cookies should be enabled in production so cookies are sent only over HTTPS.
- This project controls secure cookie behavior using `deployment.env`.
- The current login flow keeps the access token out of cookies and returns it in the response body.
- The refresh flow follows the same access-token rule: it returns the new access token in JSON instead of writing an access-token cookie.
- Refresh still writes a cookie, but only to replace the old refresh token with the newly rotated refresh token.
- The refresh token remains in an HttpOnly cookie because it is longer-lived and more sensitive.
- Logout sends a replacement `refreshToken` cookie with `Max-Age=0`, which tells the browser to delete it.
- Cookie invalidation only works when important cookie attributes, especially path and domain, match the original cookie.

### 7. OAuth2 Login

Commits:

- [`ba560fa`](https://github.com/shubhgaur37/SpringBoot/commit/ba560fa) Adding Oauth2 Client Dependency along with client-id and secret in yaml
- [`b92c130`](https://github.com/shubhgaur37/SpringBoot/commit/b92c130) removed devtools: A class-loading issue caused by the DevTools restart classloader
- [`b1b2445`](https://github.com/shubhgaur37/SpringBoot/commit/b1b2445) OAuth Flow: Failure, Success Handler, Redirects

What was learned:

- OAuth2 client support comes from `spring-boot-starter-oauth2-client`.
- Provider credentials are configured under `spring.security.oauth2.client.registration`.
- Google login verifies the user's identity, but the application still issues its own JWTs afterward.
- A custom `Oauth2SuccessHandler` can:
  - Read the OAuth2 user's email and name.
  - Create a local user if one does not exist.
  - Generate application access and refresh tokens.
  - Store the refresh token in an HttpOnly cookie.
  - Redirect the browser to a frontend success page.
- OAuth failure can redirect to a login error URL.
- DevTools restart classloader can cause class-loading surprises with security/OAuth classes, so it was removed.

```mermaid
flowchart TD
    A[Browser opens OAuth2 login] --> B[Spring Security redirects to Google]
    B --> C[Google authenticates user]
    C --> D[Google redirects back to application]
    D --> E[Oauth2SuccessHandler]
    E --> F[Read email and name from OAuth2User]
    F --> G{Local user exists?}
    G -- No --> H[Create local user]
    G -- Yes --> I[Reuse existing user]
    H --> J[Generate application JWTs]
    I --> J
    J --> K[Set refreshToken HttpOnly cookie]
    K --> L[Redirect to /home.html with access token]
```

### 8. Stateful Refresh Sessions

Commits:

- [`f771064`](https://github.com/shubhgaur37/SpringBoot/commit/f771064) Added a Session Table to prevent session abuse with max_session_limit and lru session eviction
- [`7129f90`](https://github.com/shubhgaur37/SpringBoot/commit/7129f90) Security Measure: Refresh Token Rotation

What was learned:

- Pure JWT access tokens are stateless, but refresh-token tracking can intentionally add server-side state.
- A `Session` table stores refresh tokens associated with users.
- Every login creates a new refresh-token session.
- The maximum active session count is capped at `2`.
- When the limit is reached, the least recently used session is evicted.
- Refreshing a token rotates the session by creating a new refresh token session and deleting the old one.
- Logout deletes the session associated with the refresh token, making that token unusable even if a stale cookie remains in the browser.

```mermaid
flowchart TD
    A["User logs in"] --> B["Find sessions for user"]
    B --> C{"Session count equals max?"}
    C -->|No| F["Save new session"]
    C -->|Yes| D["Sort by lastUsedAt"]
    D --> E["Delete least recently used session"]
    E --> F
    F --> G["Return refresh token"]

    H["Refresh request"] --> I["Find session by refresh token"]
    I --> J{"Found?"}
    J -->|No| K["Reject refresh"]
    J -->|Yes| L["Generate new access token and refresh token"]
    L --> M["Save new session"]
    M --> T["Delete old session"]
    T --> U["Return access token and rotated refresh cookie"]

    N["Logout request"] --> O["Find session by refresh token"]
    O --> P{"Found?"}
    P -->|No| Q["Reject logout"]
    P -->|Yes| R["Delete session"]
    R --> S["Expire refresh cookie"]
```

### 8.1 Logout Flow

Commit:

- [`7129f90`](https://github.com/shubhgaur37/SpringBoot/commit/7129f90) Security Measure: Refresh Token Rotation

What was learned:

- Logout in a refresh-token-backed system is primarily server-side invalidation.
- The backend deletes the `Session` row for the refresh token, making that refresh token unusable.
- The browser is also instructed to delete the `refreshToken` cookie by receiving an empty cookie with `Max-Age=0`.
- Cookie deletion depends on matching the original cookie identity, especially name, path, and domain.
- The server-side session remains the source of truth. A stale cookie alone should not be treated as an active login.

### 8.2 Refresh Token Rotation

Commit:

- [`7129f90`](https://github.com/shubhgaur37/SpringBoot/commit/7129f90) Security Measure: Refresh Token Rotation

What was learned:

- Refresh token rotation makes refresh tokens single-use.
- On every successful refresh, the server validates the presented refresh token, creates a new access token, creates a new refresh token, saves a new session, and deletes the old session.
- The rotated refresh token is sent back as an HttpOnly cookie. The JSON response contains the new access token but intentionally does not expose the new refresh token.
- `AuthService` now orchestrates authentication while `SessionService` owns session lifecycle, token generation, refresh validation, token rotation, concurrent session limits, and logout.

Why it matters:

- Limits replay attacks: if an old refresh token is stolen, it stops working after the legitimate client refreshes once.
- Reduces the value of leaked refresh tokens because each token has a much smaller useful lifetime.
- Gives the backend a revocation point through the `Session` table instead of trusting token expiration alone.
- Supports per-device logout because deleting one refresh-token session does not have to remove every session for the same user.
- Makes suspicious reuse detectable in a production system: a refresh attempt for a previously rotated token is a strong signal that a token may have been stolen.

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant SessionService
    participant JWTService
    participant DB

    Client->>AuthController: POST /auth/refresh with R1 cookie
    AuthController->>AuthService: refresh(R1)
    AuthService->>SessionService: refreshSession(R1)
    SessionService->>JWTService: validate R1 signature and expiry
    SessionService->>DB: find Session(R1)
    SessionService->>JWTService: create access token A2
    SessionService->>JWTService: create refresh token R2
    SessionService->>DB: save Session(R2)
    SessionService->>DB: delete Session(R1)
    AuthController-->>Client: A2 in JSON + R2 HttpOnly cookie

    Client->>AuthController: Later attempt with old R1
    AuthController->>AuthService: refresh(R1)
    AuthService->>SessionService: refreshSession(R1)
    SessionService->>DB: find Session(R1)
    DB-->>SessionService: not found
    AuthController-->>Client: reject refresh
```

### 9. RBAC Basics

Commits:

- [`74e7718`](https://github.com/shubhgaur37/SpringBoot/commit/74e7718) RBAC BASICS
- [`b80d24b`](https://github.com/shubhgaur37/SpringBoot/commit/b80d24b) RBAC: Adding Permissions along with roles. Check Request Matcher Precedence behaviour
- [`58b76ac`](https://github.com/shubhgaur37/SpringBoot/commit/58b76ac) RBAC: Linking Permissions with roles, production behaviour
- [`557d5ba`](https://github.com/shubhgaur37/SpringBoot/commit/557d5ba) RBAC: Fine Grained Access Control and avoid request matcher bloat using `@PreAuthorize` and `@Secure`

What was learned:

- Roles and authorities are related but not identical in Spring Security.
- `hasRole("ADMIN")` checks for an authority named `ROLE_ADMIN`.
- `hasAuthority("POST_VIEW")` checks the exact authority value.
- `@Secured` requires the full role authority name, such as `ROLE_ADMIN`.
- Permissions can be mapped from roles and returned as `GrantedAuthority` values.
- Endpoint matcher order matters: more specific matchers should come before broader ones.
- Large authorization rules inside `SecurityFilterChain` become hard to maintain.
- A cleaner pattern is:
  - Keep route-level rules simple: public vs authenticated.
  - Put business authorization near the method with `@Secured` or `@PreAuthorize`.

Current role hierarchy:

```mermaid
flowchart TD
    USER[USER] --> USER_PERMS[POST_VIEW<br/>USER_VIEW]
    CREATOR[CREATOR] --> CREATOR_PERMS[USER permissions<br/>POST_CREATE<br/>POST_UPDATE]
    ADMIN[ADMIN] --> ADMIN_PERMS[CREATOR permissions<br/>POST_DELETE<br/>USER_CREATE<br/>USER_UPDATE<br/>USER_DELETE]
```

Authorization flow:

```mermaid
flowchart TD
    A["HTTP Request"] --> B["JWTAuthFilter"]
    B --> C["SecurityContextHolder populated"]
    C --> D["SecurityFilterChain"]
    D --> E{"Route public?"}
    E -->|Yes| F["Controller method"]
    E -->|No| G{"Authenticated?"}
    G -->|No| H["401 Unauthorized"]
    G -->|Yes| F
    F --> I["Method security annotations"]
    I --> J{"Allowed?"}
    J -->|Yes| K["Service method"]
    J -->|No| L["403 Forbidden"]
```

### 10. Ownership Checks With SpEL

Commit:

- [`557d5ba`](https://github.com/shubhgaur37/SpringBoot/commit/557d5ba) RBAC: Fine Grained Access Control and avoid request matcher bloat using `@PreAuthorize` and `@Secure`

What was learned:

- `@PreAuthorize` supports Spring Expression Language.
- Custom beans can be called from SpEL with `@beanName.method(...)`.
- Method parameters can be referenced with `#parameterName`.
- `PostSecurityService.isOwnerOfPost(postId)` checks whether the authenticated user owns the requested post.

Example:

```java
@PreAuthorize("@postSecurityService.isOwnerOfPost(#id)")
public PostDTO getPostById(@PathVariable Long id) {
    return postService.getPostById(id);
}
```

## Final Architecture

```mermaid
flowchart LR
    Client["Client"] --> AuthController["AuthController"]
    Client --> PostController["PostController"]

    AuthController --> AuthService["AuthService"]
    AuthService --> AuthenticationManager["AuthenticationManager"]
    AuthenticationManager --> UserService["UserService and UserDetailsService"]
    UserService --> UserRepository["UserRepository"]
    AuthService --> JWTService["JWTService"]
    AuthService --> SessionService["SessionService"]
    SessionService --> SessionRepository["SessionRepository"]

    Client --> JWTAuthFilter["JWTAuthFilter"]
    JWTAuthFilter --> JWTService
    JWTAuthFilter --> UserService
    JWTAuthFilter --> SecurityContext["SecurityContextHolder"]

    PostController --> MethodSecurity["Method security annotations"]
    MethodSecurity --> PostSecurityService["PostSecurityService"]
    PostController --> PostService["PostService"]
    PostService --> PostRepository["PostRepository"]

    OAuthProvider["Google OAuth2"] --> Oauth2SuccessHandler["Oauth2SuccessHandler"]
    Oauth2SuccessHandler --> UserRepository
    Oauth2SuccessHandler --> JWTService
```

## Key Classes

| Class | Responsibility |
| --- | --- |
| `WebSecurityConfig` | Defines public routes, authenticated routes, stateless behavior, JWT filter placement, OAuth2 login, and method security. |
| `AppConfig` | Registers shared beans such as `ModelMapper` and `BCryptPasswordEncoder`. |
| `UserService` | Implements `UserDetailsService`, signs up users, loads users by email, and finds users by id. |
| `AuthService` | Performs credential authentication and delegates session creation, refresh rotation, and logout to `SessionService`. |
| `JWTService` | Creates and validates access and refresh JWTs. |
| `JWTAuthFilter` | Extracts bearer tokens, validates JWTs, loads users, and populates `SecurityContextHolder`. |
| `SessionService` | Owns session lifecycle: token generation, refresh-token rotation, max session enforcement, and logout invalidation. |
| `Oauth2SuccessHandler` | Handles successful Google login and bridges OAuth identity into application JWTs. |
| `UserEntity` | Implements `UserDetails` and converts roles/permissions into Spring Security authorities. |
| `RolePermissionMapper` | Maps application roles to permissions. |
| `PostSecurityService` | Performs post ownership authorization checks for `@PreAuthorize`. |
| `GlobalExceptionHandler` | Converts resource, authentication, JWT, and access denied exceptions into API responses. |

## API Overview

### Auth

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| `POST` | `/auth/signup` | Create a user with encoded password and roles. | Public |
| `POST` | `/auth/login` | Authenticate email/password, return an access token, and set the refresh token cookie. | Public |
| `POST` | `/auth/refresh` | Use refresh token cookie to issue a new access token and rotate the refresh token cookie. | Public route, validates refresh token |
| `DELETE` | `/auth/logout` | Delete the refresh-token session and expire the refresh token cookie. | Public route, requires refresh token cookie |

### Posts

| Method | Endpoint | Description | Authorization idea |
| --- | --- | --- | --- |
| `GET` | `/posts` | List posts. | Users with `ROLE_ADMIN` or `POST_VIEW` authority. |
| `GET` | `/posts/{id}` | Read one post. | Owner check through `PostSecurityService`. |
| `POST` | `/posts` | Create a post. | `ROLE_ADMIN` or `ROLE_CREATOR` via `@Secured`. |

## Authentication Flow

```mermaid
sequenceDiagram
    participant Client
    participant AuthController
    participant AuthService
    participant AuthenticationManager
    participant DaoProvider as DaoAuthenticationProvider
    participant UserService
    participant PasswordEncoder
    participant SessionService

    Client->>AuthController: POST /auth/login
    AuthController->>AuthService: login(LoginDTO)
    AuthService->>AuthenticationManager: authenticate(email, password)
    AuthenticationManager->>DaoProvider: supports UsernamePasswordAuthenticationToken
    DaoProvider->>UserService: loadUserByUsername(email)
    UserService-->>DaoProvider: UserEntity
    DaoProvider->>PasswordEncoder: matches(raw, encoded)
    PasswordEncoder-->>DaoProvider: valid
    DaoProvider-->>AuthenticationManager: authenticated Authentication
    AuthenticationManager-->>AuthService: principal = UserEntity
    AuthService->>SessionService: createSession(user)
    SessionService-->>AuthService: access token + refresh token
    AuthService-->>AuthController: LoginResponseDTO
    AuthController-->>Client: access token JSON + refreshToken HttpOnly cookie
```

## Request Authorization Strategy

The final design separates authentication and authorization:

1. `SecurityFilterChain` decides whether a request is public or must be authenticated.
2. `JWTAuthFilter` authenticates bearer-token requests by populating `SecurityContextHolder`.
3. Controller methods use `@Secured` and `@PreAuthorize` for business-specific authorization.
4. Custom authorization logic lives in a Spring bean such as `PostSecurityService`.

This avoids request matcher bloat. Instead of writing every role/permission rule in `WebSecurityConfig`, each business method carries the rule that protects it.

## Configuration Notes

`application.yaml` currently configures:

- MySQL datasource at `jdbc:mysql://localhost:3306/Spring_Security_Demo`
- Hibernate `ddl-auto: update`
- SQL logging
- One fallback Spring Security user
- Google OAuth2 client registration using environment variables:
  - `${google-client-id}`
  - `${google-client-secret}`
- JWT secret key
- `deployment.env`, used for production-only secure cookies

## Running Locally

Prerequisites:

- Java 21
- Maven wrapper support
- MySQL running locally
- A database named `Spring_Security_Demo`
- Google OAuth client id/secret if testing OAuth login

Run:

```bash
./mvnw spring-boot:run
```

For OAuth, provide the client values as environment variables or VM options:

```bash
google-client-id=...
google-client-secret=...
```

## Important Implementation Notes

- Access tokens currently expire very quickly for learning purposes.
- Refresh tokens are also short-lived for easier testing.
- The refresh token is stateful because it is stored in the `Session` table.
- Refresh tokens are rotated on every successful refresh, so the previous refresh token and session are immediately invalidated.
- The refresh response returns only the new access token in JSON; the new refresh token is sent as an HttpOnly cookie.
- Logout does not need to validate the JWT signature because it does not grant access or issue new tokens; it deletes the server-side session for the supplied refresh token.
- If a browser keeps sending a stale refresh cookie after logout, the backend still rejects refresh because the session row is gone.
- The app uses enum roles and permissions. This is suitable for a learning project, but production systems often model roles and permissions as database entities.
- `@ElementCollection` is used for user roles because roles are enum values, not independent role entities.
- `AccessDeniedException` is handled explicitly so REST APIs return JSON `403 Forbidden` instead of redirecting to an OAuth login page.
- `HandlerExceptionResolver` is used in `JWTAuthFilter` so JWT exceptions thrown before MVC controllers still reach `@RestControllerAdvice`.
- `PostController#getAllPosts` demonstrates composing role and permission checks in one method-level rule. `hasRole('ADMIN')` checks for `ROLE_ADMIN`, while `hasAuthority('POST_VIEW')` checks the exact permission authority:

```text
@PreAuthorize("hasRole('ADMIN') or hasAuthority('POST_VIEW')")
```

## Commit Summary

| Commit | Learning step |
| --- | --- |
| [`d5f0f01`](https://github.com/shubhgaur37/SpringBoot/commit/d5f0f01) | Added Spring Security and initial protected application. |
| [`c10afa0`](https://github.com/shubhgaur37/SpringBoot/commit/c10afa0) | Learned YAML-based default user configuration. |
| [`5d1c1dc`](https://github.com/shubhgaur37/SpringBoot/commit/5d1c1dc) | Added DB user entity, repository, and service. |
| [`5c85861`](https://github.com/shubhgaur37/SpringBoot/commit/5c85861) | Introduced custom `SecurityFilterChain`. |
| [`fbf4342`](https://github.com/shubhgaur37/SpringBoot/commit/fbf4342) | Added multiple in-memory users. |
| [`4f8fe07`](https://github.com/shubhgaur37/SpringBoot/commit/4f8fe07) | Switched toward stateless security. |
| [`c50d14f`](https://github.com/shubhgaur37/SpringBoot/commit/c50d14f) | Renamed base package. |
| [`76e4b96`](https://github.com/shubhgaur37/SpringBoot/commit/76e4b96) | Added basic JWT generation and validation. |
| [`bed117a`](https://github.com/shubhgaur37/SpringBoot/commit/bed117a) | Added signup and login. |
| [`02668a5`](https://github.com/shubhgaur37/SpringBoot/commit/02668a5) | Started sending tokens as cookies. |
| [`9f2d02f`](https://github.com/shubhgaur37/SpringBoot/commit/9f2d02f) | Added custom JWT authentication filter. |
| [`41826ff`](https://github.com/shubhgaur37/SpringBoot/commit/41826ff) | Read authenticated user from the security context. |
| [`61e3d8e`](https://github.com/shubhgaur37/SpringBoot/commit/61e3d8e) | Centralized auth and JWT exception handling. |
| [`f7e4f75`](https://github.com/shubhgaur37/SpringBoot/commit/f7e4f75) | Documented login principal mapping. |
| [`68d5f13`](https://github.com/shubhgaur37/SpringBoot/commit/68d5f13) | Documented signup/password verification behavior. |
| [`f2c3007`](https://github.com/shubhgaur37/SpringBoot/commit/f2c3007) | Added refresh token support. |
| [`ba560fa`](https://github.com/shubhgaur37/SpringBoot/commit/ba560fa) | Added OAuth2 client dependency and Google client config. |
| [`b92c130`](https://github.com/shubhgaur37/SpringBoot/commit/b92c130) | Removed DevTools due to classloader issues. |
| [`b1b2445`](https://github.com/shubhgaur37/SpringBoot/commit/b1b2445) | Added OAuth2 success handler, redirects, and frontend success page. |
| [`f771064`](https://github.com/shubhgaur37/SpringBoot/commit/f771064) | Added session table, max sessions, and LRU eviction. |
| [`74e7718`](https://github.com/shubhgaur37/SpringBoot/commit/74e7718) | Added RBAC basics. |
| [`b80d24b`](https://github.com/shubhgaur37/SpringBoot/commit/b80d24b) | Added permissions and explored matcher precedence. |
| [`58b76ac`](https://github.com/shubhgaur37/SpringBoot/commit/58b76ac) | Linked permissions with roles. |
| [`557d5ba`](https://github.com/shubhgaur37/SpringBoot/commit/557d5ba) | Moved fine-grained authorization to method security. |
| [`7129f90`](https://github.com/shubhgaur37/SpringBoot/commit/7129f90) | Added refresh token rotation and moved session lifecycle into `SessionService`. |

## Mental Model

Spring Security in this project can be understood as four layers:

```mermaid
flowchart TD
    A[Identity] --> B[Authentication]
    B --> C[Authorities]
    C --> D[Authorization]

    A1[Email/password or Google account] --> A
    B1[AuthenticationManager or OAuth2 login] --> B
    C1[Roles and permissions from UserEntity] --> C
    D1[SecurityFilterChain plus method security] --> D
```

- Identity answers: who is trying to access the system?
- Authentication answers: can we prove this identity?
- Authorities answer: what roles and permissions does this user have?
- Authorization answers: is this user allowed to perform this action?
