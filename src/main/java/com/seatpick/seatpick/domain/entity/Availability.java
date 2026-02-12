package com.seatpick.seatpick.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "availability")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "space_id")
    private Space space;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek; // MONDAY, TUESDAY...

    private LocalTime startTime; // 09:00

    private LocalTime endTime;   // 22:00

    public Availability(Space space, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.space = space;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // 특정 시간이 운영 종료 시간 이전인지 확인하는 로직
    public boolean isBeforeEndTime(LocalTime time) {
        return time.isBefore(this.endTime);
    }

    // 특정 날짜와 시간이 현재보다 과거인지 판단하는 로직
    public boolean isPast(LocalDate date, LocalTime time, LocalDateTime now) {
        LocalDateTime targetDateTime = LocalDateTime.of(date, time);
        return targetDateTime.isBefore(now);
    }
}