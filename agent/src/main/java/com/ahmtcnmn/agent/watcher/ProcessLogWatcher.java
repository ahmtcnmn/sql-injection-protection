package com.ahmtcnmn.agent.watcher;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

import com.ahmtcnmn.agent.client.HeartbeatTask;
import com.ahmtcnmn.agent.detector.Interface.Detector;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;


public class ProcessLogWatcher {

    private final List<String> command;
    private final Detector detector;
    private final HeartbeatTask heartbeatTask;
    private Process process;
    private Thread readerThread;

    public ProcessLogWatcher(List<String> command, Detector detector, HeartbeatTask heartbeatTask) {
        this.command = command;
        this.detector = detector;
        this.heartbeatTask = heartbeatTask;
    }

    @PostConstruct
    public void start() {
        // ProcessBuilder ile process'i başlat
        // readerThread'i başlat — içinde BufferedReader ile satır satır oku
        // her satırda: detector.isSuspicious(...) + heartbeatTask.sendDetectedEvent(...)
        try {
            // Örnek komut: Windows için "ping localhost -t", Mac/Linux için "ping localhost"
            ProcessBuilder builder = new ProcessBuilder(command);
            
            // Hata çıktılarını (stderr) normal çıktılara (stdout) yönlendirip tek kanaldan okuyoruz
            builder.redirectErrorStream(true); 
            
            process = builder.start();
            System.out.println("[SİSTEM] Harici süreç başlatıldı.");

            // 2. Okuma işlemini ayrı bir Thread'e devrediyoruz
            readerThread = new Thread(() -> {
                // Try-with-resources kullanarak BufferedReader'ın işi bitince otomatik kapanmasını sağlıyoruz
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String satir;
                    
                    // readLine() metodu yeni satır gelene kadar bekler.
                    // Eğer Thread'e interrupt sinyali gelirse döngüyü kırıp çıkıyoruz!
                    while (!Thread.currentThread().isInterrupted() && (satir = reader.readLine()) != null) {
                        System.out.println("[HARİCİ PROGRAM]: " + satir);

                        if (detector.isSuspicious(satir)) {
                            heartbeatTask.sendDetectedEvent(satir, detector.eventType());
                        }
                    }
                    
                } catch (Exception e) {
                    System.out.println("[SİSTEM] Okuma sırasında bir kesinti oldu veya süreç kapandı.");
                }
                
                System.out.println("[SİSTEM] Okuyucu Thread görevini tamamladı ve sessizce kapanıyor.");
            });
            readerThread.setDaemon(true);
            // Thread'i başlat
            readerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stop() {
        // process.destroy(), thread'i durdur
        System.out.println("[SİSTEM] Kapanış sinyali alındı. Temizlik yapılıyor...");

        // Önce okuyucu Thread'e "Lütfen işini bırak ve kapan" sinyali gönderiyoruz
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }

        // Sonra harici çalıştırdığımız süreci (Process) sonlandırıyoruz
        if (process != null && process.isAlive()) {
            process.destroy(); // İşletim sistemine programı kapatmasını söyler
            
            // Eğer inat edip kapanmazsa diye zorla kapatma seçeneği de vardır:
            // process.destroyForcibly(); 
        }
        
        System.out.println("[SİSTEM] Tüm harici süreçler ve threadler güvenle kapatıldı.");
    }
}
    
