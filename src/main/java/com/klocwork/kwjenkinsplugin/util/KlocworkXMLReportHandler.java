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

import com.klocwork.kwjenkinsplugin.definitions.KlocworkIssue;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkSeverities;
import com.klocwork.kwjenkinsplugin.definitions.KlocworkStatuses;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Stack;



public class KlocworkXMLReportHandler extends DefaultHandler  {

    private int totalIssueCount = 0;
    private ArrayList<KlocworkIssue> issuesList = new ArrayList<>();
    private Stack<String> elementStack = new Stack<String>();
    private StringBuilder element = new StringBuilder();
    private KlocworkIssue issue;
    private boolean enableHTMLReport;
    private final KlocworkSeverities enabledSeverites;
    private final KlocworkStatuses enabledStatuses;

    public KlocworkXMLReportHandler(boolean enableHTMLReport, KlocworkSeverities enabledSeverites, KlocworkStatuses enabledStatuses){
        this.enableHTMLReport = enableHTMLReport;
        this.enabledSeverites = enabledSeverites;
        this.enabledStatuses = enabledStatuses;
    }

    public void startElement(String uri, String localName,
        String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(qName);
        if (qName.equalsIgnoreCase("problem")) {
            issue = new KlocworkIssue();
        }
        element.delete(0, element.length());
    }

    public void endElement(String uri, String localName,
        String qName) throws SAXException {
        switch (qName.toLowerCase()) {
            case "problemid":
                issue.setId(element.toString());
                break;
            case "code":
                issue.setCode(element.toString());
                break;
            case "file":
                issue.setFile(element.toString());
                break;
            case "line":
                issue.setLine(element.toString());
                break;
            case "message":
                issue.setMessage(element.toString());
                break;
            case "severity":
                issue.setSeverity(element.toString());
                break;
            case "severitylevel":
                issue.setSeverityCode(element.toString());
            	break;
            case "citingstatus":
                issue.setStatus(element.toString());
                break;
            case "problem":
                if(((enabledSeverites.getEnabled().get("fiveToTen") && 10 - Integer.parseInt(issue.getSeverityCode()) < 6 )
                        || enabledSeverites.getEnabled().get(getSeverity_en(Integer.parseInt(issue.getSeverityCode()))))
                        && enabledStatuses.getEnabled().get(issue.getStatus().toLowerCase())) {
                    this.totalIssueCount++;
                    if (enableHTMLReport) {
                        issuesList.add(issue);
                    }
                }
                issue = null;
                break;
            default:
                break;
        }
        this.elementStack.pop();
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {
        String text = new String(ch, start, length);
        if (!text.trim().isEmpty()) {
            element.append(text);
        }
    }
    
    private String getSeverity_en(int severityLevel) {
    	String severity_en = "";
    	switch(severityLevel) {
    	case 1:
    		severity_en = "critical";
    		break;
    	case 2:
    		severity_en = "error";
    		break;
    	case 3:
    		severity_en = "warning";
    		break;
    	case 4:
    		severity_en = "review";
    		break;
    	case 5:
    	case 6:
    	case 7:
    	case 8:
    	case 9:
    	case 10:
    		severity_en = "fiveToTen";
    		break;
    	}
    	return severity_en;
    }

    private String currentElement() {
        return this.elementStack.peek();
    }

    private String currentElementParent() {
        if(this.elementStack.size() < 2) return null;
        return this.elementStack.get(this.elementStack.size()-2);
    }

    public int getTotalIssueCount() {
        return totalIssueCount;
    }

    public ArrayList<KlocworkIssue> getIssuesList() {
        return issuesList;
    }
}
