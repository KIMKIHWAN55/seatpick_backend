package com.seatpick.seatpick.domain.type;

public enum ReservationStatus {
    PENDING,    // 결제 대기 (선점 중)
    CONFIRMED,  // 예약 확정
    CANCELLED,  // 취소됨
    USED,       // 이용 완료
    NOSHOW      // 노쇼
}
