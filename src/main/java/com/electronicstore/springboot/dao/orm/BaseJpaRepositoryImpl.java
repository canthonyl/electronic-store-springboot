package com.electronicstore.springboot.dao.orm;

import com.electronicstore.springboot.dao.Datastore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.FluentQuery;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NoRepositoryBean
public class BaseJpaRepositoryImpl<E, ID> extends SimpleJpaRepository<E, ID> implements Datastore<E, ID> {

    protected EntityManager manager;

    public BaseJpaRepositoryImpl(JpaEntityInformation entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        manager = entityManager;
    }

    //Datastore Api
    @Override
    public boolean contains(ID id) {
        return existsById(id);
    }

    @Override
    public Optional<E> find(ID id) {
        return super.findById(id);
    }

    @Override
    public List<E> find(Collection<ID> ids) {
        return findAllById(ids);
    }

    @Override
    public List<E> findMatching(E entity) {
        Example<E> example = Example.of(entity);
        Function<FluentQuery.FetchableFluentQuery<E>, List<E>> query = f -> f.project("id").all();
        return findBy(example, query);
    }

    @Override
    public List<E> findMatchingValuesIn(String field, Collection values) {
        return Collections.emptyList();
    }

    @Override
    public List<E> findMatchingValuesIn(Map<String, Collection> criteria) {
        return Collections.emptyList();
    }

    /*@Override
    public int countMatching(E entity) {
        Example<E> example = Example.of(entity);
        Function<FluentQuery.FetchableFluentQuery<E>, Long> query = f -> f.project("id").count();
        return findBy(example, query).intValue();
    }*/

    @Override
    public Optional<E> persist(E entity) {
        try {
            return Optional.of(save(entity));
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public List<E> persist(Collection<E> entities) {
        return entities.stream()
                .map(this::persist)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Status persistWithStatus(E entity) {
        try {
            save(entity);
            return Status.Success;
        } catch (PersistenceException pe) {
            pe.printStackTrace();
            return Status.Error;
        }
    }

    @Override
    public Map<Status, List<E>> persistWithStatus(Collection<E> entities) {
        Map<Status, List<E>> map = new HashMap<>();
        map.put(Status.Success, new LinkedList<>());
        map.put(Status.Error, new LinkedList<>());

        for (E entity : entities) {
            Optional<E> result = persist(entity);
            if (result.isPresent()) {
                map.get(Status.Success).add(result.get());
            } else {
                map.get(Status.Error).add(entity);
            }
        }
        return map;
    }

    @Override
    public Optional<E> remove(ID id) {
        Optional<E> entity = findById(id);
        entity.ifPresent(this::delete);
        return entity;
    }

    @Override
    public List<E> remove(Collection<ID> ids) {
        try {
            List<E> exists = findAllById(ids);
            deleteAllInBatch(exists);
            return exists;
        } catch (PersistenceException pe) {
            return Collections.emptyList();
        }
    }

    @Override
    public Status removeWithStatus(ID id) {
        if (contains(id)) {
            deleteById(id);
            return Status.Success;
        } else {
            return Status.Error;
        }
    }


    @Override
    public Map<Status, List<E>> removeWithStatus(Collection<ID> ids) {
        Map<Status, List<E>> map = new HashMap<>();
        map.put(Status.Success, new LinkedList<>());
        map.put(Status.Error, new LinkedList<>());

        for (ID id : ids) {
            Optional<E> result = remove(id);
            if (result.isPresent()) {
                map.get(Status.Success).add(result.get());
            } else {
                find(id).ifPresent(e -> map.get(Status.Error).add(e));
            }
        }
        return map;
    }
}
