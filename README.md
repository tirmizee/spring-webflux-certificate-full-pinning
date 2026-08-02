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
