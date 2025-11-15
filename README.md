⚛️ Hybrid Quantum Key Distribution (QKD) Simulator API

This project demonstrates a hybrid cryptographic architecture by simulating the BB84 Quantum Key Distribution (QKD) protocol to generate an ephemeral, information-theoretically secure key, which is then used to perform fast data encryption using a standard classical cipher (AES-256 GCM).

Developed as a RESTful API using Java and Spring Boot, this application showcases a practical model for quantum-resistant key management in a microservices environment.

💡 Project Overview

The core purpose of this simulator is to bridge the gap between quantum theory and real-world application security:

Quantum Channel (Simulated): The QkdEngine simulates the BB84 protocol, generating a raw key by leveraging quantum mechanics principles (superposition and measurement).

Classical Security Post-Processing: It performs essential classical steps (Error Correction and Privacy Amplification) to refine the key.

Hybrid Encryption: The final, secure key is returned to the client and must be provided back to the service for subsequent decryption, mimicking ephemeral key usage without server-side storage.

🛠️ Technology Stack

Backend Framework: Java 21+ and Spring Boot 3

Key Generation Protocol: BB84 (Simulated)

Symmetric Encryption: AES-256 GCM (using standard Java Cryptography Architecture - JCA)

Data Serialization: JSON (via Spring Web)

✨ Core Features & Simulated Steps

The application implements all critical phases of a real-world QKD key exchange:

Random Generation: Alice and Bob generate random bits and measurement bases.

Simulated Eavesdropping (Eve): The channel model includes a configurable attack rate where an eavesdropper attempts to measure qubits, introducing detectable errors.

Sifting: Alice and Bob publicly compare bases to form a shared raw key.

QBER Check (Quantum Bit Error Rate): They compare a public sample. If the QBER exceeds the threshold (default 5%), the key is immediately discarded, guaranteeing security.

Error Correction (EC): A simplified parity check is performed to mitigate errors caused by channel noise or Eve's measurement.

Privacy Amplification (PA): The final, long bit sequence is cryptographically hashed (SHA-256) to produce the compact, uniform 256-bit AES key.

Ephemeral Key Usage: The generated key is immediately returned to the client and is required for decryption, ensuring the server remains stateless.

🚀 Getting Started

Prerequisites

Java Development Kit (JDK) 21 or newer

Maven or Gradle (for dependency management)

Build and Run

Clone the repository:

git clone [repository-url]
cd quantum-qkd-simulator


Build the project using Maven:

./mvnw clean package


Run the executable JAR file:

java -jar target/qkd-simulator-0.0.1-SNAPSHOT.jar


The API will start on http://localhost:8080.

📡 API Endpoints

1. Encryption (Key Generation & Data Encryption)

Runs the full QKD protocol, encrypts the message, and returns the key required for decryption.

Endpoint: GET /api/qkd/encrypt

Method: GET (or POST for a production environment)

Query Parameter: message (String)

Example Request:

GET http://localhost:8080/api/qkd/encrypt?message=Launch_sequence_initiated


Example Response:

{
  "status": "Successfully generated quantum key and encrypted message.",
  "encryptionKey": "L0tN4eS15tJ3+q9uYh8pG/T+T/T/W3rWq9uYh8pG/T+T/T/W3rw==",
  "ciphertext": "AAAAAAbW5jK7+KxW6xXW6xXW6xXW6xXW6xXW6xXW6xXW6xXW6xX..."
}


2. Decryption (Data Retrieval)

Decrypts the ciphertext using the key provided by the client (ephemeral key management).

Endpoint: POST /api/qkd/decrypt

Method: POST

Content-Type: application/json

Example Request Body:

Use the encryptionKey and ciphertext obtained from the /encrypt endpoint.

{
  "encryptionKey": "L0tN4eS15tJ3+q9uYh8pG/T+T/T/W3rWq9uYh8pG/T+T/T/W3rw==",
  "ciphertext": "AAAAAAbW5jK7+KxW6xXW6xXW6xXW6xXW6xXW6xXW6xXW6xXW6xX..."
}


Example Response:

{
  "status": "Decryption successful.",
  "keyProvided": "L0tN4eS15t...",
  "plaintext": "Launch_sequence_initiated"
}
