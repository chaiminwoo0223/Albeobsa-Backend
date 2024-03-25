package skhu.jijijig.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final HttpStatus status = HttpStatus.UNAUTHORIZED;

    public InvalidTokenException(String message) {
        super(message);
    }
}