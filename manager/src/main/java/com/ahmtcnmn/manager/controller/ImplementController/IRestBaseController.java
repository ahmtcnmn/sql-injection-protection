package com.ahmtcnmn.manager.controller.ImplementController;

import org.springframework.web.bind.annotation.RequestBody;

import com.ahmtcnmn.manager.common.exceptionController.RootEntity;
import com.ahmtcnmn.manager.dto.Event.DtoEventRequest;

import jakarta.servlet.http.HttpServletRequest;

public interface IRestBaseController {
    public RootEntity<String> createEvent(@RequestBody DtoEventRequest eventRequest,HttpServletRequest request);
}
