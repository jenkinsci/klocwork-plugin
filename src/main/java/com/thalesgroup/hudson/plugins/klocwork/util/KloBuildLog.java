/*******************************************************************************
 * Copyright (c) 2011 Emenda Software Ltd.                                      *
 * Author : Jacob Larfors                                                       *
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

package com.thalesgroup.hudson.plugins.klocwork.util;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;

import java.io.IOException;

public class KloBuildLog implements Action {

    public static final String URL_NAME = "klobuildlog";

    private final String displayName = "Build Log";

    private AbstractBuild<?, ?> owner;

    private FilePath kloTables;

    public KloBuildLog(AbstractBuild<?, ?> owner) {
        this.owner = owner;
        this.kloTables = new FilePath(new FilePath(new FilePath(owner.getWorkspace(), "kloTables"),
                owner.getId()), "build.log");
    }

    public AbstractBuild<?, ?> getOwner() {
        return owner;
    }

    public String getIconFileName() {
        return "notepad.png";
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUrlName() {
        return URL_NAME;
    }

    public boolean getFoundBuildLog() {
        try {
            if (kloTables.exists()) {
                return true;
            }
        } catch (IOException e) {
            // ignore
        } catch (InterruptedException e) {
            // ignore
        }
        return false;
    }

    public String getBuildLog() {
        try {
            if (!kloTables.exists()) {
                return null;
            }

            return txtToHTML(kloTables.readToString());
        } catch (IOException e) {
            return txtToHTML(e.getMessage());
        } catch (InterruptedException e) {
            return txtToHTML(e.getMessage());
        }
    }

    private String txtToHTML(String input) {
        // replace all line endings
        input = input.replaceAll("\n", "<br />");
        // replace all tabs
        input = input.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

        return input;
    }

}
