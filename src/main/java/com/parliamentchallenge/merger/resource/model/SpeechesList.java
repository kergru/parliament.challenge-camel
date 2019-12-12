package com.parliamentchallenge.merger.resource.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@ApiModel
public class SpeechesList {

    public static final SpeechesList EMPTY = new SpeechesList(Collections.emptyList());

    @ApiModelProperty("List of speeches")
    private final List<Speech> speeches;
}
