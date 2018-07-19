package com.emenda.klocwork.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Stack;



public class KlocworkXMLReportHandler extends DefaultHandler  {

    private int totalIssueCount = 0;

    private Stack<String> elementStack = new Stack<String>();

    public void startElement(String uri, String localName,
        String qName, Attributes attributes) throws SAXException {
        this.elementStack.push(qName);
        if (qName.equalsIgnoreCase("problem")) {
            this.totalIssueCount++;
        }

    }

    public void endElement(String uri, String localName,
        String qName) throws SAXException {
        this.elementStack.pop();
    }

    public void characters(char ch[], int start, int length)
        throws SAXException {

        // nothing needed here yet. Can use currentElement() to get the current
        // element when needed
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
}
