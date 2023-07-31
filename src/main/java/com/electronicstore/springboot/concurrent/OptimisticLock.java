package com.electronicstore.springboot.concurrent;

public interface OptimisticLock {

    void acquire();

    void release();

}