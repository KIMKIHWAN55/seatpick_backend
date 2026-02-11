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

import java.time.DayOfWeek; // 👈 이 import가 꼭 필요합니다!
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final AvailabilityRepository availabilityRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;

    // 👇 [수정됨] 공간 생성 시 운영 시간도 같이 만들어주는 로직 추가
    @Transactional
    public void createSpace(SpaceCreateRequest request) {
        // 1. 공간 정보 저장
        Space space = new Space(
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getOptions()
        );
        Space savedSpace = spaceRepository.save(space);

        // 2. [필수] 기본 운영 시간 데이터 생성 (이게 없으면 예약 불가!)
        // 월~일, 09:00 ~ 22:00 로 자동 설정
        for (DayOfWeek day : DayOfWeek.values()) {
            Availability availability = new Availability(
                    savedSpace,
                    day,
                    LocalTime.of(9, 0),
                    LocalTime.of(22, 0)
            );
            availabilityRepository.save(availability);
        }
    }

    @Transactional(readOnly = true)
    public Space getSpaceById(Long id) {
        return spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간을 찾을 수 없습니다."));
    }
    @Transactional
    public void deleteSpace(Long id) {
        // 1. 공간이 있는지 확인
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        // 2. [추가] 예약이 잡혀있는지 확인 (방어 로직)
        if (reservationRepository.existsBySpaceId(id)) {
            throw new IllegalStateException("아직 예약이 남아있는 공간은 삭제할 수 없습니다!");
        }

        // 3. 예약이 없으면 안전하게 삭제
        spaceRepository.delete(space);
    }
    @Transactional
    public void updateSpace(Long id, SpaceCreateRequest request) {
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        // 아까 만든 update 메서드 호출 (Dirty Checking으로 자동 저장됨)
        space.update(
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getOptions()
        );
    }
    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlots(Long spaceId, LocalDate date) {
        // 1. 해당 요일의 운영 시간 조회
        Availability availability = availabilityRepository.findBySpaceIdAndDayOfWeek(spaceId, date.getDayOfWeek())
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜는 운영하지 않습니다."));

        // 2. 해당 날짜의 기존 예약 내역 조회
        List<Reservation> reservations = reservationRepository.findBySpaceIdAndDate(spaceId, date).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .toList();

        // 3. 슬롯 생성
        List<SlotDto> slots = new ArrayList<>();
        LocalTime current = availability.getStartTime();

        // 👇 [추가] 현재 날짜와 시간 구하기
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        while (current.isBefore(availability.getEndTime())) {
            LocalTime nextTime = current.plusHours(1);

            // A. 기존 예약 확인
            boolean isBooked = checkBooked(current, nextTime, reservations);

            // 👇 [추가] B. 과거 시간인지 확인 (날짜가 과거거나, 오늘인데 시간이 지난 경우)
            boolean isPast = date.isBefore(today) || (date.equals(today) && current.isBefore(nowTime));

            // 이미 예약되었거나(BOOKED) 시간이 지났으면(BOOKED 처리해서 클릭 막음)
            // (프론트엔드에서 BOOKED면 버튼이 비활성화되므로 이렇게 처리하면 간단합니다)
            String status = (isBooked || isPast) ? "BOOKED" : "AVAILABLE";

            slots.add(new SlotDto(current, status));
            current = nextTime;
        }

        return slots;
    }

    private boolean checkBooked(LocalTime slotStart, LocalTime slotEnd, List<Reservation> reservations) {
        return reservations.stream().anyMatch(r ->
                (r.getStartTime().isBefore(slotEnd) && r.getEndTime().isAfter(slotStart))
        );
    }
}