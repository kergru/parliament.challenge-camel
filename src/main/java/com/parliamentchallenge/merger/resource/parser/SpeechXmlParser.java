package com.parliamentchallenge.merger.resource.parser;

import com.parliamentchallenge.merger.resource.model.Speech;
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

public class SpeechXmlParser {

    private XPath xpath = XPathFactory.newInstance().newXPath();

    public Speech parseToSpeech(String speechXml) throws Exception {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlSpeech = builder.parse(toInputStream(speechXml));
        return parseToSpeech(xmlSpeech);
    }

    private Speech parseToSpeech(Document xmlSpeech) throws XPathExpressionException {
        Speech speech = new Speech();
        speech.setUid(getSpeechId(xmlSpeech));
        speech.setSpeechDate(getSpeechDate(xmlSpeech));
        speech.setSubject(getSpeechSubject(xmlSpeech));
        return speech;
    }

    private String evaluate(Document xml, XPathExpression expr) throws XPathExpressionException {
        Object value = expr.evaluate(xml, STRING);
        return Objects.toString(value);
    }

    private String getSpeechSubject(Document xmlSpeech) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/anforande/avsnittsrubrik/text()");
        return evaluate(xmlSpeech, expr);
    }

    private String getSpeechDate(Document xmlSpeech) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/anforande/dok_datum/text()");
        return evaluate(xmlSpeech, expr);
    }

    private String getSpeechId(Document xmlSpeech) throws XPathExpressionException {
        XPathExpression expr = xpath.compile("/anforande/dok_id/text()");
        String dokId = evaluate(xmlSpeech, expr);
        XPathExpression expr2 = xpath.compile("/anforande/anforande_nummer/text()");
        String anforandeNummer = evaluate(xmlSpeech, expr2);
        return dokId + "-" + anforandeNummer;
    }
}
