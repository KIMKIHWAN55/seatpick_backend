package com.seatpick.seatpick.controller;

import com.seatpick.seatpick.domain.entity.Space;
import com.seatpick.seatpick.dto.SlotDto;
import com.seatpick.seatpick.dto.SpaceCreateRequest;
import com.seatpick.seatpick.repository.SpaceRepository;
import com.seatpick.seatpick.service.SpaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/spaces")
@RequiredArgsConstructor
public class SpaceController {

    private final SpaceService spaceService;
    private final SpaceRepository spaceRepository;

    // 1. 공간 생성 (사장님만 가능)
    @PostMapping
    public ResponseEntity<String> createSpace(
            @RequestBody SpaceCreateRequest request,
            Authentication authentication // 👈 OAuth2User 대신 Authentication 사용
    ) {
        // authentication.getName()이 providerId(구글ID)를 반환합니다.
        String providerId = authentication.getName();

        spaceService.createSpace(request, providerId);

        return ResponseEntity.ok("공간이 성공적으로 등록되었습니다!");
    }

    // 2. 공간 삭제 (사장님만 가능)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSpace(
            @PathVariable Long id,
            Authentication authentication // 👈 변경
    ) {
        String providerId = authentication.getName();

        spaceService.deleteSpace(id, providerId);

        return ResponseEntity.ok("공간이 삭제되었습니다.");
    }

    // 3. 공간 수정 (사장님만 가능)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateSpace(
            @PathVariable Long id,
            @RequestBody SpaceCreateRequest request,
            Authentication authentication // 👈 변경
    ) {
        String providerId = authentication.getName();

        spaceService.updateSpace(id, request, providerId);

        return ResponseEntity.ok("공간이 수정되었습니다!");
    }

    // 4. 공간 단건 조회 (누구나 가능 - 로그인 불필요)
    @GetMapping("/{id}")
    public Space getSpace(@PathVariable Long id) {
        return spaceService.getSpaceById(id);
    }

    // 5. 예약 가능 슬롯 조회 (누구나 가능)
    @GetMapping("/{spaceId}/slots")
    public List<SlotDto> getSlots(
            @PathVariable Long spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return spaceService.getAvailableSlots(spaceId, date);
    }

    // 6. 전체 공간 조회 (누구나 가능)
    @GetMapping
    public List<Space> getAllSpaces() {
        return spaceRepository.findAll();
    }


@GetMapping("/managed")
public List<Space> getMyManagedSpaces(Authentication authentication) {
    String providerId = authentication.getName();
    return spaceRepository.findAllByOwnerProviderId(providerId);
   }
}