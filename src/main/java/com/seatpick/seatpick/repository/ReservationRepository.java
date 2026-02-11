package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 날짜, 특정 공간의 예약 내역 모두 가져오기
    List<Reservation> findBySpaceIdAndDate(Long spaceId, LocalDate date);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsBySpaceId(Long spaceId);
}