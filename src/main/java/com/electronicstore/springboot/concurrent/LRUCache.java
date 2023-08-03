package com.electronicstore.springboot.concurrent;

import com.electronicstore.springboot.model.ShoppingCart;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class LRUCache {

    final CircularQueue cache;
    final Map<Long, Entry> cacheMap;
    final Queue<ShoppingCart> evicted;
    final int maxSize;
    final OptimisticLock lock;

    public LRUCache(int capacity, Queue<ShoppingCart> queue) {
        this(capacity, queue, new AtomicBooleanLock());
    }

    public LRUCache(int capacity, Queue<ShoppingCart> queue, OptimisticLock l){
        cache = new CircularQueue(capacity);
        cacheMap = new HashMap<>();
        evicted = queue;
        maxSize = capacity;
        lock = l;
    }

    public Optional<ShoppingCart> get(Long id) {
        try {
            lock.acquire();
            if (cacheMap.containsKey(id)) {
                Entry e = cacheMap.get(id);
                setFirst(e);
                return Optional.of(e.cart.get());
            } else {
                return Optional.empty();
            }
        } finally {
            lock.release();
        }
    }

    public void put(Long id, ShoppingCart cart) {
        try {
            lock.acquire();
            Entry e;
            if ((e = cacheMap.get(id)) == null) {
                cacheMap.put(id, (e = getFreeSlot()));
            }
            e.cart.set(cart);
            setFirst(e);
        } finally {
            lock.release();
        }
    }

    public List<ShoppingCart> values(){
        try {
            lock.acquire();
            return cacheMap.values().stream().map(Entry::get).toList();
        } finally {
            lock.release();
        }
    }

    private void setFirst(Entry e) {
        if (e != cache.ref.get().next.get()) {
            if (cache.ref.get().next.get().previous.get() != e) {
                if (e.previous.get() == cache.ref.get().next.get()) {
                    Entry previous = cache.ref.get().next.get().previous.get();
                    link(cache.ref.get().next.get(), e.next.get());
                    link(e, cache.ref.get().next.get());
                    link(previous, e);
                } else {
                    link(e.previous.get(), e.next.get());
                    Entry next = cache.ref.get().next.get();
                    Entry previous = next.previous.get();
                    link(previous, e);
                    link(e, next);
                }
            }
            cache.ref.get().next.set(e);
        }
    }

    private void link(Entry current, Entry next) {
        current.next.set(next);
        next.previous.set(current);
    }

    private Entry getFreeSlot() {
        Entry tail = cache.ref.get().next.get().previous.get();
        ShoppingCart shoppingCart = tail.cart.get();
        if (shoppingCart != null) {
            cacheMap.remove(shoppingCart.getId());
            evicted.add(shoppingCart);
            tail.cart.set(null);
        }
        return tail;
    }

    class Entry {
        AtomicReference<Entry> next = new AtomicReference<>();
        AtomicReference<Entry> previous = new AtomicReference<>();
        AtomicReference<ShoppingCart> cart = new AtomicReference<>();
        ShoppingCart get(){
            return cart.get();
        }
    }

    class CircularQueue {
        AtomicReference<Entry> ref;

        CircularQueue(int capacity) {
            ref = new AtomicReference<>(new Entry());
            Entry current = ref.get();
            for (int i=0; i<capacity; i++) {
                Entry next = new Entry();
                link(current, next);
                current = next;
            }
            link(current, ref.get().next.get());
        }
    }
}
