/**
 * *****************************************************************************
 * Copyright (c) 2012 Emenda Software Ltd. * Author : Andreas Larfors * *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * * of this software and associated documentation files (the "Software"), to
 * deal* in the Software without restriction, including without limitation the
 * rights * to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell * copies of the Software, and to permit persons to whom the Software is
 * * furnished to do so, subject to the following conditions: * * The above
 * copyright notice and this permission notice shall be included in * all copies
 * or substantial portions of the Software. * * THE SOFTWARE IS PROVIDED "AS
 * IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR * IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, * FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE * AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER * LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,* OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN * THE SOFTWARE. * *
 * *****************************************************************************
 */
package com.thalesgroup.hudson.plugins.klocwork.util;

import com.emenda.kwjlib.KWApi;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import org.emendashaded.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.jenkinsci.remoting.RoleChecker;

/**
 * KloXMLGenerator uses the kwjlib library to connect to the Klocwork server and
 * retrieve a list of issues using the Web API. This list of issues is then
 * converted to XML format in order to replicate the deprecated kwinspectreport
 * functionality as closely as possible. Only used for systems running Klocwork
 * v9.6 or later.
 */
public class KloXMLGenerator implements Serializable {

    public static class callGenerateXMLFromIssues implements Callable<String, RuntimeException> {

        String a_host = "";
        String a_port = "";
        boolean useSSL = false;
        String a_projectname = "";
        String a_filename = "";
        BuildListener listener = null;
        String a_query = "";
        String a_user = "";
        String ltokenlocation = null;

        public callGenerateXMLFromIssues(String a_host, String a_port, boolean useSSL,
                String a_projectname, String a_filename, BuildListener listener, String a_query, String a_user, String ltokenlocation) {
            this.a_host = a_host;
            this.a_port = a_port;
            this.useSSL = useSSL;
            this.a_projectname = a_projectname;
            this.a_filename = a_filename;
            this.listener = listener;
            this.a_query = a_query;
            this.a_user = a_user;
            this.ltokenlocation = ltokenlocation;
        }

        public String call() throws RuntimeException {
            return GenerateXMLFromIssues(a_host, a_port, useSSL, a_projectname, a_filename, listener, a_query, a_user, ltokenlocation);
        }

        public void checkRoles(RoleChecker checker)
                    throws SecurityException {
            // added for support with newer Jenkins v1.6+
        }
    }

    public static String GenerateXMLFromIssues(String a_host, String a_port,
            boolean useSSL,
            String a_projectname, String a_filename, BuildListener listener, String a_query, String a_user, String ltokenlocation) {
        String kwurl = "";
        if (useSSL) {
            kwurl = "https://" + a_host + ":" + a_port;
        } else {
            kwurl = "http://" + a_host + ":" + a_port;
        }
        listener.getLogger().println("Connecting to Klocwork Web API service... " + kwurl);
        KWApi KWservice = new KWApi(kwurl, ltokenlocation);

        try {
            listener.getLogger().println("creating XML document");
            File outputFile = new File(a_filename);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(outputFile));
                if (outputFile.canWrite()) {
                    //Get issues
                    listener.getLogger().println("Retrieving Klocwork issues using kwjlib...");
                    listener.getLogger().println("Sending request for project: " + a_projectname + " with query: " + a_query);
                    ArrayList<JSONObject> issues = KWservice.search(a_projectname, a_query, null, null, null);
                    if (issues != null) {
                        listener.getLogger().println("Number of issues returned: " + issues.size());
                        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?><errorList xmlns=\"http://www.klocwork.com/inForce/report/1.0\">");
                        //Iterate through issues
                        for (JSONObject issue : issues) {
                            if (issue != null) {
                                try {
                                    if (issue.has("id")
                                            && issue.optString("id").length() > 0) {
                                        String id = StringEscapeUtils.escapeXml(issue.optString("id"));
                                        String file = StringEscapeUtils.escapeXml(issue.optString("file"));
                                        String method = StringEscapeUtils.escapeXml(issue.optString("method"));
                                        String code = StringEscapeUtils.escapeXml(issue.optString("code"));
                                        String message = StringEscapeUtils.escapeXml(issue.optString("message"));
                                        String status = StringEscapeUtils.escapeXml(issue.optString("status"));
                                        String state = StringEscapeUtils.escapeXml(issue.optString("state"));
                                        String owner = StringEscapeUtils.escapeXml(issue.optString("owner"));
                                        String severity = StringEscapeUtils.escapeXml(issue.optString("severity"));
                                        String severityCode = StringEscapeUtils.escapeXml(issue.optString("severityCode"));
                                        String taxonomyName = StringEscapeUtils.escapeXml(issue.optString("taxonomyName"));
                                        String url = StringEscapeUtils.escapeXml(issue.optString("url"));
                                        bw.write("<problem>");
                                        bw.newLine();
                                        bw.write("<problemID>" + id + "</problemID>");
                                        bw.newLine();
                                        bw.write("<file>" + file + "</file>");
                                        bw.newLine();
                                        bw.write("<method>" + method + "</method>");
                                        bw.newLine();
                                        bw.write("<code>" + code + "</code>");
                                        bw.newLine();
                                        bw.write("<message>" + message + "</message>");
                                        bw.newLine();
                                        bw.write("<citingStatus>" + status + "</citingStatus>");
                                        bw.newLine();
                                        bw.write("<state>" + state + "</state>");
                                        bw.newLine();
                                        bw.write("<owner>" + owner + "</owner>");
                                        bw.newLine();
                                        bw.write("<severity>" + severity + "</severity>");
                                        bw.newLine();
                                        bw.write("<severitylevel>" + severityCode + "</severitylevel>");
                                        bw.newLine();
                                        bw.write("<displayAs>" + severity + "</displayAs>");
                                        bw.newLine();
                                        bw.write("<taxonomies>");
                                        bw.newLine();
                                        bw.write("<taxonomy name=\"" + taxonomyName + "\" metaInf=\"\" />");
                                        bw.newLine();
                                        bw.write("</taxonomies>");
                                        bw.newLine();
                                        bw.write("<url>" + url + "</url>");
                                        bw.newLine();
                                        bw.write("</problem>");
                                        bw.newLine();
                                    }
                                } catch (Exception e) {
                                    listener.getLogger().println("[ERROR]: " + e.getMessage());
                                    listener.getLogger().println("\tissue: " + issue.toString());
                                    e.printStackTrace();
                                }
                            }
                        }
                        bw.write("</errorList>");
                    }
                    else{
                        listener.getLogger().println("ERROR: Unable to get issues from Klocwork server");
                        if(!KWservice.errorMessage.isEmpty()){
                            for(JSONObject message : KWservice.errorMessage){
                                listener.getLogger().println("\t"+message.toString());
                            }
                            KWservice.errorMessage.clear();
                        }
                        return "1";
                    }           
                } else {
                    listener.getLogger().println("ERROR while generating XML. Could not open file for writing: " + a_filename);
                }
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
        } catch (IOException ioe) {
            listener.getLogger().println("ERROR while generating XML - IOException:"
                    + ioe.getMessage());
            return "1";
        }
        File outputFile = new File(a_filename);
        if (outputFile.exists() && outputFile.length() > 0) {
            listener.getLogger().println("Creation of XML file complete. Closing connection to Web API.");
        } else {
            listener.getLogger().println("Creation of XML file failed. You may have to run the kwauth command on your machine.");
        }
        return "0";
    }
}
