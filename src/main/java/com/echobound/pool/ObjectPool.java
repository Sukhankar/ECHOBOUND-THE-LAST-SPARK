package com.echobound.pool;

import java.util.function.Supplier;

public class ObjectPool<T> {
    private final Object[] pool;
    private final int capacity;
    private int nextIndex = 0;

    @SuppressWarnings("unchecked")
    public ObjectPool(int capacity, Supplier<T> factory) {
        this.capacity = capacity;
        this.pool = new Object[capacity];
        for (int i = 0; i < capacity; i++) {
            this.pool[i] = factory.get();
        }
    }

    @SuppressWarnings("unchecked")
    public T acquire() {
        T obj = (T) pool[nextIndex];
        nextIndex = (nextIndex + 1) % capacity;
        return obj;
    }

    public int getCapacity() {
        return capacity;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index >= 0 && index < capacity) {
            return (T) pool[index];
        }
        return null;
    }
}
