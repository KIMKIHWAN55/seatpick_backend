package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.dto.SlotDto;
import com.seatpick.seatpick.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;

    // GET /api/spaces/1/slots?date=2026-02-01
    @GetMapping("/{spaceId}/slots")
    public List<SlotDto> getSlots(
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return spaceService.getAvailableSlots(spaceId, date);
    }
}