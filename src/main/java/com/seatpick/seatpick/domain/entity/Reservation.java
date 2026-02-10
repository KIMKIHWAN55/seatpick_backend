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

    public void cancel() {
        this.status = ReservationStatus.CANCELLED;
    }
}