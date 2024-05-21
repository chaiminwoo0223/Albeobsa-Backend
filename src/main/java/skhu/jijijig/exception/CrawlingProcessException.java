package skhu.jijijig.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class CrawlingProcessException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CrawlingProcessException(String message) {
        super(message);
    }
}