package com.parliamentchallenge.merger.resource.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
@ApiModel
public class Speaker {

    @ApiModelProperty("Lastname of speaker")
    String name;

    @ApiModelProperty("Speakers' political affiliation")
    String politicalAffiliation;

    @ApiModelProperty("Speakers' official email address")
    String email;

    @ApiModelProperty("Speakers' constituency")
    String constituency;

    @ApiModelProperty("Image of speaker")
    String imageUrl;
}
