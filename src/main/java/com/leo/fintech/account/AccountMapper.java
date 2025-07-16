package com.leo.fintech.account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "createdAt", target = "createdAt")
    AccountDto toDto(Account account);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "createdAt", target = "createdAt")
    Account toEntity(AccountDto dto);
}
