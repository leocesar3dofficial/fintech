package com.leo.fintech.category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.leo.fintech.auth.SecurityUtils;
import com.leo.fintech.auth.User;
import com.leo.fintech.auth.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final UserRepository userRepository;

    public List<CategoryDto> getUserCategories() {
        UUID userId = SecurityUtils.extractUserId();

        return categoryRepository.findAllByUserId(userId).stream()
                .map(categoryMapper::toDto)
                .collect(Collectors.toList());
    }

    public Optional<CategoryDto> getUserCategoryById(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        return categoryRepository.findByIdAndUserId(id, userId)
                .map(categoryMapper::toDto);
    }

    public CategoryDto createUserCategory(CategoryDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        final User userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Category category = categoryMapper.toEntity(dto);
        category.setUser(userEntity);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public Optional<CategoryDto> updateCategory(Long id, CategoryDto dto) {
        UUID userId = SecurityUtils.extractUserId();

        return categoryRepository.findByIdAndUserId(id, userId).map(category -> {
            category.setName(dto.getName());
            category.setIsIncome(dto.getIsIncome());
            return categoryMapper.toDto(categoryRepository.save(category));
        });
    }

    @Transactional
    public Boolean deleteCategory(Long id) {
        UUID userId = SecurityUtils.extractUserId();

        Optional<Category> category = categoryRepository.findByIdAndUserId(id, userId);
        if (category.isPresent()) {
            categoryRepository.deleteByIdAndUserId(id, userId);
            return true;
        }

        return false;
    }
}
