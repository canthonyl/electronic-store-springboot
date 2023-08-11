package com.electronicstore.springboot.dao;


import com.electronicstore.springboot.dao.Datastore;
import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DatastoreTest {

    @Autowired
    EntityDatastore<Product> entityDatastore;

    Product request1, request2, request3, request4, request5;
    Product category1, category2, category3;

    @BeforeEach
    public void setup(){
        request1 = new Product();
        request1.setName("Apple Macbook Pro");
        request1.setDescription("Apple Macbook Pro Desc");
        request1.setCategoryId(2L);
        request1.setPrice(11000.0);

        request2 = new Product();
        request2.setName("Dell Desktop");
        request2.setDescription("Dell Desktop i5");
        request2.setCategoryId(1L);
        request2.setPrice(5000.0);

        request3 = new Product();
        request3.setName("iPad Pro");
        request3.setDescription("Apple iPad Pro");
        request3.setCategoryId(3L);
        request3.setPrice(8000.0);

        request4 = new Product();
        request4.setName("Macbook Air");
        request4.setDescription("Apple Macbook Air");
        request4.setCategoryId(2L);
        request4.setPrice(9000.0);

        request5 = new Product();
        request5.setName("Mechanical Keyboard");
        request5.setDescription("Mechanical Keyboard Clicky");
        request5.setCategoryId(3L);
        request5.setPrice(500.0);

        category1 = new Product();
        category1.setCategoryId(1L);

        category2 = new Product();
        category2.setCategoryId(2L);

        category3 = new Product();
        category3.setCategoryId(3L);
    }

    @Test
    public void persistNewAndFind(){
        entityDatastore.persist(request1);
        assertEquals(1L, request1.getId());

        Optional<Product> result = entityDatastore.find(1L);
        assertEquals(true, result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    public void persistUpdateAndFind(){
        entityDatastore.persist(request1);
        assertEquals(1L, request1.getId());

        Optional<Product> result = entityDatastore.find(1L);
        assertEquals("Apple Macbook Pro", result.get().getName());

        request1.setName("Apple Macbook Pro X");
        entityDatastore.persist(request1);
        assertEquals(1L, request1.getId());//check result from key holder

        Optional<Product> resultUpdate = entityDatastore.find(1L);
        assertEquals("Apple Macbook Pro X", resultUpdate.get().getName());
    }

    @Test
    public void persistAndFindMultiple(){
        List<Product> result = entityDatastore.persist(List.of(request1, request2, request3));
        assertEquals(1L, request1.getId());
        assertEquals(2L, request2.getId());
        assertEquals(3L, request3.getId());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(3L, result.get(2).getId());

        List<Product> findResult = entityDatastore.find(List.of(1L, 2L, 3L));
        assertEquals(3, findResult.size());
        assertEquals(1L, findResult.get(0).getId());
        assertEquals(2L, findResult.get(1).getId());
        assertEquals(3L, findResult.get(2).getId());
    }

    @Test
    public void findMatching(){
        entityDatastore.persist(List.of(request1, request2, request3, request4, request5));

        List<Product> category1Products = entityDatastore.findMatching(category1);
        assertEquals(1, category1Products.size());
        assertEquals("Dell Desktop", category1Products.get(0).getName());

        List<Product> category2Products = entityDatastore.findMatching(category2);
        assertEquals(2, category2Products.size());
        assertEquals("Apple Macbook Pro", category2Products.get(0).getName());
        assertEquals("Macbook Air", category2Products.get(1).getName());

        List<Product> category3Products = entityDatastore.findMatching(category3);
        assertEquals(2, category3Products.size());
        assertEquals("iPad Pro", category3Products.get(0).getName());
        assertEquals("Mechanical Keyboard", category3Products.get(1).getName());

    }

    @Test
    public void findMatchingValuesIn() {
        entityDatastore.persist(List.of(request1, request2, request3, request4, request5));

        List<Product> result = entityDatastore.findMatchingValuesIn("category_id", List.of(1L, 2L));
        assertEquals(3, result.size());
        Map<Long, Product> resultMap = result.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
        assertEquals(true, resultMap.containsKey(1L));
        assertEquals(true, resultMap.containsKey(2L));
        assertEquals(true, resultMap.containsKey(4L));
    }

    @Test
    public void remove(){
        entityDatastore.persist(request1);
        assertEquals(1L, request1.getId());
        assertEquals(true, entityDatastore.contains(1L));
        assertEquals(true, entityDatastore.find(1L).isPresent());

        entityDatastore.remove(1L);
        assertEquals(false, entityDatastore.contains(1L));
        assertEquals(false, entityDatastore.find(1L).isPresent());
    }


    @Test
    public void removeMultiple(){
        entityDatastore.persist(List.of(request1, request2, request3));
        assertEquals(true, entityDatastore.contains(1L));
        assertEquals(true, entityDatastore.contains(2L));
        assertEquals(true, entityDatastore.contains(3L));
        assertEquals(3, entityDatastore.find(List.of(1L,2L,3L)).size());

        entityDatastore.remove(List.of(1L, 2L, 3L));
        assertEquals(false, entityDatastore.contains(1L));
        assertEquals(false, entityDatastore.contains(2L));
        assertEquals(false, entityDatastore.contains(3L));
        assertEquals(0, entityDatastore.find(List.of(1L,2L,3L)).size());
    }


    @Test
    public void persistWithStatus(){
        Datastore.Status result = entityDatastore.persistWithStatus(request1);
        assertEquals(Datastore.Status.Success, result);

        Product badRequest = request2;
        badRequest.setCategoryId(9L);
        result = entityDatastore.persistWithStatus(badRequest);
        assertEquals(Datastore.Status.Error, result);
    }

    @Test
    public void persistMultipleWithStatus(){
        Product badRequest = request2;
        badRequest.setCategoryId(9L);

        Map<Datastore.Status, List<Product>> result = entityDatastore.persistWithStatus(List.of(request1, badRequest, request3));

        List<Product> successRequests = result.get(Datastore.Status.Success);
        List<Product> errorRequests = result.get(Datastore.Status.Error);

        assertEquals(2, successRequests.size());
        assertEquals(1, successRequests.get(0).getId());
        assertEquals(3, successRequests.get(1).getId()); //TODO revise ID depending on txn management handling of batch update
        assertEquals("Apple Macbook Pro", successRequests.get(0).getName());
        assertEquals("iPad Pro", successRequests.get(1).getName());

        assertEquals(1, errorRequests.size());
        assertEquals(null, errorRequests.get(0).getId());
        assertEquals("Dell Desktop", errorRequests.get(0).getName());

    }




}
