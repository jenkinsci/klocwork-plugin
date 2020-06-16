/*
 * *****************************************************************************
 * Copyright (c) 2020 Rogue Wave Software, Inc., a Perforce company
 * Author : Klocwork
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * *****************************************************************************
 */

package com.klocwork.kwjenkinsplugin.util;

import com.klocwork.kwjenkinsplugin.definitions.KlocworkSeverities;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkStatuses;
import hudson.AbortException;
import jenkins.security.MasterToSlaveCallable;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.file.Paths;

public class KlocworkXMLReportParser extends MasterToSlaveCallable<Integer,IOException> implements Serializable {

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
