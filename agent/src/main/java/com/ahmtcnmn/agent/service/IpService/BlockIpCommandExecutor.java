package com.ahmtcnmn.agent.service.IpService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ahmtcnmn.agent.Dto.responder.DtoEventRequest;
import com.ahmtcnmn.agent.detector.Interface.CommandExecutor;
import com.ahmtcnmn.agent.service.MyRestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockIpCommandExecutor implements CommandExecutor {

    private final IpBlockManager ipBlockManager;
    private final MyRestClient myRestClient;

    @Value("${manager.event-url-sql}") 
    private String managerEventUrl;


    @Override
    public boolean supports(String commandType) {
        return "BLOCK_IP".equalsIgnoreCase(commandType);
    }

    @Override
    public String execute(String payload) throws Exception {
        String ip = payload;
        // ProcessBuilder pb = new ProcessBuilder("sudo", "ufw", "deny", "from", ip);
        ProcessBuilder pb = new ProcessBuilder("true");
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            ipBlockManager.recordBlock(ip);
            log.warn("IP engellendi: {}", ip);
            notifyBlocked(ip);
            return "IP engellendi: " + ip;
        } else {
            throw new RuntimeException("ufw komutu başarısız, exit code: " + exitCode);
        }
    }
    private void notifyBlocked(String ip) {
        try {
            myRestClient.postRestClient(managerEventUrl, DtoEventRequest.builder()
                .eventType("IP_BLOCKED")
                .severity(3)
                .rawData("Otomatik olarak engellendi: " + ip)
                .sourceIp(ip)
                .build());
        } catch (Exception e) {
            log.warn("IP_BLOCKED event gönderilemedi: {}", e.getMessage());
            // 📝 isterseniz queueService.savePendingRequest(...) ile kuyruğa da alabilirsiniz
        }
    }
    
}
