package com.leo.fintech.category;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    private static final int PAGE_SIZE = 10;

    public Page<CategoryDto> getUserCategories(int page, String sort, String filter) {
        UUID userId = SecurityUtils.extractUserId();

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction sortDirection = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(sortDirection, sortField));

        if (StringUtils.hasText(filter)) {
            return categoryRepository.findAllByUserIdAndNameIgnoreCaseContaining(userId, filter, pageable)
                    .map(categoryMapper::toDto);
        }

        return categoryRepository.findAllByUserId(userId, pageable)
                .map(categoryMapper::toDto);
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
