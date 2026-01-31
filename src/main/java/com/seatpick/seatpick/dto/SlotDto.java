package com.seatpick.seatpick.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class SlotDto {
    private LocalTime time;   // 14:00
    private String status;    // AVAILABLE, BOOKED
}