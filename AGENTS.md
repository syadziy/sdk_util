# AGENTS.md

## Project Overview

`sdk-util` is a reusable Java library that centralizes cross-service conventions. It is a JAR, not
a runnable Spring Boot application.

Stack utama:

- Java 21
- Spring Boot 4.1.0 dependency BOM and auto-configuration APIs
- Maven
- Spring MVC integration
- Spring Security OAuth2 Resource Server and JWT conversion
- Springdoc OpenAPI
- Jakarta Validation
- Jackson
- SLF4J structured logging and MDC
- JUnit 5 and Spring Boot test utilities

Public capabilities:

- Standard `ResponseDTO`, response codes, and response helpers.
- Auto-configured global REST exception handling.
- ECS structured logging, HTTP trace ID, MDC utilities, and configurable AOP method logging.
- OAuth2/JWT servlet security, standardized 401/403 responses, method security, and CORS.
- OpenAPI/Swagger defaults with optional application path prefix.
- Application timezone and general date/string/query constants.

Prioritas desain:

- Safe defaults with explicit opt-out properties.
- Spring Boot auto-configuration without consumer component scanning.
- Backward compatibility for public classes, methods, JSON fields, property names, and defaults.
- Consumer override through conditional beans rather than copied configuration.
- No assumptions about one specific business service.

---

## Project Structure

```text
src/main/java/com/mac/sdk_util/
├── config/
│   ├── logging/            # ECS format, MDC filter, and AOP logging auto-configuration
│   │   └── properties/
│   ├── openapi/            # OpenAPI bean and prefixed-path forwarding
│   │   └── properties/
│   ├── securities/         # JWT, filter chain, method security, CORS
│   │   └── properties/
│   ├── timezone/           # ZoneId and DateUtil initialization
│   │   └── properties/
│   └── web/                # Global exception handler auto-configuration
├── entities/
│   ├── constant/           # Status, JWT, logging, role, and table constants
│   └── dto/                # Shared response and paging DTOs
├── exception/              # Shared exceptions safe for consumers
├── openapi/                # OpenAPI conditions, constants, environment setup
├── securities/             # JWT converter, response writer, customization SPI
├── utils/                  # Response, logging, date, string, and query helpers
└── web/                    # Shared ControllerAdvice implementation

src/main/resources/META-INF/spring/
├── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── org.springframework.boot.env.EnvironmentPostProcessor

src/test/java/com/mac/sdk_util/ # Unit and auto-configuration tests
docs/                          # ADR and consumer guides
```

The package name `com.mac.sdk_util` is an established public namespace. Do not rename it as a style
cleanup because that would break every consumer import.

---

## Development Commands

Run commands from the `sdk_util` directory. This repository currently uses the system Maven command
and does not contain Maven Wrapper scripts.

### Build

```bash
mvn clean verify
```

### Install for local consumers

```bash
mvn clean install
```

### Test

```bash
mvn test
```

### Run a specific test

```bash
mvn -Dtest=ResponseHelperTest test
```

There is no `spring-boot:run` command for this project because the artifact is a library without an
application main class.

---

## Coding Guidelines

### Java Version

Use Java 21 while keeping public APIs straightforward for consumer services.

Preferred:

- Records for new immutable DTOs when Jackson/Spring compatibility is verified.
- Switch expressions and pattern matching for clearer closed-type logic.
- Small final utility classes with private constructors.
- Immutable return values where changing mutability would not break compatibility.
- Spring `ObjectProvider` for optional consumer-provided beans.

Avoid:

- Legacy date APIs except where required to configure JVM timezone compatibility.
- Raw types, unchecked casts, and reflection-heavy shortcuts.
- Static mutable global state; `DateUtil` is an existing compatibility exception and must remain
  thread-safe.
- Business-domain types or behavior from a single service.
- Adding heavy transitive dependencies for a small helper.

---

## Naming Convention

- Class, record, and enum: `PascalCase`.
- Method and variable: `camelCase`.
- Constants: `UPPER_SNAKE_CASE`.
- Existing package root: `com.mac.sdk_util`.
- Auto-configuration: suffix `AutoConfiguration`.
- Configuration binding: suffix `Properties`.
- Spring conditions: prefix `On` and suffix `Condition`.
- Shared exceptions: suffix `Exception`.
- Request/response DTOs: suffix `Request` or `Response` when applicable.

Property prefixes must stay within an established namespace such as `sdk.logging.*`,
`sdk.security.*`, `sdk.openapi.*`, `sdk.web.*`, or the existing
`jwt.auth.converter.*` namespace.

---

## Spring Guidelines

Auto-configuration is part of the SDK's public behavior.

- Register every new auto-configuration in
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
- Register environment post-processors only in
  `META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor`.
