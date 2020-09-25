package com.klocwork.kwjenkinsplugin.util;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

public class KlocworkXMLUtil {

    /**
     * Initializes XML parser factory with security features to prevent XXE vulnerability
     *
     * @return
     * @throws SAXNotSupportedException
     * @throws SAXNotRecognizedException
     * @throws ParserConfigurationException
     */
    public static SAXParserFactory getSecureXmlParserFactory() throws SAXNotSupportedException, SAXNotRecognizedException, ParserConfigurationException {
        SAXParserFactory factory = SAXParserFactory.newInstance();

        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);

        return factory;
    }

}
