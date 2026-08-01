package com.tirmizee.security;

import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import static javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm;

@Slf4j
public class FullCertPinningTrustManager implements X509TrustManager {

    private final List<byte[]> pinnedCertificates;
    private final X509TrustManager defaultTrustManager;

    public FullCertPinningTrustManager(List<byte[]> pinnedCertificates) throws Exception {
        this.pinnedCertificates = pinnedCertificates;

        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        X509TrustManager x509Tm = null;
        for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                x509Tm = (X509TrustManager) tm;
                break;
            }
        }

        this.defaultTrustManager = x509Tm;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

        log.info("[TLS Pinning] Inspecting server certificate: {}", chain[0].getSubjectX500Principal().getName());

        if (defaultTrustManager != null) {
            defaultTrustManager.checkServerTrusted(chain, authType);
        }

        byte[] serverCertBytes = chain[0].getEncoded();
        boolean isMatched = false;

        for (byte[] pinnedCertBytes : pinnedCertificates) {
            if (MessageDigest.isEqual(serverCertBytes, pinnedCertBytes)) {
                isMatched = true;
                break;
            }
        }

        if (!isMatched) {
            log.error("[TLS Pinning Failed] Certificate does not match Google Pin!");
            throw new CertificateException("TLS Pinning Failed: Server certificate does not match any pinned certificates!");
        }

        log.info("[TLS Pinning Matched] Connection allowed.");
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        if (defaultTrustManager != null) {
            defaultTrustManager.checkClientTrusted(chain, authType);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return defaultTrustManager != null
                ? defaultTrustManager.getAcceptedIssuers()
                : new X509Certificate[0];
    }
}
