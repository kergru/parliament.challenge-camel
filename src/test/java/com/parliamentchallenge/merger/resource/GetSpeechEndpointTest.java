package com.parliamentchallenge.merger.resource;

import com.parliamentchallenge.merger.resource.model.Speech;
import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

import static org.apache.camel.builder.SimpleBuilder.simple;

/**
 * Real unit test, no spring context required
 */
public class GetSpeechEndpointTest extends CamelTestSupport {

    private FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new MergerRestEndpoints();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Resource anforande = resourceLoader.getResource("classpath:mocks/anforande.xml");
        AdviceWithRouteBuilder.adviceWith(this.context, "direct:speech", a -> {
            a.interceptSendToEndpoint("http://data.riksdagen.se/anforande/*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:anforande").process(exchange -> exchange.getOut().setBody(anforande.getInputStream()))
                    .when(simple("${header.speechid} == 'H70949-226'"));
        });

        Resource personlista = resourceLoader.getResource("classpath:mocks/personlista.xml");
        AdviceWithRouteBuilder.adviceWith(this.context, "direct:speaker", a -> {
            a.interceptSendToEndpoint("http://data.riksdagen.se/personlista/*")
                    .skipSendToOriginalEndpoint()
                    .to("mock:personlista").process(exchange -> exchange.getOut().setBody(personlista.getInputStream()))
                    .when(simple("${header.iid} == '123'"));
        });
    }

    @Test
    public void speeches_route_should_return_speeches() throws InterruptedException {
        //given
        Exchange exchange = new DefaultExchange(this.context);
        exchange.getIn().setHeader("speechid", "H70949-226");

        MockEndpoint mockEndpointAnforande = getMockEndpoint("mock:anforande");
        mockEndpointAnforande.setExpectedCount(1);

        MockEndpoint mockEndpointPersonlista = getMockEndpoint("mock:personlista");
        mockEndpointPersonlista.setExpectedCount(1);

        //when
        template.send("direct:speech", exchange);

        //then
        Speech speech = exchange.getIn().getBody(Speech.class);
        assertNotNull(speech);
        assertNotNull(speech.getSpeaker());

        mockEndpointAnforande.assertIsSatisfied();
        mockEndpointPersonlista.assertIsSatisfied();
    }
}