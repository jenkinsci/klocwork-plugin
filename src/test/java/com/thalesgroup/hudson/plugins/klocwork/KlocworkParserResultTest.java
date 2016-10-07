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


package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.config.KloConfig;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.parser.KloParserResult;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KlocworkParserResultTest extends AbstractWorkspaceTest {
    private BuildListener listener;
    private VirtualChannel channel;
    private KloConfig config;

    @Before
    public void setUp() throws Exception {
        listener = mock(BuildListener.class);
        when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        channel = mock(VirtualChannel.class);
        config = mock(KloConfig.class);
        super.createWorkspace();
    }

    @Test
    public void testNullPattern() {
        KloParserResult parserResult = new KloParserResult(listener, null);
        Assert.assertEquals("With none pattern, the default pattern must be " + KloParserResult.DELAULT_REPORT_MAVEN, KloParserResult.DELAULT_REPORT_MAVEN, parserResult.getKlocworkReportPattern());
    }

    @Test
    public void testEmptyPattern() {
        KloParserResult parserResult = new KloParserResult(listener, null);
        Assert.assertEquals("With empty pattern, the default pattern must be " + KloParserResult.DELAULT_REPORT_MAVEN, KloParserResult.DELAULT_REPORT_MAVEN, parserResult.getKlocworkReportPattern());
    }

    @Test
    public void testNoMatch() throws Exception {
        KloParserResult parserResult = new KloParserResult(listener, config);
        KloReport report = parserResult.invoke(new File(workspace.toURI()), channel);
        Assert.assertEquals("A pattern with no match files is not allowed.", null, report);
    }
}