- Prefer `@AutoConfiguration` for new configurations; preserve existing `@Configuration` classes
  unless deliberately migrating them.
- Guard servlet features with `@ConditionalOnWebApplication(SERVLET)` and relevant
  `@ConditionalOnClass` checks.
- Use `@ConditionalOnMissingBean` so consumers can provide their own implementation.
- Use `@ConditionalOnProperty(..., matchIfMissing = true)` only when enabled-by-default behavior is
  intentional and documented.
- Bind related settings through `@ConfigurationProperties`, including explicit default values.
- Do not require consumers to add `@ComponentScan("com.mac.sdk_util")`.
- Avoid component annotations on utility types unless component scanning is genuinely intended.
- Be careful when optional dependency classes appear in bean method signatures; class loading must
  remain safe when the optional technology is absent.

Environment post-processors provide low-precedence defaults. Never overwrite a property explicitly
configured by the consumer.

---

## Entity Rules

The SDK has no persistence entities. `entities/dto` contains public wire-format objects and
`entities/constant` contains shared constants.

- Preserve `ResponseDTO` JSON field names and null-handling behavior.
- Preserve response codes and HTTP status mappings unless releasing a deliberate breaking version.
- Do not add service-specific domain fields to shared DTOs.
- Keep paging metadata generic.
- Treat enum names and public constants as consumer-facing API.
- Prefer additive changes over renaming or deleting fields/methods.

---

## Mapper

The SDK currently has no mapping framework. Do not add MapStruct solely for trivial conversions.

- Keep generic object conversion behavior in focused utilities such as `StringUtil`.
- Reuse a consumer-provided `ObjectMapper` in Spring integration code when possible.
- Do not create a new `ObjectMapper` in request handling when the application mapper is available.
- Document naming strategies or serialization behavior that can affect consumers.
- Mapping that contains business rules belongs in the consuming service, not the SDK.

---

## Database

The SDK does not connect to a database and must not introduce datasource, JPA entity, repository,
or migration assumptions.

`QueryUtil` only exposes generic operator/data-type constants. It does not build, validate, or
sanitize SQL. Do not expand it into a database abstraction without an explicit architectural
decision and security review.

Avoid adding database drivers or persistence starters as transitive dependencies.

---

## Error Handling

`GlobalExceptionHandler` standardizes Spring MVC failures through `ResponseHelper`.

- Keep response messages in English.
- Do not return stack traces, exception class names, SQL, secrets, or internal paths to clients.
- Unexpected exceptions must be logged with structured fields and return the generic 500 response.
- Treat `IllegalArgumentException` as 400 only when it represents invalid client input.
- Keep `ResourceNotFoundException` generic and free of service-specific fields.
- Keep the SDK handler at low precedence so consumer advice can handle domain exceptions first.
- Preserve the `sdk.web.exception-handler.enabled` opt-out property.

This handler covers only Spring MVC requests. Do not claim that it handles Kafka listeners,
schedulers, executors, reactive pipelines, or virtual threads. Consumer services own those error
boundaries.

When adding a handled exception, test its status, response code, English message, and whether
internal details remain hidden.

---

## Logging

Use SLF4J and `StructuredLog`. Never use `System.out` or `System.err`.

- Prefer ECS-compatible keys from `LogFields`.
- `StructuredLog.error(..., Throwable)` must preserve the cause.
- Never log credentials, JWT values, authorization headers, request bodies, or personal data.
- Keep HTTP trace header behavior configurable.
- Clear request MDC in `finally` to prevent thread-pool context leakage.
- `withMdc` must restore the previous context even when the action fails.
- Document that MDC does not propagate automatically to asynchronous work.
- AOP logging must rethrow the original exception and must not alter method results.
- Keep `event.duration` units documented and consistent; it is currently milliseconds.

Changes to default ECS format, trace header, filter order, AOP package matching, or field names are
consumer-visible behavior and require compatibility review.

---

## Validation

Jakarta Validation is used by the shared MVC exception handler, but validation rules remain owned
by consumer DTOs.

- Do not impose business-specific constraints from the SDK.
- Keep validation error output deterministic; constraint violations are currently sorted.
- Preserve the `field: message` error format unless an API contract change is approved.
- Test malformed JSON separately from bean validation errors.
- Keep validation messages returned by SDK-owned code in English.

---

## Testing

Every behavior or default change must include focused tests.

- Unit-test all public helper status/body/header mappings.
- Test auto-configuration with Spring Boot context runners.
- For each conditional configuration, test enabled/default, disabled, and consumer override cases.
- Test property binding and fallback precedence.
- Test servlet filters with incoming/missing trace headers and MDC cleanup.
- Test JWT roles from scopes, `realm_access`, and configured `resource_access` client.
- Test 401/403 and global exception response contracts.
- Test OpenAPI path-prefix behavior with and without servlet context path.
- Avoid requiring external identity providers, databases, Kafka, or network access in SDK tests.

