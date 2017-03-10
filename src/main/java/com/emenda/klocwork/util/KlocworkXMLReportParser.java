package com.emenda.klocwork.util;

import com.emenda.klocwork.KlocworkConstants;

import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;



import hudson.AbortException;;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;

public class KlocworkXMLReportParser extends MasterToSlaveCallable<Integer,IOException>  {

    private final String workspace;
    private final String xmlReport;

    public KlocworkXMLReportParser(String workspace, String xmlReport) {
        this.workspace = workspace;
        this.xmlReport = xmlReport;
    }

    public Integer call() throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
           InputStream xmlInput = new FileInputStream(new File(workspace, xmlReport));

           SAXParser saxParser = factory.newSAXParser();
           KlocworkXMLReportHandler handler = new KlocworkXMLReportHandler();
           saxParser.parse(xmlInput, handler);

           return handler.getTotalIssueCount();

       } catch (ParserConfigurationException | SAXException ex) {
           throw new AbortException(ex.getMessage());
        }
    }
}
