package api.v2.travel_social_network_server.exceptions;

import api.v2.travel_social_network_server.responses.Response;
import com.cloudinary.api.exceptions.BadRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<?> handleValidationExceptions(Exception ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (ex instanceof MethodArgumentNotValidException validationEx) {
            validationEx.getBindingResult().getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        } else if (ex instanceof ConstraintViolationException constraintViolationEx) {
            constraintViolationEx.getConstraintViolations().forEach(violation -> {
                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errors.put(fieldName, errorMessage);
            });
        }

        return Response.error(request.getRequestURI(),"Validation error", errors);
    }

    @ExceptionHandler(ResourceAlreadyExistedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Response<?> handleResourceAlreadyExisted(ResourceAlreadyExistedException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Resource already existed", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Response<?> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Resource not found", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<?> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Illegal Argument", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<?> handleIllegalState(IllegalArgumentException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Illegal Argument", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Response<?> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Bad credentials", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BadRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Response<?> handleBadRequest(BadRequest ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(), "Bad request", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public Response<?> handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"File size exceeds limit", Map.of("error", "File size exceeds limit."));
    }

    @ExceptionHandler(DisabledException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Response<?> handleDisabledAccount(DisabledException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Your account is not active yet. Please contact support.", Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Response<?> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return Response.error(request.getRequestURI(),"Access denied", Map.of("error", ex.getMessage()));
    }
}
