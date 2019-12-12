package com.parliamentchallenge.merger.resource;

import com.parliamentchallenge.merger.resource.model.Speaker;
import com.parliamentchallenge.merger.resource.model.Speech;
import com.parliamentchallenge.merger.resource.parser.SpeakerXmlParser;
import com.parliamentchallenge.merger.resource.parser.SpeechXmlParser;
import lombok.SneakyThrows;
import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.springframework.hateoas.Link;

/**
 * Merges speech and speaker responses
 */
class CreateSpeechAggregationStrategy implements AggregationStrategy {

    private SpeechXmlParser speechXmlParser = new SpeechXmlParser();

    private SpeakerXmlParser speakerXmlParser = new SpeakerXmlParser();

    @SneakyThrows
    @Override
    public Exchange aggregate(Exchange speechExchange, Exchange speakerExchange) {
        Speech speech = speechXmlParser.parseToSpeech(speechExchange.getIn().getBody(String.class));
        Link link = new Link("http://data.riksdagen.se/anforandelista/anforande/" + speech.getUid(), Link.REL_SELF);
        speech.add(link);
        Speaker speaker = speakerXmlParser.parseToSpeaker(speakerExchange.getIn().getBody(String.class));
        speech.setSpeaker(speaker);
        speechExchange.getIn().setBody(speech);
        return speechExchange;
    }
}
