package com.leo.fintech.transaction;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "category.id", target = "categoryId")
    TransactionDto toDto(Transaction transaction);

    @Mapping(source = "accountId", target = "account.id")
    @Mapping(source = "categoryId", target = "category.id")
    Transaction toEntity(TransactionDto dto);

    @Mapping(source = "accountId", target = "account.id")
    @Mapping(source = "categoryId", target = "category.id")
    void updateEntityFromDto(TransactionDto dto, @MappingTarget Transaction transaction);
}