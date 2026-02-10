package com.seatpick.seatpick.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String spaceName;   // "강남 스터디룸"
    private LocalDate date;     // 2026-02-02
    private LocalTime startTime; // 14:00
    private LocalTime endTime;   // 15:00
    private String status;      // PENDING, CONFIRMED
}