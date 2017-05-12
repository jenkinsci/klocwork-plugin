/**
 * *****************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Loic Quentin                                                        *
 *		                                                                        *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *                                                                              *
 ******************************************************************************
 */
package com.thalesgroup.hudson.plugins.klocwork.parser;

import com.thalesgroup.hudson.plugins.klocwork.model.*;
import hudson.FilePath;
import hudson.model.BuildListener;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class KloParser implements Serializable {

    private static final long serialVersionUID = 1L;

    private FilePath resultFilePath;

    public KloParser() {
        resultFilePath = null;
//        kloFiles = new ArrayList<KloFile>();
    }

    @SuppressWarnings("unused")
    public FilePath getResultFilePath() {
        return resultFilePath;
    }

    public KloReport parse(final File file, final BuildListener listener, boolean use96up) throws IOException {

        if (file == null) {
            throw new IllegalArgumentException("File input is mandatory.");
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File input " + file.getName() + " must exist.");
        }

        KloReport report = new KloReport();

        try (FileInputStream resultsFileStream = new FileInputStream(file.getAbsolutePath())) {

            double numFixed = 0.0;
            double numExisting = 0.0;
            double numNew = 0.0;
            int numCrit = 0;
            int numErr = 0;
            int numWarn = 0;
            int numRev = 0;
            int totalNumCrit = 0;
            int totalNumErr = 0;
            int totalNumWarn = 0;
            int totalNumRev = 0;
            int highSeverities = 0;
            int lowSeverities = 0;
            int errors = 0;

            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document
                    = builder.parse(resultsFileStream);
            NodeList nodeList = document.getDocumentElement().getChildNodes();

            for (int i = 0; i < nodeList.getLength(); i++) {
                //We have encountered an <employee> tag.
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    NodeList childNodes = node.getChildNodes();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node cNode = childNodes.item(j);
                        //Identifying the child tag of employee encountered.
                        if (cNode instanceof Element) {
                            String content = cNode.getLastChild().
                                    getTextContent().trim();
                            switch (cNode.getNodeName()) {
                                case "severitylevel":
                                    switch (content) {
                                        case "1":
                                            totalNumCrit++;
                                            lowSeverities++;
                                            break;
                                        case "2":
                                            totalNumErr++;
                                            lowSeverities++;
                                            break;
                                        case "3":
                                            totalNumWarn++;
                                            highSeverities++;
                                            break;
                                        case "4":
                                            totalNumRev++;
                                            highSeverities++;
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                                case "state":
                                    errors++;
                                    switch (content) {
                                        case "New":
                                            numNew++;
                                            for (int h = 0; h < childNodes.getLength(); h++) {
                                                Node tempNode = childNodes.item(h);
                                                if (tempNode instanceof Element) {
                                                    if(tempNode.getNodeName().equalsIgnoreCase("severitylevel")){
                                                        String tNodeContent = tempNode.getLastChild().getTextContent().trim();
                                                        switch (tNodeContent){
                                                            case "1":
                                                                numCrit++;
                                                                break;
                                                            case "2":
                                                                numErr++;
                                                                break;
                                                            case "3":
                                                                numWarn++;
                                                                break;
                                                            case "4":
                                                                numRev++;
                                                                break;
                                                            default:
                                                                break;
                                                        }
                                                    }
                                                }
                                            }
                                            break;
                                        case "Existing":
                                            numExisting++;
                                            break;
                                        case "Fixed":
                                            numFixed++;
                                            break;
                                        default:
                                            break;
                                    }
                                    break;
                            }
                        }
                    }
                }
            }

            report.setLowSeverities(lowSeverities);
            report.setHighSeverities(highSeverities);
            report.setErrors(errors);

            report.setFixed(numFixed);
            report.setExisting(numExisting);
            report.setNeww(numNew);

            report.setNumCrit(numCrit);
            report.setNumErr(numErr);
            report.setNumWarn(numWarn);
            report.setNumRev(numRev);

            report.setTotalNumCrit(totalNumCrit);
            report.setTotalNumErr(totalNumErr);
            report.setTotalNumWarn(totalNumWarn);
            report.setTotalNumRev(totalNumRev);

        } catch (SAXException | ParserConfigurationException ex) {
            Logger.getLogger(KloParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return report;
    }

}
