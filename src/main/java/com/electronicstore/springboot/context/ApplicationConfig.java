package com.electronicstore.springboot.context;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.DataSourceFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import javax.sql.DataSource;

import java.sql.Driver;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@Configuration
public class ApplicationConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    public DataSource dataSource(){
        DataSourceFactory factory = new DataSourceFactory() {
            DatasourceConnectionProperties properties;

            class DatasourceConnectionProperties implements ConnectionProperties {
                String driverClassName, jdbcUrl, username, password;
                @Override
                public void setDriverClass(Class<? extends Driver> driverClass) { driverClassName = driverClass.getName();}

                @Override
                public void setUrl(String url) { jdbcUrl = url;}

                @Override
                public void setUsername(String name) { username = name;}

                @Override
                public void setPassword(String pwd) { password = pwd; }
            }

            @Override
            public ConnectionProperties getConnectionProperties() {
                properties = new DatasourceConnectionProperties();
                return properties;
            }

            @Override
            public DataSource getDataSource() {
                HikariConfig config = new HikariConfig();
                config.setDriverClassName(properties.driverClassName);
                config.setJdbcUrl(properties.jdbcUrl);
                config.setUsername(properties.username);
                config.setPassword(properties.password);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(10);
                config.setAutoCommit(true);

                HikariDataSource ds = new HikariDataSource(config);

                (((AnnotationConfigServletWebServerApplicationContext)context).getBeanFactory())
                        .registerSingleton("hikariPoolMXBean", ds.getHikariPoolMXBean());
                return ds;
            }
        };


        EmbeddedDatabase database = new EmbeddedDatabaseBuilder()
                .setName("electronicstoredb")
                .setDataSourceFactory(factory)
                .setType(H2)
                .setScriptEncoding("UTF-8")
                .ignoreFailedDrops(true)
                .build();

        return database;
    }


}
