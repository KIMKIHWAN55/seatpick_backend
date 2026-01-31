package com.seatpick.seatpick.domain.entity;

import com.seatpick.seatpick.domain.type.SpaceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "space")
public class Space {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    @Enumerated(EnumType.STRING)
    private SpaceType type;

    // PostgreSQL JSONB 매핑 (공간별 옵션 유연하게 저장)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> options = new HashMap<>();

    public Space(String name, String location, SpaceType type, Map<String, Object> options) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.options = options;
    }
}