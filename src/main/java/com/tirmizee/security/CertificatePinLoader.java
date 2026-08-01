package com.tirmizee.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CertificatePinLoader {

    @Value("classpath:certs/*.crt,classpath:certs/*.pem")
    private Resource[] certResources;

    public List<byte[]> loadPinSet() throws Exception {
        List<byte[]> pinSet = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        System.out.println(certResources.length);

        for (Resource resource : certResources) {
            try (InputStream is = resource.getInputStream()) {
                X509Certificate cert = (X509Certificate) certFactory.generateCertificate(is);
                pinSet.add(cert.getEncoded());
            }
        }

        return pinSet;
    }

}
