package com.electronicstore.springboot.concurrent;

public class TestLock implements OptimisticLock {

    private final boolean performLock;
    private final AtomicBooleanLock lock;

    public TestLock(boolean toLock) {
        performLock = toLock;
        lock = new AtomicBooleanLock();
    }

    @Override
    public void acquire() {
        if (performLock) {
            lock.acquire();
        }
    }

    @Override
    public void release() {
        if (performLock) {
            lock.release();
        }
    }
}
