package com.ahmtcnmn.agent.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pending_events")
@Data
public class PendingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String uri;

    @Column(columnDefinition = "TEXT")
    private String payloadJson;   // artık eventType/rawData değil, HAM JSON

    private Instant createdAt;
    // getter/setter'lar
}