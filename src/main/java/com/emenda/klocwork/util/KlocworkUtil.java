package com.emenda.klocwork.util;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.services.KlocworkApiConnection;

import net.sf.json.JSONArray;

import org.apache.commons.lang3.StringUtils;

import com.emenda.klocwork.definitions.KlocworkIssue;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import hudson.util.ArgumentListBuilder;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KlocworkUtil {

    public static void validateServerConfigs(EnvVars envVars) throws AbortException {
        validateServerURL(envVars);
        validateServerProject(envVars);
    }

    public static void validateServerURL(EnvVars envVars) throws AbortException {
        if (StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_URL))) {
            throw new AbortException("Klocwork Server not specified. Klocwork " +
            "servers are configured on the Jenkins global configuration page and " +
            "referenced under Build Environment settings on the Job configuration " +
            "page.");
        }
    }

    public static void validateServerProject(EnvVars envVars) throws AbortException {
        if (StringUtils.isEmpty(envVars.get(KlocworkConstants.KLOCWORK_PROJECT))) {
            throw new AbortException("Klocwork Server Project not specified. " +
            "Server projects are provided under Build Environment settings on the " +
            "Job configuration page.");
        }
    }

    public static String[] getLtokenValues(EnvVars envVars, Launcher launcher) throws AbortException {
        try {
            String[] ltokenLine = launcher.getChannel().call(
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

    public static String getKlocworkProjectUrl(EnvVars envVars) throws AbortException {
        try {
            // handle URLs ending with "/", e.g. http://kwserver:8080/
            URL url = new URL(getNormalizedKlocworkUrl(envVars) +
                envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
            return url.toString();
        } catch (MalformedURLException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    // handle URLs ending with "/", e.g. http://kwserver:8080/
    public static String getNormalizedKlocworkUrl(EnvVars envVars) {
        String urlStr = envVars.get(KlocworkConstants.KLOCWORK_URL);
        return (urlStr.endsWith("/")) ? urlStr : urlStr + "/";
    }

    public static String getIssueListUrl(String url, String project) {
        return String.format("%sreview/insight-review.html#issuelist_goto:project=%s",
            url, project);
    }

    public static String getBuildIssueListUrl(String url, String project, String buildName)
        throws UnsupportedEncodingException {
        return String.format("%s,searchquery=%s",
            getIssueListUrl(url, project),
            URLEncoder.encode(String.format("build:%s", buildName), "UTF-8"));
    }

    public static String getBuildSpecPath(String buildSpec, FilePath workspace)
                    throws AbortException {
        return (new FilePath(workspace, getDefaultBuildSpec(buildSpec))).getRemote();
    }

    public static String getDefaultBuildSpec(String buildSpec) {
        return (StringUtils.isEmpty(buildSpec)) ? KlocworkConstants.DEFAULT_BUILD_SPEC : buildSpec;
    }

    public static String getDefaultKwtablesDir(String tablesDir) {
        return (StringUtils.isEmpty(tablesDir)) ? KlocworkConstants.DEFAULT_TABLES_DIR : tablesDir;
    }

    public static String getDefaultKwcheckReportFile(String reportFile) {
        return (StringUtils.isEmpty(reportFile)) ? KlocworkConstants.DEFAULT_KWCHECK_REPORT_FILE : reportFile;
    }

    public static String getDefaultBuildName(String buildName, EnvVars envVars) {
        if (StringUtils.isEmpty(buildName)) {
            return envVars.get("BUILD_TAG");
        } else {
            return envVars.expand(buildName);
        }
    }

    public static int executeCommand(Launcher launcher, TaskListener listener,
                        FilePath buildDir, EnvVars envVars, ArgumentListBuilder cmds) throws AbortException {
        return executeCommand(launcher, listener, buildDir, envVars, cmds, false);
    }

    public static int executeCommand(Launcher launcher, TaskListener listener,
                        FilePath buildDir, EnvVars envVars, ArgumentListBuilder cmds,
                        boolean ignoreReturnCode)
                        throws AbortException {
        if (launcher.isUnix()) {
            cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        } else {
            cmds.add("&&", "exit", "%%ERRORLEVEL%%");
            cmds = new ArgumentListBuilder("cmd.exe", "/C", cmds.toString());
        }
        try {
            int returnCode = launcher.launch().
                stdout(listener).stderr(listener.getLogger()).
                pwd(buildDir).envs(envVars).cmds(cmds)
                .join();
            listener.getLogger().println("Return code: " + Integer.toString(returnCode));
            if (!ignoreReturnCode && returnCode != 0) {
                throw new AbortException("Non-zero Return Code. Aborting.");
            } else {
                return returnCode;
            }
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    public static ByteArrayOutputStream executeCommandParseOutput(Launcher launcher,
                                     FilePath buildDir, EnvVars envVars, ArgumentListBuilder cmds)
            throws AbortException {
        if (launcher.isUnix()) {
            cmds = new ArgumentListBuilder("/bin/sh", "-c", cmds.toString());
        } else {
            cmds.add("&&", "exit", "%%ERRORLEVEL%%");
            cmds = new ArgumentListBuilder("cmd.exe", "/C", cmds.toString());
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            launcher.launch()
                    .stdout(outputStream).stderr(outputStream).
                    pwd(buildDir).envs(envVars).cmds(cmds)
                    .join();
            return outputStream;
        } catch (IOException | InterruptedException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

	public static String getAbsolutePath(EnvVars envVars, String path) {
		String absolutePath = path;
		return absolutePath;
	}

	public static int generateKwListOutput(FilePath xmlReport, ByteArrayOutputStream outputStream, TaskListener listener, String ciTool, Launcher launcher){
        int returnCode = 0;
        if(ciTool.equalsIgnoreCase("kwciagent")){
            try {
                outputStream.writeTo(xmlReport.write());
            } catch (IOException | InterruptedException e) {
                returnCode = 1;
                listener.getLogger().println(e.getMessage());
            }
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                if (launcher.isUnix()) {
                	bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                } else {
                	bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                }
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.trim().startsWith("<problemID>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<file>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<method>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<code>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<message>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<citingStatus>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severity>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severitylevel>")) {
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
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
            BufferedReader bufferedReader = null;
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(xmlReport.write(), "UTF-8"));
                bufferedWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
                bufferedWriter.newLine();
                bufferedWriter.write("<errorList>");
                bufferedWriter.newLine();
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                if (launcher.isUnix()) {
                	bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
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
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<file>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<method>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<code>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<message>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<citingStatus>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severity>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
                        if (matcher.find()) {
                            listener.getLogger().print(matcher.group(1) + "\t");
                        }
                    } else if (line.trim().startsWith("<severitylevel>")) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                        Matcher matcher = Pattern.compile("<.+>(.+)<.+>").matcher(line);
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

    public static String createKlocworkAPIRequest(String action,
        String query, EnvVars envVars) throws AbortException {

        String request = "action=" + action + "&project=" + envVars.get(KlocworkConstants.KLOCWORK_PROJECT);
        if (!StringUtils.isEmpty(query)) {
            try {
                request += "&query=" +
                    KlocworkUtil.getQueryDefaultGroupingOff(query) + query;
                request += URLEncoder.encode(request, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new AbortException(ex.getMessage());
            }
        }

        return request;
    }

    public static JSONArray getJSONRespose(String request,
        EnvVars envVars, Launcher launcher) throws AbortException {
        JSONArray response;
        try {
            String[] ltokenLine = KlocworkUtil.getLtokenValues(envVars, launcher);
            KlocworkApiConnection kwService = new KlocworkApiConnection(
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

    private static String getQueryDefaultGroupingOff(String query) {
        if(!query.toLowerCase().contains("grouping:off")
                && !query.toLowerCase().contains("grouping:on")){
            return "grouping:off ";
        }
        return "";
    }

}
