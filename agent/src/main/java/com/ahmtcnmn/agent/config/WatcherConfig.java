package com.ahmtcnmn.agent.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ahmtcnmn.agent.client.HeartbeatTask;
import com.ahmtcnmn.agent.detector.DbAuthFailureDetector;
import com.ahmtcnmn.agent.detector.SqliDetector;
import com.ahmtcnmn.agent.detector.SuspiciousSystemActivityDetector;
import com.ahmtcnmn.agent.watcher.FileLogWatcher;
import com.ahmtcnmn.agent.watcher.ProcessLogWatcher;

@Configuration
public class WatcherConfig {
    // Bu sınıf, Spring'in yapılandırma sınıfı olarak işaretlenmiştir.
    // Burada, log izleme ile ilgili yapılandırmalar yapılabilir.
    @Bean
    public FileLogWatcher nginxLogWatcher(
            @Value("${agent.log-file-path}") String filePath,
            SqliDetector sqliDetector,
            HeartbeatTask heartbeatTask) {

        return new FileLogWatcher(filePath, sqliDetector, heartbeatTask);
    }

    @Bean
    public FileLogWatcher dbLogWatcher(
            @Value("${agent.db-log-file-path}") String filePath,
            DbAuthFailureDetector dbAuthFailureDetector,
            HeartbeatTask heartbeatTask) {
        return new FileLogWatcher(filePath, dbAuthFailureDetector, heartbeatTask);
    }

    @Bean
    public ProcessLogWatcher processLogWatcher(
            @Value("${agent.process-command}") String command,
            SuspiciousSystemActivityDetector suspiciousSystemActivityDetector,
            HeartbeatTask heartbeatTask) {
        return new ProcessLogWatcher(List.of(command.split(" ")), suspiciousSystemActivityDetector, heartbeatTask);
    }
}
