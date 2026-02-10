package com.seatpick.seatpick.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {
    private Long spaceId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime; // 이번엔 1시간 단위라 필수임
}