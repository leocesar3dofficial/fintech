package com.leo.fintech.category;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.leo.fintech.auth.JwtUserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public PagedModel<EntityModel<CategoryDto>> getAllCategories(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "sort", defaultValue = "name,asc") String sort,
            @RequestParam(name = "filter", defaultValue = "") String filter,
            PagedResourcesAssembler<CategoryDto> assembler, @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {

        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Page<CategoryDto> resultPage = categoryService.getUserCategories(page, sort, filter, userId);

        return assembler.toModel(resultPage, category -> EntityModel.of(category,
                WebMvcLinkBuilder.linkTo(
                        WebMvcLinkBuilder.methodOn(CategoryController.class).getAllCategories(page, sort, filter,
                                assembler, userPrincipal))
                        .withSelfRel()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<CategoryDto> category = categoryService.getUserCategoryById(id, userId);

        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(
            @Valid @RequestBody CategoryDto dto,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        CategoryDto created = categoryService.createUserCategory(dto, userId);
        return ResponseEntity.ok(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long id,
            @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        boolean deleted = categoryService.deleteCategory(id, userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable("id") Long id,
            @Valid @RequestBody CategoryDto dto, @AuthenticationPrincipal JwtUserPrincipal userPrincipal) {
        UUID userId = UUID.fromString(userPrincipal.getUserId());
        Optional<CategoryDto> updated = categoryService.updateCategory(id, dto, userId);

        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
