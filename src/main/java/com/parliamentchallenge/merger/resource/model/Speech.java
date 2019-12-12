package com.parliamentchallenge.merger.resource.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.ResourceSupport;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
@ApiModel
public class Speech extends ResourceSupport {

    @ApiModelProperty("Unique speech id")
    String uid;

    @ApiModelProperty("Date of speech in format: ISO 8601")
    String speechDate;

    @ApiModelProperty("subject of speech")
    String subject;

    @ApiModelProperty("The speaker")
    Speaker speaker;

}
