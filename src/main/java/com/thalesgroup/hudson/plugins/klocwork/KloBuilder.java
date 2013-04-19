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
import com.thalesgroup.hudson.plugins.klocwork.model.KloOption;
import com.thalesgroup.hudson.plugins.klocwork.util.KloBuildInfo;
import com.thalesgroup.hudson.plugins.klocwork.util.KloXMLGenerator;
import com.thalesgroup.hudson.plugins.klocwork.util.UserAxisConverter;
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
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kohsuke.stapler.DataBoundConstructor;

public class KloBuilder extends Builder {

    private String projectName;
    private String kloName;

    private int buildUsing;

    private boolean kwinspectreportDeprecated = false;
    private boolean compilerBinaryBuild = false;
    private boolean kwBinaryBuild = false;
    private boolean deleteTable=false;

    private KloOption[] kloOptions = new KloOption[0];
    private KloOption[] compilerOptions = new KloOption[0];


    private final static String DEFAULT_CONFIGURATION = "Default";

    //AM : Variables for the building process
    private String kwCommand;

    public KloBuilder() {

    }

    @DataBoundConstructor
    public KloBuilder(boolean kwinspectreportDeprecated,boolean deleteTable, String projectName, String kloName,
                                      String buildUsing, String kwCommand, boolean compilerBinaryBuild,
                                      boolean kwBinaryBuild) {
        this.kwinspectreportDeprecated = kwinspectreportDeprecated;
        this.deleteTable=deleteTable;
        this.projectName = projectName;
        this.kloName = kloName;
        this.kwCommand = kwCommand;
        this.buildUsing = Integer.parseInt(buildUsing);
        this.compilerBinaryBuild = compilerBinaryBuild;
        this.kwBinaryBuild = kwBinaryBuild;
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

    public void setKloOptions(KloOption[] kloOptions) {
        this.kloOptions = kloOptions;
    }

    public KloOption[] getKloOptions() {
        return kloOptions;
    }

    public boolean getDeleteTable() {
            return deleteTable;
    }


    public KloInstallation getKlo() {
        for (KloInstallation i : DESCRIPTOR.getInstallations()) {
            if (kloName != null && i.getName().equals(kloName))
                return i;
        }
        return null;
    }

    public void setCompilerOptions(KloOption[] compilerOptions) {
            this.compilerOptions = compilerOptions;
    }
    public KloOption[] getCompilerOptions() {
            return compilerOptions;
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


        //AM : for compatibility with old versions
        if (kloOptions == null){
            kloOptions = new KloOption[0];
        }
        if (compilerOptions == null){
            compilerOptions = new KloOption[0];
        }
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
            execKwadmin = exec.getRemote() + FS + "bin" + FS + execKwadmin;
            execKwbuildproject = exec.getRemote() + FS + "bin" + FS
                    + execKwbuildproject;
            execKwinspectreport = exec.getRemote() + FS + "bin" + FS
                    + execKwinspectreport;
        }
        //AM : avoiding having a currentInstall with null value
        else {
            currentInstall = new KloInstallation(DEFAULT_CONFIGURATION, "", "localhost", "8074", false, "localhost", "27000");
        }

        if (!new File(build.getWorkspace().getRemote() + FS + "kloTables")
                .exists()) {
            new File(build.getWorkspace().getRemote() + FS + "kloTables")
                    .mkdir();
        }

        //AM : changing lastBuildNo
        String lastBuildNo = "build_ci_" + build.getId();
        lastBuildNo = lastBuildNo.replaceAll("[^a-zA-Z0-9_]", "");

        //AM : Since version 0.2.1, fileOut doesn't exist anymore
        //AM : changing the way to add the arguments
        argsKwbuildproject.add(execKwbuildproject);
        // JL : Fix - now kloOptions are added after the kwbuildproject executable.
        // kloTables is also set before being added
        String kloTables = build.getWorkspace().getRemote() + FS + "kloTables";


        String outputFile="";
        if (buildUsing == 0) {
            outputFile = build.getWorkspace().getRemote() + FS + "kwinject.out";
	} 
        else {
            //New in 1.15: Enables multiple, comma-separated build spec files
            String[] kwinjectFiles=kwCommand.split(",");
            //New in 1.15: Enable wildcard managment
            //User can now give a path with *.out if there is many build specification file in the directory.
            for(String kwinject: kwinjectFiles) {
                if(kwinject.endsWith("*.out")){
                    try {
                            FilePath file= new FilePath(new File(kwinject.replace(FS+"*.out", FS)));
                            FilePath [] list =file.list("*.out");
                            for(FilePath buildspec: list)
                            {                           
                                    argsKwbuildproject.add(buildspec);              

                            }
                    } catch (IOException ex) {
                            Logger.getLogger(KloBuilder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                else{
                    argsKwbuildproject.add(kwinject);
                }
            }//for
        }//else

        argsKwbuildproject.add("--project", UserAxisConverter.AxeConverter(build,projectName), "--tables-directory",
                /*proj.getModuleRoot()*/ kloTables, "--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                "--license-host", currentInstall.getLicenseHost(), "--license-port",
				currentInstall.getLicensePort(), "--force");

		List<String> existingBuildOption = new ArrayList<String>();
		Collections.addAll(existingBuildOption,"--project","-S","--host","-h","--port","-p","--licence-host","-H","--licence-port","-P","--force","-f","--incremental","-I","--help","--version","--add-compiler-options","-a");
		List<String> buildOptionWithoutValue = new ArrayList<String>();
		Collections.addAll(buildOptionWithoutValue,"--verbose","-v","--no-lef","-n","--no-link","-N","--remote","--resume","-r","--color","-c","--no-color");

        for (KloOption kloOption : kloOptions) {
            String opt=kloOption.getCmdOption().trim();
            // If tables directory was specified, set kloTables, as this will be added later
            if (opt.equals("--tables-directory") || opt.equals("-o")) {
                kloTables = kloOption.getCmdValue().replace("${BUILD_ID}", build.getId());
            }
            else if (existingBuildOption.contains(opt)){
                continue;      
            }
            else{    
                argsKwbuildproject.add(opt);
                if(buildOptionWithoutValue.contains(opt)) continue;
                else if(!kloOption.getCmdValue().trim().equals((""))) {
                        argsKwbuildproject.add(kloOption.getCmdValue());
                }               
            }
        }
        argsKwbuildproject.add("--tables-directory",/*proj.getModuleRoot()*/ kloTables);

        String addCompilerOptions = "";
        //New in 1.15: Separate build options and compiler options.
        for (KloOption kloOptionaddCompilerOptions : compilerOptions) {
                addCompilerOptions += kloOptionaddCompilerOptions.getCmdOption();
                addCompilerOptions+=kloOptionaddCompilerOptions.getCmdValue();
        }

        if (!addCompilerOptions.equals("")) {
                argsKwbuildproject.add("--add-compiler-options");
                argsKwbuildproject.addQuoted(addCompilerOptions);
        }

        //AM : changing the way to add the arguments
        argsKwadmin.add(execKwadmin);
        argsKwadmin.add("--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                "load",
                UserAxisConverter.AxeConverter(build,projectName),/*proj.getModuleRoot()*/ kloTables, "--name", lastBuildNo);

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
            execCmd = exec.getRemote() + FS + "bin" + FS;
        }

        // If buildUsing build command
        if (buildUsing == 0) {
            if (kwCommand != null) {
                moreArgs = kwCommand;
                moreArgs = moreArgs.replaceAll("[\t\r\n]+", " ");

                List<String> arguments = splitArgs(moreArgs);
                if (arguments.size() >0 && arguments.get(0) != null) {
                    argsBuild.add(execCmd + arguments.get(0));
                }
                argsBuild.add("--output").add(outputFile);
                for (int i = 1; i < arguments.size(); i++) {
                    argsBuild.add(arguments.get(i));
                }
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
            int rBuild = 0;
            if (buildUsing == 0) {
                rBuild = launcher.launch().cmds(argsBuild).envs(build.getEnvironment(listener)).stdout(listener).pwd(build.getWorkspace()).join();
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
            argsKwinspectreport.add("--project", UserAxisConverter.AxeConverter(build,projectName), "--build", lastBuildNo, "--xml",/*proj.getModuleRoot()*/build.getWorkspace().getRemote() +
                    FS +/*"kloXML"+FS+build.getId()+".xml"*/"klocwork_result.xml", "--host", currentInstall.getProjectHost(), "--port", currentInstall.getProjectPort(),
                    (currentInstall.getUseSSL() ? "--ssl" : null), //New in v1.15
                    "--license-host", currentInstall.getLicenseHost(), "--license-port", currentInstall.getLicensePort());

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
            build.addAction(new KloBuildInfo(build, currentInstall, UserAxisConverter.AxeConverter(build,projectName)));

            //New in 1.15: allow user to delete the klotable after all analysis.
            if(deleteTable && new File(kloTables).exists())
            {
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
