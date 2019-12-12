package com.parliamentchallenge.merger.resource;

import com.parliamentchallenge.merger.resource.model.Speech;
import com.parliamentchallenge.merger.resource.model.SpeechesList;
import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;

import java.util.List;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Aggregate all speeches and wrap it in {@link SpeechesList}
 */
public class CreateSpeechesListAggregationStrategy extends AbstractListAggregationStrategy<Object> {

    @Override
    public void onCompletion(Exchange exchange) {
        super.onCompletion(exchange);
        List<Speech> body = (List<Speech>) exchange.getIn().getBody();
        exchange.getIn().setBody(new SpeechesList(body));
        exchange.getIn().setHeader(CONTENT_TYPE, APPLICATION_JSON);
    }

    @Override
    public Object getValue(Exchange exchange) {
        return exchange.getIn().getBody();
    }
}
