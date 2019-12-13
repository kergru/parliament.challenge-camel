package com.parliamentchallenge.merger.resource;

import org.apache.camel.Exchange;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;

/**
 * Unittest for single route direct:split
 */
public class SplitSpeechesRouteTest extends CamelTestSupport {
    private FileSystemResourceLoader resourceLoader = new FileSystemResourceLoader();

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new MergerRestEndpoints();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        AdviceWithRouteBuilder.adviceWith(this.context, "direct:split-enrich-merge", a -> {
            a.interceptSendToEndpoint("direct:enrich")
                    .skipSendToOriginalEndpoint()
                    .to("mock:result");
        });
    }


    @Test
    public void route_should_split_speeches_xml_into_speech_xmls() throws Exception {
        MockEndpoint resultEndpoint = resolveMandatoryEndpoint("mock:result", MockEndpoint.class);
        resultEndpoint.expectedMessageCount(2);
        Resource anforandelista = resourceLoader.getResource("classpath:mocks/anforandelista.xml");
        Exchange exchange = new DefaultExchange(this.context);
        exchange.getIn().setBody(IOUtils.toString(anforandelista.getInputStream()));

        template.send("direct:split-enrich-merge", exchange);

        resultEndpoint.assertIsSatisfied();
    }
}
