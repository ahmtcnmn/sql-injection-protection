package com.ahmtcnmn.agent.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ahmtcnmn.agent.Libs.ColorLogger;
import com.ahmtcnmn.agent.model.PendingEvent;
import com.ahmtcnmn.agent.repository.PendingEventRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class QueueService {

    private final PendingEventRepository pendingEventRepository;
    private final MyRestClient myRestClient;
    private final ObjectMapper objectMapper;

    public void savePendingRequest(String uri, Object payload) {
        try {
            PendingEvent pendingEvent = new PendingEvent();
            pendingEvent.setUri(uri);
            pendingEvent.setPayloadJson(objectMapper.writeValueAsString(payload));  // nesneyi JSON metnine çevir
            pendingEvent.setCreatedAt(Instant.now());
            pendingEventRepository.save(pendingEvent);
            ColorLogger.info("Pending request saved: " + uri);
        } catch (Exception e) {
            ColorLogger.warn("Kuyruğa kaydedilemedi: {}"+ e.getMessage());
        }
    }

    @Transactional
    public void processPendingEvents() {
        List<PendingEvent> pendingEvents = pendingEventRepository.findAll();
        if (pendingEvents.isEmpty()) return;

        ColorLogger.info("Processing " + pendingEvents.size() + " pending events...");

        for (PendingEvent pendingEvent : pendingEvents) {
            try {
                myRestClient.postRawJson(pendingEvent.getUri(), pendingEvent.getPayloadJson());
                pendingEventRepository.delete(pendingEvent);
                ColorLogger.success("Kuyruktan gönderildi: " + pendingEvent.getUri());
            } catch (Exception e) {
                ColorLogger.warn("Kuyruktaki event hâlâ gönderilemiyor: " + e.getMessage());
            }
        }
    }
}