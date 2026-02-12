package com.seatpick.seatpick.repository;

import com.seatpick.seatpick.domain.entity.Space;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpaceRepository extends JpaRepository<Space, Long> {
    List<Space> findAllByOwnerProviderId(String providerId);
}