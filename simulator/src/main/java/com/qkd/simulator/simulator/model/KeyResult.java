package com.qkd.simulator.simulator.model;

import java.util.List;

/**
 * Final result of the QKD protocol.
 */
public class KeyResult {
    public final List<Integer> finalKey;

    public KeyResult(List<Integer> finalKey) {
        this.finalKey = finalKey;
    }
}