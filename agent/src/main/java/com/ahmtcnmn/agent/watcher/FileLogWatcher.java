package com.ahmtcnmn.agent.watcher;

import java.io.File;
import java.time.Duration;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import com.ahmtcnmn.agent.Libs.ColorLogger;
import com.ahmtcnmn.agent.client.HeartbeatTask;
import com.ahmtcnmn.agent.detector.Interface.Detector;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


public class FileLogWatcher extends TailerListenerAdapter {
    
    private final Detector detector;
    private final HeartbeatTask heartbeatTask;   // event göndermek için gerçek sahibi bu
    private final String filePath;

    private Tailer tailer;

    public FileLogWatcher(String filePath, Detector detector, HeartbeatTask heartbeatTask) {
        this.filePath = filePath;
        this.detector = detector;
        this.heartbeatTask = heartbeatTask;
    }

    @PostConstruct
    public void start() {
        // Tailer'ı burada başlat (yukarıdaki gibi)
        File file = new File(filePath);
        this.tailer = Tailer.builder()
                .setFile(file)
                .setTailerListener(this)          
                .setDelayDuration(Duration.ofMillis(1000))
                .setTailFromEnd(true)
                .get();
        ColorLogger.info("Log izleme başlatıldı: " + filePath);
    }
    
    @Override
    public void handle(String line) {
        ColorLogger.info("[CANLI LOG] " + line);
        if (detector.isSuspicious(line)) {
            ColorLogger.error("[SUSPİÇİOUS] Potansiyel " + detector.eventType() + " attempt detected: " + line);
            boolean isEventSent = heartbeatTask.sendDetectedEvent(line, detector.eventType());
            if(isEventSent) {
                ColorLogger.warn("[SİSTEM] LogTailer: " + detector.eventType() + " event manager'a gönderildi.");
            } else {
                
                ColorLogger.error("[SİSTEM] LogTailer: " + detector.eventType() + " event manager'a gönderilemedi.");
            }
        }
    } 

    @PreDestroy
    private void stopLogWatcher() {
        if (tailer != null) {
            tailer.close();            // ← uygulama/restart kapanırken temizle
            ColorLogger.info("Log izleme durduruldu.");
        }
    }
    
}
