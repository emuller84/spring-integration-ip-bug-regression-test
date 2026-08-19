package com.company.app.data.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
public class EndpointARequest {
    @NotBlank
    @JsonProperty("value")
    private String value;
}
