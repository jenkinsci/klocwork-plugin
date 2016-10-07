/*******************************************************************************
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
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork.parser;

import com.thalesgroup.dtkit.util.validator.ValidationError;
import com.thalesgroup.hudson.plugins.klocwork.model.*;
import hudson.FilePath;
import hudson.model.BuildListener;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KloParser implements Serializable {

    private static final long serialVersionUID = 1L;

    private FilePath resultFilePath;
    private ArrayList<KloFile> kloFiles;
    Map<Integer, KloFile> agregateMap = new HashMap<Integer, KloFile>();

    public KloParser() {
        resultFilePath = null;
        kloFiles = new ArrayList<KloFile>();
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

        List<ValidationError> list = KlocworkModel.OUTPUT_KLOCWORK_9_2.validate(file);
        if (!list.isEmpty()) {
            StringBuilder sb = new StringBuilder("XML Validation failed. See errors below :\n");
            for (ValidationError val : list) {
                sb.append(val.toString()).append("\n");
            }
            throw new IllegalArgumentException(sb.toString());
        }


        try {

            ErrorList errList = getErrorList(file);
            List<KloFile> lowSeverities = new ArrayList<KloFile>();
            List<KloFile> highSeverities = new ArrayList<KloFile>();
            List<KloFile> errors = new ArrayList<KloFile>();

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

            String kloVersion = null;
            try {
                kloVersion = errList.getVersion();
            } catch (Exception e) {

            }
            
            int severityDelimiter = 3; // default
            // Version 9.5 Klocwork changed issue severity levels
            // TODO: Improve maintainability of code by removing version-dependency here
            if (
                    (kloVersion != null && (kloVersion.startsWith("9.5") || kloVersion.startsWith("9.6")|| kloVersion.startsWith("10.0")|| kloVersion.startsWith("10.1")))
                    || use96up
                    ) {
                severityDelimiter = 2;
            }
           
            int i = 0;
            for (Problem problem : errList.getProblem()) {
                KloFile kloFile;
                kloFile = new KloFile();
                kloFile.setKey(i + 1);
                /**
                 * Using reflection to get the tags' name and value and to put them in kloFile map
                 */
                for (Field f : problem.getClass().getDeclaredFields()) {
                    f.setAccessible(true);
                    try {
                        String name = f.getName();
                        Object value = f.get(problem);
                        if (value != null) {
                            String valueToString = value.toString();
                            //Changing the default value returned by Object.toString() by an empty value
                            if (valueToString.startsWith("com.thalesgroup.hudson.plugins.klocwork.model") && valueToString.contains("@")) {
                                kloFile.store(name, "");
                            } else {
                                kloFile.store(name, valueToString);
                            }

                            //Treating the trace tag
                            if (name.equals("trace")) {
                                Trace trace = (Trace) value;
                                for (TraceBlock tracelt : trace.getTraceBlock()) {

                                    kloFile.addTraceBlock(tracelt.getFile(),
                                            tracelt.getMethod(), tracelt
                                            .getName(), tracelt.getId());


                                    for (TraceLine traceLinelt : tracelt.getTraceLine()) {

                                        //Element traceLinelt = (Element) listTraceLine.get(k);
                                        String refId = null;
                                        if (problem.getRefID() != null && (refId = problem.getRefID().toString()) != null) {
                                            kloFile.addTraceLine(tracelt.getId(),
                                                    traceLinelt.getLine(),
                                                    traceLinelt.getText(),
                                                    traceLinelt.getType().charAt(0), Integer.parseInt(refId));
                                        } else {
                                            kloFile.addTraceLine(tracelt.getId(),
                                                    traceLinelt.getLine(),
                                                    traceLinelt.getText(),
                                                    traceLinelt.getType().charAt(0));
                                        }
                                    }
                                }
                            }


                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                    f.setAccessible(false);
                }
                //AM : adding a href in the message to the corresponding defect in klocwork review
                StringBuilder message = new StringBuilder(kloFile.get("message")).append(" <a href=\"").append(kloFile.get("url")).append("\" target=\"_blank\">Link to defect in Klocwork Review</a>");
                kloFile.store("message", message.toString());

                //Adding a new entry in the map corresponding to the file name without its path
                String fileName = kloFile.get("file");
                String fileNameWithoutPath = extractFileName(fileName, "\\");
                if (fileName.equals(fileNameWithoutPath)) {
                    fileNameWithoutPath = extractFileName(fileName, "/");
                }
                kloFile.store("fileNameOnly", fileNameWithoutPath);
                
                if (kloFile.get("severitylevel") != null) {
                    if (!kloFile.get("state").equalsIgnoreCase("Fixed")) {
                        if (kloFile.get("severitylevel").equals("1")) {
                            totalNumCrit++;
                        } else if (kloFile.get("severitylevel").equals("2")) {
                            totalNumErr++;
                        } else if (kloFile.get("severitylevel").equals("3")) {
                            totalNumWarn++;
                        } else if (kloFile.get("severitylevel").equals("4")) {
                            totalNumRev++;
                        }
                    }
                }

                if (Integer.parseInt((String) kloFile.get("severitylevel")) > severityDelimiter) {
                    highSeverities.add(kloFile);
                } else {
                    lowSeverities.add(kloFile);
                }

                if (kloFile.get("state") != null) {
                    String state = kloFile.get("state");
                    if (state.equalsIgnoreCase("New") || state.equalsIgnoreCase("Recurred")) {
                        numNew++;
                        if(kloFile.get("severitylevel").equals("1")){
                            numCrit++;
                        }
                        else if(kloFile.get("severitylevel").equals("2")){
                            numErr++;
                        }
                        else if(kloFile.get("severitylevel").equals("3")){
                            numWarn++;
                        }
                        else if(kloFile.get("severitylevel").equals("4")){
                            numRev++;
                        }
                    } else if (state.equalsIgnoreCase("Existing")) {
                        numExisting++;
                    } else if (state.equalsIgnoreCase("Fixed") || state.equalsIgnoreCase("Obsolete") ||
                            state.equalsIgnoreCase("Not in scope")) {
                        numFixed++;
                    }
                }

                errors.add(kloFile);

                agregateMap.put(kloFile.getKey(), kloFile);

                kloFiles.add(kloFile);

                i++;
            }

            if (!lowSeverities.isEmpty()) {
                report.setLowSeverities(lowSeverities.size());
            }

            if (!highSeverities.isEmpty()) {
                report.setHighSeverities(highSeverities.size());
            }
            
            report.setErrors(errors.size());
            
            report.setFixed(numFixed);
            report.setExisting(numExisting);
            report.setNeww(numNew);
            report.setKloVersion(kloVersion);
            
            report.setNumCrit(numCrit);
            report.setNumErr(numErr);
            report.setNumWarn(numWarn);
            report.setNumRev(numRev);
            
            report.setTotalNumCrit(totalNumCrit);
            report.setTotalNumErr(totalNumErr);
            report.setTotalNumWarn(totalNumWarn);
            report.setTotalNumRev(totalNumRev);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return report;
    }


    @Override
    public String toString() {

        String ret = "";

        for (int i = 0; i < kloFiles.size(); i++) {
            KloFile kloFile = kloFiles.get(i);
            ret += "Error n " + (i + 1) + " " + kloFile.toString();
        }

        return ret;
    }

    /**
     * Return the name of the given filename without its path
     *
     * @param fileNameWithPath The file name with its path
     * @param separator        The separator uses by the OS for the file system (/ for linux, \ for windows)
     */
    private String extractFileName(String fileNameWithPath, String separator) {
        int lastIndex = fileNameWithPath.lastIndexOf(separator);
        return lastIndex == -1 ? fileNameWithPath : fileNameWithPath.substring(lastIndex + 1);
    }

    private ErrorList getErrorList(File xmlInputStream) throws JAXBException {
        ClassLoader cl = ObjectFactory.class.getClassLoader();
        JAXBContext jc = JAXBContext.newInstance("com.thalesgroup.hudson.plugins.klocwork.model", cl);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (ErrorList) unmarshaller.unmarshal(xmlInputStream);

    }
}
