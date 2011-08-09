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
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *                                                                              *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.model.KloInstallation;
import hudson.Extension;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.util.ArgumentListBuilder;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KloBuilder extends Builder {

    private String projectName;
    private String kloName;

    private final static String DEFAULT_CONFIGURATION = "Default";

    //AM : Variables for the building process
    private String kwCommand;

    @DataBoundConstructor
    public KloBuilder(String projectName, String kloName, String kwCommand) {
        this.projectName = projectName;
        this.kloName = kloName;
        this.kwCommand = kwCommand;
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
     * @return the kloName
     */
    public String getKloName() {
        return kloName;
    }


    public KloInstallation getKlo() {
        for (KloInstallation i : DESCRIPTOR.getInstallations()) {
            if (kloName != null && i.getName().equals(kloName))
                return i;
        }
        return null;
    }

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                           BuildListener listener) throws InterruptedException {

        ArgumentListBuilder argsKwadmin = new ArgumentListBuilder();
        ArgumentListBuilder argsKwbuildproject = new ArgumentListBuilder();
        ArgumentListBuilder argsKwinspectreport = new ArgumentListBuilder();

        String execKwadmin = "kwadmin";
        String execKwbuildproject = "kwbuildproject";
        String execKwinspectreport = "kwinspectreport";
        String FS;
        KloInstallation currentInstall = getKlo();

        if (!launcher.isUnix()) {

            FS = "\\";
        } else {
            FS = "/";
        }


        if (currentInstall != null) {

            File exec = currentInstall.getExecutable();

            if (!currentInstall.getExists()) {
                listener.fatalError(exec + " doesn't exist");
                return false;
            }

            execKwadmin = exec.getPath() + FS + "bin" + FS + execKwadmin;
            execKwbuildproject = exec.getPath() + FS + "bin" + FS
                    + execKwbuildproject;
            execKwinspectreport = exec.getPath() + FS + "bin" + FS
                    + execKwinspectreport;
        }
        //AM : avoiding having a currentInstall with null value
        else {
            currentInstall = new KloInstallation(DEFAULT_CONFIGURATION, "", "localhost", "8074", "localhost", "27000");
        }

        /*if (!new File(build.getWorkspace().getRemote() + FS + "kloXML")
          .exists()) {
              new File(build.getWorkspace().getRemote() + FS + "kloXML").mkdir();
          }*/

        if (!new File(build.getWorkspace().getRemote() + FS + "kloTables")
                .exists()) {
            new File(build.getWorkspace().getRemote() + FS + "kloTables")
                    .mkdir();
        }

        if (!new File(build.getWorkspace().getRemote() + FS + "kloTables" + FS
                + build.getId()).exists()) {
            new File(build.getWorkspace().getRemote() + FS + "kloTables" + FS
                    + build.getId()).mkdir();
        }


        //AM : changing lastBuildNo
        String lastBuildNo = "build_ci_" + build.getId();
        lastBuildNo = lastBuildNo.replaceAll("[^a-zA-Z0-9_]", "");

        //AM : changing the way to add the arguments
        argsKwadmin.add(execKwadmin);
        argsKwadmin.add("--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(), "load", projectName,/*proj.getModuleRoot()*/build.getWorkspace().getRemote() +
                FS + "kloTables" + FS + build.getId(), "--name", lastBuildNo);


        //AM : Since version 0.2.1, fileOut doesn't exist anymore

        String outputFile = build.getWorkspace().getRemote() + FS + "kwinject.out";

        //AM : changing the way to add the arguments
        argsKwbuildproject.add(execKwbuildproject);
        argsKwbuildproject.add(/*proj.getModuleRoot()*/outputFile, "--project", projectName, "--tables-directory",/*proj.getModuleRoot()*/build.getWorkspace().getRemote() + FS + "kloTables" +
                FS + build.getId(), "--project-host", currentInstall.getProjectHost(), "--project-port", currentInstall.getProjectPort(),
                "--license-host", currentInstall.getLicenseHost(), "--license-port", currentInstall.getLicensePort(), "--force", "--verbose", "-j", "auto");

        //Building process
        ArgumentListBuilder argsBuild = new ArgumentListBuilder();
        String execCmd = "";
        String moreArgs = "";
        //if (currentInstall != null) {
        if (!DEFAULT_CONFIGURATION.equals(currentInstall.getName())) {
            File exec = currentInstall.getExecutable();

            if (!currentInstall.getExists()) {
                listener.fatalError(exec + " doesn't exist");
                return false;
            }
            execCmd = exec.getPath() + FS + "bin" + FS;
        }

        if (kwCommand != null) {
            moreArgs = kwCommand;
            moreArgs = moreArgs.replaceAll("[\t\r\n]+", " ");

            List<String> arguments = splitArgs(moreArgs);
            if (arguments.get(0) != null) {
                argsBuild.add(execCmd + arguments.get(0));
            }
            argsBuild.add("--output").add(outputFile);
            for (int i = 1; i < arguments.size(); i++) {
                argsBuild.add(arguments.get(i));
            }
        }

        if (!launcher.isUnix()) {
            argsKwadmin.add("&&", "exit", "%%ERRORLEVEL%%");
            argsKwbuildproject.add("&&", "exit", "%%ERRORLEVEL%%");
            argsBuild.add("&&", "exit", "%%ERRORLEVEL%%");

            argsKwadmin = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwadmin.toStringWithQuote());
            argsKwbuildproject = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwbuildproject.toStringWithQuote());
            argsBuild = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsBuild.toStringWithQuote());
        }

        try {
            int rBuild = launcher.launch().cmds(argsBuild).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();
            int rKwBuildproject = launcher.launch().cmds(argsKwbuildproject).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();

            int rKwAdmin = launcher.launch().cmds(argsKwadmin).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();

            //AM : changing the way to add the arguments
            argsKwinspectreport.add(execKwinspectreport);
            argsKwinspectreport.add("--project", projectName, "--build", lastBuildNo, "--xml",/*proj.getModuleRoot()*/build.getWorkspace().getRemote() +
                    FS +/*"kloXML"+FS+build.getId()+".xml"*/"klocwork_result.xml", "--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                    "--license-host", currentInstall.getLicenseHost(), "--license-port", currentInstall.getLicensePort());

            if (!launcher.isUnix()) {
                argsKwinspectreport.add("&&", "exit", "%%ERRORLEVEL%%");
                argsKwinspectreport = new ArgumentListBuilder().add("cmd.exe", "/C").addQuoted(argsKwinspectreport.toStringWithQuote());
            }

            int rKwInspectreport = launcher.launch().cmds(argsKwinspectreport).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();

            return (rBuild == 0 && rKwAdmin == 0 && rKwBuildproject == 0 && rKwInspectreport == 0);


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
                /** Special case :
                 * 	If there are no spaces in the protected String (i.e. using an environment variable whose value contains whitespace),
                 * 	we keep the opening and closing quotes.
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

    public Descriptor<Builder> getDescriptor() {
        return DESCRIPTOR;
    }

    @Extension
    public static final KloBuilderDescriptor DESCRIPTOR = new KloBuilderDescriptor();

}