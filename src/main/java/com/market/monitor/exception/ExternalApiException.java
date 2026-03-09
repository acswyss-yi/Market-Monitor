package com.market.monitor.exception;

public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String api, String detail) {
        super("External API error [" + api + "]: " + detail);
    }
}