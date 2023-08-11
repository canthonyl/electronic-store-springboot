package com.electronicstore.springboot.context;

import com.electronicstore.springboot.dao.Datastore;
import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.dao.orm.BaseJpaRepositoryImpl;
import com.electronicstore.springboot.dao.orm.ProductCategoryJpaRepository;
import com.electronicstore.springboot.dao.orm.ProductJpaRepository;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//TODO dev profile
//TODO enable logging of orm mapping
//TODO applicable managed entity manager for concurrency
@Profile("data.orm")
@Configuration
//@EnableJpaRepositories(basePackages = "com.electronicstore.springboot", repositoryBaseClass = BaseJpaRepositoryImpl.class)
@EntityScan(basePackages = {"com.electronicstore.springboot.model"})
@EnableTransactionManagement
public class JpaHibernateH2Config {

    /*
    @Autowired
    private ProductJpaRepository productRepository;

    @Autowired
    private ProductCategoryJpaRepository productCategoryRepository;

    @Bean
    public EntityDatastore<Product> productDatastore(){
        return new EntityDatastore<>(productRepository);
    }

    @Bean
    public EntityDatastore<ProductCategory> productCategoryDatastore(){
        return new EntityDatastore<>(productCategoryRepository);
    }*/

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
