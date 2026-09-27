package com.ahmtcnmn.agent.detector.Command;

import org.springframework.stereotype.Component;

import com.ahmtcnmn.agent.detector.Interface.CommandExecutor;

@Component
public class PingCommandExecutor  implements CommandExecutor{
    
    @Override
    public boolean supports(String commandType) {
        return "PING".equalsIgnoreCase(commandType);
    }    

    @Override
    public String execute(String command){
        return "pong";
    }
}