When fixing a consumer-reported regression, add a regression test in the SDK whenever it can be
reproduced without consumer business code.

---

## API Design

For this library, public API includes more than Java methods:

- Maven coordinates and transitive dependency behavior.
- Public classes, constructors, methods, enums, constants, and package names.
- `ResponseDTO` JSON shape and response codes.
- Configuration property names and defaults.
- Auto-configuration activation and back-off conditions.
- Bean names used by `@ConditionalOnMissingBean(name = ...)`.
- Log field names, trace header behavior, and OpenAPI/security defaults.

Prefer additive, backward-compatible changes. A breaking change requires a version change,
migration notes, and coordinated consumer updates.

Do not add service-specific REST endpoints to the SDK.

---

## Performance

- Keep servlet filters allocation-conscious and always clean thread-local MDC state.
- Cache or reuse framework objects where safe; avoid constructing mappers per request.
- Keep AOP pointcuts limited to explicitly configured packages.
- Do not serialize method arguments or return values in generic AOP logging.
- Avoid blocking network calls during auto-configuration except behavior explicitly required by a
  framework, such as JWT issuer-based decoder initialization.
- Avoid increasing startup time or dependency footprint without measurement and justification.

---

## Security

Security defaults affect every consuming service and require extra scrutiny.

- Never include secrets, test tokens, real issuer credentials, or private keys.
- Preserve standardized 401/403 bodies without exposing authentication details.
- Keep JWT authority extraction type-safe when claims are missing or malformed.
- Roles receive the `ROLE_` prefix; test changes against `hasRole` semantics.
- `sdk.security.permit-all-paths` uses `WebSecurityCustomizer.ignoring`, which bypasses the security
  filter chain. Do not broaden defaults casually.
- CORS is currently permissive and enabled independently through `sdk.security.cors.enabled`.
  Tightening defaults requires migration notes; broadening them is prohibited without review.
- `sdk.security.enabled`, method security, and CORS conditions are independent; document this when
  changing activation behavior.
- Validate redirects/forwards and normalize prefixes to prevent unsafe path behavior.

Add security tests for any change to JWT, public paths, CORS, CSRF, session policy, or error
responses.

---

## Before Finishing Any Task

The agent must:

1. Inspect `git status` and preserve unrelated user changes.
2. Compile and run the smallest relevant tests during development.
3. Run `mvn test` for completed code changes.
4. Run `mvn clean verify` for auto-configuration, dependency, security, or public-contract changes.
5. Run `git diff --check`.
6. Confirm new auto-configurations/environment processors are registered in the correct resource.
7. Check enabled, disabled, and consumer-override behavior for conditional features.
8. Remove unused imports and temporary files.
9. Update README/property reference for public features or default changes.
10. Report any skipped validation and its exact blocker.

Existing unrelated warnings are not a reason to rewrite unrelated code, but do not introduce new
warnings.

---

## Pull Request Checklist

- Tests for changed behavior pass.
- Public API and configuration compatibility were reviewed.
- Auto-configuration backs off when the consumer supplies an equivalent bean.
- Optional technologies are guarded by appropriate class/web conditions.
- Default properties and activation behavior are documented.
- Response/error messages owned by the SDK are in English.
- Structured logs are ECS-friendly and contain no sensitive payloads.
- No business-service-specific types or assumptions were added.
- No generated `target/`, local Maven artifacts, secrets, or temporary files are committed.
- README and consumer examples match the implemented behavior.

---

## Things Never To Do

- Do not turn the SDK into a runnable business service.
- Do not require consumer component scanning for SDK features.
- Do not register auto-configuration only through legacy `spring.factories`.
- Do not overwrite consumer-provided configuration values.
- Do not remove `@ConditionalOnMissingBean` override points without justification.
- Do not introduce database, Kafka, scheduler, or service-specific dependencies into the core SDK.
- Do not leak exceptions, credentials, JWTs, or request payloads through responses or logs.
- Do not silently change response codes, JSON fields, public paths, CORS, security, or trace defaults.
- Do not rename `com.mac.sdk_util` casually.
- Do not use destructive Git commands or delete consumer-owned changes.

---

## Preferred Code Style

Prefer:

- Small cohesive classes and methods.
- Constructor injection for Spring components.
- Immutable configuration inputs and DTOs where compatible.
- Explicit conditions and defaults.
- Composition over inheritance.
- Public APIs that are difficult to misuse.
- Readable code over clever code.

Match the surrounding formatting when editing an existing file. Avoid unrelated mechanical
reformatting, dependency churn, or public API cleanup in a scoped change.
