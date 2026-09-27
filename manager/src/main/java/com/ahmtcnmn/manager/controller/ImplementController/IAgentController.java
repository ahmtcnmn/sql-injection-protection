package com.ahmtcnmn.manager.controller.ImplementController;

import com.ahmtcnmn.manager.common.exceptionController.RootEntity;
import com.ahmtcnmn.manager.dto.dtoAgent.DtoRegisterRequest;
import com.ahmtcnmn.manager.dto.dtoAgent.DtoRegisterResponse;

import jakarta.servlet.http.HttpServletRequest;

public interface IAgentController {

    public RootEntity<DtoRegisterResponse> registerAgent(DtoRegisterRequest request);
    public RootEntity<Void> heartBeat(HttpServletRequest request);

}
