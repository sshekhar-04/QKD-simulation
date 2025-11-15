package com.qkd.simulator.simulator.api;

/**
 * Data model for the Decryption POST request body.
 */
public class DecryptionRequest {
    private String encryptionKey;
    private String ciphertext;

    // Default constructor (required by Jackson/Spring)
    public DecryptionRequest() {}

    // Getters and Setters
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public void setEncryptionKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}