package com.electronicstore.springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    void refresh(T t);

    //TODO resolve error
    default <S extends T> S saveAndRefresh(S entity) {
        S result = save(entity);
        refresh(result);
        return result;
    }
}
