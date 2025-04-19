package com.example.assignment.global.exception;


import com.example.assignment.global.dto.response.ErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return switch (e.getMessage()) {
            case "이미 가입된 사용자입니다." -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of("USER_ALREADY_EXISTS", e.getMessage()));
            case "아이디 또는 비밀번호가 올바르지 않습니다." -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("INVALID_CREDENTIALS", e.getMessage()));
            case "관리자 권한이 필요한 요청입니다. 접근 권한이 없습니다." -> ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponse.of("ACCESS_DENIED", e.getMessage()));
            case "사용자를 찾을 수 없습니다." -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of("USER_NOT_FOUND", e.getMessage()));
            default -> ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        };
    }
}