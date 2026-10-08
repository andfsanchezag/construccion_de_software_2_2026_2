package application.adapters.rest.exception;

import application.domain.exceptions.CustomerNotFoundException;
import application.domain.exceptions.DomainException;
import application.domain.exceptions.EntityNotFoundException;
import application.domain.exceptions.InvalidCredentialsException;
import application.domain.exceptions.UnauthorizedOperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(
            InvalidCredentialsException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn("Autenticación fallida [requestId={}] {} {} -> {}",
                requestId, request.getMethod(), request.getRequestURI(), ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, requestId);
    }

    @ExceptionHandler({EntityNotFoundException.class, CustomerNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleEntityNotFound(
            RuntimeException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        String entity = ex instanceof EntityNotFoundException ? "EntityNotFoundException"
                : "CustomerNotFoundException";
        log.warn("Entidad no encontrada [requestId={}] {} {} -> {}: {}. Causa probable: el identificador "
                        + "(customer, cuenta, préstamo, transferencia, usuario) enviado en el token/cuerpo no existe "
                        + "en base de datos o pertenece a otro usuario. Verifique las semillas y el login.",
                requestId, request.getMethod(), request.getRequestURI(), entity, ex.getMessage());
        return body(HttpStatus.NOT_FOUND, ex.getMessage(), request, requestId);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(
            UnauthorizedOperationException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn("Operación no autorizada (403) [requestId={}] {} {} -> {}: {}. "
                        + "El usuario autenticado no tiene permiso sobre el recurso solicitado.",
                requestId, request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage());
        return body(HttpStatus.FORBIDDEN, ex.getMessage(), request, requestId);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomain(DomainException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn("Regla de negocio violada (409) [requestId={}] {} {} -> {}: {}. "
                        + "La petición es sintácticamente válida pero el estado del negocio la rechaza.",
                requestId, request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage());
        return body(HttpStatus.CONFLICT, ex.getMessage(), request, requestId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        String requestId = resolveRequestId(request);
        log.warn("Validación de cuerpo fallida [requestId={}] {} {} -> {}",
                requestId, request.getMethod(), request.getRequestURI(), message);
        return body(HttpStatus.BAD_REQUEST, message, request, requestId);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedBody(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn("Cuerpo JSON malformado [requestId={}] {} {} -> {}",
                requestId, request.getMethod(), request.getRequestURI(), ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Malformed JSON request body.", request, requestId);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleMethodValidation(
            HandlerMethodValidationException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.warn("Parámetro inválido [requestId={}] {} {} -> {}",
                requestId, request.getMethod(), request.getRequestURI(), ex.getMessage());
        return body(HttpStatus.BAD_REQUEST, "Invalid request parameter.", request, requestId);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String hint = buildRouteHint(method, uri);
        log.warn("Ruta no encontrada (404 de Spring, NO es entidad de negocio) [requestId={}] {} {} -> {}. {}",
                requestId, method, uri, ex.getMessage(), hint);
        String message = "Resource not found: " + method + " " + uri + ". " + hint;
        return body(HttpStatus.NOT_FOUND, message, request, requestId);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        String requestId = resolveRequestId(request);
        log.error("Error inesperado [requestId={}] {} {} -> {}: {}",
                requestId, request.getMethod(), request.getRequestURI(),
                ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error.", request, requestId);
    }

    /**
     * Pista diciente para el error más común: usar /loan en singular cuando la
     * ruta real es /loans en plural (y equivalentes).
     */
    private String buildRouteHint(String method, String uri) {
        if (uri == null) {
            return "Verifique el path y el verbo HTTP contra los controllers REST.";
        }
        if (uri.endsWith("/natural-customer/loan") && "POST".equalsIgnoreCase(method)) {
            return "Causa probable: usó '/loan' en singular. La ruta correcta es "
                    + "POST /api/v1/natural-customer/loans (plural). Se acepta /loan como alias por compatibilidad.";
        }
        if (uri.contains("/natural-customer/loan/")) {
            return "Causa probable: usó '/loan/{id}' en singular. Use "
                    + "GET /api/v1/natural-customer/loans/{loanId} o POST .../loans/{loanId}/payments.";
        }
        return "Verifique que el path exista (p. ej. /api/v1/natural-customer/loans, "
                + "/transfers, /accounts, /products, /operations) y que el verbo HTTP sea el correcto.";
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        return requestId;
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, HttpServletRequest request, String requestId) {
        if (requestId == null || requestId.isBlank()) {
            requestId = resolveRequestId(request);
        }
        Map<String, Object> payload = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", status.value(),
                "message", message == null ? "" : message,
                "path", request.getRequestURI() == null ? "" : request.getRequestURI(),
                "requestId", requestId);
        return ResponseEntity.status(status).header("X-Request-Id", requestId).body(payload);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message, HttpServletRequest request) {
        return body(status, message, request, resolveRequestId(request));
    }
}
