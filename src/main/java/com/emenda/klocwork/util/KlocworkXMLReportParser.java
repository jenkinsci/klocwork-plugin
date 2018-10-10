package com.emenda.klocwork.util;

import com.emenda.klocwork.definitions.KlocworkSeverities;
import com.emenda.klocwork.definitions.KlocworkStatuses;
import hudson.AbortException;
import jenkins.security.MasterToSlaveCallable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

;

public class KlocworkXMLReportParser extends MasterToSlaveCallable<Integer,IOException>  {

    private final String workspace;
    private final String xmlReport;
    private final KlocworkSeverities enabledSeverites;
    private final KlocworkStatuses enabledStatuses;

    public KlocworkXMLReportParser(String workspace, String xmlReport, KlocworkSeverities enabledSeverites, KlocworkStatuses enabledStatuses) {
        this.workspace = workspace;
        this.xmlReport = xmlReport;
        this.enabledSeverites = enabledSeverites;
        this.enabledStatuses = enabledStatuses;
    }

    public Integer call() throws IOException {
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
			KlocworkXMLReportHandler handler = new KlocworkXMLReportHandler(false, enabledSeverites, enabledStatuses);
			saxParser.parse(inputSource, handler);

			return handler.getTotalIssueCount();

       } catch (ParserConfigurationException | SAXException ex) {
           throw new AbortException(ex.getMessage());
        }
    }
}
