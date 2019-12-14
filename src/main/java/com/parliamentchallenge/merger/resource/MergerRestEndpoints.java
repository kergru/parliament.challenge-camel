package com.parliamentchallenge.merger.resource;

import com.parliamentchallenge.merger.resource.model.Speech;
import com.parliamentchallenge.merger.resource.model.SpeechesList;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.camel.Exchange.HTTP_QUERY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Endpoint definitions for Speeches-Speaker-Merger API
 */
@Component
class MergerRestEndpoints extends RouteBuilder {


    @Override
    public void configure() {

        rest().description("Speeches-Speaker-Merger API")
                .id("api-speeches").skipBindingOnErrorCode(true)

                /*REST-API REQUESTS:*/
                .get("/speeches")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .param().name("party").type(RestParamType.query).description("Search speeches by party.").required(false).dataType("string").endParam()
                .param().name("memberId").type(RestParamType.query).description("Search speeches by speaker").required(false).dataType("string").endParam()
                .param().name("parliamentarySession").type(RestParamType.query).description("Search speeches by parliament session").required(false).dataType("string in format yyyy/MM").endParam()
                .responseMessage().code(200).responseModel(SpeechesList.class).endResponseMessage() //OK
                .description("Get speeches and speaker from Parliament API and merged it to single document")
                .to("direct:speeches")

                .get("/speeches/{speechid}")
                .produces(MediaType.APPLICATION_JSON_VALUE)
                .param().name("speechid").type(RestParamType.path).description("Speeches id.").required(false).dataType("string").endParam()
                .responseMessage().code(200).responseModel(Speech.class).endResponseMessage() //OK
                .description("Get single speech and speaker from Parliament API and merged it to single document")
                .to("direct:speech");

        from("direct:speeches").routeId("direct:speeches")
                .description("Get speeches from Parliament API")
                .process(this::translateQueryParams)
                .to("http://data.riksdagen.se/anforandelista/?bridgeEndpoint=true")
                .convertBodyTo(String.class)
                .to("direct:speeches-splitter");

        from("direct:speech").routeId("direct:speech")
                .description("Get speech from Parliament API")
                .toD("http://data.riksdagen.se/anforande/${header.speechid}/?bridgeEndpoint=true")
                .convertBodyTo(String.class)
                .to("direct:enrich");

        from("direct:speeches-splitter").routeId("direct:speeches-splitter")
                .description("Split speeches response and enrich each with speaker")
                .choice()
                .when().xpath("/anforandelista/@antal != '0'")
                .to("direct:split-enrich-merge")
                .otherwise()
                .setBody(constant(SpeechesList.EMPTY)).setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
                .end();

        from("direct:split-enrich-merge").routeId("direct:split-enrich-merge")
                .split(xpath("/anforandelista/anforande"), new CreateSpeechesListAggregationStrategy())
                .parallelProcessing()
                .to("direct:enrich");

        from("direct:enrich").routeId("direct:enrich")
                .enrich("direct:speaker", new CreateSpeechAggregationStrategy());

        from("direct:speaker").routeId("direct:speaker")
                .description("Get speaker from Parliament API")
                .setHeader("intressent_id")
                .xpath("/anforande/intressent_id/text()")
                .setHeader(Exchange.HTTP_QUERY, simple("sz=1&iid=${header.intressent_id}"))
                .setBody(simple(null))
                .to("http://data.riksdagen.se/personlista/?bridgeEndpoint=true")
                .convertBodyTo(String.class);
    }

    private void translateQueryParams(Exchange exchange) {
        StringBuilder query = new StringBuilder("sz=10");
        Object party = exchange.getIn().removeHeader("party");
        if (party != null) {
            query.append("&parti=" + party);
        }
        Object memberId = exchange.getIn().removeHeader("memberId");
        if (memberId != null) {
            query.append("&iid=" + memberId);
        }
        Object parliamentarySession = exchange.getIn().removeHeader("parliamentarySession");
        if (parliamentarySession != null) {
            query.append("&rm=" + parliamentarySession);
        }
        exchange.getIn().setHeader(HTTP_QUERY, query.toString());
    }
}