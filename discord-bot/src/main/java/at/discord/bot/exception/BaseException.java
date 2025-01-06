package at.discord.bot.exception;

import lombok.Data;

@Data
public abstract class BaseException extends RuntimeException {

    private final int httpStatusCode;
    private final ErrorCode errorCode;

    public BaseException(int httpStatusCode, ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }

    public BaseException(int httpStatusCode, ErrorCode errorCode, String message) {
        super(message);
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }
}
