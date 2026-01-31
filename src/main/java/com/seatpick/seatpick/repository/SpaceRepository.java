package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpaceRepository extends JpaRepository<Space, Long> {
}