package com.company.app.config.client.tcp.device;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class DeviceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeviceAdapter.class);

    public String sendMsg(String msg) throws DeviceAdapterException {
        return communicateWithDevice(msg);
    }

    private String communicateWithDevice(String msg) throws DeviceAdapterException {
        byte[] rawMsg = msg.getBytes(StandardCharsets.UTF_8);
        long startTime = System.currentTimeMillis();
        try {
            byte[] rawResponse = Objects.requireNonNull(communicateWithDevice(rawMsg), "Null response");
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.info("successful communication with the device - took {} milliseconds", elapsedTime);
            return new String(rawResponse, StandardCharsets.UTF_8);

        } catch (Exception e) {
            long elapsedTime = System.currentTimeMillis() - startTime;
            logger.error("failed communication with the device - took {}} milliseconds", elapsedTime, e);
            throw new DeviceAdapterException("device communication error", e);
        }
    }

    protected abstract byte[] communicateWithDevice(byte[] msg);

    public static class DeviceAdapterException extends Exception {
        public DeviceAdapterException(String message) {
            super(message);
        }

        public DeviceAdapterException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
