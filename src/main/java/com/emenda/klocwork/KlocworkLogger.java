
package com.emenda.klocwork;

import java.io.PrintStream;
import hudson.model.TaskListener;

public class KlocworkLogger {

    private String msgPrefix = null;
    private PrintStream printStream = null;

    public KlocworkLogger(String id, PrintStream printStream) {
        this.msgPrefix = "[Klocwork " + id + "] - ";
        this.printStream = printStream;
    }

    public void logMessage(String message) {
        printStream.println(msgPrefix + message);
    }


}
