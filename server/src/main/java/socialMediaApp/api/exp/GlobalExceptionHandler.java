package socialMediaApp.api.exp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException ex, HttpServletRequest req) {
        return base(HttpStatus.NOT_FOUND, ex.getMessage(), req, "NOT_FOUND");
    }

    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public ApiError handleAlreadyExists(AlreadyExistsException ex, HttpServletRequest req) {

        ApiError err = base(HttpStatus.CONFLICT, ex.getMessage(), req, "ALREADY_EXISTS");

        if ("EMAIL_ALREADY_EXISTS".equals(ex.getMessage())) {
            err.setCode("EMAIL_EXISTS");
            err.setMessage("This email is already registered");
            err.setFieldErrors(Map.of("email", "Email already exists"));
        }

        return err;
    }


    @ExceptionHandler(ForbiddenOperationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleForbidden(ForbiddenOperationException ex, HttpServletRequest req) {
        return base(HttpStatus.FORBIDDEN, ex.getMessage(), req, "FORBIDDEN");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));
        ApiError err = base(HttpStatus.BAD_REQUEST, "Validation failed", req, "VALIDATION_ERROR");
        err.setFieldErrors(fields);
        return err;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        Map<String, String> fields = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(v -> v.getPropertyPath().toString(), v -> v.getMessage(), (a, b) -> a));
        ApiError err = base(HttpStatus.BAD_REQUEST, "Validation failed", req, "VALIDATION_ERROR");
        err.setFieldErrors(fields);
        return err;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String msg = "Parameter '" + ex.getName() + "' has invalid value '" + ex.getValue() + "'";
        return base(HttpStatus.BAD_REQUEST, msg, req, "TYPE_MISMATCH");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String msg = "Missing required parameter '" + ex.getParameterName() + "'";
        return base(HttpStatus.BAD_REQUEST, msg, req, "MISSING_PARAMETER");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return base(HttpStatus.BAD_REQUEST, "Malformed JSON request", req, "MALFORMED_JSON");
    }

    @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAuth(Exception ex, HttpServletRequest req) {
        return base(HttpStatus.UNAUTHORIZED, "Invalid credentials", req, "AUTH_FAILED");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return base(HttpStatus.FORBIDDEN, "Access denied", req, "ACCESS_DENIED");
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return base(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, "UNEXPECTED");
    }

    private ApiError base(HttpStatus s, String msg, HttpServletRequest req, String code) {
        return ApiError.builder()
                .timestamp(Instant.now())
                .status(s.value())
                .error(s.getReasonPhrase())
                .message(msg)
                .path(req.getRequestURI())
                .code(code)
                .build();
    }
}
