package com.electronicstore.springboot.dao;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

//TODO resolve could not initialize proxy - no Session (related?)
public class BaseRepositoryImpl<E, ID> extends SimpleJpaRepository<E, ID> implements BaseRepository<E, ID> {

    protected EntityManager manager;

    public BaseRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        manager = entityManager;
    }

    @Override
    @Transactional
    public void refresh(E e) {
        manager.refresh(e);
    }

}
