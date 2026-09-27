package com.ahmtcnmn.agent.client;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ahmtcnmn.agent.Dto.responder.DtoEventRequest;
import com.ahmtcnmn.agent.Libs.ColorLogger;
import com.ahmtcnmn.agent.service.ExtractSourceIp;
import com.ahmtcnmn.agent.service.MyRestClient;
import com.ahmtcnmn.agent.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class HeartbeatTask {

    private final MyRestClient myRestClient;
    private final RestClient restClient;
    private final QueueService queueService;
    private final ExtractSourceIp extractSourceIp;

    @Value("${manager.api-key}")
    private String apiKey;

    @Value("${manager.post-url}")
    private String managerPostUrl;

    @Value("${manager.event-url-sql}")
    private String managerEventSqlUrl;

    
    @Scheduled(fixedRate = 30000)          // her 30 saniyede bir
    public void sendHeartbeat() {
        try {
            restClient.post()
                .uri(managerPostUrl)
                .header("X-API-KEY", apiKey)
                .retrieve()
                .toBodilessEntity();
            queueService.processPendingEvents();   
            ColorLogger.success("Heartbeat gönderildi.");
        } catch (Exception e) {
            // Manager erişilemez — logla, çökme (Faz 9'da offline kuyruk)
            log.warn("Heartbeat gönderilemedi: {}", e.getMessage());
        }
    }

    public boolean sendDetectedEvent(String rawData, String eventType) {
        try {
            myRestClient.postRestClient(managerEventSqlUrl, DtoEventRequest.builder()
                .eventType(eventType)
                .severity(5)
                .rawData(rawData)
                .sourceIp(extractSourceIp.extractIp(rawData))
                .build());
            ColorLogger.success("HeartbeatTask: " + eventType + " event gönderildi.");
            return true;
        } catch (Exception e) {
            queueService.savePendingRequest(managerEventSqlUrl,
                DtoEventRequest.builder().eventType(eventType).severity(5).rawData(rawData).build());
                return false;
        }
    }

    

    
}