package com.qkd.simulator.simulator.model;

/**
 * Represents an encoded quantum state (qubit) used in BB84.
 */
public class Photon {
    public final int bit;
    public final int basis;

    public Photon(int bit, int basis) {
        this.bit = bit;
        this.basis = basis;
    }
}