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

import com.thalesgroup.hudson.plugins.klocwork.model.KloFile;
import com.thalesgroup.hudson.plugins.klocwork.model.KloReport;
import com.thalesgroup.hudson.plugins.klocwork.parser.KloParser;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class KlocworkParserTest {

    KloParser kloParser;

    @Before
    public void setUp() throws Exception {
        kloParser = new KloParser();
    }


    @Test
    public void nullFile() throws Exception {
        try {
            kloParser.parse(null);
            Assert.fail("null parameter is not allowed.");
        } catch (IllegalArgumentException iea) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void nonExistFile() throws Exception {
        try {
            kloParser.parse(new File("nonExistFile"));
            Assert.fail("A valid file is mandatory.");
        } catch (IllegalArgumentException iea) {
            Assert.assertTrue(true);
        }
    }

    //Reminder : in klocwork, the higher the severity is, the less important the error is
    private void analyzeFiles(String file, int nbHighSeverities, int nbLowSeverities) {
        try {
            // Class thisClass = this.getClass();
            // URL url = thisClass.getResource(file); // returns null....
            // URI uri = url.toURI();
            // File reportFile = new File(uri);
            File reportFile = new File(file);
            KloReport report = kloParser.parse(reportFile);
            List<KloFile> highSeverities = report.getHighSeverities();
            List<KloFile> lowSeverities = report.getLowSeverities();
            List<KloFile> allSeverities = report.getAllSeverities();

            assert highSeverities != null;
            assert lowSeverities != null;
            assert allSeverities != null;

            Assert.assertEquals("Wrong number of total severities", allSeverities.size(), nbHighSeverities + nbLowSeverities);
            Assert.assertEquals("Wrong number of high severities", highSeverities.size(), nbHighSeverities);
            Assert.assertEquals("Wrong number of low severities", lowSeverities.size(), nbLowSeverities);
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (URISyntaxException e) {
        // e.printStackTrace();
        // }
    }

    @Test
    //Warning : the version of Klocwork used was 9.0 and no custom java checker were used in Klocwork
    public void testCsvToSQLProject() {
        analyzeFiles("src/test/resources/com/thalesgroup/hudson/plugins/klocwork/report-csvtosql.xml", 66, 18);
        analyzeFiles("src/test/resources/com/thalesgroup/hudson/plugins/klocwork/bug-jenkins-10735.xml", 1, 0);
        analyzeFiles("src/test/resources/com/thalesgroup/hudson/plugins/klocwork/bug-jenkins-12601-parse-isSystem.xml", 1, 0);
    }
    //TO BE COMPLETED (with other files to test)...
}
