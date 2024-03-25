package skhu.jijijig.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    public EmailAlreadyExistsException(String email) {
        super("이메일이 이미 사용 중입니다: " + email);
    }
}