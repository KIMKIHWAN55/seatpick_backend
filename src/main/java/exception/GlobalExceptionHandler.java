package com.seatpick.seatpick.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 의도한 "삭제 금지" 에러 (지금 발생한 게 이놈입니다!)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        // 500(서버 고장) 대신 400(사용자 실수)으로 보내고, 메시지를 그대로 전달합니다.
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 2. 잘못된 인자 에러
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    // 3. DB 제약조건 에러 (혹시 모를 대비)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity.badRequest().body("관련된 데이터(예약 등)가 남아있어 삭제할 수 없습니다.");
    }

    // 4. 나머지 알 수 없는 에러
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        e.printStackTrace(); // 서버 로그에는 남김
        return ResponseEntity.internalServerError().body("알 수 없는 오류가 발생했습니다.");
    }
}