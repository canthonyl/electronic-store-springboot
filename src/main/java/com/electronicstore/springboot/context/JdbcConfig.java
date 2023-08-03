package com.electronicstore.springboot.context;

import com.electronicstore.springboot.dao.ProductDatastore;
import com.electronicstore.springboot.dao.jdbc.ProductDatastoreJdbc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("data.jdbc")
@Configuration
public class JdbcConfig {

    @Bean
    public ProductDatastore productDatastore(){
        return new ProductDatastoreJdbc();
    }

}
