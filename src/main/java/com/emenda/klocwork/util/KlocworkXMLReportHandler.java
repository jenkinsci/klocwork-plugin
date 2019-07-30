package com.emenda.klocwork.util;

import com.emenda.klocwork.definitions.KlocworkIssue;
import com.emenda.klocwork.definitions.KlocworkSeverities;
import com.emenda.klocwork.definitions.KlocworkStatuses;
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
            	issue.setSeveritylevel(element.toString());
            	break;
            case "citingstatus":
                issue.setStatus(element.toString());
                break;
            case "problem":
            	if(((enabledSeverites.getEnabled().get("fiveToTen") && 10 - Integer.parseInt(issue.getSeveritylevel()) < 6 )
                        || enabledSeverites.getEnabled().get(getSeverity_en(Integer.parseInt(issue.getSeveritylevel()))))
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
