package com.electronicstore.springboot.feature;

import com.electronicstore.springboot.context.CommonContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartFeatures {

    @Autowired
    private CommonContext applicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

}
