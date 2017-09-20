/**
 * *****************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS * Author : Loic Quentin * *
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
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.model.KloInstallation;
import com.thalesgroup.hudson.plugins.klocwork.util.KloBuildInfo;
import com.thalesgroup.hudson.plugins.klocwork.util.UserAxisConverter;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import java.io.BufferedReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jenkins.model.ArtifactManager;
import org.kohsuke.stapler.DataBoundConstructor;

public class KloBuilder extends Builder {

    private String projectName;
    private String convertedProjectName;
    private String buildName;
    private String kloName;
    private String kwbuildprojectOptions;
    private int buildUsing;
    private boolean kwinspectreportDeprecated = false;
    private boolean compilerBinaryBuild = false;
    private boolean kwBinaryBuild = false;
    private boolean deleteTable = false;
    private final static String DEFAULT_CONFIGURATION = "Default";
    //AM : Variables for the building process
    private String kwCommand;
    private boolean buildLog = true;
    private boolean parseLog = true;

    public KloBuilder() {
    }

    @DataBoundConstructor
    public KloBuilder(boolean kwinspectreportDeprecated, boolean deleteTable, String projectName,
            String buildName, String kloName, String buildUsing,
            String kwCommand, String kwbuildprojectOptions,
            boolean compilerBinaryBuild,
            boolean kwBinaryBuild, boolean buildLog, boolean parseLog) {
        this.kwinspectreportDeprecated = kwinspectreportDeprecated;
        this.deleteTable = deleteTable;
        this.projectName = projectName;
        this.buildName = buildName;
        this.kloName = kloName;
        this.kwCommand = kwCommand;
        this.kwbuildprojectOptions = kwbuildprojectOptions;
        this.buildUsing = Integer.parseInt(buildUsing);
        this.compilerBinaryBuild = compilerBinaryBuild;
        this.kwBinaryBuild = kwBinaryBuild;
        this.buildLog = buildLog;
        this.parseLog = parseLog;
    }

    public String getKwCommand() {
        return kwCommand;
    }

    /**
     * @return the projectName
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * @return the buildName
     */
    public String getBuildName() {
        return buildName;
    }

    /**
     * @return the kloName
     */
    public String getKloName() {
        return kloName;
    }

    public String getKwbuildprojectOptions() {
        return kwbuildprojectOptions;
    }

    public int getBuildUsing() {
        return buildUsing;
    }

    public boolean getKwinspectreportDeprecated() {
        return kwinspectreportDeprecated;
    }

    public boolean getCompilerBinaryBuild() {
        return compilerBinaryBuild;
    }

    public boolean getKwBinaryBuild() {
        return kwBinaryBuild;
    }

    public boolean getDeleteTable() {
        return deleteTable;
    }

    public KloInstallation getKlo() {
        for (KloInstallation i : DESCRIPTOR.getInstallations()) {
            if (kloName != null && i.getName().equals(kloName)) {
                return i;
            }
        }
        return null;
    }

    public boolean getBuildLog() {
        return buildLog;
    }

    public boolean getParseLog() {
        return parseLog;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
            BuildListener listener) throws InterruptedException {
        ArgumentListBuilder argsKwadmin = new ArgumentListBuilder();
        ArgumentListBuilder argsKwbuildproject = new ArgumentListBuilder();
        ArgumentListBuilder argsKwinspectreport = new ArgumentListBuilder();

        String execKwadmin = "kwadmin";
        String execKwbuildproject = "kwbuildproject";
        String execKwinspectreport = "kwinspectreport";
        String FS;
        String localKwbuildprojectOptions = this.kwbuildprojectOptions;
        String localKwCommand = this.kwCommand;
        String localProjectName = this.projectName;
        if (localKwbuildprojectOptions == null) {
            localKwbuildprojectOptions = "";
        }
        if (localKwCommand == null) {
            localKwCommand = "";
        }
        if (localProjectName == null) {
            localProjectName = "";
        }
        Map<String, String> matrixBuildVars = build.getBuildVariables();
        if (matrixBuildVars != null) {
            Iterator it = matrixBuildVars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                if (localKwCommand.contains("%" + pairs.getKey().toString() + "%")) {
                    localKwCommand = localKwCommand.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localKwbuildprojectOptions.contains("%" + pairs.getKey().toString() + "%")) {
                    localKwbuildprojectOptions = localKwbuildprojectOptions.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localProjectName.contains("%" + pairs.getKey().toString() + "%")) {
                    localProjectName = localProjectName.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                }
                if (localKwCommand.contains("${" + pairs.getKey().toString() + "}")) {
                    localKwCommand = localKwCommand.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localKwbuildprojectOptions.contains("${" + pairs.getKey().toString() + "}")) {
                    localKwbuildprojectOptions = localKwbuildprojectOptions.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                if (localProjectName.contains("${" + pairs.getKey().toString() + "}")) {
                    localProjectName = localProjectName.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                }
                it.remove(); // avoids a ConcurrentModificationException
            }
        }

        KloInstallation currentInstall = getKlo();
        // JL : get list of environment variables for build
        EnvVars envVars = null;
        String nextBuildName = "";
        try {
            envVars = build.getEnvironment(listener);
            if (envVars != null) {
                if (!this.buildName.isEmpty()) {
                    nextBuildName = envVars.expand(this.buildName);
                }
                Iterator it = envVars.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    //listener.getLogger().println(pairs.getKey() + " = " + pairs.getValue());
                    if (localKwCommand.contains("%" + pairs.getKey().toString() + "%")) {
                        localKwCommand = localKwCommand.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                    }
                    if (localKwbuildprojectOptions.contains("%" + pairs.getKey().toString() + "%")) {
                        localKwbuildprojectOptions = localKwbuildprojectOptions.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                    }
                    if (localProjectName.contains("%" + pairs.getKey().toString() + "%")) {
                        localProjectName = localProjectName.replace("%" + pairs.getKey().toString() + "%", pairs.getValue().toString());
                    }
                    if (localKwCommand.contains("${" + pairs.getKey().toString() + "}")) {
                        localKwCommand = localKwCommand.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                    }
                    if (localKwbuildprojectOptions.contains("${" + pairs.getKey().toString() + "}")) {
                        localKwbuildprojectOptions = localKwbuildprojectOptions.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                    }
                    if (localProjectName.contains("${" + pairs.getKey().toString() + "}")) {
                        localProjectName = localProjectName.replace("${" + pairs.getKey().toString() + "}", pairs.getValue().toString());
                    }

                    if (localKwCommand.contains("$" + pairs.getKey().toString())) {
                        localKwCommand = localKwCommand.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                    }
                    if (localKwbuildprojectOptions.contains("$" + pairs.getKey().toString())) {
                        localKwbuildprojectOptions = localKwbuildprojectOptions.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                    }
                    if (localProjectName.contains("$" + pairs.getKey().toString())) {
                        localProjectName = localProjectName.replace("$" + pairs.getKey().toString(), pairs.getValue().toString());
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
            }
        } catch (IOException e) {
            listener.getLogger().println("Warning: Could not retrieve list of environment variables. Any use of these may not get resolved.");
        } catch (InterruptedException ie) {
            listener.getLogger().println("Warning: Could not retrieve list of environment variables. Any use of these may not get resolved.");
        }

        localProjectName = UserAxisConverter.AxeConverter(build, localProjectName);
        localProjectName = localProjectName.substring(0, Math.min(localProjectName.length(), 64));

        if (!launcher.isUnix()) {
            FS = "\\";
        } else {
            FS = "/";
        }

        if (currentInstall != null) {
            //File exec = currentInstall.getExecutable();
            FilePath exec = new FilePath(launcher.getChannel(), currentInstall.getExecutablePath());

            try {
                if (!exec.exists()) {
                    listener.fatalError(exec + " doesn't exist");
                    return false;
                }
            } catch (IOException e) {
                listener.fatalError(e.getMessage());
                return false;
            }
            if (exec.getRemote() != null && !exec.getRemote().equals(".")) {
                execKwadmin = exec.getRemote() + FS + "bin" + FS + execKwadmin;
                execKwbuildproject = exec.getRemote() + FS + "bin" + FS
                        + execKwbuildproject;
                execKwinspectreport = exec.getRemote() + FS + "bin" + FS
                        + execKwinspectreport;
            }

        } //AM : avoiding having a currentInstall with null value
        //JL : Update default values
        else {
            currentInstall = new KloInstallation(DEFAULT_CONFIGURATION, "", "localhost", "8080", false, "localhost", "27000");
        }

        //AM : changing lastBuildNo
        //AS : Support for DynamicAxis Plugin
        String suffix = "";
        if (build.getProject().getClass().getName().equals(MatrixConfiguration.class.getName())) {
            MatrixConfiguration matrix = (MatrixConfiguration) build.getProject();
            Combination currentAxes = matrix.getCombination();
            suffix = "_" + currentAxes.digest();
        }

        String lastBuildNo;
        if (!nextBuildName.isEmpty()) {
            lastBuildNo = nextBuildName;    //IP : Yes, I know these are badly named (lastBuildNo for current build name?)
        } else {
            lastBuildNo = "build_ci_" + build.getId() + suffix;
            lastBuildNo = lastBuildNo.replaceAll("[^a-zA-Z0-9_]", "");
        }
        //listener.getLogger().println("buildName = \"" + buildName + "\"");
        //listener.getLogger().println("lastBuildName = \"" + lastBuildNo + "\"");
        listener.getLogger().println("Build Name = \"" + lastBuildNo + "\"");

        //AM : Since version 0.2.1, fileOut doesn't exist anymore
        //AM : changing the way to add the arguments
        argsKwbuildproject.add(execKwbuildproject);
        // JL : Fix - now kloOptions are added after the kwbuildproject executable.
        // kloTables is also set before being added

        String kloTables;
        List<String> buildOpsList = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(localKwbuildprojectOptions);
        while (m.find()) {
            buildOpsList.add(m.group(1));
        }

        if (buildOpsList.contains("--tables-directory")) {
            kloTables = buildOpsList.get((buildOpsList.indexOf("--tables-directory") + 1));
            if (kloTables.startsWith("\"") && kloTables.endsWith("\"")) {
                kloTables = kloTables.substring(1, kloTables.length() - 1);
            }
        } else if (buildOpsList.contains("-o")) {
            kloTables = buildOpsList.get((buildOpsList.indexOf("-o") + 1));
            if (kloTables.startsWith("\"") && kloTables.endsWith("\"")) {
                kloTables = kloTables.substring(1, kloTables.length() - 1);
            }
        } else {
            if (!new File(build.getWorkspace().getRemote() + FS + "kloTables")
                    .exists()) {
                new File(build.getWorkspace().getRemote() + FS + "kloTables")
                        .mkdir();
            }
            kloTables = build.getWorkspace().getRemote() + FS + "kloTables";
            if (localKwbuildprojectOptions.length() == 0 || localKwbuildprojectOptions.endsWith(" ")) {
                if (kloTables.contains(" ")) {
                    localKwbuildprojectOptions += "--tables-directory "
                            + "\"" + kloTables + "\"";
                } else {
                    localKwbuildprojectOptions += "--tables-directory "
                            + kloTables;
                }
            } else if (kloTables.contains(" ")) {
                localKwbuildprojectOptions += " --tables-directory "
                        + "\"" + kloTables + "\"";
            } else {
                localKwbuildprojectOptions += " --tables-directory "
                        + kloTables;
            }
        }

        String outputFile = "";
        boolean found = false;
        boolean specifiedOutput = false;
        if (buildUsing == 0) {
            listener.getLogger().println("building using build command");
            m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(localKwCommand);
            outputFile = build.getWorkspace().getRemote() + FS + "kwinject.out";
            while (m.find()) {
                if (m.group(1).contains(".out")) {
                    outputFile = m.group(1);
                    if (!outputFile.contains("\\") && !outputFile.contains("/")) {
                        outputFile = build.getWorkspace().getRemote() + FS + outputFile;
                    }
                    specifiedOutput = true;
                    break;
                }
            }
            listener.getLogger().println("Output file location: " + outputFile);
            //v1.16.1 hotfix - adding buildspec back to kwbuildproject command
            //when using build command (was erroneously removed in v1.16)
            argsKwbuildproject.add(outputFile);
        } else {
            listener.getLogger().println("building using provided buildspec");
            //New in 1.15: Enables multiple, comma-separated build spec files
            String[] kwinjectFiles = localKwCommand.split(",");
            //New in 1.15: Enable wildcard managment
            //User can now give a path with *.out if there is many build specification file in the directory.
            listener.getLogger().println("Buildspec(s) provided: " + localKwCommand);
            for (String kwinject : kwinjectFiles) {
                if (kwinject.endsWith("*.out")) {
                    listener.getLogger().println("multipul buildspecs provided with *.out");
                    try {
                        FilePath file = new FilePath(launcher.getChannel(), (kwinject.replace(FS + "*.out", FS)));
                        listener.getLogger().println("checking buildspec: " + file.getRemote());
                        FilePath[] list = file.list("*.out");
                        for (FilePath buildspec : list) {
                            if (!found && buildspec.exists()) {
                                listener.getLogger().println(buildspec.getRemote() + " exists");
                                BufferedReader br = new BufferedReader(new InputStreamReader(buildspec.read()));
                                String line;
                                while ((line = br.readLine()) != null) {
                                    if (line.contains("compile;")) {
                                        listener.getLogger().println(file.getRemote() + " contains compile lines");
                                        found = true;
                                        break;
                                    }
                                }
                                br.close();
                            }
                            argsKwbuildproject.add(buildspec);
                        }
                    } catch (IOException ex) {
                        found = true;
                        listener.getLogger().println("Error reading buildspec: " + ex.getMessage());
                    } catch (Exception e) {
                        found = true;
                        listener.getLogger().println("Error reading buildspec: " + e.getMessage());
                    }
                } else {
                    File isAbs = new File(kwinject);
                    FilePath file;
                    if (isAbs.isAbsolute()) {
                        file = new FilePath(launcher.getChannel(), kwinject);
                    } else {
                        file = new FilePath(build.getWorkspace(), kwinject);
                    }
                    listener.getLogger().println("checking buildspec: " + file.getRemote());
                    try {
                        if (!found && file.exists()) {
                            listener.getLogger().println(file.getRemote() + " exists");
                            BufferedReader br = new BufferedReader(new InputStreamReader(file.read()));
                            String line;
                            while ((line = br.readLine()) != null) {
                                if (line.contains("compile;")) {
                                    listener.getLogger().println(file.getRemote() + " contains compile lines");
                                    found = true;
                                    break;
                                }
                            }
                            br.close();
                        }
                    } catch (IOException ex) {
                        found = true;
                        listener.getLogger().println("Error reading buildspec: " + ex.getMessage());
                    } catch (Exception e) {
                        found = true;
                        listener.getLogger().println("Error reading buildspec: " + e.getMessage());
                    }
                    argsKwbuildproject.add(kwinject);
                }
            }//for
            if (!found) {
                listener.getLogger().println("Error: Buildspec(s) contains no compile line(s) at: " + localKwCommand);
                return false;
            }
        }//else

        if (!buildOpsList.contains("--force")
                && !buildOpsList.contains("-f")
                && !buildOpsList.contains("-I")
                && !buildOpsList.contains("--incremental")) {
            localKwbuildprojectOptions += " --force";
        }

        argsKwbuildproject.add("--project", localProjectName,
                /* "--tables-directory", kloTables, */
                "--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                "--license-host", currentInstall.getLicenseHost(), "--license-port",
                currentInstall.getLicensePort());

        List<String> existingBuildOption = new ArrayList<String>();
        Collections.addAll(existingBuildOption, "--project", "-S", "--host", "-h", "--port", "-p", "--licence-host", "-H", "--licence-port", "-P", "--force", "-f", "--incremental", "-I", "--help", "--version", "--add-compiler-options", "-a");
        List<String> buildOptionWithoutValue = new ArrayList<String>();
        Collections.addAll(buildOptionWithoutValue, "--verbose", "-v", "--no-lef", "-n", "--no-link", "-N", "--remote", "--resume", "-r", "--color", "-c", "--no-color");

        //AM : changing the way to add the arguments
        argsKwadmin.add(execKwadmin);
        argsKwadmin.add("--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                "load",
                localProjectName,/*proj.getModuleRoot()*/ kloTables, "--name", lastBuildNo);

        //Building process
        ArgumentListBuilder argsBuild = new ArgumentListBuilder();
        String execCmd = "";
        String moreArgs = "";
        //if (currentInstall != null) {
        if (!DEFAULT_CONFIGURATION.equals(currentInstall.getName())) {
            //File exec = currentInstall.getExecutable();
            FilePath exec = new FilePath(launcher.getChannel(), currentInstall.getExecutablePath());
            try {
                if (!exec.exists()) {
                    listener.fatalError(exec + " doesn't exist");
                    return false;
                }
            } catch (IOException e) {
                listener.fatalError(e.getMessage());
                return false;
            }
            if (exec.getRemote() != null && !exec.getRemote().equals(".")) {
                execCmd = exec.getRemote() + FS + "bin" + FS;
            } else {
                execCmd = "";
            }

        }

        // If buildUsing build command
        if (buildUsing == 0) {
            if (localKwCommand != null) {
                moreArgs = localKwCommand;
                moreArgs = moreArgs.replaceAll("[\t\r\n]+", " ");

                List<String> arguments = splitArgs(moreArgs);
                if (arguments.size() > 0 && arguments.get(0) != null) {
                    argsBuild.add(execCmd + arguments.get(0));
                }
                if (!specifiedOutput) {
                    argsBuild.add("--output").add(outputFile);
                }
                for (int i = 1; i < arguments.size(); i++) {
                    argsBuild.add(arguments.get(i));
                }
            }
        }
        m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(localKwbuildprojectOptions);
        while (m.find()) {
            if (m.group(1).startsWith("\"") && m.group(1).endsWith("\"")) {
                argsKwbuildproject.add(m.group(1).substring(1, m.group(1).length() - 1));
            } else {
                argsKwbuildproject.add(m.group(1));
            }
        }
        if (!launcher.isUnix()) {
            argsKwadmin.add("&&", "exit", "%%ERRORLEVEL%%");
            // now added below
            // argsKwbuildproject.add("&&", "exit", "%%ERRORLEVEL%%");
            argsBuild.add("&&", "exit", "%%ERRORLEVEL%%");

            argsKwadmin = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwadmin.toStringWithQuote());
            // add kwbuildprojectOptions WITHOUT quotes

            argsKwbuildproject = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwbuildproject.toStringWithQuote() + " && exit %%ERRORLEVEL%%");
            argsBuild = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsBuild.toStringWithQuote());
        }

        try {
            int rBuild = 0;
            if (buildUsing == 0) {
                rBuild = launcher.launch().cmds(argsBuild).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();
            }
            File isAbs = new File(outputFile);
            FilePath file;
            if (isAbs.isAbsolute()) {
                file = new FilePath(launcher.getChannel(), outputFile);
            } else {
                file = new FilePath(build.getWorkspace(), outputFile);
            }
            listener.getLogger().println("checking buildspec: " + file.getRemote());
            try {
                if (!found && file.exists()) {
                    listener.getLogger().println(file.getRemote() + " exists");
                    BufferedReader br = new BufferedReader(new InputStreamReader(file.read()));
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.contains("compile;")) {
                            listener.getLogger().println(file.getRemote() + " contains compile lines");
                            found = true;
                            break;
                        }
                    }
                    br.close();
                } else if (!found && !file.exists()) {
                    listener.getLogger().println("Warning: Unable to read " + file.getRemote());
                    found = true;
                }
            } catch (IOException ex) {
                found = true;
                listener.getLogger().println("Warning: could not read file: " + file.getRemote() + " " + ex.getMessage());
            }
            if (!found) {
                listener.getLogger().println("Error: Generated buildspec contains no compile line.");
                return false;
            }
            // If binary build enabled, check return value
            if (compilerBinaryBuild && rBuild != 0) {
                listener.getLogger().println("Error: Build errors exist. Failing build.");
                return false;
            }

            int rKwBuildproject = launcher.launch().cmds(argsKwbuildproject).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();

            // If binary build enabled, check return value
            if (kwBinaryBuild && rKwBuildproject != 0) {
                listener.getLogger().println("Error: Klocwork build errors exist. Failing build.");
                return false;
            }

            int rKwAdmin = launcher.launch().cmds(argsKwadmin).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();

            //AM : changing the way to add the arguments
            argsKwinspectreport.add(execKwinspectreport);
            argsKwinspectreport.add("--project", localProjectName,
                    "--build", lastBuildNo, "--xml",
                    build.getWorkspace().getRemote() + FS + "klocwork_result.xml",
                    "--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                    (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                    "--license-host", currentInstall.getLicenseHost(),
                    "--license-port", currentInstall.getLicensePort());

            if (!launcher.isUnix()) {
                argsKwinspectreport.add("&&", "exit", "%%ERRORLEVEL%%");
                argsKwinspectreport = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwinspectreport.toStringWithQuote());
            }

            int rKwInspectreport = 0;
            // check whether to run kwinspectreport. Klocwork v9.6 or later does not support xml
            // report generation through kwinspectreport. For v9.6 and later, kwjlib is used
            // to retrieve list of issues
            if (!kwinspectreportDeprecated) {
                rKwInspectreport = launcher.launch().cmds(argsKwinspectreport).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();
            }

            // Finally store currentInstall and projectName for publisher to use
            build.addAction(new KloBuildInfo(build, currentInstall, localProjectName));

            //save logs
            HashMap<String, String> artifact_list = new HashMap<String, String>();
            if (buildLog) {
                artifact_list.put("kloTables/build.log", "kloTables/build.log");
            }
            if (parseLog) {
                artifact_list.put("kloTables/parse_errors.log", "kloTables/parse_errors.log");
            }
            if (buildLog || parseLog) {
                ArtifactManager am = build.getArtifactManager();
                am.archive(build.getWorkspace(), launcher, listener, artifact_list);
            }

            //New in 1.15: allow user to delete the klotable after all analysis.
            if (deleteTable && new File(kloTables).exists()) {
                new FilePath(new File(kloTables)).deleteRecursive();
                listener.getLogger().println("Table directory deleted.");
            }

            // return (rBuild == 0 && rKwAdmin == 0 && rKwBuildproject == 0 && rKwInspectreport == 0);
            return (rKwAdmin == 0 && rKwInspectreport == 0);

        } catch (IOException e) {
            Util.displayIOException(e, listener);
            e.printStackTrace(listener
                    .fatalError("command execution failed"));
            return false;
        }

    }

    private List<String> splitArgs(String args) {
        int length = args.length();
        List<String> arguments = new ArrayList<String>();
        String tmpStr = "";
        int nbQuotes = 0;
        boolean inProtectedString = false;
        for (int i = 0; i < length; i++) {
            char currentChar = args.charAt(i);

            //End of protected string
            if (currentChar == '\"' && inProtectedString) {
                /**
                 * Special case : If there are no spaces in the protected String
                 * (i.e. using an environment variable whose value contains
                 * whitespace), we keep the opening and closing quotes.
                 */
                tmpStr += currentChar;
                if (tmpStr.contains(" ")) {
                    tmpStr = tmpStr.substring(1, tmpStr.length() - 1);
                }
                arguments.add(tmpStr);
                nbQuotes++;
                tmpStr = "";
                inProtectedString = false;
                continue;
            }

            //Begining of protected string
            if (currentChar == '\"' && !inProtectedString) {
                tmpStr += currentChar;
                inProtectedString = true;
                nbQuotes++;
                continue;
            }

            //Whitespace and no protected String
            if (currentChar == ' ' && !inProtectedString) {
                if (!tmpStr.isEmpty()) {
                    arguments.add(tmpStr);
                    tmpStr = "";
                }
                continue;
            }

            //Other cases
            tmpStr += currentChar;

            //Final character : adding the word if not empty
            if (i == length - 1 && !tmpStr.trim().isEmpty()) {
                arguments.add(tmpStr);
                tmpStr = "";
            }
        }
        if (nbQuotes % 2 != 0) {
            System.err.println("WARNING : one opening quote is not closed ! Parsing will not work properly.");
        }
        return arguments;
    }

    @Override
    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }
    @Extension
    public static final KloBuilderDescriptor DESCRIPTOR = new KloBuilderDescriptor();
}
