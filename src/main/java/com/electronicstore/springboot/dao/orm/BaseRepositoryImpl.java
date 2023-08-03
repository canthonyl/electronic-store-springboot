package com.electronicstore.springboot.dao.orm;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class BaseRepositoryImpl<E, ID> extends SimpleJpaRepository<E, ID>{

    protected EntityManager manager;

    public BaseRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        manager = entityManager;
    }

    @Override
    public void deleteById(ID id) {
        super.deleteById(id);
    }

}
