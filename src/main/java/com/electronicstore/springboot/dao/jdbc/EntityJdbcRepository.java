package com.electronicstore.springboot.dao.jdbc;

import com.electronicstore.springboot.dao.Datastore;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

public class EntityJdbcRepository<E> implements Datastore<E, Long>
{
    private static final RowMapper<Integer> intRowMapper = SingleColumnRowMapper.newInstance(Integer.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationContext appContext;


    private final JdbcTableMetadata<E> metadata;
    private final BiFunction<E, Boolean, SqlParameterSource> entityParamSource;
    private final BiConsumer<GeneratedKeyHolder, E> keySetter;
    private final RowMapper<E> entityRowMapper;

    private final String queryByKey;
    private final String queryByMultipleKeys;
    private final String queryExistByKey;
    private final String queryAll;
    private final String insertOrUpdate;
    private final String deleteFromTable;

    public EntityJdbcRepository(JdbcTableMetadata<E> md) {
        metadata = md;
        entityParamSource = md.entityParamSource();
        keySetter = metadata.keySetter();
        entityRowMapper = metadata.rowMapper();

        queryByKey = metadata.queryByKeyColumns();
        queryByMultipleKeys = metadata.queryByMultipleKeyColumns();
        queryExistByKey = metadata.queryRowExistByKey();
        queryAll = metadata.queryAll();
        insertOrUpdate = metadata.insertOrUpdate();
        deleteFromTable = metadata.deleteRowByKey();

    }

    //Datastore API
    @Override
    public boolean contains(Long id) {
        try (Stream<Integer> result = jdbcTemplate.queryForStream(queryExistByKey, metadata.keyParamSource(id), intRowMapper)) {
            return result.findAny().isPresent();
        }
    }

    @Override
    public Optional<E> find(Long id) {
        try (Stream<E> resultStream = jdbcTemplate.queryForStream(queryByKey, metadata.keyParamSource(id), entityRowMapper)) {
            return resultStream.findFirst();
        }
    }

    @Override
    public List<E> find(Collection<Long> ids) {
        try (Stream<E> resultStream = jdbcTemplate.queryForStream(queryByMultipleKeys, metadata.keyParamSource(ids), entityRowMapper)) {
            return resultStream.toList();
        }
    }

    @Override
    public List<E> findMatching(E entity) {
        String queryByFields = metadata.queryByNonNullFields(entity);
        SqlParameterSource paramSource = entityParamSource.apply(entity, true);
        try (Stream<E> resultStream = jdbcTemplate.queryForStream(queryByFields, paramSource, entityRowMapper)){
            return resultStream.toList();
        }
    }

    @Override
    public List<E> findMatchingValuesIn(String fieldName, Collection values) {
        return findMatchingValuesIn(Collections.singletonMap(fieldName, values));
    }

    @Override
    public List<E> findMatchingValuesIn(Map<String, Collection> criteria) {
        if (!metadata.containsAllColumns(criteria.keySet())) {
            throw new IllegalArgumentException("Cannot find one of columns "+criteria.keySet()+" in "+List.of(metadata.allColumnNames()));
        }
        String queryValuesIn = metadata.queryByValuesIn(criteria.keySet());
        SqlParameterSource paramSource = new MapSqlParameterSource(criteria);
        try (Stream<E> resultStream = jdbcTemplate.queryForStream(queryValuesIn, paramSource, entityRowMapper)) {
            return resultStream.toList();
        }
    }

    @Override
    public List<E> findAll() {
        return jdbcTemplate.query(queryAll, entityRowMapper);
    }

    @Override
    public Optional<E> persist(E entity) {
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            int rowsUpdated = jdbcTemplate.update(insertOrUpdate, entityParamSource.apply(entity, false), keyHolder, metadata.keyColumnNames());
            if (rowsUpdated == 0) {
                return Optional.empty();
            } else {
                Long key = keyHolder.getKeyAs(Long.class);
                keySetter.accept(keyHolder, entity);
                return find(key);
            }
        } catch (DataAccessException dae) {
            dae.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Status persistWithStatus(E entity) {
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            int rowsUpdated = jdbcTemplate.update(insertOrUpdate, entityParamSource.apply(entity, false), keyHolder, metadata.keyColumnNames());
            if (rowsUpdated == 0) {
                return Status.Error;
            } else {
                keySetter.accept(keyHolder, entity);
                return Status.Success;
            }
        } catch (DataAccessException dae) {
            dae.printStackTrace();
            return resolveStatus(dae);
        }
    }

    @Override
    public List<E> persist(Collection<E> entities) {
        return entities.stream().map(this::persist).filter(Optional::isPresent).map(Optional::get).toList();
    }

    @Override
    public Map<Status, List<E>> persistWithStatus(Collection<E> entities) {
        Map<Status, List<E>> result = new HashMap<>();
        for (E entity : entities) {
            Status status = persistWithStatus(entity);
            result.computeIfAbsent(status, s->new LinkedList<>()).add(entity);
        }
        return result;
    }

    @Override
    public Optional<E> remove(Long id) {
        try {
            Optional<E> entity = find(id);
            if (entity.isPresent()) {
                jdbcTemplate.update(deleteFromTable, metadata.keyParamSource(id));
            }
            return entity;
        } catch (DataAccessException dae) {
            dae.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Status removeWithStatus(Long id) {
        try {
            if (contains(id)) {
                jdbcTemplate.update(deleteFromTable, metadata.keyParamSource(id));
            }
            return Status.Success;
        } catch (DataAccessException dae) {
            dae.printStackTrace();
            return resolveStatus(dae);
        }
    }

    @Override
    public List<E> remove(Collection<Long> ids) {
        try {
            List<E> entities = find(ids);
            SqlParameterSource[] params = entities.stream().map(e -> entityParamSource.apply(e, false)).toArray(SqlParameterSource[]::new);
            int[] result = jdbcTemplate.batchUpdate(deleteFromTable, params);
            List<E> removedList = new LinkedList<>();
            for (int i = 0; i < result.length; i++) {
                if (result[i] == 1) {
                    removedList.add(entities.get(i));
                }
            }
            return removedList;
        } catch (DataAccessException dae) {
            dae.printStackTrace();
            return emptyList();
        }
    }

    @Override
    public Map<Status, List<E>> removeWithStatus(Collection<Long> ids) {
        Map<Status, List<E>> removed = new HashMap<>();
        removed.put(Status.Success, new LinkedList<>());
        removed.put(Status.Error, new LinkedList<>());
        try {
            List<E> entities = find(ids);
            SqlParameterSource[] params = entities.stream().map(e -> entityParamSource.apply(e, false)).toArray(SqlParameterSource[]::new);
            int[] result = jdbcTemplate.batchUpdate(deleteFromTable, params);
            for (int i = 0; i < result.length; i++) {
                E entity = entities.get(i);
                if (result[i] == 1) {
                    removed.get(Status.Success).add(entity);
                } else {
                    removed.get(Status.Error).add(entity);
                }
            }
        } catch (DataAccessException dae) {
            dae.printStackTrace();
        }
        return removed;
    }

    private Status resolveStatus(DataAccessException dae) {
        return Status.Error;
    }

}
