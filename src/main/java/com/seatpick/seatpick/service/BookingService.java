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

import java.util.List; // 👈 List 오류 해결용 import
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

            boolean isOverlapped = reservationRepository.findBySpaceIdAndDate(request.getSpaceId(), request.getDate())
                    .stream()
                    .anyMatch(r -> r.getStatus() != ReservationStatus.CANCELLED &&
                            (r.getStartTime().isBefore(request.getEndTime()) && r.getEndTime().isAfter(request.getStartTime())));

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

    // 내 예약 조회 (GET) - 🌟 이 부분이 꼭 있어야 합니다!
    @Transactional(readOnly = true)
    public List<ReservationResponse> getMyBookings(String providerId) {
        User user = userRepository.findByProviderId(providerId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(r -> new ReservationResponse(
                        r.getId(),
                        r.getSpace().getName(),
                        r.getDate(),
                        r.getStartTime(),
                        r.getEndTime(),
                        r.getStatus().name()
                ))
                .collect(Collectors.toList());
    }
}