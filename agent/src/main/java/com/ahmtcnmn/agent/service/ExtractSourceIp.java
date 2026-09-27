package com.ahmtcnmn.agent.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;


@Component
public class ExtractSourceIp {
    private static final Pattern IP_PATTERN = Pattern.compile("\\b(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\b");

    public String extractIp(String logLine){
        Matcher matcher = IP_PATTERN.matcher(logLine);
        return matcher.find() ? matcher.group(1) : null;
    }
}
