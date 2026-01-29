package com.cdac.booking.feign.config;

import com.cdac.booking.exception.SeatLockFailedException;
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
                return new IllegalArgumentException("Bad request to downstream service");
            case 404:
                return new RuntimeException("Resource not found in downstream service");
            case 409:
                return new SeatLockFailedException("Conflict - seats may already be locked");
            case 503:
                return new RuntimeException("Downstream service unavailable");
            default:
                return defaultDecoder.decode(methodKey, response);
        }
    }
}
