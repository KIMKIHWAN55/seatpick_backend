package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.dto.BookingRequest;
import com.seatpick.seatpick.dto.ReservationResponse;
import com.seatpick.seatpick.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 👈 변경됨 (가장 중요!)
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 1. 예약 요청
    @PostMapping
    public ResponseEntity<String> createBooking(
            @RequestBody BookingRequest request,
            Authentication authentication // 👈 UserDetails 대신 Authentication 사용
    ) {
        // authentication.getName()으로 안전하게 ID 꺼내기
        String providerId = authentication.getName();

        bookingService.createBooking(request, providerId);
        return ResponseEntity.ok("예약(선점) 성공!");
    }

    // 2. 내 예약 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyBookings(
            Authentication authentication // 👈 변경
    ) {
        String providerId = authentication.getName();

        return ResponseEntity.ok(bookingService.getMyBookings(providerId));
    }

    // 3. 예약 취소 요청
    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelBooking(
            @PathVariable Long id,
            Authentication authentication // 👈 변경
    ) {
        String providerId = authentication.getName();

        bookingService.cancelBooking(id, providerId);
        return ResponseEntity.ok("예약이 취소되었습니다.");
    }
}