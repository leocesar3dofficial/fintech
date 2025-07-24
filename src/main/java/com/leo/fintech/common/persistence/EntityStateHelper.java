package com.leo.fintech.common.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;

@Component
class EntityStateHelper {

    private static final Logger logger = LoggerFactory.getLogger(EntityStateHelper.class);

    @Autowired
    private EntityManager entityManager;

    public <T> T safeSave(JpaRepository<T, ?> repository, T entity) {
        try {
            if (entityManager.contains(entity)) {
                logger.debug("Entity is already managed, performing merge operation");
                return entityManager.merge(entity);
            } else {
                logger.debug("Entity is not managed, performing save operation");
                return repository.save(entity);
            }
        } catch (Exception e) {
            logger.error("Error during safe save operation", e);
            throw new RuntimeException("Failed to save entity safely", e);
        }
    }

    public <T> boolean isManaged(T entity) {
        return entityManager.contains(entity);
    }

    public <T> void detach(T entity) {
        if (entityManager.contains(entity)) {
            entityManager.detach(entity);
        }
    }

}