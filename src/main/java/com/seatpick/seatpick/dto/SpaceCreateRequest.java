package com.seatpick.seatpick.dto;

import com.seatpick.seatpick.domain.type.SpaceType;
import lombok.Data;
import java.util.Map;

@Data
public class SpaceCreateRequest {
    private String name;        // 공간 이름
    private String location;    // 위치
    private SpaceType type;     // 타입 (STUDIO, MEETING_ROOM 등)
    private Map<String, Object> options; // 옵션 (와이파이, 프로젝터 등)
}