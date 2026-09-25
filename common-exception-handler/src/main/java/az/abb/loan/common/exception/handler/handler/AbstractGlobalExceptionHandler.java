package az.abb.loan.common.exception.handler.handler;

import az.abb.loan.common.exception.handler.config.ErrorProperties;
import az.abb.loan.common.exception.handler.error.CommonErrorCode;
import az.abb.loan.common.exception.handler.error.ErrorCode;
import az.abb.loan.common.exception.handler.exception.BaseException;
import az.abb.loan.common.exception.handler.model.FieldErrorResponse;
import az.abb.loan.common.exception.handler.model.TraceHeaders;
import az.abb.loan.common.exception.handler.support.ErrorMessageResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Base class for a service's {@code @RestControllerAdvice}.
 *
 * <p>All standard Spring MVC exceptions (404 for unknown URLs, 405, 415, missing parameters, validation, ...)
 * are handled by {@link ResponseEntityExceptionHandler} with their correct HTTP status and are then
 * converted into the common localized {@link ProblemDetail} format in {@link #handleExceptionInternal}.
 */
public abstract class AbstractGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ACCEPT_LANGUAGE = "Accept-Language";

    protected final MessageSource messageSource;
    protected final ErrorProperties errorProperties;
    protected final ErrorMessageResolver messages;
    private final Clock clock;

    protected AbstractGlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties) {
        this(messageSource, errorProperties, Clock.systemUTC());
    }

    protected AbstractGlobalExceptionHandler(MessageSource messageSource, ErrorProperties errorProperties,
                                             Clock clock) {
        this.messageSource = messageSource;
        this.errorProperties = errorProperties;
        this.messages = new ErrorMessageResolver(messageSource);
        this.clock = clock;
    }

    // ---------------------------------------------------------------- application exceptions

    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBaseException(BaseException ex, HttpServletRequest request) {
        if (ex.getStatus().is5xxServerError()) {
            logger.error("Request " + request.getRequestURI() + " failed: " + ex.getMessage(), ex);
        } else if (logger.isDebugEnabled()) {
            logger.debug("Request " + request.getRequestURI() + " rejected: " + ex.getMessage());
        }
        return createProblemDetail(ex.getErrorCode(), ex.getStatus(), ex.getArgs(), ex.getDetail(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        ProblemDetail pd = createProblemDetail(CommonErrorCode.CONSTRAINT_VIOLATION,
                CommonErrorCode.CONSTRAINT_VIOLATION.httpStatus(), null, null, request);
        List<FieldErrorResponse> fields = ex.getConstraintViolations().stream()
                .map(v -> new FieldErrorResponse(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        pd.setProperty("fieldErrors", fields);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatusCode annotated = annotatedStatus(ex);
        if (annotated != null) {
            if (annotated.is5xxServerError()) {
                logger.error("Request " + request.getRequestURI() + " failed", ex);
            }
            return createProblemDetail(errorCodeForStatus(annotated), annotated, null, null, request);
        }
        if (isSecurityException(ex, "AccessDeniedException")) {
            return createProblemDetail(CommonErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN, null, null, request);
        }
        if (isSecurityException(ex, "AuthenticationException")) {
            return createProblemDetail(CommonErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, null, null, request);
        }
        logger.error("Unexpected error for request " + request.getRequestURI(), ex);
        return createProblemDetail(CommonErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR,
                null, null, request);
    }

    // ---------------------------------------------------------------- Spring MVC exceptions

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (!(request instanceof ServletWebRequest servletWebRequest)) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }
        HttpServletRequest servletRequest = servletWebRequest.getRequest();
        ProblemDetail pd = createProblemDetail(errorCodeFor(ex, statusCode), statusCode, argsFor(ex), null,
                servletRequest);
        List<FieldErrorResponse> fieldErrors = fieldErrorsFor(ex, resolveLocale(servletRequest));
        if (!fieldErrors.isEmpty()) {
            pd.setProperty("fieldErrors", fieldErrors);
        }
        return super.handleExceptionInternal(ex, pd, headers, statusCode, request);
    }

    /** Maps a Spring MVC exception to an error code. Override to customize. */
    protected ErrorCode errorCodeFor(Exception ex, HttpStatusCode status) {
        return switch (ex) {
            case MethodArgumentNotValidException e -> CommonErrorCode.VALIDATION_ERROR;
            case HandlerMethodValidationException e -> CommonErrorCode.VALIDATION_ERROR;
            case HttpMessageNotReadableException e -> CommonErrorCode.JSON_PARSE_ERROR;
            case TypeMismatchException e -> CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
            case MissingServletRequestParameterException e -> CommonErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
            default -> errorCodeForStatus(status);
        };
    }

    protected ErrorCode errorCodeForStatus(HttpStatusCode status) {
        return switch (status.value()) {
            case 401 -> CommonErrorCode.UNAUTHORIZED;
            case 403 -> CommonErrorCode.ACCESS_DENIED;
            case 404 -> CommonErrorCode.RESOURCE_NOT_FOUND;
            case 405 -> CommonErrorCode.METHOD_NOT_ALLOWED;
            case 406 -> CommonErrorCode.NOT_ACCEPTABLE;
            case 415 -> CommonErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default -> status.is4xxClientError() ? CommonErrorCode.BAD_REQUEST : CommonErrorCode.INTERNAL_ERROR;
        };
    }

    protected Object[] argsFor(Exception ex) {
        return switch (ex) {
            case TypeMismatchException e -> new Object[] {e.getPropertyName(), e.getValue()};
            case MissingServletRequestParameterException e -> new Object[] {e.getParameterName()};
            default -> null;
        };
    }

    protected List<FieldErrorResponse> fieldErrorsFor(Exception ex, Locale locale) {
        List<FieldErrorResponse> result = new ArrayList<>();
        if (ex instanceof MethodArgumentNotValidException e) {
            e.getBindingResult().getFieldErrors().forEach(f ->
                    result.add(new FieldErrorResponse(f.getField(), messages.resolve(f, locale))));
            e.getBindingResult().getGlobalErrors().forEach(g ->
                    result.add(new FieldErrorResponse(g.getObjectName(), messages.resolve(g, locale))));
        } else if (ex instanceof HandlerMethodValidationException e) {
            e.getParameterValidationResults().forEach(r -> {
                String name = r.getMethodParameter().getParameterName();
                r.getResolvableErrors().forEach(err ->
                        result.add(new FieldErrorResponse(name, messages.resolve(err, locale))));
            });
        }
        return result;
    }

    // ---------------------------------------------------------------- building the response

    protected ProblemDetail createProblemDetail(ErrorCode errorCode, HttpStatusCode status, Object[] args,
                                                String detailOverride, HttpServletRequest request) {
        Locale locale = resolveLocale(request);
        String title = messages.resolve(errorCode.titleKey(), null, locale);
        String detail = detailOverride != null ? detailOverride : messages.resolve(errorCode.messageKey(), args, locale);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(title);
        pd.setInstance(resolveInstance(request));
        pd.setProperty("key", errorProperties.getServiceKey() + errorCode.code());
        pd.setProperty("path", request.getRequestURI());
        pd.setProperty("timestamp", Instant.now(clock));
        return pd;
    }

    protected Locale resolveLocale(HttpServletRequest request) {
        if (request.getHeader(ACCEPT_LANGUAGE) == null) {
            return errorProperties.getDefaultLocale();
        }
        // Honors the application's LocaleResolver (Accept-Language by default)
        return LocaleContextHolder.getLocale();
    }

    protected URI resolveInstance(HttpServletRequest request) {
        String traceId = firstNonBlank(
                request.getHeader(TraceHeaders.TRACEPARENT),
                request.getHeader(TraceHeaders.X_B3_TRACE_ID),
                request.getHeader(TraceHeaders.X_TRACE_ID),
                request.getHeader(TraceHeaders.X_B3_SPAN_ID)
        );
        try {
            return traceId != null ? URI.create("trace:" + traceId.strip()) : URI.create(request.getRequestURI());
        } catch (IllegalArgumentException e) {
            // Malformed client-supplied header must not break error handling itself
            return URI.create(request.getRequestURI());
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static HttpStatusCode annotatedStatus(Exception ex) {
        ResponseStatus rs = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        return rs != null ? rs.code() : null;
    }

    /** Detects Spring Security exceptions without a compile-time dependency on Spring Security. */
    private static boolean isSecurityException(Exception ex, String simpleName) {
        for (Class<?> c = ex.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().startsWith("org.springframework.security.") && c.getSimpleName().equals(simpleName)) {
                return true;
            }
        }
        return false;
    }
}
