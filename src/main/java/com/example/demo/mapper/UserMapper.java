package com.example.demo.mapper;

import com.example.demo.dto.request.UserDtoRequest;
import com.example.demo.dto.response.UserDtoResponse;
import com.example.demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
     User toEntity(UserDtoRequest userDtoRequest);
     UserDtoResponse toDto(User user);
}
