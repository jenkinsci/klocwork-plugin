package com.emenda.klocwork.util;

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

    public KlocworkXMLReportHandler(boolean enableHTMLReport){ this.enableHTMLReport = enableHTMLReport;}

    public void startElement(String uri, String localName,
        String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(qName);
        if (qName.equalsIgnoreCase("problem")) {
            this.totalIssueCount++;
            if(enableHTMLReport) {
                issue = new KlocworkIssue();
            }
        }
        element.delete(0, element.length());
    }

    public void endElement(String uri, String localName,
        String qName) throws SAXException {
        if(enableHTMLReport) {
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
                case "problem":
                    issuesList.add(issue);
                    issue = null;
                    break;
                default:
                    break;
            }
        }
        this.elementStack.pop();
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {
        if(enableHTMLReport) {
            String text = new String(ch, start, length);
            if (!text.trim().isEmpty()) {
                element.append(text);
            }
        }
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
