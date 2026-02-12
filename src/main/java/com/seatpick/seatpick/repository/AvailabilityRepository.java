package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Availability;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.DayOfWeek;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    // 특정 공간의 특정 요일 운영 시간 조회
    Optional<Availability> findBySpaceIdAndDayOfWeek(Long spaceId, DayOfWeek dayOfWeek);
    @Modifying
    @Transactional
    @Query("DELETE FROM Availability a WHERE a.space.id = :spaceId")
    void deleteBySpaceId(@Param("spaceId") Long spaceId);
}
