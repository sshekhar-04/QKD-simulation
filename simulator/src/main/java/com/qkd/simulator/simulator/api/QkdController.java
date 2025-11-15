package com.qkd.simulator.simulator.api;

import com.qkd.simulator.simulator.service.QkdService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // New Import
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/qkd")
public class QkdController {

    private final QkdService qkdService;

    public QkdController(QkdService qkdService) {
        this.qkdService = qkdService;
    }

    /**
     * Endpoint to run the full QKD protocol, generate a key, and encrypt a message.
     * Returns the encryption key (Base64) and the ciphertext.
     */
    @GetMapping("/encrypt")
    public ResponseEntity<?> encryptWithQkdKey(@RequestParam(defaultValue = "Default secret message") String message) {
        try {
            Map<String, String> result = qkdService.runHybridProtocolAndEncrypt(message);

            return ResponseEntity.ok(Map.of(
                    "status", "Successfully generated quantum key and encrypted message.",
                    "encryptionKey", result.get("encryptionKey"),
                    "ciphertext", result.get("ciphertext")
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint to decrypt a message using the key provided by the client (via JSON body).
     * @param request The DecryptionRequest object containing the key and ciphertext.
     * @return The original plaintext message.
     */
    @PostMapping("/decrypt")
    public ResponseEntity<?> decryptMessage(
            @RequestBody DecryptionRequest request) { // Changed to @RequestBody
        try {
            String plaintext = qkdService.decryptMessage(request.getEncryptionKey(), request.getCiphertext());

            return ResponseEntity.ok(Map.of(
                    "status", "Decryption successful.",
                    "keyProvided", request.getEncryptionKey().substring(0, 10) + "...",
                    "plaintext", plaintext
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Key Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Decryption failed: " + e.getMessage()));
        }
    }
}