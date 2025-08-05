package com.leo.fintech.user;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.leo.fintech.account.Account;
import com.leo.fintech.budget.Budget;
import com.leo.fintech.category.Category;
import com.leo.fintech.goal.Goal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 255)
    @Email
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 255)
    private String role;

    @Column(length = 255)
    private String username;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    @CreationTimestamp(source = SourceType.DB)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    @UpdateTimestamp(source = SourceType.DB)
    private OffsetDateTime updatedAt;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private final List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private final List<Category> categories = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private final List<Budget> budgets = new ArrayList<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(CascadeType.ALL)
    @JsonManagedReference
    private final List<Goal> goals = new ArrayList<>();

    public void addAccount(Account account) {
        accounts.add(account);
        account.setUser(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setUser(null);
    }
}
