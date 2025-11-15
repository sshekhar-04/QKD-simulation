package com.qkd.simulator.simulator.model;

import java.util.List;

/**
 * Result structure after the Sifting step (matching bases).
 */
public class SiftedKey {
    public final List<Integer> aliceKey;
    public final List<Integer> bobKey;

    public SiftedKey(List<Integer> aliceKey, List<Integer> bobKey) {
        this.aliceKey = aliceKey;
        this.bobKey = bobKey;
    }
}