package com.ahmtcnmn.agent.detector.Interface;

public interface Detector {
    boolean isSuspicious(String logLine);
    String eventType();
}
