package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.dao.jdbc.EntityJdbcRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityDatastore<E> {

    @Autowired
    Environment environment;

    private Datastore<E, Long> datastore;

    public EntityDatastore(Datastore<E, Long> datastore) {
        this.datastore = datastore;
    }

    public boolean contains(Long id) {
        return datastore.contains(id);
    }

    public Optional<E> find(Long id) {
        return datastore.find(id);
    }

    public Optional<E> persist(E entity) {
        return datastore.persist(entity);
    }

    public Optional<E> remove(Long id){
        return datastore.remove(id);
    }

    public Datastore.Status persistWithStatus(E entity){
        return datastore.persistWithStatus(entity);
    }

    public Datastore.Status removeWithStatus(Long id){
        return datastore.removeWithStatus(id);
    }

    public List<E> find(Collection<Long> ids) {
        return datastore.find(ids);
    }

    public List<E> findMatching(E entity) {
        return datastore.findMatching(entity);
    }

    public List<E> findMatchingValuesIn(String field, Collection values) {
        return datastore.findMatchingValuesIn(field, values);
    }

    public List<E> findMatchingValuesIn(Map<String, Collection> criteria) {
        return datastore.findMatchingValuesIn(criteria);
    }

    public List<E> findAll(){
        return datastore.findAll();
    }

    public List<E> persist(Collection<E> entities){
        return datastore.persist(entities);
    }

    public List<E> remove(Collection<Long> ids){
        return datastore.remove(ids);
    }

    public Map<Datastore.Status, List<E>> persistWithStatus(Collection<E> entities){
        return datastore.persistWithStatus(entities);
    }

    public Map<Datastore.Status, List<E>> removeWithStatus(Collection<Long> entities){
        return datastore.removeWithStatus(entities);
    }


    @PostConstruct
    public void postConstruct(){
        List<String> activeProfiles = Arrays.asList(environment.getActiveProfiles());
        boolean entityRepositoryIsJdbc = datastore.getClass() == EntityJdbcRepository.class;
        boolean activeProfileJdbc = activeProfiles.contains("data.jdbc");

        if (entityRepositoryIsJdbc != activeProfileJdbc) {
            String message = "Unexpected class of "+datastore.getClass().getSimpleName()+" when profile is "+ activeProfiles;
            throw new RuntimeException(message);
        }
    }
}
