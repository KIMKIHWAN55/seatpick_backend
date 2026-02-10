package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.dto.SlotDto;
import com.seatpick.seatpick.service.SpaceService;
import com.seatpick.seatpick.dto.SpaceCreateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.seatpick.seatpick.domain.entity.Space;
import com.seatpick.seatpick.repository.SpaceRepository;
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
    private final SpaceRepository spaceRepository;

    // GET /api/spaces/1/slots?date=2026-02-01
    @GetMapping("/{spaceId}/slots")
    public List<SlotDto> getSlots(
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return spaceService.getAvailableSlots(spaceId, date);
    }
    @PostMapping
    public ResponseEntity<String> createSpace(@RequestBody SpaceCreateRequest request) {
        spaceService.createSpace(request);
        return ResponseEntity.ok("공간이 성공적으로 등록되었습니다!");
    }
    @GetMapping
    public List<Space> getAllSpaces() {
        return spaceRepository.findAll();
    }
}