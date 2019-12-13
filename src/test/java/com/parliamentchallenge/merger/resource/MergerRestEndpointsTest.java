package com.parliamentchallenge.merger.resource;

import com.parliamentchallenge.merger.resource.model.SpeechesList;
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

/**
 * Real unit test, no spring context required
 */
public class MergerRestEndpointsTest extends CamelTestSupport {

    private FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new MergerRestEndpoints();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        Resource anforandelista = resourceLoader.getResource("classpath:mocks/anforandelista.xml");
        AdviceWithRouteBuilder.adviceWith(this.context, "direct:speeches", a -> {
            a.interceptSendToEndpoint("http://data.riksdagen.se/anforandelista/?bridgeEndpoint=true")
                    .skipSendToOriginalEndpoint()
                    .to("mock:anforandelista").process(exchange -> {
                exchange.getOut().setBody(anforandelista.getInputStream());
            });
        });

        Resource personlista = resourceLoader.getResource("classpath:mocks/personlista.xml");
        AdviceWithRouteBuilder.adviceWith(this.context, "direct:speaker", a -> {
            a.interceptSendToEndpoint("http://data.riksdagen.se/personlista/?bridgeEndpoint=true")
                    .skipSendToOriginalEndpoint()
                    .to("mock:personlista").process(exchange -> {
                exchange.getOut().setBody(personlista.getInputStream());
            });
        });
    }

    @Test
    public void speeches_route_should_return_speeches() throws InterruptedException {
        //given
        Exchange exchange = new DefaultExchange(this.context);

        MockEndpoint mockEndpointAnforandelista = getMockEndpoint("mock:anforandelista");
        mockEndpointAnforandelista.setExpectedCount(1);

        MockEndpoint mockEndpointPersonlista = getMockEndpoint("mock:personlista");
        mockEndpointPersonlista.setExpectedCount(2);

        //when
        template.send("direct:speeches", exchange);

        //then
        SpeechesList speeches = exchange.getIn().getBody(SpeechesList.class);
        assertNotNull(speeches);
        assertCollectionSize(speeches.getSpeeches(), 2);

        mockEndpointAnforandelista.assertIsSatisfied();
        mockEndpointPersonlista.assertIsSatisfied();
    }
}