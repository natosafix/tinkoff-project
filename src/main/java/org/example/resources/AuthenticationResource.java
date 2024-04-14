package org.example.resources;

import org.example.dtos.auth.AuthenticationLoginDto;
import org.example.dtos.auth.AuthenticationRegisterDto;
import org.example.dtos.auth.AuthenticationResponseDto;
import org.example.mapper.AuthenticationMapper;
import lombok.RequiredArgsConstructor;
import org.example.services.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationResource {
    private final AuthenticationService authenticationService;
    private final AuthenticationMapper authenticationMapper;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthenticationResponseDto register(
            @RequestBody AuthenticationRegisterDto authenticationRegisterDto) {
        var user = authenticationMapper.authenticationRegisterDtoToUser(authenticationRegisterDto);

        return new AuthenticationResponseDto(authenticationService.register(user));
    }

    @PostMapping("/authenticate")
    public AuthenticationResponseDto authenticate(
            @RequestBody AuthenticationLoginDto authenticationLoginDto) {
        var user = authenticationMapper.authenticationLoginDtoToUser(authenticationLoginDto);

        return new AuthenticationResponseDto(authenticationService.authenticate(user));
    }
}


