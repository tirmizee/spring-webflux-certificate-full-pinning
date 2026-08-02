# spring-webflux-certificate-full-pinning

A Spring Boot WebFlux Proof of Concept demonstrating Full Certificate Pinning with Reactor Netty HttpClient and WebClient.

The application calls the public JSONPlaceholder API over HTTPS. Before the HTTP request is allowed, the client:

Performs the normal JVM trust validation for the server certificate chain.
Reads the server's leaf certificate.
Compares the complete DER-encoded certificate against every configured certificate pin.
Allows the connection only when at least one pin matches exactly.

This project implements full leaf certificate pinning, not certificate fingerprint pinning, public-key pinning, or mutual TLS.

## What This Project Shows

- Spring WebFlux WebClient with a custom Reactor Netty SSL context
- A custom X509TrustManager
- Normal CA trust validation before pin validation
- Exact full-certificate comparison using DER-encoded bytes
- Multiple certificate pins for certificate rotation
- Loading .pem and .crt certificate files from the classpath
- Rejecting a TLS connection when no configured certificate matches the server leaf certificate

## Important Behavior

This implementation performs two separate validations

<img width="1672" height="941" alt="ChatGPT Image Aug 2, 2026, 07_17_07 AM" src="https://github.com/user-attachments/assets/ed1094b1-8fcd-453c-96a5-28775d3a1f11" />


A certificate must therefore satisfy both conditions:

- The certificate chain must be trusted by the JVM default trust store.
- The leaf certificate must exactly match one of the configured pins.

The files under src/main/resources/certs are used as pins, not as trust anchors.

---

## Technology Stack

- Java 17
- Spring Boot 4.0.7
- Spring WebFlux
- Reactor Netty
- Maven Wrapper
- Lombok

---

## Project Structure

```text
spring-webflux-certificate-full-pinning
├── src
│   ├── main
│   │   ├── java/com/tirmizee
│   │   │   ├── client
│   │   │   │   └── MockJsonClient.java
│   │   │   ├── config
│   │   │   │   └── WebClientConfig.java
│   │   │   ├── controller
│   │   │   │   └── TestController.java
│   │   │   ├── security
│   │   │   │   ├── CertificatePinLoader.java
│   │   │   │   └── FullCertPinningTrustManager.java
│   │   │   └── SpringWebfluxCertificateFullPinningApplication.java
│   │   └── resources
│   │       ├── certs
│   │       │   ├── typicode-current.pem
│   │       │   ├── typicode-backup.pem
│   │       │   └── typicode-future.pem
│   │       └── application.yaml
│   └── test
├── pom.xml
├── mvnw
└── mvnw.cmd
```

## Core Components

### `CertificatePinLoader`

Loads every `.crt` and `.pem` file from:

```text
classpath:certs/
```

Each X.509 certificate is converted to its DER-encoded byte representation and added to the pin set.

This provides multiple-pin support without hard-coding certificates in Java code.

### `FullCertPinningTrustManager`

Implements `X509TrustManager` and performs the following operations:

1. Delegates certificate-chain validation to the JVM default `X509TrustManager`.
2. Reads `chain[0]`, which is the server leaf certificate.
3. Calls `X509Certificate#getEncoded()` to obtain the complete DER certificate.
4. Compares the certificate with every configured pin using `MessageDigest.isEqual`.
5. Throws `CertificateException` when no pin matches.

Because the whole encoded certificate is compared, a newly issued certificate will not match even when it uses:

- The same domain
- The same issuer
- The same subject
- The same public key

Any change to the certificate produces different encoded certificate bytes.

### `WebClientConfig`

Creates a Reactor Netty `SslContext` with the custom trust manager and attaches it to Spring WebFlux `WebClient`.

### `MockJsonClient`

Calls:

```text
https://jsonplaceholder.typicode.com/todos/{id}
```

### `TestController`

Exposes the local endpoint:

```http
GET /api/todo/{id}
```

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/tirmizee/spring-webflux-certificate-full-pinning.git
cd spring-webflux-certificate-full-pinning
```

### 2. Run the Application

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
mvnw.cmd spring-boot:run
```

The application starts on the default port:

```text
http://localhost:8080
```

### 3. Call the Test Endpoint

```bash
curl http://localhost:8080/api/todo/1
```

Example response:

```json
{
  "userId": 1,
  "id": 1,
  "title": "delectus aut autem",
  "completed": false
}
```

When the server certificate matches a configured pin, the application logs messages similar to:

```text
[TLS Pinning] Inspecting server certificate: CN=typicode.com
[TLS Pinning Matched] Connection allowed.
```
---

## How to Retrieve the Server Leaf Certificate

Use SNI when connecting to the target server:

```bash
openssl s_client \
  -connect jsonplaceholder.typicode.com:443 \
  -servername jsonplaceholder.typicode.com \
  </dev/null 2>/dev/null \
  | openssl x509 -outform PEM \
  > src/main/resources/certs/typicode-current.pem
```

---

## Managing Multiple Pins

All `.pem` and `.crt` files under the following directory are loaded automatically:

```text
src/main/resources/certs/
```

Example:

```text
certs/
├── typicode-current.pem
├── typicode-future.pem
└── typicode-backup.pem
```

The TLS connection is accepted when the server leaf certificate matches **any one** of these files.
