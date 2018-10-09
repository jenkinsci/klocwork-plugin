package com.emenda.klocwork.util;

import hudson.AbortException;
import jenkins.security.MasterToSlaveCallable;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

;

public class KlocworkXMLReportParserIssueList extends MasterToSlaveCallable<ArrayList<KlocworkIssue>,IOException>  {

    private final String workspace;
    private final String xmlReport;

    public KlocworkXMLReportParserIssueList(String workspace, String xmlReport) {
        this.workspace = workspace;
        this.xmlReport = xmlReport;
    }

    public ArrayList<KlocworkIssue> call() throws IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
			//We must handle both relative and absolute paths
			InputStream xmlInput = null;
			if (Paths.get(xmlReport).isAbsolute()) {
				xmlInput = new FileInputStream(new File(xmlReport));
			}
			else {
				xmlInput = new FileInputStream(new File(workspace, xmlReport));
			}

			InputSource inputSource = new InputSource(new InputStreamReader(xmlInput, "UTF-8"));
			inputSource.setEncoding("UTF-8");

			SAXParser saxParser = factory.newSAXParser();
			KlocworkXMLReportHandler handler = new KlocworkXMLReportHandler(true);
			saxParser.parse(inputSource, handler);
			return handler.getIssuesList();

       } catch (ParserConfigurationException | SAXException ex) {
           throw new AbortException(ex.getMessage());
        }
    }
}
