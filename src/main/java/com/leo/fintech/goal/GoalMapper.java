package com.leo.fintech.goal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface GoalMapper {

    @Mapping(source = "account.id", target = "accountId")
    GoalDto toDto(Goal goal);

    @Mapping(source = "accountId", target = "account.id")
    @Mapping(target = "user", ignore = true)
    Goal toEntity(GoalDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "account", ignore = true)
    void updateEntityFromDto(GoalDto dto, @MappingTarget Goal goal);
}