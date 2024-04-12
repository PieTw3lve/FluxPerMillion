package com.github.pietw3lve.fpm.utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollectionUtil<E> {
    
    private final NavigableMap<Double, E> map = new TreeMap<Double, E>();
    private final Random random;
    private double total = 0;

    /**
     * Creates a new RandomCollectionUtil with the default Random instance.
     */
    public RandomCollectionUtil() {
        this(new Random());
    }

    /**
     * Creates a new RandomCollectionUtil with the given Random instance.
     * @param random the Random instance to use.
     */
    public RandomCollectionUtil(Random random) {
        this.random = random;
    }

    /**
     * Adds a new element to the collection.
     * @param weight the weight of the element.
     * @param result the element to add.
     * @return the RandomCollectionUtil instance.
     */
    public RandomCollectionUtil<E> add(double weight, E result) {
        if (weight <= 0) return this;
        total += weight;
        map.put(total, result);
        return this;
    }

    /**
     * Returns a random element from the collection.
     * @return a random element from the collection.
     */
    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
