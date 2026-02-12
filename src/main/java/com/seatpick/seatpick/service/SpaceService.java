package com.seatpick.seatpick.service;

import com.seatpick.seatpick.domain.entity.Availability;
import com.seatpick.seatpick.domain.entity.Reservation;
import com.seatpick.seatpick.domain.entity.Space;
import com.seatpick.seatpick.domain.entity.User; // 👈 User 엔티티 import
import com.seatpick.seatpick.domain.type.ReservationStatus;
import com.seatpick.seatpick.dto.SlotDto;
import com.seatpick.seatpick.dto.SpaceCreateRequest;
import com.seatpick.seatpick.repository.AvailabilityRepository;
import com.seatpick.seatpick.repository.ReservationRepository;
import com.seatpick.seatpick.repository.SpaceRepository;
import com.seatpick.seatpick.repository.UserRepository; // 👈 UserRepository import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpaceService {

    private final AvailabilityRepository availabilityRepository;
    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final UserRepository userRepository; // 👈 유저 권한 확인을 위해 추가

    // 👇 [수정됨] 1. 공간 생성 (사장님만 가능) - providerId 파라미터 추가
    @Transactional
    public void createSpace(SpaceCreateRequest request, String providerId) {
        // 1. [해결 키포인트] DB에서 주인(User) 정보를 먼저 가져와서 'owner'라는 이름의 변수에 담습니다.
        User owner = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 1. 공간 정보 저장
        Space space = Space.builder()
                .name(request.getName())
                .location(request.getLocation())
                .type(request.getType())
                .options(request.getOptions())
                .owner(owner) // 👈 주인 설정도 잊지 않게 됨
                .build();
        Space savedSpace = spaceRepository.save(space);

        // 2. [필수] 기본 운영 시간 데이터 생성
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
        // 조회는 누구나 가능하므로 권한 체크 안 함
        return spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 공간을 찾을 수 없습니다."));
    }

    // 👇 [수정됨] 2. 공간 삭제 (사장님만 가능) - providerId 파라미터 추가
    @Transactional
    public void deleteSpace(Long id, String providerId) {
        // [권한 체크]
        validateOwner(providerId);

        // 1. 공간이 있는지 확인
        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        LocalDateTime now = LocalDateTime.now();

        boolean hasFutureReservations = reservationRepository.existsFutureReservations(
                id,
                now.toLocalDate(), // 오늘 날짜
                now.toLocalTime()  // 현재 시간
        );

        if (hasFutureReservations) {
            throw new IllegalStateException("아직 진행 중이거나 예정된 예약이 있어 삭제할 수 없습니다!");
        }

        // 3. 예약이 없으면 안전하게 삭제
        reservationRepository.deleteBySpaceId(id);

        spaceRepository.delete(space);
    }

    // 👇 [수정됨] 3. 공간 수정 (사장님만 가능) - providerId 파라미터 추가
    @Transactional
    public void updateSpace(Long id, SpaceCreateRequest request, String providerId) {
        // [권한 체크]
        validateOwner(providerId);

        Space space = spaceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

        // Dirty Checking으로 자동 저장됨
        space.update(
                request.getName(),
                request.getLocation(),
                request.getType(),
                request.getOptions()
        );
    }

    @Transactional(readOnly = true)
    public List<SlotDto> getAvailableSlots(Long spaceId, LocalDate date) {
        // 조회는 누구나 가능하므로 권한 체크 안 함

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

        // 현재 날짜와 시간 구하기 (람다 내부 사용을 위해 final처럼 취급)
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        while (availability.isBeforeEndTime(current)) {
            LocalTime nextTime = current.plusHours(1);

            // 람다 식 내부에서 사용하기 위해 변수 복사 (Effectively Final)
            LocalTime slotStart = current;
            LocalTime slotEnd = nextTime;

            // Reservation 엔티티에게 중복 확인 요청
            boolean isBooked = reservations.stream()
                    .anyMatch(r -> r.isOverlappingWith(date, slotStart, slotEnd));

            // Availability 엔티티에게 과거 여부 확인 요청
            boolean isPast = availability.isPast(date, slotStart, LocalDateTime.now());

            String status = (isBooked || isPast) ? "BOOKED" : "AVAILABLE";
            slots.add(new SlotDto(current, status));

            current = nextTime;
        }
        return slots;
    }

    // 👇 [추가됨] 사장님 권한 확인용 내부 메서드 (코드 중복 제거)
    private void validateOwner(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (!user.isOwner()) { // User 엔티티의 메서드 활용
            throw new IllegalStateException("해당 작업은 사장님(OWNER) 권한이 필요합니다.");
        }
    }
}