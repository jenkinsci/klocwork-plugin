package com.emenda.klocwork.util;

import com.emenda.klocwork.KlocworkConstants;
import com.emenda.klocwork.KlocworkServerAnalysisBuilder;

import org.apache.commons.lang3.StringUtils;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.Run;
import hudson.model.Project;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.InterruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
            String urlStr = envVars.get(KlocworkConstants.KLOCWORK_URL);
            String separator = (urlStr.endsWith("/")) ? "" : "/";
            URL url = new URL(urlStr + separator +
                envVars.get(KlocworkConstants.KLOCWORK_PROJECT));
            return url.toString();
        } catch (MalformedURLException ex) {
            throw new AbortException(ex.getMessage());
        }
    }

    // public static String getBuildSpecFile(EnvVars envVars)
    //                 throws AbortException {
    //     String envBuildSpec = envVars.get(KlocworkConstants.KLOCWORK_BUILD_SPEC);
    //     return (StringUtils.isEmpty(envBuildSpec)) ? KlocworkConstants.DEFAULT_BUILD_SPEC : envBuildSpec;
    // }

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
            cmds = cmds.toWindowsCommand();
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

	public static String getAbsolutePath(EnvVars envVars, String path) {
		String absolutePath = path;

		return absolutePath;
	}


}
