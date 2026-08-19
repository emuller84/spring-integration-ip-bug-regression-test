package com.company.app.data.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EndpointAResponse {
    @JsonProperty("deviceResponseValue")
    private String deviceResponseValue;

    @JsonProperty("deviceError")
    private String deviceError;

    @JsonProperty("otherError")
    private String otherError;

}
