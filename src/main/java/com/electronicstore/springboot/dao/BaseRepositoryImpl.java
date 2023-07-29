package com.electronicstore.springboot.dao;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;

public class BaseRepositoryImpl<E, ID> extends SimpleJpaRepository<E, ID> implements BaseRepository<E, ID> {

    private EntityManager manager;

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
