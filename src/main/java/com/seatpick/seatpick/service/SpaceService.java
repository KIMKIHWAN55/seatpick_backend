package com.seatpick.seatpick.service;

import com.seatpick.seatpick.domain.entity.Availability;
import com.seatpick.seatpick.domain.entity.Reservation;
import com.seatpick.seatpick.domain.type.ReservationStatus;
import com.seatpick.seatpick.dto.SlotDto;
import com.seatpick.seatpick.repository.AvailabilityRepository;
import com.seatpick.seatpick.repository.ReservationRepository;
import com.seatpick.seatpick.domain.entity.Space;
import com.seatpick.seatpick.dto.SpaceCreateRequest;
import com.seatpick.seatpick.repository.SpaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final AvailabilityRepository availabilityRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;

    @Transactional
    public void createSpace(SpaceCreateRequest request) {
        Space space = new Space(
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getOptions()
        );
        spaceRepository.save(space);
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlots(Long spaceId, LocalDate date) {
        // 1. 해당 요일의 운영 시간 조회
        Availability availability = availabilityRepository.findBySpaceIdAndDayOfWeek(spaceId, date.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 운영하지 않습니다."));

        // 2. 해당 날짜의 기존 예약 내역 조회 (취소된 건 제외)
        List<Reservation> reservations = reservationRepository.findBySpaceIdAndDate(spaceId, date).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .toList();

        // 3. 1시간 단위로 슬롯 생성 및 상태 계산
        List<SlotDto> slots = new ArrayList<>();
        LocalTime current = availability.getStartTime(); // 시작 시간 (ex: 09:00)

        while (current.isBefore(availability.getEndTime())) { // 종료 시간 전까지 반복
            LocalTime nextTime = current.plusHours(1);

            // 현재 슬롯이 예약되었는지 확인
            boolean isBooked = checkBooked(current, nextTime, reservations);

            slots.add(new SlotDto(current, isBooked ? "BOOKED" : "AVAILABLE"));
            current = nextTime;
        }

        return slots;
    }

    // 예약 충돌 확인 로직
    private boolean checkBooked(LocalTime slotStart, LocalTime slotEnd, List<Reservation> reservations) {
        return reservations.stream().anyMatch(r ->
                // 예약 시작 시간이 슬롯 범위 안에 있거나
                (r.getStartTime().isBefore(slotEnd) && r.getEndTime().isAfter(slotStart))
        );
    }
}