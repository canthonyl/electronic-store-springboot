package com.electronicstore.springboot.concurrent;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ProductCategory;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.electronicstore.springboot.service.ShoppingCartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConcurrentShoppingCartModificationTest {

    @Autowired
    ShoppingCartService shoppingCartService;

    @Autowired
    EntityDatastore<ProductCategory> productCategoryRepository;

    @Autowired
    EntityDatastore<Product> productDatastore;

    //@Test
    public void multipleGetRequests(){
        try {

            double expectDiffAmount = 0.0;
            int numThreads = 1;
            int getRequestElapsedTimeMs = 1000;
            createProducts(1000, defaultValues());

            ShoppingCart shoppingCart = new ShoppingCart(Collections.emptyList());
            shoppingCartService.createShoppingCart(shoppingCart);
            assertEquals(1L, shoppingCart.getId());

            List<ShoppingCartItem> allItemRequest = createItemList(1000, defaultValues());
            shoppingCartService.addShoppingCartItems(1L, allItemRequest);

            List<Callable<Double>> requests = IntStream.range(0, numThreads)
                    .mapToObj(i -> {
                        Callable<Double> req = () -> {
                            long start = System.currentTimeMillis();
                            double diff = 0.0;
                            while (System.currentTimeMillis() - start > getRequestElapsedTimeMs) {
                                ShoppingCart cart = shoppingCartService.getShoppingCart(1L).get();
                                double amountFromService = cart.getTotalAmount();
                                double amountRecalculated = getTotalAmount(cart);
                                diff += amountFromService - amountRecalculated;
                            }
                            return diff;
                        };
                        return req;
                    }).toList();

            List<Callable<Double>> addItemRequest = IntStream.range(0, numThreads)
                    .mapToObj(i -> {
                        Callable<Double> req = () -> {
                            int startProductId = 0;
                            int endProductId = startProductId + 10;
                            while (endProductId < allItemRequest.size()) {
                                List<ShoppingCartItem> list = allItemRequest.subList(startProductId, endProductId);
                                shoppingCartService.addShoppingCartItems((long) i, list);
                                startProductId = endProductId + 1;
                                endProductId = startProductId + 10;
                            }
                            return 0.0;
                        };
                        return req;
                    }).toList();
            List<Callable<Double>> allRequests = Stream.concat(requests.stream(), addItemRequest.stream()).toList();
            ExecutorService service = Executors.newFixedThreadPool(numThreads);
            List<Future<Double>> result = service.invokeAll(allRequests);
            List<Double> amountField = new LinkedList<>();
            for (Future<Double> e : result) {
                amountField.add(e.get());
            }
            for (Double a : amountField) {
                assertEquals(expectDiffAmount, a, 0.001);
            }
        } catch (InterruptedException | ExecutionException ie) {
            ie.printStackTrace();
            fail(ie.getMessage());
        }
    }

    private <T> Consumer<T> defaultValues() {
        return q -> {};
    }

    private List<ShoppingCartItem> createItemList(int numItems) { return createItemList(numItems, defaultValues());}
    private List<ShoppingCartItem> createItemList(int numItems, Consumer<ShoppingCartItem> modify) {
        List<ShoppingCartItem> result = IntStream.range(0, numItems)
                .mapToObj(i -> {
                    ShoppingCartItem item = new ShoppingCartItem();
                    item.setProductId((long)i+1);
                    item.setQuantity(1);
                    modify.accept(item);
                    return item;
                }).toList();
        return result;
    }

    private List<Product> createProducts(int productCount, Consumer<Product> setupProduct) {
        int numCategories = productCategoryRepository.findAll().size();
        List<Product> productList = IntStream.range(0, productCount)
                .mapToObj(i -> {
                    Product p = new Product();
                    p.setName("Product Name "+(i+1));
                    p.setDescription("Product Description "+(i+1));
                    p.setCategoryId(((long) (i % numCategories)+1L));
                    p.setPrice(1.0);
                    setupProduct.accept(p);
                    return p;
                }).toList();
        productDatastore.persist(productList);
        return productList;
    }

    private double getTotalAmount(ShoppingCart cart) {
        double totalAmountBeforeDiscount = 0 ;
        double totalDiscount = 0;
        double totalAmount = 0;

        for (ShoppingCartItem i : cart.getItems()) {
            double amountBeforeDiscount = i.getQuantity() * i.getPrice();
            double discountAmount = i.getDiscountAmount();
            double amount = amountBeforeDiscount - discountAmount;

            totalAmountBeforeDiscount += amountBeforeDiscount;
            totalDiscount += discountAmount;
            totalAmount += amount;
        }
        return totalAmount;
    }

    @Test
    public void testAtomicBooleanLock(){
        try {
            Holder h = new Holder();
            h.useLock = true;
            long elapsedTimeMs = 100;
            int numThreads = 10;
            List<Callable<Long>> callables = IntStream.range(0, numThreads)
                    .mapToObj(i -> {
                        Callable<Long> c = () -> {
                            long start = System.currentTimeMillis();
                            long numTimes = 0L;
                            while (System.currentTimeMillis() - start < elapsedTimeMs) {
                                h.ops();
                                numTimes += 1;
                            }
                            return numTimes;
                        };
                        return c;
                    }).toList();

            ExecutorService service = Executors.newFixedThreadPool(numThreads);
            List<Future<Long>> results = service.invokeAll(callables);
            long total = 0L;
            for (Future<Long> r : results) {
                total += r.get();
            }
            assertEquals(total, h.var0);
            assertEquals(h.var0, h.var1);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class Holder {
        AtomicBooleanLock lock = new AtomicBooleanLock();
        boolean useLock = false;

        volatile int var0 = 0;
        volatile int var1 = 0;

        public void ops(){
            if (useLock) {
                incWithLock();
            } else {
                inc();
            }
        }

        private void inc(){
            var0 += 1;
            var1 += 1;
        }

        private void incWithLock(){
            try {
                lock.acquire();
                inc();
            } finally {
                lock.release();
            }
        }

    }
}
