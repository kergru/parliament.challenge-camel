package com.parliamentchallenge.merger.resource;

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

        rest("/speeches").description("Speeches-Speaker-Merger API")
                .produces(MediaType.APPLICATION_JSON_VALUE).consumes(MediaType.APPLICATION_JSON_VALUE)
                .skipBindingOnErrorCode(false)
                .id("api-speeches")

                /*REST-API REQUESTS:*/
                .get()
                .param().name("party").type(RestParamType.query).description("Search speeches by party.").required(false).dataType("string").endParam()
                .param().name("memberId").type(RestParamType.query).description("Search speeches by speaker").required(false).dataType("string").endParam()
                .param().name("parliamentarySession").type(RestParamType.query).description("Search speeches by parliament session").required(false).dataType("string in format yyyy/MM").endParam()
                .responseMessage().code(200).responseModel(SpeechesList.class).endResponseMessage() //OK
                .description("Get speeches and speaker from Parliament API and merged it to single document")
                .to("direct:speeches");

        from("direct:speeches").routeId("parliament-api-speeches")
                .description("Get speeches from Parliament API")
                .process(exchange -> {
                    translateQueryParams(exchange);
                })
                .to("http://data.riksdagen.se/anforandelista/?bridgeEndpoint=true")
                .convertBodyTo(String.class)
                .to("direct:speeches-splitter");

        from("direct:speeches-splitter")
                .description("Split speeches response and enrich each with speaker")
                .choice()
                .when().xpath("/anforandelista/@antal != '0'")
                .to("direct:split")
                .otherwise()
                .setBody(constant(SpeechesList.EMPTY)).setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
                .end();

        from("direct:split")
                .split(xpath("/anforandelista/anforande"), new CreateSpeechesListAggregationStrategy())
                .parallelProcessing()
                .enrich("direct:speaker", new CreateSpeechAggregationStrategy())
                .end();

        from("direct:speaker").
                routeId("parliament-api-speaker")
                .description("Get speaker from Parliament API")
                .setHeader("intressent_id")
                .xpath("/anforande/intressent_id/text()")
                .setHeader(Exchange.HTTP_QUERY, simple("sz=1&iid=${header.intressent_id}"))
                .setBody(simple(null))
                .to("http://data.riksdagen.se/personlista/?bridgeEndpoint=true")
                .convertBodyTo(String.class);
    }

    private void translateQueryParams(Exchange exchange) {
        StringBuffer query = new StringBuffer("sz=10");
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