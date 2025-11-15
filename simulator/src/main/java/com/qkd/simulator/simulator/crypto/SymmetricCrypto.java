package com.qkd.simulator.simulator.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class SymmetricCrypto {

    private static final String ALGORITHM = "AES";
    private static final String CIPHER_MODE = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Converts the final QKD bit list into a 32-byte hash suitable for AES-256.
     */
    public byte[] convertToAesKey(List<Integer> bitList, int requiredBytes) throws NoSuchAlgorithmException {
        // Privacy Amplification (Hashing the bit string)
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Convert List<Integer> to a byte array for hashing
        byte[] bitBytes = new byte[bitList.size()];
        for (int i = 0; i < bitList.size(); i++) {
            bitBytes[i] = bitList.get(i).byteValue();
        }

        byte[] keyHash = digest.digest(bitBytes);

        return Arrays.copyOf(keyHash, requiredBytes);
    }

    /**
     * Encrypts the plaintext using AES-256 GCM mode.
     */
    public String encrypt(String plaintext, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);

        byte[] iv = new byte[GCM_IV_LENGTH];
        new Random().nextBytes(iv);

        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV and ciphertext for transmission: IV | Ciphertext
        byte[] output = new byte[GCM_IV_LENGTH + ciphertext.length];
        System.arraycopy(iv, 0, output, 0, GCM_IV_LENGTH);
        System.arraycopy(ciphertext, 0, output, GCM_IV_LENGTH, ciphertext.length);

        return Base64.getEncoder().encodeToString(output);
    }

    /**
     * Decrypts the ciphertext using the shared AES-256 GCM key.
     */
    public String decrypt(String base64Ciphertext, byte[] key) throws Exception {
        byte[] input = Base64.getDecoder().decode(base64Ciphertext);

        byte[] iv = Arrays.copyOfRange(input, 0, GCM_IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(input, GCM_IV_LENGTH, input.length);

        Cipher cipher = Cipher.getInstance(CIPHER_MODE);
        SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

        byte[] decryptedBytes = cipher.doFinal(ciphertext);

        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}