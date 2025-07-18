package com.leo.fintech.category;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leo.fintech.auth.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public List<CategoryDto> getAllCategories(Principal principal) {
        String userId = SecurityUtils.extractUserId();
        return categoryService.getCategoriesByUser(userId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable("id") Long id, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        Optional<CategoryDto> category = categoryService.getCategoryByIdAndUser(id, userId);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CategoryDto dto, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        CategoryDto created = categoryService.createCategoryForUser(dto, userId);
        return ResponseEntity.ok(created);          
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        boolean deleted = categoryService.deleteCategoryByUser(id, userId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();       
        }   }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody CategoryDto dto, Principal principal) {
        String userId = SecurityUtils.extractUserId();
        Optional<CategoryDto> updated = categoryService.updateCategoryByUser(id, dto, userId);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build()); 
    }
}
