package com.ahmtcnmn.agent.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.ahmtcnmn.agent.Libs.ColorLogger;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyRestClient {
    private final RestClient restClient;

    @Value("${manager.api-key}")
    private String apiKey;

    public <T> void postRestClient(String uri, T body) {
        ColorLogger.info("restClient Event gönderme deneniyor...");
        restClient.post()
                .uri(uri)
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON) 
                .body(body)                              
                .retrieve()
                .toBodilessEntity();                     
        ColorLogger.success("restClient Event başarıyla gönderildi.");
    }
    
    public void postRawJson(String uri, String rawJsonBody) {
        ColorLogger.info("Raw JSON event gönderme deneniyor...");
        restClient.post()
            .uri(uri)
            .header("X-API-KEY", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .body(rawJsonBody)
            .retrieve()
            .toBodilessEntity();
        ColorLogger.success("Raw JSON event başarıyla gönderildi.");
    }

}
