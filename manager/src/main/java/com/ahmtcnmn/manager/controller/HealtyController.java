package com.ahmtcnmn.manager.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ahmtcnmn.manager.common.RestBaseController;
import com.ahmtcnmn.manager.common.exceptionController.RootEntity;
import com.ahmtcnmn.manager.controller.ImplementController.IHealtyController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealtyController extends RestBaseController implements IHealtyController {

    @Override
    @GetMapping("/health")
    public RootEntity<String> getHealtyStatus() {
        return ok("Agent is healthy");
    }
}