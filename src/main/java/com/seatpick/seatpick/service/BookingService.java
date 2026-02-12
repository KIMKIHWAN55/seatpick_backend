package com.seatpick.seatpick.service;

import com.seatpick.seatpick.domain.entity.Reservation;
import com.seatpick.seatpick.domain.entity.Space;
import com.seatpick.seatpick.domain.type.ReservationStatus;
import com.seatpick.seatpick.dto.BookingRequest;
import com.seatpick.seatpick.dto.ReservationResponse;
import com.seatpick.seatpick.repository.ReservationRepository;
import com.seatpick.seatpick.repository.SpaceRepository;
import com.seatpick.seatpick.domain.entity.User;
import com.seatpick.seatpick.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;
    private final RedissonClient redissonClient;
    private final UserRepository userRepository;

    // 예약 생성 (POST)
    public void createBooking(BookingRequest request, String providerId) {
        String lockKey = "lock:booking:" + request.getSpaceId() + ":" + request.getDate() + ":" + request.getStartTime();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(2, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new IllegalStateException("다른 사용자가 예약 중입니다. 잠시 후 다시 시도해주세요.");
            }

            User user = userRepository.findByProviderId(providerId)
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

            Space space = spaceRepository.findById(request.getSpaceId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공간입니다."));

            // 👇 [수정] 복잡한 시간 비교 로직을 엔티티의 isOverlappingWith 메서드로 대체
            boolean isOverlapped = reservationRepository.findBySpaceIdAndDate(request.getSpaceId(), request.getDate())
                    .stream()
                    .anyMatch(r -> r.isOverlappingWith(request.getDate(), request.getStartTime(), request.getEndTime()));

            if (isOverlapped) {
                throw new IllegalStateException("이미 예약된 시간입니다!");
            }

            Reservation reservation = new Reservation(
                    user.getId(),
                    space,
                    request.getDate(),
                    request.getStartTime(),
                    request.getEndTime(),
                    ReservationStatus.PENDING
            );
            reservationRepository.save(reservation);
            log.info("예약 성공: {}", lockKey);

        } catch (InterruptedException e) {
            log.error("락 획득 중 에러", e);
            throw new RuntimeException("서버 에러가 발생했습니다.");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // 내 예약 조회 (GET)
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyBookings(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        LocalDateTime now = LocalDateTime.now();

        return reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getSpace().getName(),
                        r.getDate(),
                        r.getStartTime(),
                        r.getEndTime(),
                        r.calculateDisplayStatus(now) // 👇 [수정] 상태 가공 로직을 엔티티 메서드로 대체
                ))
                .collect(Collectors.toList());
    }

    // 예약 취소 요청
    @Transactional
    public void cancelBooking(Long reservationId, String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        // 본인 확인은 여전히 서비스의 몫 (여러 엔티티의 정보를 대조해야 하므로)
        if (!reservation.getUserId().equals(user.getId())) {
            throw new IllegalStateException("본인의 예약만 취소할 수 있습니다.");
        }

        // 👇 [수정] 서비스에서 직접 하던 시간 체크 로직을 삭제하고, 엔티티의 cancel() 메서드에 맡김
        // 엔티티 내부의 cancel()에서 시간이 지났는지 스스로 검증하고 상태를 변경합니다.
        reservation.cancel();
    }
}