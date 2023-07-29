package com.electronicstore.springboot;

import com.electronicstore.springboot.dao.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class SpringbootApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}

}
