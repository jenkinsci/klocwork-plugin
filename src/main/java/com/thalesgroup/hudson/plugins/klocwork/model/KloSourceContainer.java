/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Aravindan Mahendran                                                 *
 *                                                                              *
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
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.klocwork.model;

import hudson.FilePath;
import hudson.model.BuildListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KloSourceContainer {


    private Map<Integer, KloWorkspaceFile> internalMap = new HashMap<Integer, KloWorkspaceFile>();

    public KloSourceContainer(BuildListener listener, FilePath basedir, List<KloFile> files) throws IOException, InterruptedException {
        for (KloFile kloFile : files) {
            KloWorkspaceFile kloWorkspaceFile = new KloWorkspaceFile();
            FilePath sourceFilePath = new FilePath(basedir, kloFile.get("file"));

            if (!sourceFilePath.exists()) {
                //listener.getLogger().println("[WARNING] - The source file '" + sourceFilePath.toURI() + "' doesn't exist on the slave. The ability to display its source code has been removed.");
                //[MB]: No longer needed as source availble from review link
                kloWorkspaceFile.setSourceIgnored(true);
                kloWorkspaceFile.setFileName(null);
            } else if (sourceFilePath.isDirectory()) {
                kloWorkspaceFile.setFileName(sourceFilePath.getRemote());
                kloWorkspaceFile.setSourceIgnored(true);
            } else {
                kloWorkspaceFile.setFileName(sourceFilePath.getRemote());
                kloWorkspaceFile.setSourceIgnored(false);
            }
            kloWorkspaceFile.setKloFile(kloFile);

            internalMap.put(kloFile.getKey(), kloWorkspaceFile);
        }
    }


    public Map<Integer, KloWorkspaceFile> getInternalMap() {
        return internalMap;
    }
}
