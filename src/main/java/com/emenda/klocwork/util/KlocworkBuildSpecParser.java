package com.emenda.klocwork.util;

import jenkins.security.MasterToSlaveCallable;

import java.io.IOException;
import java.util.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class KlocworkBuildSpecParser extends MasterToSlaveCallable<List<String>, IOException> {

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

    private void populateFileList() throws IOException {
        Path diffFileListPath = Paths.get(diffFileList);
        //We must handle both relative and absolute paths
        if (!diffFileListPath.isAbsolute()) {
            diffFileListPath = Paths.get(workspace, diffFileList);
        }
        //Let's check the file exists before we try to read it
        if (Files.exists(diffFileListPath)) {
            try (Scanner scanner = new Scanner(diffFileListPath)) {
                while (scanner.hasNextLine()) {
                    fileList.add(diffFileListPath.resolveSibling(scanner.nextLine().trim()).normalize().toString());
                }
            }
        } else {
            //Diff file list is missing, report an error by propagating exception
            throw new IOException("Diff file list " + diffFileList + " does not " +
                    "exist. Either provide a valid diff file list or disable Diff Analysis");
        }
    }

    public List<String> call() throws IOException {
        List<String> validFiles = new ArrayList<String>();
        HashSet<String> buildSpecFiles = new HashSet<String>(); // optimisation for initial capacity?

        // read list of files from a file
        populateFileList();

        try (Scanner scanner = new Scanner(Paths.get(buildSpec))) {
            while (scanner.hasNextLine()) {
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
        if (scanner.hasNext()) {
            String tag = scanner.next();
            Path workingDir;
            String sourceFile;
            try {
                switch (tag) {
                    case "compile":
                        workingDir = Paths.get(scanner.next());
                        scanner.next();
                        scanner.next(); // skip compiler and output file
                        sourceFile = scanner.next();
                        buildSpecFiles.add(workingDir.resolve(sourceFile).normalize().toString());
                        break;
                    case "jcompile":
                        scanner.next();
                        workingDir = Paths.get(scanner.next());
                        sourceFile = scanner.next();
                        buildSpecFiles.add(workingDir.resolve(sourceFile).normalize().toString());
                        break;
                    default:
                        return;
                }
            } catch (NoSuchElementException e) {
                return;
            }
        }
    }
}
