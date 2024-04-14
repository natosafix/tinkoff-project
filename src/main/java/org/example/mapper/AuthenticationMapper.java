package org.example.mapper;

import org.example.dtos.auth.AuthenticationLoginDto;
import org.example.dtos.auth.AuthenticationRegisterDto;
import org.example.domain.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationMapper {
    User authenticationLoginDtoToUser(AuthenticationLoginDto authenticationLoginDto);

    User authenticationRegisterDtoToUser(AuthenticationRegisterDto authenticationRegisterDto);
}

