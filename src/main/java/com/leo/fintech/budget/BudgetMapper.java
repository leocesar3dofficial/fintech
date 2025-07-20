package com.leo.fintech.budget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(source = "category.id", target = "categoryId")
    BudgetDto toDto(Budget budget);
    
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "user", ignore = true)
    Budget toEntity(BudgetDto dto);
}