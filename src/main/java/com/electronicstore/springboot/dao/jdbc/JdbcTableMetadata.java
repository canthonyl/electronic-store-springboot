package com.electronicstore.springboot.dao.jdbc;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JdbcTableMetadata<E> {

    private String tableName;
    private final Map<String, String> keyColumns;
    private final Map<String, String> columns;
    private final Map<String, String> allColumns;
    private final Map<String, Function<E, Object>> getters;
    private final Map<String, BiConsumer> setters;
    private final Map<String, Class> types;
    private Supplier<E> supplierForRowMapper;

    public JdbcTableMetadata(){
        keyColumns = new LinkedHashMap<>();
        columns = new LinkedHashMap<>();
        allColumns = new LinkedHashMap<>();
        getters = new LinkedHashMap<>();
        setters = new LinkedHashMap<>();
        types = new LinkedHashMap<>();
    }

    boolean containsColumn(String columnName) {
        return allColumns.containsKey(columnName);
    }

    boolean containsAllColumns(Collection<String> columnNames) {
        return allColumns.keySet().containsAll(columnNames);
    }

    String queryByKeyColumns() {
        String condition = keyColumns.entrySet().stream()
                .map(e -> equalsParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName).append(" where ");
        sb.append(condition);
        return sb.toString();
    }

    String queryByMultipleKeyColumns(){
        String condition = keyColumns.entrySet().stream()
                .map(e -> inParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName).append(" where ");
        sb.append(condition);
        return sb.toString();
    }

    String queryByNonNullFields(E entity) {
        String condition = allColumns.entrySet().stream()
                .filter(e -> Objects.nonNull(getters.get(e.getKey()).apply(entity)))
                .map(e -> equalsParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));
        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName).append(" where ");
        sb.append(condition);
        return sb.toString();
    }

    String queryByValuesIn(Set<String> fields) {
        String condition = fields.stream()
                .map(colName -> inParam(colName, columns.get(colName)))
                .collect(Collectors.joining(" or "));

        StringBuilder sb = new StringBuilder();
        sb.append("select * from ").append(tableName).append(" where ");
        sb.append(condition);
        return sb.toString();
    }

    String countMatchingNonNullFields(E entity) {
        String condition = allColumns.entrySet().stream()
                .filter(e -> Objects.nonNull(getters.get(e.getKey()).apply(entity)))
                .map(e -> equalsParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));
        StringBuilder sb = new StringBuilder();
        sb.append("select count(*) as record_count from ").append(tableName).append(" where ");
        sb.append(condition);
        return sb.toString();
    }

    String queryAll(){
        return "select * from "+tableName;
    }

    //"merge into product (id, category_id, name, description, price) key (id) values (:id, :category_id, :name, :description, :price)";
    String insertOrUpdate() {
        String insertColumns = allColumns.keySet().stream().collect(Collectors.joining(",", "(", ")"));
        String namedParams = allColumns.values().stream().collect(Collectors.joining(",", "(", ")"));
        String keyColumn = keyColumns.keySet().stream().collect(Collectors.joining(",", "(", ")"));

        return new StringJoiner(" ")
                .add("merge into ").add(tableName).add(insertColumns)
                .add("key").add(keyColumn)
                .add("values").add(namedParams)
                .toString();
    }

    String queryRowExistByKey() {
        String condition = keyColumns.entrySet().stream()
                .map(e -> equalsParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));

        return new StringBuilder()
                .append("select 1 as record_count from ").append(tableName).append(" where ")
                .append(condition)
                .toString();
    }

    String deleteRowByKey(){
        String condition = keyColumns.entrySet().stream()
                .map(e -> equalsParam(e.getKey(), e.getValue()))
                .collect(Collectors.joining(" and "));

        return new StringBuilder()
                .append("delete from ").append(tableName).append(" where ")
                .append(condition)
                .toString();
    }

    SqlParameterSource keyParamSource(Object... values) {
        if (keyColumns.size() != values.length) {
            throw new IllegalArgumentException("# key column mismatch, expected "+keyColumns.size()+" "+keyColumns.keySet()+" actual "+values.length);
        }
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        List<String> fieldNames = keyColumns.keySet().stream().toList();
        for (int i=0; i<values.length; i++) {
            parameterSource.addValue(fieldNames.get(i), values[i]);
        }
        return parameterSource;
    }

    String equalsParam(String colName, String paramName) {
        return colName + " = "+paramName;
    }

    String inParam(String colName, String paramName) {
        return colName + " in ("+paramName+")";
    }

    BiConsumer<GeneratedKeyHolder, E> keySetter() {
        return (gk, e) -> {
            Map<String, Object> keyMap = gk.getKeys();
            keyColumns.forEach((name, namedParam) -> {
                Object val = keyMap.get(name);
                BiConsumer setterRaw = setters.get(name);
                setterRaw.accept(e, val);
            });
        };
    }

    RowMapper<E> rowMapper() {
        return (rs, rownum) -> {
                E instance = supplierForRowMapper.get();
                allColumns.keySet().forEach(name -> {
                    BiConsumer setter = setters.get(name);
                    Object val;
                    try {
                        val = rs.getObject(name);
                        if (val instanceof BigDecimal) {
                            val = ((BigDecimal)val).doubleValue();
                        }
                        Class clazz = types.get(name);
                        if (clazz.isEnum()){
                            setter.accept(instance, Enum.valueOf(clazz, val.toString()));
                        } else {
                            setter.accept(instance, val);
                        }
                    } catch (SQLException sqle) {
                        sqle.printStackTrace();
                    }
                });
                return instance;
            };
    }

    BiFunction<E, Boolean, SqlParameterSource> entityParamSource(){
        return (e,requireNonNull) -> {
          MapSqlParameterSource paramSource = new MapSqlParameterSource();
          getters.forEach((column, beanGetter) -> {
              Object val = beanGetter.apply(e);
              if (!requireNonNull || val != null) {
                  if (val instanceof Enum) {
                      paramSource.addValue(column, val.toString());
                  } else {
                      paramSource.addValue(column, val);
                  }
              }
          });
          return paramSource;
        };
    }


    String[] keyColumnNames() {
        return keyColumns.keySet().toArray(String[]::new);
    }

    String[] allColumnNames() {
        return allColumns.keySet().toArray(String[]::new);
    }

    public static class Builder<E> {
        private JdbcTableMetadata<E> tableMetaData = new JdbcTableMetadata<>();

        public Builder<E> tableName(String name) {
            tableMetaData.tableName = name;
            return this;
        }

        public Builder<E> rowMapperInstance(Supplier<E> supplier) {
            tableMetaData.supplierForRowMapper = supplier;
            return this;
        }

        /*public Builder<E> keyColumn(String colName){
            return keyColumn(colName, colName);
        }*/

        public <F> Builder<E> keyColumn(String colName, BiConsumer<E, F> setter, Function<E, Object> getter, Class type) {
            tableMetaData.keyColumns.put(colName, ":"+colName);
            tableMetaData.getters.put(colName, getter);
            tableMetaData.setters.put(colName, setter);
            tableMetaData.types.put(colName, type);
            return this;
        }

        public <F> Builder<E> column(String colName, BiConsumer<E, F> setter, Function<E, Object> getter, Class type){
            tableMetaData.columns.put(colName, ":"+colName);
            tableMetaData.getters.put(colName, getter);
            tableMetaData.setters.put(colName, setter);
            tableMetaData.types.put(colName, type);
            return this;
        }

        /*public Builder<E> keyColumn(String colName, String preferredParamName){
            tableMetaData.keyColumns.put(colName, ":"+preferredParamName);
            return this;
        }*/

        /*public Builder<E> column(String colName){
            return column(colName, colName);
        }

        public Builder<E> column(String colName, String preferredParamName){
            tableMetaData.columns.put(colName, ":"+preferredParamName);
            return this;
        }*/

        public JdbcTableMetadata<E> build(){
            if (tableMetaData.keyColumns.keySet().size() == 0
                    || tableMetaData.supplierForRowMapper == null
                    || tableMetaData.tableName == null) {
                throw new IllegalArgumentException("Missing argument");
            }
            tableMetaData.allColumns.putAll(tableMetaData.keyColumns);
            tableMetaData.allColumns.putAll(tableMetaData.columns);
            return tableMetaData;
        }
    }
}
