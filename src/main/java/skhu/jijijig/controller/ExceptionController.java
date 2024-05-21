package skhu.jijijig.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import skhu.jijijig.domain.dto.ErrorResponseDTO;
import skhu.jijijig.exception.CrawlingProcessException;
import skhu.jijijig.exception.ResourceNotFoundException;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException e) {
        log.error("인증 실패", e);
        return buildErrorResponse("인증 실패: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CrawlingProcessException.class)
    public ResponseEntity<ErrorResponseDTO> handleCrawlingProcessException(CrawlingProcessException e) {
        log.error("크롤링 오류", e);
        return buildErrorResponse("크롤링 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException e) {
        log.error("리소스 찾을 수 없음", e);
        return buildErrorResponse("리소스를 찾을 수 없음: " + e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDeniedException(AccessDeniedException e) {
        log.error("접근 권한 없음", e);
        return buildErrorResponse("접근 권한 없음: " + e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDTO> handleRuntimeException(RuntimeException e) {
        log.error("런타임 오류", e);
        return buildErrorResponse("런타임 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleException(Exception e) {
        log.error("서버 오류", e);
        return buildErrorResponse("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(String message, HttpStatus status) {
        ErrorResponseDTO errorResponse = new ErrorResponseDTO(message, status.value());
        return new ResponseEntity<>(errorResponse, status);
    }
}