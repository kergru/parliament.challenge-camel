package com.parliamentchallenge.merger.resource.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

@Getter
@Setter
@ApiModel
public class Speech extends ResourceSupport {

    @ApiModelProperty("Unique speech id")
    private String uid;

    @ApiModelProperty("Date of speech in format: ISO 8601")
    private String speechDate;

    @ApiModelProperty("subject of speech")
    private String subject;

    @ApiModelProperty("The speaker")
    private Speaker speaker;

}
