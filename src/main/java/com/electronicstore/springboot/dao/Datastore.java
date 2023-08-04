package com.electronicstore.springboot.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Datastore<E, ID> {

    enum Status { Success, Error, NotFound }

    boolean contains(ID id);

    Optional<E> find(ID id);

    Optional<E> persist(E entity);

    Optional<E> remove(ID id);

    Status persistWithStatus(E entity);

    Status removeWithStatus(ID id);

    List<E> find(Collection<ID> ids) ;

    List<E> findMatching(E entity);

    List<E> findMatchingValuesIn(String field, Collection values);

    List<E> findMatchingValuesIn(Map<String, Collection> criteria);

    List<E> findAll();

    List<E> persist(Collection<E> entities);

    List<E> remove(Collection<ID> ids);

    Map<Status, List<E>> persistWithStatus(Collection<E> entities);

    Map<Status, List<E>> removeWithStatus(Collection<ID> entities);


}
