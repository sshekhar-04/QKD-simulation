package com.qkd.simulator.simulator.core;

import com.qkd.simulator.simulator.model.Photon;
import com.qkd.simulator.simulator.model.SiftedKey;
import com.qkd.simulator.simulator.model.QBERCheckResult;
import com.qkd.simulator.simulator.model.KeyResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class QkdEngine {

    private final Random random = ThreadLocalRandom.current();

    // --- Core BB84 Steps ---

    public List<Integer> generateRandomBits(int n) {
        return random.ints(n, 0, 2).boxed().collect(Collectors.toList());
    }

    public List<Integer> generateRandomBases(int n) {
        return random.ints(n, 0, 2).boxed().collect(Collectors.toList());
    }

    public List<Photon> encodePhotons(List<Integer> bits, List<Integer> bases) {
        List<Photon> photons = new ArrayList<>();
        for (int i = 0; i < bits.size(); i++) {
            photons.add(new Photon(bits.get(i), bases.get(i)));
        }
        return photons;
    }

    public List<Photon> eavesdropChannel(List<Photon> photons, double eveAttackRate) {
        List<Photon> photonsToBob = new ArrayList<>();
        for (Photon photon : photons) {
            if (random.nextDouble() < eveAttackRate) {
                // Eve intercepts
                int eveBasis = random.nextInt(2);
                int measuredBit;

                if (photon.basis == eveBasis) {
                    measuredBit = photon.bit;
                } else {
                    measuredBit = random.nextInt(2);
                }
                photonsToBob.add(new Photon(measuredBit, eveBasis));
            } else {
                photonsToBob.add(photon);
            }
        }
        return photonsToBob;
    }

    public List<Integer> measurePhotons(List<Photon> photonsToBob, List<Integer> bobBases) {
        List<Integer> bobMeasurements = new ArrayList<>();
        for (int i = 0; i < photonsToBob.size(); i++) {
            Photon photon = photonsToBob.get(i);
            int bobBasis = bobBases.get(i);

            if (photon.basis == bobBasis) {
                bobMeasurements.add(photon.bit);
            } else {
                bobMeasurements.add(random.nextInt(2));
            }
        }
        return bobMeasurements;
    }

    public SiftedKey siftKey(List<Integer> aliceBits, List<Integer> aliceBases,
                             List<Integer> bobBits, List<Integer> bobBases) {
        List<Integer> siftedAlice = new ArrayList<>();
        List<Integer> siftedBob = new ArrayList<>();

        for (int i = 0; i < aliceBits.size(); i++) {
            if (aliceBases.get(i).equals(bobBases.get(i))) {
                siftedAlice.add(aliceBits.get(i));
                siftedBob.add(bobBits.get(i));
            }
        }
        return new SiftedKey(siftedAlice, siftedBob);
    }

    // --- Classical Post-Processing ---

    public QBERCheckResult calculateQber(List<Integer> keyA, List<Integer> keyB, int sampleSize) {
        if (keyA.size() < sampleSize) {
            throw new IllegalArgumentException("Key too short for QBER sampling.");
        }

        int errors = 0;
        for (int i = 0; i < sampleSize; i++) {
            if (!keyA.get(i).equals(keyB.get(i))) {
                errors++;
            }
        }
        double qber = (double) errors / sampleSize;

        List<Integer> remainingKeyA = keyA.subList(sampleSize, keyA.size());
        List<Integer> remainingKeyB = keyB.subList(sampleSize, keyB.size());

        return new QBERCheckResult(qber, remainingKeyA, remainingKeyB);
    }

    // Simplified Error Correction
    public List<Integer> errorCorrection(List<Integer> keyA, int blockSize) {
        List<Integer> finalKey = new ArrayList<>();
        int numBlocks = keyA.size() / blockSize;

        for (int i = 0; i < numBlocks; i++) {
            int parity = 0;
            for (int j = 0; j < blockSize; j++) {
                parity += keyA.get(i * blockSize + j);
            }

            // In this simplified model, only keep blocks that match parity (simulating success)
            if (parity % 2 == 0) {
                finalKey.addAll(keyA.subList(i * blockSize, i * blockSize + blockSize));
            }
        }
        return finalKey;
    }
}