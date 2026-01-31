package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.DayOfWeek;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    // 특정 공간의 특정 요일 운영 시간 조회
    Optional<Availability> findBySpaceIdAndDayOfWeek(Long spaceId, DayOfWeek dayOfWeek);
}