package com.seatpick.seatpick.domain.entity;

import com.seatpick.seatpick.domain.type.ReservationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "reservation", indexes = {
        @Index(name = "idx_reservation_date_space", columnList = "space_id, date")
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId; // 추후 User 엔티티 생성 시 연관관계 맺을 예정

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    private LocalDateTime createdAt;

    public Reservation(Long userId, Space space, LocalDate date, LocalTime startTime, LocalTime endTime, ReservationStatus status) {
        this.userId = userId;
        this.space = space;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }


    // 예약 확정 처리 메서드
    public void confirm() {
        this.status = ReservationStatus.CONFIRMED;
    }

    // 1. 상태 변경 전 검증 로직을 포함한 cancel 메서드
    public void cancel() {
        // 근거: 내 데이터(date, startTime)를 내가 직접 검증함
        LocalDateTime startDateTime = LocalDateTime.of(this.date, this.startTime);
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("이미 지난 예약은 취소할 수 없습니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    // 2. 시간 중복 여부를 판단하는 비즈니스 계산 로직
    public boolean isOverlappingWith(LocalDate requestDate, LocalTime requestStart, LocalTime requestEnd) {
        // 날짜가 다르면 겹치지 않음
        if (!this.date.equals(requestDate)) return false;
        // 취소된 예약은 계산에서 제외
        if (this.status == ReservationStatus.CANCELLED) return false;

        // 시간 겹침 공식: (내 시작 < 요청 종료) AND (내 종료 > 요청 시작)
        return this.startTime.isBefore(requestEnd) && this.endTime.isAfter(requestStart);
    }
    public String calculateDisplayStatus(LocalDateTime now) {
        if (this.status == ReservationStatus.CANCELLED) {
            return "CANCELLED";
        }

        LocalDateTime endDateTime = LocalDateTime.of(this.date, this.endTime);
        if (endDateTime.isBefore(now)) {
            return "COMPLETED"; // DB 상태와 별개로 시간이 지났으면 '이용완료'
        }

        return this.status.name();
    }
}