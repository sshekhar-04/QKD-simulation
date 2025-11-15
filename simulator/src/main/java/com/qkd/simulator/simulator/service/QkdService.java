package com.qkd.simulator.simulator.service;

import com.qkd.simulator.simulator.core.QkdEngine;
import com.qkd.simulator.simulator.model.QBERCheckResult;
import com.qkd.simulator.simulator.model.Photon;
import com.qkd.simulator.simulator.model.SiftedKey;
import com.qkd.simulator.simulator.crypto.SymmetricCrypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Base64;

@Service
public class QkdService {

    private final QkdEngine qkdEngine;
    private final SymmetricCrypto symmetricCrypto;

    // Configuration loaded from application.properties
    @Value("${qkd.qubits.count:2000}")
    private int numQubits;

    @Value("${qkd.eve.attack.rate:0.15}")
    private double eveAttackRate;

    @Value("${qkd.qber.threshold:0.05}")
    private double qberThreshold;

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int AES_KEY_BITS = KEY_LENGTH_BYTES * 8;
    private static final int ERROR_CORRECTION_BLOCK_SIZE = 4;

    public QkdService(QkdEngine qkdEngine, SymmetricCrypto symmetricCrypto) {
        this.qkdEngine = qkdEngine;
        this.symmetricCrypto = symmetricCrypto;
    }

    /**
     * Runs the full QKD protocol, encrypts the message, and returns the key and ciphertext.
     * The key is not stored, ensuring it is ephemeral and managed client-side.
     * * @param originalMessage The message to encrypt.
     * @return A map containing the Base64 encoded 'encryptionKey' and the 'ciphertext'.
     */
    public Map<String, String> runHybridProtocolAndEncrypt(String originalMessage) throws Exception {

        // --- 1. QKD Key Establishment ---

        // Alice generates bits and bases
        List<Integer> aliceBits = qkdEngine.generateRandomBits(numQubits);
        List<Integer> aliceBases = qkdEngine.generateRandomBases(numQubits);
        List<Integer> bobBases = qkdEngine.generateRandomBases(numQubits);

        List<Photon> encodedPhotons = qkdEngine.encodePhotons(aliceBits, aliceBases);

        // Channel simulation (with Eve)
        List<Photon> photonsToBob = qkdEngine.eavesdropChannel(encodedPhotons, eveAttackRate);
        List<Integer> bobBits = qkdEngine.measurePhotons(photonsToBob, bobBases);

        // Sifting
        SiftedKey sifted = qkdEngine.siftKey(aliceBits, aliceBases, bobBits, bobBases);

        // QBER Check
        int sampleSize = Math.min(100, sifted.aliceKey.size() / 5);
        QBERCheckResult checkResult = qkdEngine.calculateQber(sifted.aliceKey, sifted.bobKey, sampleSize);

        if (checkResult.qber > qberThreshold) {
            throw new Exception("Key discarded due to high QBER: " + checkResult.qber);
        }

        // Error Correction (using Alice's remaining key)
        List<Integer> keyAfterEC = qkdEngine.errorCorrection(checkResult.keyA, ERROR_CORRECTION_BLOCK_SIZE);

        // Privacy Amplification & Key Preparation
        List<Integer> finalKeyBits = keyAfterEC.subList(0, Math.min(AES_KEY_BITS, keyAfterEC.size()));

        // Convert the secure bit string into the final AES key (Privacy Amplification step)
        byte[] aesKey = symmetricCrypto.convertToAesKey(finalKeyBits, KEY_LENGTH_BYTES);

        // Encode the key for safe transmission back to the client
        String base64Key = Base64.getEncoder().encodeToString(aesKey);

        // --- 2. Hybrid Encryption ---

        // Encrypt the message using the QKD-derived AES key
        String ciphertext = symmetricCrypto.encrypt(originalMessage, aesKey);

        // Self-Test check
        String decryptedSelfTest = symmetricCrypto.decrypt(ciphertext, aesKey);
        if (!originalMessage.equals(decryptedSelfTest)) {
            throw new Exception("Self-Test Failed: Decrypted message does not match original.");
        }

        // Return the key and ciphertext
        return Map.of("encryptionKey", base64Key, "ciphertext", ciphertext);
    }

    /**
     * Decrypts a message using a key provided by the client.
     * @param base64KeyString The Base64 encoded key provided by the client.
     * @param ciphertext The message to decrypt.
     * @return The original plaintext message.
     */
    public String decryptMessage(String base64KeyString, String ciphertext) throws Exception {
        // Decode the key string provided by the user back into a byte array
        byte[] aesKey = Base64.getDecoder().decode(base64KeyString);

        if (aesKey.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("Invalid key length. Must be " + KEY_LENGTH_BYTES + " bytes.");
        }

        return symmetricCrypto.decrypt(ciphertext, aesKey);
    }
}