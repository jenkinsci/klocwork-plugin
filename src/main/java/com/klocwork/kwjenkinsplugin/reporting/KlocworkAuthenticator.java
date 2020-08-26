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

package com.klocwork.kwjenkinsplugin.reporting;

import com.klocwork.kwjenkinsplugin.util.KlocworkUtil;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.LauncherExecutionResults;
import com.klocwork.kwjenkinsplugin.util.KlocworkUtil.StreamReferences;
import hudson.AbortException;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.util.ArgumentListBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KlocworkAuthenticator {
    private static final String KW_AUTH_TOOL = "kwauth";
    private static final String CONSOLE_WARNING = "^(Warning|警告).*";
    private static final String UNIX_SEPARATOR = "\n";
    private static final String WINDOWS_SEPARATOR = "\r\n";

    public static void authenticate(final Launcher launcher, final FilePath workspace, final String username, final String password, final String url, final EnvVars envVars)
            throws ConsoleErrorException, AbortException {
        final ArgumentListBuilder kwauthCommand = new ArgumentListBuilder(KW_AUTH_TOOL, "--url", url);
        final String lineSeparator = getLineSeparator(launcher.isUnix());
        final StringBuilder authSB = new StringBuilder()
                .append(username).append(lineSeparator)
                .append(password).append(lineSeparator);
        final LauncherExecutionResults results = KlocworkUtil.executeCommandParseOutput(launcher,
                                                                                        workspace,
                                                                                        envVars,
                                                                                        kwauthCommand,
                                                                                        new ByteArrayInputStream(authSB.toString().getBytes()));
        checkForErrors(results, lineSeparator);
    }

    private static String getLineSeparator(final boolean isUnix) {
        return isUnix ? UNIX_SEPARATOR : WINDOWS_SEPARATOR;
    }

    private static void checkForErrors(final LauncherExecutionResults results, final String lineSeparator) throws ConsoleErrorException {
        final ByteArrayOutputStream stream = results.getOutputStreams().get(StreamReferences.ERR_STREAM);
        for (String error : stream.toString().split(lineSeparator)) {
            if (!error.matches(CONSOLE_WARNING)) {
                throw new ConsoleErrorException(error);
            }
        }
        if (results.getExitCode() != 0) {
            throw new ConsoleErrorException(Messages.KlocworkResultAction_unknown_error());
        }
    }
}