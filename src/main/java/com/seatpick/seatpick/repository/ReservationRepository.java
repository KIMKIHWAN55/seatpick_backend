package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 특정 날짜, 특정 공간의 예약 내역 모두 가져오기
    List<Reservation> findBySpaceIdAndDate(Long spaceId, LocalDate date);
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsBySpaceId(Long spaceId);
    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.space.id = :spaceId")
    void deleteBySpaceId(@Param("spaceId") Long spaceId);
    @Query("SELECT COUNT(r) > 0 FROM Reservation r " +
            "WHERE r.space.id = :spaceId " +
            "AND r.status != 'CANCELLED' " +
            "AND (r.date > :today OR (r.date = :today AND r.endTime > :nowTime))")
    boolean existsFutureReservations(@Param("spaceId") Long spaceId,
                                     @Param("today") LocalDate today,
                                     @Param("nowTime") LocalTime nowTime);

}