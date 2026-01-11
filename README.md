# Common Exception Handler

A tiny, reusable library for centralized exception handling across Spring Boot microservices. It returns consistent, localized (i18n) error responses compliant with RFC 7807 (Problem Details) and eliminates duplicated error-handling code.

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen)
![i18n](https://img.shields.io/badge/i18n-EN%20%7C%20AZ%20%7C%20RU-9cf)

---

## Table of Contents
- Overview
- How It Works
- Tech Stack
- Integration Guide
  - Add the library
  - Configure service key
  - Add translations (i18n)
  - Configure MessageSource
  - Extend the global exception handler
- Usage Examples
- Sample Responses
- FAQ / Troubleshooting
- Notes

---

## Overview
- Purpose: Provide a shared, opinionated way to handle exceptions in all microservices.
- Result: You get standardized, localized (EN, AZ, RU) error responses out of the box. MS-to-MS errors are handled automatically via Feign.
- Format: Responses follow RFC 7807 (Problem Details) JSON structure.

## How It Works
- ErrorCode: All possible errors are defined as enums (e.g., `CommonErrorCode`). Each code has a numeric code plus i18n keys (title/message).
- Exceptions: Domain-specific exceptions extend `BaseException` (e.g., `BadRequestException`) and are created with an `ErrorCode` and optional arguments.
- AbstractGlobalExceptionHandler: Catches exceptions and returns a `ProblemDetail` JSON with fields: `type`, `title`, `status`, `detail`, `instance`, `key`, `path`, `timestamp`.
- Localization (i18n): Messages are read from `MessageSource` using request locale headers (fallback to EN).
- Service key: Every error payload includes a `key` composed of `service-key + errorCode` (e.g., `ABLIMS1000`).
- Feign support: Includes `CommonFeignErrorDecoder` to consistently map MS-to-MS errors.

## Tech Stack
- Java 21
- Spring Boot 3.5.x
- Spring Web, Spring Context
- Jakarta Validation API
- Jackson (JSON serialization)
- Spring MessageSource (i18n)
- OpenFeign (optional)

## Integration Guide

### 1) Add the library
Place this module under your root `libs/` folder and add a project dependency from your microservice:

```gradle
// build.gradle of your microservice
dependencies {
    implementation project(":common-exception-handler")
}
```

### 2) Configure the service key
Add your service key to `application.yml`:

```yaml
common:
  error:
    service-key: ABLIMS
```

Note: `ABLIMS` is just an example. Use a key specific to your service. This key prefixes each error code (e.g., `ABLIMS1000`).

### 3) Add translations (i18n)
Create translation files under your microservice at `src/main/resources/i18n/`:

- `messages_en.properties`
- `messages_az.properties`
- `messages_ru.properties`

Example `messages_en.properties`:

```
error.validation.title=Validation Error
error.validation.message=One or more fields are invalid
error.json.parse.title=Malformed JSON
error.json.parse.message=Malformed JSON request
error.constraint.title=Constraint Violation
error.constraint.message=Request constraint violated
error.method.argument.title=Invalid Argument
error.method.argument.message=Invalid value for parameter {0}
error.resource.notfound.title=Not Found
error.resource.notfound.message=Requested resource not found
error.internal.title=Internal Error
error.internal.message=Unexpected internal error
error.client.title=Client Error
error.client.message=Feign client error: {0}
```

Mirror the same keys in AZ and RU property files with appropriate translations.

### 4) Configure MessageSource
Create a simple configuration class in your microservice:

```java
@Configuration
public class MessageConfig {

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages");
        ms.setDefaultEncoding("UTF-8");
        ms.setUseCodeAsDefaultMessage(true); // fallback to key if translation is missing
        return ms;
    }
}
```

### 5) Extend the global exception handler
Create an advice class and extend the abstract handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler extends AbstractGlobalExceptionHandler {

    public GlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        super(messageSource, errorProperties);
    }
}
```

That’s it. All exceptions will now be handled automatically and returned as RFC 7807 Problem Details.

## Usage Examples
- Validation / Bad Request:

```java
throw CommonErrorCode.VALIDATION_ERROR.badRequest("amount");
```

- Resource not found:

```java
throw CommonErrorCode.RESOURCE_NOT_FOUND.notFound(42);
```

- Feign client error:

```java
throw CommonErrorCode.CLIENT_ERROR.exceptionWithCustomMessage(clientResponse.getBody());
```

- Internal error (with custom message):

```java
throw CommonErrorCode.INTERNAL_ERROR.exceptionWithCustomMessage("Unexpected issue during processing");
```

## Sample Responses
- Validation Error:

```json
{
  "type": "about:blank",
  "title": "Validation Error",
  "status": 400,
  "detail": "One or more fields are invalid",
  "instance": "/insurance-ms/test/send",
  "key": "ABLIMS1000",
  "path": "/insurance-ms/test/send",
  "timestamp": "2026-01-11T07:42:47.889628Z",
  "fieldErrors": [
    { "field": "amount", "message": "must be greater than or equal to 100" }
  ]
}
```

- Feign Client Error:

```json
{
  "type": "about:blank",
  "title": "Client Error",
  "status": 502,
  "detail": "Service XYZ unavailable",
  "instance": "/insurance-ms/api/xyz",
  "key": "ABLIMS4000",
  "path": "/insurance-ms/api/xyz",
  "timestamp": "2026-01-11T07:50:00.123Z"
}
```

## FAQ / Troubleshooting
- Why are title/message not translated?
  - `MessageSource` might be misconfigured or i18n files are not on the classpath.
  - If no locale header (Accept-Language) is provided, EN is used by default.

- Why is the `key` empty or incorrect?
  - You may have forgotten to set `common.error.service-key` in `application.yml`.

- Why aren’t Feign errors returned as Problem Details?
  - Ensure `CommonFeignErrorDecoder` is in place or verify your decoder configuration.

## Notes
- This module is packaged as a library (bootJar disabled, jar enabled). Consumer microservices should add it as `project(':common-exception-handler')`.
- Spring’s `ProblemDetail` is used for RFC 7807 compliance.
