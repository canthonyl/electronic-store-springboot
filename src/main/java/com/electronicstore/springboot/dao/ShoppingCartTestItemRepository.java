package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.TestItem;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//@Repository
public interface ShoppingCartTestItemRepository
        /*extends ListCrudRepository<TestItem, Long>*/ {

    //@Override
    <S extends TestItem> S save(S item);


}
