package com.leo.fintech.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.auth.UserRepository;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;

    public CategoryService(
        CategoryRepository categoryRepository,
        CategoryMapper categoryMapper,
        UserRepository userRepository
        ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
        this.userRepository = userRepository;
    }

    public List<CategoryDto> getCategoriesByUser(String userId) {
        UUID uuid = UUID.fromString(userId);
        return categoryRepository.findAllByUserId(uuid).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDto> getCategoryByIdAndUser(Long id, String userId) {
        UUID uuid = UUID.fromString(userId);
        return categoryRepository.findByIdAndUserId(id, uuid)
                .map(categoryMapper::toDto);
    }

    public CategoryDto createCategoryForUser(CategoryDto dto, String userId) {
        UUID uuid = UUID.fromString(userId);
        final com.leo.fintech.auth.User userEntity = userRepository.findById(uuid)
            .orElseThrow(() -> new IllegalStateException("User not found for id: " + userId));
        Category category = categoryMapper.toEntity(dto);
        category.setUser(userEntity);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public Optional<CategoryDto> updateCategoryByUser(Long id, CategoryDto dto, String userId) {
        UUID uuid = UUID.fromString(userId);
        return categoryRepository.findByIdAndUserId(id, uuid).map(category -> {
            category.setName(dto.getName());
            category.setIsIncome(dto.getIsIncome());
            return categoryMapper.toDto(categoryRepository.save(category));
        });
    }

    @Transactional
    public Boolean deleteCategoryByUser(Long id, String userId) {
        UUID uuid = UUID.fromString(userId);
        Optional<Category> category = categoryRepository.findByIdAndUserId(id, uuid);
        if (category.isPresent()) {
            categoryRepository.deleteByIdAndUserId(id, uuid);
            return true;
        }
        return false;
    }
}
