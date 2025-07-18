package com.leo.fintech.category;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(source = "user.id", target = "userId")
    CategoryDto toDto(Category category);

    @Mapping(source = "userId", target = "user.id")
    Category toEntity(CategoryDto dto);
}
