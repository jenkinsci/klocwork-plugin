package com.emenda.klocwork.util;

import com.emenda.klocwork.KlocworkConstants;

import hudson.AbortException;
import hudson.remoting.VirtualChannel;
import jenkins.security.MasterToSlaveCallable;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class KlocworkBuildSpecParser extends MasterToSlaveCallable<List<String>,IOException>  {

    private final String workspace;
    private final List<String> fileList;
    private final String buildSpec;
    private final String diffFileList;

    public KlocworkBuildSpecParser(String workspace, String diffFileList, String buildSpec) throws IOException {
        this.workspace = workspace;
        this.fileList = new ArrayList<String>();
        this.buildSpec = buildSpec;
        this.diffFileList = diffFileList;
    }

    public KlocworkBuildSpecParser(String workspace, List<String> fileList, String buildSpec) {
        this.workspace = workspace;
        this.fileList = fileList;
        this.buildSpec = buildSpec;
        this.diffFileList = null;
    }

    private void populateFileList() throws IOException {
		Path diffFileListPath = Paths.get(diffFileList);
		//We must handle both relative and absolute paths
		if (! diffFileListPath.isAbsolute()) {
			diffFileListPath = Paths.get(workspace, diffFileList);
		}
        try (Scanner scanner = new Scanner(diffFileListPath)) {
            while (scanner.hasNextLine()) {
                fileList.add(diffFileListPath.resolveSibling(scanner.nextLine()).normalize().toString());
            }
        }
    }

    public List<String> call() throws IOException {
        List<String> validFiles = new ArrayList<String>();
        HashSet<String> buildSpecFiles = new HashSet<String>(); // optimisation for initial capacity?

        if (diffFileList != null) {
            // read list of files from a file
            populateFileList();
        }

        try (Scanner scanner =  new Scanner(Paths.get(buildSpec))) {
            while (scanner.hasNextLine()){
                processBuildSpecLine(scanner.nextLine(), buildSpecFiles);
            }
        }
        for (String file : fileList) {
            // for optimisation we could do String compare, but feels like it is
            // safer to compare Path(s)
            if (buildSpecFiles.contains(file)) {
                validFiles.add(file);
            }
        }
        return validFiles;
    }

    private void processBuildSpecLine(String line, HashSet<String> buildSpecFiles) throws IOException {
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(";");
        if (scanner.hasNext()){
          String tag = scanner.next();
          if (!tag.equals("compile")) {
              return;
          }
          Path workingDir = Paths.get(scanner.next());
          scanner.next(); scanner.next(); // skip compiler and output file
          String sourceFile = scanner.next();

          buildSpecFiles.add(workingDir.resolve(sourceFile).normalize().toString());
        }
    }
}
