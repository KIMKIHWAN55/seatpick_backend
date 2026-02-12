package com.seatpick.seatpick.domain.entity;

import com.seatpick.seatpick.domain.type.SpaceType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder; // 👈 추가
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> options = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    // 👇 [핵심 변경] @Builder 추가
    // 이렇게 하면 Service에서 Space.builder().name(..).owner(..).build() 형태로 만들 수 있음
    @Builder
    public Space(String name, String location, SpaceType type, Map<String, Object> options, User owner) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.options = options;
        this.owner = owner;
    }

    public void update(String name, String location, SpaceType type, Map<String, Object> options) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.options = options;
    }
}