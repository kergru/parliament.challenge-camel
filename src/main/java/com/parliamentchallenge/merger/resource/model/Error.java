package com.parliamentchallenge.merger.resource.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

@Value
@ApiModel
public class Error {
    @ApiModelProperty("Error message")
    private final String errorMessage;
}
