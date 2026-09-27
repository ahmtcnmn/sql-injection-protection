package com.ahmtcnmn.manager.service.ImplementService;

import com.ahmtcnmn.manager.dto.dtoAgent.DtoRegisterRequest;
import com.ahmtcnmn.manager.dto.dtoAgent.DtoRegisterResponse;

public interface IAgentService {
    public DtoRegisterResponse registerAgent(DtoRegisterRequest request);
}
