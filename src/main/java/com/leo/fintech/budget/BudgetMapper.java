package com.leo.fintech.budget;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {
    
    @Mapping(source = "category.id", target = "categoryId")
    BudgetDto toDto(Budget budget);
    
    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(target = "user", ignore = true)
    Budget toEntity(BudgetDto dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(BudgetDto dto, @MappingTarget Budget budget); 
}