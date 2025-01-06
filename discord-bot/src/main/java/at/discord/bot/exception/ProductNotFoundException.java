package at.discord.bot.exception;

public class ProductNotFoundException extends BaseException{
    public ProductNotFoundException(String message, Throwable cause) {
        super(404, ErrorCode.PRODUCT_NOT_FOUND, message, cause);
    }

    public ProductNotFoundException(String message) {
        super(404, ErrorCode.PRODUCT_NOT_FOUND, message);
    }
}
