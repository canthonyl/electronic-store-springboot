package com.electronicstore.springboot.concurrent;

import com.electronicstore.springboot.concurrent.LRUCache;
import com.electronicstore.springboot.model.ShoppingCart;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

public class LRUCacheTest {

    @Test
    public void lruCacheEvictEntryNotRecentlyGetOrPut(){
        Queue<ShoppingCart> evicted = new ConcurrentLinkedQueue<>();

        LRUCache lruCache = new LRUCache(3, evicted);
        assertDoublyLinked(lruCache);

        ShoppingCart s1 = new ShoppingCart(1L);
        ShoppingCart s2 = new ShoppingCart(2L);
        ShoppingCart s3 = new ShoppingCart(3L);
        ShoppingCart s4 = new ShoppingCart(4L);

        lruCache.put(1L, s1);
        lruCache.put(2L, s2);
        lruCache.get(1L);
        lruCache.put(3L, s3);
        assertEquals(0, evicted.size());

        lruCache.put(4L, s4);
        assertEquals(1, evicted.size());
        assertEquals(2L, evicted.peek().getId());

        LRUCache.Entry entry1 = lruCache.cache.ref.get().next.get();
        LRUCache.Entry entry2 = entry1.next.get();
        LRUCache.Entry entry3 = entry2.next.get();

        assertEquals(4L, entry1.cart.get().getId());
        assertEquals(3L, entry2.cart.get().getId());
        assertEquals(1L, entry3.cart.get().getId());

        //ensure link consistent
        assertDoublyLinked(lruCache);
    }

    @Test
    public void concurrentGetAndPutIntoLruCache() {
        Queue<ShoppingCart> evicted = new ConcurrentLinkedQueue<>();

        int lruCacheSize = 1000;
        long numShoppingCarts = 3000;
        int numThreads = 10;
        int threadRunMs = 1000;

        LRUCache lruCache = new LRUCache(lruCacheSize, evicted);

        Map<Long, ShoppingCart> carts = LongStream.rangeClosed(1, numShoppingCarts)
                .mapToObj(ShoppingCart::new)
                .collect(Collectors.toMap(ShoppingCart::getId, Function.identity()));
        carts.forEach(lruCache::put);
        //assertDoublyLinked(lruCache, 10);

        Callable<Integer> webRequest = () -> {
            long startTime = System.currentTimeMillis();
            Random random = new Random();
            double getVsPut = 0.5;
            while (System.currentTimeMillis() - startTime < threadRunMs) {
                Long id = random.nextLong(numShoppingCarts) + 1L;
                if (random.nextDouble() > getVsPut) {
                    lruCache.put(id, carts.get(id));
                } else {
                    lruCache.get(id);
                }
            }
            return 0;
        };

        List<Callable<Integer>> request = IntStream.range(0, numThreads).mapToObj(i -> webRequest).toList();
        List<Future<Integer>> results;
        ExecutorService executor =
                Executors.newFixedThreadPool(numThreads)
                //Executors.newSingleThreadExecutor()
        ;
        try {
            results = executor.invokeAll(request);
            for (Future<Integer> f : results) {
                f.get();
            }
            System.out.println("Number of evictions = "+evicted.size());
            assertDoublyLinked(lruCache);
            assertCacheMapConsistentState(lruCache);
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }


    private void assertDoublyLinked(LRUCache lruCache) {
        LRUCache.Entry first = lruCache.cache.ref.get().next.get();
        LRUCache.Entry current = first;
        LRUCache.Entry next = current.next.get();
        LRUCache.Entry[] array = new LRUCache.Entry[lruCache.maxSize];
        int i=0;
        while (i < lruCache.maxSize) {
            //back link check
            assertSame(next.previous.get(), current, "Back Link Check: Next Entry's previous != Current");
            current = next;
            next = current.next.get();
            array[i] = current;
            //each entry's next/previous should point to a different entry other than itself
            assertNotSame(current, next, "Current Entry's next pointer points back to itself");
            i++;
        }
        //end of iteration should return back to the first entry
        assertSame(current, first, "End of iteration does not land back on first entry");
    }

    private void assertCacheMapConsistentState(LRUCache lruCache) {
        LRUCache.Entry current = lruCache.cache.ref.get().next.get();
        int i=0;
        Set<Long> cartIdFromQueue = new HashSet<>();
        while (i++ < lruCache.maxSize) {
            if (current.cart != null) {
                cartIdFromQueue.add(current.cart.get().getId());
            }
            current = current.next.get();
        }
        assertEquals(cartIdFromQueue, lruCache.cacheMap.keySet());
    }

}
