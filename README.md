# Common Exception Handler

A small, reusable library for centralized exception handling across Spring Boot microservices. It returns consistent, localized (i18n) error responses in the RFC 9457 (Problem Details, successor of RFC 7807) format and removes duplicated error-handling code.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)
![i18n](https://img.shields.io/badge/i18n-EN%20%7C%20AZ%20%7C%20RU-9cf)

---

## Table of Contents
- Overview
- How It Works
- Tech Stack
- Integration Guide
- Usage Examples
- Sample Responses
- Service-to-service errors (Feign / RestClient)
- Configuration reference
- Error codes
- Migrating from 1.x (Spring Boot 3)
- Releasing a new version
- FAQ / Troubleshooting

---

## Overview
- **Purpose:** one shared, opinionated way to handle exceptions in all microservices.
- **Result:** standardized, localized (EN, AZ, RU) error responses out of the box, including all standard Spring MVC errors (404, 405, 415, validation, malformed JSON, ...).
- **Format:** `application/problem+json` (RFC 9457) with extra `key`, `path`, `timestamp` and, for validation errors, `fieldErrors`.

## How It Works
- **ErrorCode:** errors are enums implementing `ErrorCode` (e.g. `CommonErrorCode`). Each code has a numeric code, i18n keys for title/message and a default HTTP status.
- **Exceptions:** `ErrorCode#exception(...)` creates a `BaseException`; the status defaults to the code's status (e.g. `RESOURCE_NOT_FOUND` → 404).
- **AbstractGlobalExceptionHandler:** extends Spring's `ResponseEntityExceptionHandler`, so every Spring MVC exception gets its correct status, and converts everything into the common `ProblemDetail` format.
- **Localization:** texts are taken from your service's `MessageSource` first and fall back to the translations bundled with the library. You only define keys you want to override.
- **Service key:** every payload has `key` = `service-key + code` (e.g. `ABLIMS1000`).
- **Logging:** unexpected errors and 5xx errors are logged with stack trace; the response never exposes internal exception messages.
- **Database and security errors:** `DataIntegrityViolationException` → 409 (`2001`), optimistic lock → 409 (`2002`), pessimistic lock → 423 (`2003`), Spring Security access denied → 403, authentication → 401. Detected by class name, so no extra dependency is required. SQL details are logged, never returned.
- **Auto-configuration:** `ErrorProperties` and the Feign error decoder are registered automatically.

## Tech Stack
- Java 21
- Spring Boot 4.x (Spring Framework 7, Jackson 3)
- Spring Web MVC, Jakarta Validation
- OpenFeign and RestClient (optional)

## Integration Guide

### 1) Add the library
The library is published to GitHub Packages. GitHub Packages requires authentication even for reading: create a personal access token with the `read:packages` scope and put it into `~/.gradle/gradle.properties` (on CI use the pipeline's secret store):

```properties
gpr.user=your-github-username
gpr.token=ghp_xxx
```

```gradle
// build.gradle of your microservice
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/GShahrza/common-exception-handler')
        credentials {
            username = findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = findProperty('gpr.token') ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'az.abb.loan:common-exception-handling:2.0.0'
}
```

### 2) Configure the service key
```yaml
common:
  error:
    service-key: ABLIMS
```

`ABLIMS` is an example; use a key specific to your service.

### 3) Extend the global exception handler
```java
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public GlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        super(messageSource, errorProperties);
    }
}
```

That's it. Translations for all `CommonErrorCode`s are bundled; no `MessageSource` configuration is required.

### 4) (Optional) Your own error codes and translations
```java
public enum LoanErrorCode implements ErrorCode {
    LOAN_NOT_FOUND("5001", "error.loan.not.found.title", "error.loan.not.found.message", HttpStatus.NOT_FOUND);
    // fields, constructor and methods like CommonErrorCode
}
```

Put texts for your own codes (or overrides of library texts) into the service's messages and let Spring Boot pick them up:

```yaml
spring:
  messages:
    basename: i18n/messages      # src/main/resources/i18n/messages_en.properties, _az, _ru
```

```properties
# i18n/messages_en.properties
error.loan.not.found.title=Loan Not Found
error.loan.not.found.message=Loan {0} does not exist
```

Save `.properties` files as **UTF-8**.

## Usage Examples

```java
// 404, message "Requested resource not found"
throw CommonErrorCode.RESOURCE_NOT_FOUND.exception();

// arguments are used in the message: "Loan 42 does not exist"
throw LoanErrorCode.LOAN_NOT_FOUND.exception(42);

// custom message instead of the translated one
throw CommonErrorCode.INTERNAL_ERROR.exceptionWithMessage("Unexpected issue during processing");

// explicit status
throw CommonErrorCode.DATA_INTEGRITY_VIOLATION.exception(HttpStatus.UNPROCESSABLE_CONTENT);

// extra fields in the response: {..., "maxAmount": 5000}
throw CommonErrorCode.BAD_REQUEST.exception().withProperty("maxAmount", 5000);
```

To customize the response, override the protected methods of `AbstractGlobalExceptionHandler`, e.g. `createProblemDetail`, `errorCodeFor` or `resolveLocale`.

## Sample Responses

Validation error:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Validation failed for one or more fields",
  "instance": "/insurance-ms/test/send",
  "key": "ABLIMS1000",
  "path": "/insurance-ms/test/send",
  "timestamp": "2026-01-11T07:42:47.889628Z",
  "fieldErrors": [
    { "field": "amount", "message": "must be greater than or equal to 100" }
  ]
}
```

Unknown URL (`GET /insurance-ms/nope`):

```json
{
  "type": "about:blank",
  "title": "Not Found",
  "status": 404,
  "detail": "Requested resource not found",
  "instance": "/insurance-ms/nope",
  "key": "ABLIMS3001",
  "path": "/insurance-ms/nope",
  "timestamp": "2026-01-11T07:50:00.123Z"
}
```

If a trace header (`traceparent`, `X-B3-TraceId`, `X-Trace-Id`) is present, or Micrometer Tracing has put a `traceId` into the logging MDC, `instance` is `trace:<id>`.

## Service-to-service errors (Feign / RestClient)

**Feign:** when OpenFeign and Jackson 3 are on the classpath, `CommonFeignErrorDecoder` is registered automatically as the default `ErrorDecoder` (unless you define your own). A downstream error becomes `CLIENT_ERROR` (code `4000`) with the **downstream HTTP status** and the downstream Problem Details `detail`. The downstream error key is kept as `downstreamKey`:

```json
{ "status": 404, "title": "Client Error", "detail": "Policy for loan 7 not found",
  "key": "LOAN4000", "downstreamKey": "INS3001", "...": "..." }
```

**RestClient / HTTP interface clients** (recommended for new code in Spring Boot 4):

```java
@Bean
RestClient loanClient(RestClient.Builder builder, JsonMapper jsonMapper) {
    return builder
            .baseUrl("http://loan-ms")
            .defaultStatusHandler(HttpStatusCode::isError, new ProblemDetailResponseErrorHandler(jsonMapper))
            .build();
}
```

## Configuration reference

| Property | Default | Description |
|---|---|---|
| `common.error.service-key` | `""` | Prefix for the `key` field |
| `common.error.default-locale` | `en` | Locale used when the request has no `Accept-Language` header |
| `common.error.feign.enabled` | `true` | Register `CommonFeignErrorDecoder` automatically |

## Error codes

| Enum | Code | Status |
|---|---|---|
| `VALIDATION_ERROR` | 1000 | 400 |
| `JSON_PARSE_ERROR` | 1001 | 400 |
| `CONSTRAINT_VIOLATION` | 1002 | 400 |
| `METHOD_ARGUMENT_TYPE_MISMATCH` | 1003 | 400 |
| `BAD_REQUEST` | 1004 | 400 |
| `DATA_INTEGRITY_VIOLATION` | 2001 | 409 |
| `OPTIMISTIC_LOCK` | 2002 | 409 |
| `PESSIMISTIC_LOCK` | 2003 | 423 |
| `RESOURCE_NOT_FOUND` | 3001 | 404 |
| `METHOD_NOT_ALLOWED` | 3002 | 405 |
| `UNSUPPORTED_MEDIA_TYPE` | 3003 | 415 |
| `NOT_ACCEPTABLE` | 3004 | 406 |
| `CLIENT_ERROR` | 4000 | 502 (or the downstream status) |
| `UNAUTHORIZED` | 4001 | 401 |
| `ACCESS_DENIED` | 4003 | 403 |
| `INTERNAL_ERROR` | 9999 | 500 |

## Migrating from 1.x (Spring Boot 3)

- The library is consumed as a versioned dependency (`az.abb.loan:common-exception-handling`) instead of copying the module into `libs/`.
- Requires Spring Boot 4.0+ / Spring Framework 7 and Jackson 3 (`tools.jackson`). Tested with Boot 4.0.8 and 4.1.1.
- `CommonFeignErrorDecoder` now takes a Jackson 3 `tools.jackson.databind.ObjectMapper` (or use the no-arg constructor). It is registered automatically; remove manual registration unless you need a custom one.
- `CommonErrorCode.exception()` now uses the code's own status (e.g. `RESOURCE_NOT_FOUND` → 404 instead of 400). Pass a status explicitly to keep the old behaviour.
- `CLIENT_ERROR` now uses i18n keys `error.client.title` / `error.client.message`.
- The handler methods `handleValidationException`, `handleJsonParse`, `handleRequestParamError` and `handleMethodNotSupported` were removed: these exceptions are now handled through `ResponseEntityExceptionHandler`. If your subclass declares its own `@ExceptionHandler` for one of Spring MVC's standard exceptions, override the matching `handle...` method of `ResponseEntityExceptionHandler` instead, otherwise Spring reports an ambiguous handler at startup.
- Bundled translations moved from `i18n/messages*.properties` to `i18n/common-errors*.properties` so they no longer clash with the service's own files. The `MessageConfig` class from 1.x is no longer needed.

## Releasing a new version
1. Merge the changes into `main` (CI builds and tests against Spring Boot 4.0 and 4.1).
2. Create and push a tag with the version: `git tag v2.0.1 && git push origin v2.0.1`.
3. The `release` workflow builds, tests and publishes `az.abb.loan:common-exception-handling:2.0.1`.

Local builds use version `2.0.0-SNAPSHOT`; `./gradlew publishToMavenLocal -PreleaseVersion=2.0.1` installs a version into `~/.m2` for local testing. To publish to another Maven repository (e.g. Nexus), pass `-PpublishUrl=... -PpublishUser=... -PpublishPassword=...`.

## FAQ / Troubleshooting
- **Title/message are not translated:** check `spring.messages.basename` and that the files are UTF-8. Without `Accept-Language`, `common.error.default-locale` is used.
- **`key` has no prefix:** set `common.error.service-key`.
- **Feign errors are not converted:** another `ErrorDecoder` bean exists, or `common.error.feign.enabled=false`.
