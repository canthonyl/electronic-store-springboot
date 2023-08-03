package com.electronicstore.springboot.dao.orm;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

public class BaseRepositoryImpl<E, ID> extends SimpleJpaRepository<E, ID>{

    protected EntityManager manager;

    public BaseRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        manager = entityManager;
    }

    @Override
    @Transactional
    public <S extends E> S save(S s) {
        S result = super.save(s);
        return result;
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        super.deleteById(id);
    }

}
