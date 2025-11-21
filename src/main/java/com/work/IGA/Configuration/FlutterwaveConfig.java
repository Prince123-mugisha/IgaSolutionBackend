package com.work.IGA.Configuration;

import org.springframework.beans.factory.annotation.Value;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FlutterwaveConfig {
    @Value("${flutterwave.api.public.key}")
    private String publicKey;

    @Value("${flutterwave.api.secret.key}")
    private String secretKey;

    @Value("${flutterwave.api.encryption.key}")
    private String encryptionKey;

    @Value("${flutterwave.api.base.url}")
    private String baseUrl;

    @Value("${flutterwave.api.callback.url}")
    private String callbackUrl;
    
}
