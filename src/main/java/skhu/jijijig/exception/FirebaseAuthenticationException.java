package skhu.jijijig.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class FirebaseAuthenticationException extends RuntimeException {
    private final HttpStatus status;

    public FirebaseAuthenticationException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}