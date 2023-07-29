package com.electronicstore.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl.class)
public class SpringbootApplication {


	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}

}
