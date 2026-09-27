package com.ahmtcnmn.agent.service.IpService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ahmtcnmn.agent.Libs.ColorLogger;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IpBlockManager {
    private final Map<String, Instant> blockedIps= new ConcurrentHashMap<>();

    public void recordBlock(String ip){
        blockedIps.put(ip, Instant.now());
    }

    @Scheduled(fixedRate = 60000)
    public void unblockExpired(){
        // 1 saat öncesini temsil eden sınır zamanı
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);

        // 📝 1. Adım: Güvenli silme işlemi için Iterator (Gezici) oluşturuyoruz
        Iterator<Map.Entry<String, Instant>> iterator = blockedIps.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Instant> entry = iterator.next();
            String ip = entry.getKey();
            Instant blockedAt = entry.getValue();

            // Eğer engellenme zamanı, 1 saat öncesinden daha eskiyse (threshold'dan önceyse)
            if (blockedAt.isBefore(threshold)) {
                
                // 📝 2. Adım: İşletim sisteminde komutu çalıştır
                try {
                    // ProcessBuilder komutları ve parametreleri boşluklardan bölerek dizi olarak alır
                    ProcessBuilder processBuilder = new ProcessBuilder("sudo", "ufw", "delete", "deny", "from", ip);
                    Process process = processBuilder.start();
                    
                    // İşletim sisteminin komutu bitirmesini bekle ve çıkış kodunu (Exit Code) al
                    int exitCode = process.waitFor();

                    // 📝 3. Adım: Başarılıysa map'ten sil
                    if (exitCode == 0) {
                        iterator.remove(); // Sadece o anki IP'yi Map'ten siler, döngü çökmeksizin devam eder
                        ColorLogger.error("[UFW] Engel başarıyla kaldırıldı ve liste güncellendi: " + ip);
                    } else {
                        ColorLogger.error("[UFW HATA] Engel kaldırılamadı! IP: " + ip + " | Exit Code: " + exitCode);
                    }
                    
                } catch (Exception e) {
                    ColorLogger.error("[SİSTEM HATASI] Komut çalıştırılırken sorun oluştu: " + e.getMessage());
                }
            }
        }
    }
}
