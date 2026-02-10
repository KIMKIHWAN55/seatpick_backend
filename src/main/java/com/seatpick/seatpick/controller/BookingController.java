package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.dto.BookingRequest;
import com.seatpick.seatpick.dto.ReservationResponse;
import com.seatpick.seatpick.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // 👈 추가
import org.springframework.security.core.userdetails.UserDetails; // 👈 추가
import org.springframework.web.bind.annotation.*; // 👈 *로 퉁치거나 각각 import

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // 예약 요청 (진짜 유저 정보 사용)
    @PostMapping
    public ResponseEntity<String> createBooking(
            @RequestBody BookingRequest request,
            @AuthenticationPrincipal UserDetails userDetails // 👈 토큰에서 유저 정보 꺼내기
    ) {
        // userDetails.getUsername()에는 구글 ID(sub)가 들어있음
        // 서비스에서 구글 ID로 유저를 찾아서 예약해야 함
        bookingService.createBooking(request, userDetails.getUsername());
        return ResponseEntity.ok("예약(선점) 성공!");
    }

    // 내 예약 조회
    @GetMapping("/my")
    public ResponseEntity<List<ReservationResponse>> getMyBookings(
            @AuthenticationPrincipal UserDetails userDetails // 👈 토큰에서 유저 정보 꺼내기
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(userDetails.getUsername()));
    }
}