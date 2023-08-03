package com.electronicstore.springboot.context;

import com.electronicstore.springboot.dao.ProductDatastore;
import com.electronicstore.springboot.dao.jdbc.ProductDatastoreJdbc;
import com.electronicstore.springboot.dao.orm.BaseRepositoryImpl;
import com.electronicstore.springboot.dao.orm.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

//TODO dev profile
//TODO enable logging of orm mapping
//TODO applicable managed entity manager for concurrency
@Profile("data.orm")
@Configuration
@EnableJpaRepositories(basePackages = "com.electronicstore.springboot", repositoryBaseClass = BaseRepositoryImpl.class)
@EntityScan(basePackages = {"com.electronicstore.springboot.model"})
@EnableTransactionManagement
public class JpaHibernateH2Config {

    @Autowired
    private ProductRepository productRepository;

    @Bean
    public ProductDatastore productDatastore(){
        return productRepository;
    }

    /*@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(true);

        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("com.electronicstore.springboot.model");
        factory.setDataSource(dataSource());

        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager txManager = new JpaTransactionManager();
        txManager.setEntityManagerFactory(entityManagerFactory);
        return txManager;
    }*/

    //TODO enable transaction management
    /*@Bean
    public PlatformTransactionManager txManager() {
        return new DataSourceTransactionManager(dataSource());
    }*/

    /*@Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http) {
        return http.authorizeExchange()
                .pathMatchers("/actuator/**").permitAll()
                .anyExchange().authenticated()
                .and().build();
    }*/


}
