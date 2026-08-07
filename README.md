# sdk_util
This is an SDK for Java including OpenAPI, Spring Security, response helpers, structured logging,
and standardized REST exception responses.

## REST exception handler

Servlet-based Spring Boot services automatically receive the SDK `GlobalExceptionHandler` when
Spring MVC is available. It handles request validation, malformed JSON, argument type mismatches,
constraint violations, invalid arguments, missing resources, and unexpected HTTP exceptions.

Use `com.mac.sdk_util.exception.ResourceNotFoundException` for a standardized HTTP 404 response.
All response messages are in English and use `ResponseDTO` through `ResponseHelper`.

The handler is enabled by default. A service can disable it when it needs to replace the complete
HTTP exception policy:

```yaml
sdk:
  web:
    exception-handler:
      enabled: false
```

Service-specific `@ControllerAdvice` classes can coexist with the SDK handler for domain-specific
exceptions. Give them a higher precedence, for example `@Order(Ordered.HIGHEST_PRECEDENCE)`, so they
run before the SDK fallback handler. Kafka listeners, schedulers, and asynchronous tasks require
their own error handlers because they do not run through Spring MVC.
