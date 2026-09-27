package com.ahmtcnmn.agent.detector;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.ahmtcnmn.agent.detector.Interface.Detector;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SqliDetector implements Detector  {

    private static final List<Pattern> PATTERNS = List.of(
        Pattern.compile("('|\")\\s*(or|and)\\s*('|\")?\\d+('|\")?\\s*=", Pattern.CASE_INSENSITIVE),
        Pattern.compile("union\\s+(all\\s+)?select", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(--|#|/\\*)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b(drop\\s+table|xp_cmdshell|sleep\\s*\\()", Pattern.CASE_INSENSITIVE)
    );

    @Override
    public boolean isSuspicious(String logLine) {
        return PATTERNS.stream().anyMatch(p -> p.matcher(logLine).find());
    }

    @Override
    public String eventType() {
        return "SQL_INJECTION";
    }

    
}