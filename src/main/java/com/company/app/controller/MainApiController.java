package com.company.app.controller;

import com.company.app.config.client.tcp.device.DeviceAdapter;
import com.company.app.data.message.EndpointARequest;
import com.company.app.data.message.EndpointAResponse;
import jakarta.validation.Valid;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainApiController {
    private final DeviceAdapter deviceAdapter;

    public MainApiController(DeviceAdapter deviceAdapter) {
        this.deviceAdapter = deviceAdapter;
    }

    @PostMapping(value = "/api/endpointA", produces = {"application/json"})
    public ResponseEntity<EndpointAResponse> endpointA(@RequestBody @Valid EndpointARequest request) {
        try {
            String deviceResponse = deviceAdapter.sendMsg(request.getValue());
            var response = EndpointAResponse.builder()
                .deviceResponseValue(deviceResponse)
                .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (DeviceAdapter.DeviceAdapterException e) {
            var response = EndpointAResponse.builder()
                .deviceError(ExceptionUtils.getStackTrace(e))
                .build();
            return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
        } catch (RuntimeException e) {
            var response = EndpointAResponse.builder()
                .otherError(ExceptionUtils.getStackTrace(e))
                .build();
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
