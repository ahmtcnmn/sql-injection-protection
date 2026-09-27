package com.ahmtcnmn.agent.detector.Interface;

public interface CommandExecutor {
    boolean supports(String commandType);
    String execute(String command) throws Exception;
}
