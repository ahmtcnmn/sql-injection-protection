package com.ahmtcnmn.manager.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ahmtcnmn.manager.common.RestBaseController;
import com.ahmtcnmn.manager.common.exceptionController.RootEntity;
import com.ahmtcnmn.manager.dto.User.DtoLoginRequest;
import com.ahmtcnmn.manager.dto.User.DtoLoginResponse;
import com.ahmtcnmn.manager.service.User.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends RestBaseController  {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public RootEntity<DtoLoginResponse> login(@RequestBody DtoLoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String token = jwtUtil.generateToken(request.username());
        return ok(new DtoLoginResponse(token));
    }
}
// Uygulamayı başlatın, DataSeeder'ın admin kullanıcı oluşturduğunu konsol log'undan doğrulayın
// POST /api/auth/login ile {"username":"admin","password":"..."} gönderin, bir JWT token dönmeli
// GET /api/agents'ı token olmadan deneyin → artık 401 almalısınız (öncekinden farklı!)
// Aynı isteği Authorization: Bearer <token> header'ıyla deneyin → 200 dönmeli
// Agent'ınızı çalıştırıp heartbeat'in hâlâ sorunsuz çalıştığını doğrulayın (çünkü o path permitAll)