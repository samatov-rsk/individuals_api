package org.samatov.individuals_api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.samatov.individuals_api.exception.AuthenticationException;
import org.samatov.individuals_api.exception.UserAlreadyExistsException;
import org.samatov.individuals_api.exception.UserNotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.Map;

@ControllerAdvice
@Order(-2)
@Slf4j
public class GlobalExceptionHandler implements WebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status;
        String errorMessage;

        if (ex instanceof UserNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            errorMessage = ex.getMessage();
        } else if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            errorMessage = ex.getMessage();
        } else if (ex instanceof UserAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
            errorMessage = ex.getMessage();
        } else if (ex instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
            errorMessage = ex.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorMessage = "An unexpected error occurred";
        }

        log.error("Error occurred: {} - {}", status, errorMessage, ex);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorAttributes = Map.of(
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", errorMessage,
                "path", exchange.getRequest().getPath().value()
        );

        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(errorAttributes);
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse()
                            .bufferFactory()
                            .wrap(jsonBytes)));
        } catch (Exception e) {
            log.error("Failed to write error response", e);
            return Mono.error(e);
        }
    }
}
