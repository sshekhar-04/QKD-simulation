package com.qkd.simulator.simulator.model;

import java.util.List;

/**
 * Result structure after the Quantum Bit Error Rate (QBER) check.
 */
public class QBERCheckResult {
    public final double qber;
    public final List<Integer> keyA;
    public final List<Integer> keyB;

    public QBERCheckResult(double qber, List<Integer> keyA, List<Integer> keyB) {
        this.qber = qber;
        this.keyA = keyA;
        this.keyB = keyB;
    }
}