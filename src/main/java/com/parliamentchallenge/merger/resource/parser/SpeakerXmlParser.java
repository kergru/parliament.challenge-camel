package com.parliamentchallenge.merger.resource.parser;

import com.parliamentchallenge.merger.resource.model.Speaker;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.Objects;

import static javax.xml.xpath.XPathConstants.STRING;
import static org.apache.commons.io.IOUtils.toInputStream;

public class SpeakerXmlParser {

    private XPath xpath = XPathFactory.newInstance().newXPath();

    public Speaker parseToSpeaker(String speakerXml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlSpeaker = builder.parse(toInputStream(speakerXml));
        return parseToSpeaker(xmlSpeaker);
    }

    private Speaker parseToSpeaker(Document xmlSpeaker) throws XPathExpressionException {
        Speaker speaker = new Speaker();
        speaker.setName(getName(xmlSpeaker));
        speaker.setConstituency(getConstituency(xmlSpeaker));
        speaker.setEmail(getEmail(xmlSpeaker));
        speaker.setPoliticalAffiliation(getPoliticalAffiliation(xmlSpeaker));
        speaker.setImageUrl(getImageUrl(xmlSpeaker));
        return speaker;
    }

    private String evaluate(Document xml, XPathExpression expr) throws XPathExpressionException {
        Object value = expr.evaluate(xml, STRING);
        return Objects.toString(value);
    }

    private String getImageUrl(Document xmlSpeaker) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/personlista/person/bild_url_80/text()");
        return evaluate(xmlSpeaker, expr);
    }

    private String getPoliticalAffiliation(Document xmlSpeaker) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/personlista/person/parti/text()");
        return evaluate(xmlSpeaker, expr);
    }

    private String getEmail(Document xmlSpeaker) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("personlista/person/personuppgift/uppgift[contains(kod,'Officiell e-postadress')]/uppgift/text()");
        return evaluate(xmlSpeaker, expr);
    }

    private String getConstituency(Document xmlSpeaker) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/personlista/person/valkrets/text()");
        return evaluate(xmlSpeaker, expr);
    }

    private String getName(Document xmlSpeaker) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/personlista/person/efternamn/text()");
        return evaluate(xmlSpeaker, expr);
    }
}
