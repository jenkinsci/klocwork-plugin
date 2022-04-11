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

import com.klocwork.kwjenkinsplugin.KlocworkConstants;
import com.klocwork.kwjenkinsplugin.config.KlocworkCiConfig;
import com.klocwork.kwjenkinsplugin.services.KlocworkApiConnection;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import net.sf.json.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KlocworkUtil {

    public static FilePath getNormalizedPath(final FilePath workspace, final String dir) throws AbortException {
        File workspaceDir = new File(workspace.getRemote(), dir);
        return new FilePath(workspaceDir);
    }

    public enum StreamReferences {
        ERR_STREAM,
        OUT_STREAM
    }

    public static void validateServerConfigs(final EnvVars envVars) throws AbortException {
        validateServerURL(envVars);
        validateServerProject(envVars);
    }

    public static void validateServerURL(final EnvVars envVars) throws AbortException {
        if (StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_URL))) {
            throw new AbortException("Klocwork Server not specified. Klocwork " +
            "servers are configured on the Jenkins global configuration page and " +
            "referenced under Build Environment settings on the Job configuration " +
            "page.");
        }
    }

    private static void validateServerProject(final EnvVars envVars) throws AbortException {
        if (StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_PROJECT))) {
            throw new AbortException("Klocwork Server Project not specified. " +
            "Server projects are provided under Build Environment settings on the " +
            "Job configuration page.");
        }
    }

    public static String[] getLtokenValues(final EnvVars envVars, final Launcher launcher) throws AbortException {
        try {
            final String[] ltokenLine = launcher.getChannel().call(
                new KlocworkLtokenFetcher(
                    envVars.get(KlocworkConstants.KLOCWORK_URL),
                    envVars.get(KlocworkConstants.KLOCWORK_LTOKEN)));

            if (ltokenLine.length < 4) {
                throw new IOException("Error: ltoken string returned is too short: " +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            } else if (StringUtils.isEmpty(ltokenLine[KlocworkConstants.LTOKEN_USER_INDEX])) {
                throw new IOException("Error: ltoken invalid. Reason: user is empty" +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            }  else if (StringUtils.isEmpty(ltokenLine[KlocworkConstants.LTOKEN_HASH_INDEX])) {
                throw new IOException("Error: ltoken invalid. Reason: ltoken is empty" +
                "\"" + Arrays.toString(ltokenLine) + "\"");
            } else {
                return ltokenLine;
            }
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    // public static String exceptionToString(Exception e) {
    //     StringWriter sw = new StringWriter();
    //     PrintWriter pw = new PrintWriter(sw);
    //     e.printStackTrace(pw);
    //     return sw.toString();
    // }

    // public static String getAndExpandEnvVar(EnvVars envVars, String var) {
    //     String value = envVars.get(var, "");
    //     if (StringUtils.isEmpty(value)) {
    //         return ""; // TODO - handle empty vs null
    //     }
    //     return envVars.expand(value);
    // }

    public static String getKlocworkProjectUrl(final EnvVars envVars) throws AbortException {
        try {
            // handle URLs ending with "/", e.g. http://kwserver:8080/
            final URL url = new URL(getNormalizedKlocworkUrl(envVars) +
                                    envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
            return url.toString();
        } catch (MalformedURLException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    // handle URLs ending with "/", e.g. http://kwserver:8080/
    public static String getNormalizedKlocworkUrl(final EnvVars envVars) {
        final String urlStr = envVars.get(KlocworkConstants.KLOCWORK_URL);
        return urlStr.endsWith("/") ? urlStr : urlStr + "/";
    }

    public static String getIssueListUrl(final String url, final String project) {
        return String.format("%sreview/insight-review.html#issuelist_goto:project=%s",
            url, project);
    }

    public static String getBuildIssueListUrl(final String url, final String project, final String buildName)
        throws UnsupportedEncodingException {
        return String.format("%s,searchquery=%s",
            getIssueListUrl(url, project),
            URLEncoder.encode(String.format("build:%s", buildName), "UTF-8"));
    }

    public static String getBuildSpecPath(final String buildSpec, final FilePath workspace)
                    throws AbortException {
        return new FilePath(workspace, getDefaultBuildSpec(buildSpec)).getRemote();
    }

    public static String getDefaultBuildSpec(final String buildSpec) {
        return StringUtils.isEmpty(buildSpec) ? KlocworkConstants.DEFAULT_BUILD_SPEC : buildSpec;
    }

    public static String getDefaultKwtablesDir(final String tablesDir) {
        return StringUtils.isEmpty(tablesDir) ? KlocworkConstants.DEFAULT_TABLES_DIR : tablesDir;
    }

    public static String getDefaultReportFileName(final String reportFile) {
        return StringUtils.isEmpty(reportFile) ? KlocworkConstants.DEFAULT_REPORT_FILENAME : reportFile;
    }

    public static String getJsonReportFileName() {
        return KlocworkConstants.JSON_REPORT_FILENAME;
    }

    public static String getDefaultBuildName(final String buildName, final EnvVars envVars) {
        if (StringUtils.isEmpty(buildName)) {
            return envVars.get("BUILD_TAG");
        } else {
            return envVars.expand(buildName);
        }
    }

    public static int executeCommand(final Launcher launcher, final TaskListener listener,
                                     final FilePath buildDir, final EnvVars envVars, final ArgumentListBuilder cmds) throws AbortException {
        return executeCommand(launcher, listener, buildDir, envVars, cmds, false);
    }

    public static int executeCommand(final Launcher launcher, final TaskListener listener,
                                     final FilePath buildDir, final EnvVars envVars, ArgumentListBuilder cmds,
                                     final boolean ignoreReturnCode)
                        throws AbortException {
        if (launcher.isUnix()) {
            cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        } else {
            cmds.add("&&", "exit", "%%ERRORLEVEL%%");
            cmds = new ArgumentListBuilder("cmd.exe", "/C", cmds.toString());
        }
        try {
            final int returnCode = launcher.launch().
                stdout(listener).stderr(listener.getLogger()).
                pwd(buildDir).envs(envVars).cmds(cmds)
                                           .join();
            listener.getLogger().println("Return code: " + returnCode);
            if (!ignoreReturnCode && returnCode != 0) {
                throw new AbortException("Non-zero Return Code. Aborting.");
            } else {
                return returnCode;
            }
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    public static Map<StreamReferences, ByteArrayOutputStream> executeCommandParseOutput(final Launcher launcher, final FilePath buildDir, final EnvVars envVars, ArgumentListBuilder cmds)
            throws AbortException {
        return executeCommandParseOutput(launcher, buildDir, envVars, cmds, null).getOutputStreams();
    }

    public static LauncherExecutionResults executeCommandParseOutput(final Launcher launcher, final FilePath buildDir, final EnvVars envVars, ArgumentListBuilder cmds, final InputStream inputStream)
            throws AbortException {
        if (launcher.isUnix()) {
            cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        } else {
            cmds.add("&&", "exit", "%%ERRORLEVEL%%");
            cmds = new ArgumentListBuilder("cmd.exe", "/C", cmds.toString());
        }
        try {
            final Map<StreamReferences, ByteArrayOutputStream> outputStreams = new HashMap<>();
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStreams.put(StreamReferences.OUT_STREAM, outputStream);
            final ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            outputStreams.put(StreamReferences.ERR_STREAM, errorStream);
            final int exitCode = launcher.launch()
                                         .stdout(outputStream).stderr(errorStream).stdin(inputStream).
                    pwd(buildDir).envs(envVars).cmds(cmds)
                                         .join();
            return new LauncherExecutionResults(outputStreams, exitCode);
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    public static int generateKwListOutput(final FilePath xmlReport, final ByteArrayOutputStream outputStream, final TaskListener listener, final String ciTool, final Launcher launcher){
        int returnCode = 0;
        if(ciTool.equalsIgnoreCase(KlocworkCiConfig.getCiTool())){
            try {
                if (launcher.isUnix()) {
                    outputStream.writeTo(xmlReport.write());
                } else {
                    xmlReport.write().write(outputStream.toString().replaceFirst("MS932", "UTF-8").getBytes(StandardCharsets.UTF_8));
                }
            } catch (IOException | InterruptedException e) {
                returnCode = 1;
                listener.getLogger().println(e.getMessage());
            }
            InputStream inputStream = null;
            try {
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                BufferedReader bufferedReader = null;
                if (launcher.isUnix()) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.trim().startsWith("<problemID>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<file>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<method>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<code>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<message>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<citingStatus>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severity>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severitylevel>")) {
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("</problem>")) {
                        listener.getLogger().println();
                    }
                }
            } catch (IOException e) {
                returnCode = 1;
                listener.getLogger().println(e.getMessage());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception ex) {
                    returnCode = 1;
                }
            }
        }
        else {
            InputStream inputStream = null;
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(xmlReport.write(), StandardCharsets.UTF_8));
                bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bufferedWriter.newLine();
                bufferedWriter.write("<errorList>");
                bufferedWriter.newLine();
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                BufferedReader bufferedReader = null;
                if (launcher.isUnix()) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.trim().startsWith("<problem>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    } else if (line.trim().startsWith("<problemID>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<file>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<method>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<code>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<message>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<citingStatus>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severity>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severitylevel>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        final Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("</problem>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        listener.getLogger().println();
                    }
                }
                bufferedWriter.write("</errorList>");
                bufferedWriter.newLine();
            } catch (IOException | InterruptedException e) {
                returnCode = 1;
                listener.getLogger().println(e.getMessage());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception ex) {
                    returnCode = 1;
                }
                try {
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                } catch (Exception ex) {
                    returnCode = 1;
                }
            }
        }
        return returnCode;
    }

    public static String createKlocworkAPIRequestOld(final String action,
                                                     final String query, final EnvVars envVars) throws AbortException {

        String request = "action=" + action + "&project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
        if (!StringUtils.isEmpty(query)) {
            try {
                request += "&query=";
                //Build the query value
                String queryEncoded = getQueryDefaultGroupingOff(query);
                queryEncoded += query;
                //Encode the query value
                queryEncoded = URLEncoder.encode(queryEncoded, "UTF-8");
                //Add the query value to the request
                request += queryEncoded;
            } catch (UnsupportedEncodingException ex) {
                throw new AbortException(ex.getMessage());
            }
        }

        return request;
    }

    public static String createKlocworkAPIRequest(final String action, final HashMap<String, String> args) throws AbortException {
        String request = "&action="+action;
        if (!args.isEmpty()) {
            try {
                for(String key : args.keySet()){
                    if(action.equals("search") && key.equals("query")){
                        request += "&"+key+"="+URLEncoder.encode(getQueryDefaultGroupingOff(args.get(key)) + args.get(key), "UTF-8");
                    }
                    else {
                        request += "&" + key + "=" + URLEncoder.encode(args.get(key), "UTF-8");
                    }
                }
            } catch (UnsupportedEncodingException ex) {
                throw new AbortException(ex.getMessage());
            }
        }
        return request;
    }

    public static JSONArray getJSONResponse(final String request, final EnvVars envVars, final Launcher launcher) throws AbortException {
        final JSONArray response;
        try {
            final String[] ltokenLine = getLtokenValues(envVars, launcher);
            final KlocworkApiConnection kwService = new KlocworkApiConnection(
                            envVars.get(KlocworkConstants.KLOCWORK_URL),
                            ltokenLine[KlocworkConstants.LTOKEN_USER_INDEX],
                            ltokenLine[KlocworkConstants.LTOKEN_HASH_INDEX]);
            response = kwService.sendRequest(request);
        } catch (IOException ex) {
            throw new AbortException("Error: failed to connect to the Klocwork" +
                " web API.\nCause: " + ex.getMessage());
        }
        return response;
    }

    private static String getQueryDefaultGroupingOff(final String query) {
        if(!query.toLowerCase().contains("grouping:off")
                && !query.toLowerCase().contains("grouping:on")){
            return "grouping:off ";
        }
        return "";
    }

    public static ArgumentListBuilder getCreateOrDuplicateCmd(final String url, final String project, final String duplicate, final FilePath workspace) {
        final ArgumentListBuilder kwadminCmd =
                new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", url);
        if(StringUtils.isEmpty(duplicate)){
            kwadminCmd.add("create-project");
        }
        else{
            kwadminCmd.add("duplicate-project");
            kwadminCmd.add(duplicate);
        }
        kwadminCmd.add(project);
        return kwadminCmd;
    }

    public static ArgumentListBuilder getProjectListCmd(final String url, final FilePath workspace) {
        final ArgumentListBuilder kwadminCmd =
                new ArgumentListBuilder("kwadmin");
        kwadminCmd.add("--url", url);
        kwadminCmd.add("list-projects");
        return kwadminCmd;
    }

    public static boolean projectExists(final ByteArrayOutputStream kwadminProjectListOutput, final Launcher launcher, final String project) throws AbortException {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new ByteArrayInputStream(kwadminProjectListOutput.toByteArray());
            if (launcher.isUnix()) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            } else {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            }
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.trim().equals(project)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new AbortException(e.getMessage());
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception ex) {
                throw new AbortException(ex.getMessage());
            }
        }
        return false;
    }

    public static class LauncherExecutionResults {
        final Map<StreamReferences, ByteArrayOutputStream> outputStreams;
        final int exitCode;

        private LauncherExecutionResults(final Map<StreamReferences, ByteArrayOutputStream> outputStreams, final int exitCode) {
            this.outputStreams = outputStreams;
            this.exitCode = exitCode;
        }

        public Map<StreamReferences, ByteArrayOutputStream> getOutputStreams() {
            return outputStreams;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

}
