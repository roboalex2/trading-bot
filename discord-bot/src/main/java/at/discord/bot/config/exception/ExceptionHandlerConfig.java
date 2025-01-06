package at.discord.bot.config.exception;

import at.discord.bot.api.model.ErrorResponse;
import at.discord.bot.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class ExceptionHandlerConfig {

    @Order(value = Ordered.LOWEST_PRECEDENCE)
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exception) {
        log.error("", exception);
        return mapException(exception, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    protected ResponseEntity<ErrorResponse> handleOpenAPIException(Exception exception) {
        log.error("", exception);
        return mapException(exception, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {BaseException.class})
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException exception) {
        log.warn("Exception on API Call: ", exception);
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setCode(exception.getErrorCode().toString());
        return ResponseEntity.status(exception.getHttpStatusCode()).body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> mapException(Exception exception, HttpStatus statusCode) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessage(exception.getMessage());
        errorResponse.setCode(statusCode.toString());
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}
