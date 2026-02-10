package com.movie.theatre.feign.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom Feign error decoder for handling downstream service errors.
 */
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign error: method={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        switch (response.status()) {
            case 400:
                return new IllegalArgumentException("Bad request to Identity Service");
            case 403:
                return new RuntimeException("Forbidden - insufficient permissions");
            case 404:
                return new RuntimeException("User not found in Identity Service");
            case 503:
                return new RuntimeException("Identity Service unavailable");
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
}
