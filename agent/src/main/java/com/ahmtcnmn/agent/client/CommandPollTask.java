package com.ahmtcnmn.agent.client;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.ahmtcnmn.agent.Dto.Command.DtoCommand;
import com.ahmtcnmn.agent.Dto.Command.DtoCommandResultRequest;
import com.ahmtcnmn.agent.Libs.ColorLogger;
import com.ahmtcnmn.agent.detector.Interface.CommandExecutor;
import com.ahmtcnmn.agent.service.MyRestClient;
import com.ahmtcnmn.agent.service.QueueService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CommandPollTask {
    
    private final RestClient restClient;
    private final MyRestClient myRestClient;
    private final QueueService queueService;
    private final List<CommandExecutor> commandExecutors;

    @Value("${manager.api-key}")
    private String apiKey;

    @Scheduled(fixedRate=10000)
    public void pollAndExecuteCommands(){
        try{
            DtoCommand[] pendingCommands= restClient.get()
                .uri("/api/commands/pending")
                .header("X-API-KEY",apiKey)
                .retrieve()
                .body(DtoCommand[].class);

            for(DtoCommand command: pendingCommands){
                handleCommand(command);
            }
        }
        catch(Exception e){
            log.warn("Komut sorgulama başarısız: {}", e.getMessage());
        }
    }

    private void handleCommand(DtoCommand command) {
            log.info(">>> Gelen komut tipi: [" + command.commandType() + "]");

        Optional<CommandExecutor> executorOpt = commandExecutors.stream()
            .filter(executor -> executor.supports(command.commandType()))
            .findFirst();
        if(executorOpt.isEmpty()){
            sendResult(command.id(), "FAILED", "Bilinmeyen komut: " + command.commandType());
            return;
        }
        try{
            String result = executorOpt.get().execute(command.payload());
            ColorLogger.info("[SİSTEM] Komut çalıştırıldı: " + command.commandType() + " | Sonuç: " + result);
            sendResult(command.id(), "COMPLETED", result);
        }
        catch(Exception e){
            log.warn("Komut çalıştırma başarısız: {}", e.getMessage());
            sendResult(command.id(), "FAILED", "Komut çalıştırma başarısız: " + e.getMessage());
        }
    }

    private void sendResult(Long commandId, String status, String result) {
        String uri = "/api/commands/" + commandId + "/result";
        try {
            myRestClient.postRestClient(uri, new DtoCommandResultRequest(status, result));
        } catch (Exception e) {
            log.warn("Komut sonucu gönderilemedi: {}", e.getMessage());
            queueService.savePendingRequest(uri, new DtoCommandResultRequest(status, result));
        }
    }
    

}
