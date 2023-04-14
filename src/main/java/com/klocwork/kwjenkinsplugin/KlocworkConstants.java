
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

package com.klocwork.kwjenkinsplugin;

public class KlocworkConstants {

    public static final String ICON_URL = "/plugin/klocwork/icons/klocwork-24.gif";
    public static final String DISPLAY_NAME = Messages.KlocworkConstants_display_name();

    public static final String KLOCWORK_BUILD_WRAPPER_DISPLAY_NAME =
        Messages.KlocworkConstants_build_wrapper_display_names();

    public static final String KLOCWORK_BUILD_SPEC_DISPLAY_NAME =
            Messages.KlocworkConstants_spec_display_name();
    public static final String KLOCWORK_SERVER_ANALYSIS_DISPLAY_NAME =
        Messages.KlocworkConstants_server_analysis_display_name();
    public static final String KLOCWORK_SERVER_LOAD_DISPLAY_NAME =
        Messages.KlocworkConstants_server_load_display_name();
    public static final String KLOCWORK_XSYNC_DISPLAY_NAME =
        Messages.KlocworkConstants_xsync_display_name();
    public static final String KLOCWORK_BUILD_FAILURE_CONDITION_DISPLAY_NAME =
        Messages.KlocworkConstants_build_failure_condition_display_name();
    public static final String KLOCWORK_CI_BUILDER_DISPLAY_NAME =
            Messages.KlocworkConstants_ci_builder_display_name();

    public static final String KLOCWORK_ISSUE_CRITICAL = Messages.KlocworkConstants_klocwork_issue_critical();
    public static final String KLOCWORK_ISSUE_ERROR = Messages.KlocworkConstants_klocwork_issue_error();
    public static final String KLOCWORK_ISSUE_WARNING = Messages.KlocworkConstants_klocwork_issue_warning();
    public static final String KLOCWORK_ISSUE_REVIEW = Messages.KlocworkConstants_klocwork_issue_review();
    public static final int MAX_CHART_HISTORY = 20;

    public static final String DEFAULT_BUILD_SPEC = "kwinject.out";
    public static final String DEFAULT_TABLES_DIR = "kwtables";
    public static final String DEFAULT_REPORT_FILENAME = "kwciagent_report.xml";
    public static final String JSON_REPORT_FILENAME = "kwciagent_report.json";
    public static final String DEFAULT_DIFF_FILE_LIST = "diff_file_list.txt";

    public static final String KLOCWORK_URL = Messages.KlocworkConstants_klocwork_url();
    public static final String KLOCWORK_LICENSE_HOST = Messages.KlocworkConstants_klocwork_license_host();
    public static final String KLOCWORK_LICENSE_PORT = Messages.KlocworkConstants_klocwork_license_port();
    public static final String KLOCWORK_PROJECT = Messages.KlocworkConstants_klocwork_project();
    public static final String KLOCWORK_LTOKEN = Messages.KlocworkConstants_klocwork_ltoken();

    public static final int LTOKEN_HOST_INDEX = 0;
    public static final int LTOKEN_PORT_INDEX = 1;
    public static final int LTOKEN_USER_INDEX = 2;
    public static final int LTOKEN_HASH_INDEX = 3;
    public static final String LTOKEN_SEPARATOR = ";";

    // kwxsync constants
    public static final String LASTSYNC_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String REGEXP_LASTSYNC =
        "^([0-9][0-9])-([0-9][0-9])-([0-9][0-9][0-9][0-9]) ([0-9][0-9]):([0-9][0-9]):([0-9][0-9])$";
    public static final int REGEXP_GROUP_DAY = 1;
    public static final int REGEXP_GROUP_MONTH = 2;
    public static final int REGEXP_GROUP_YEAR = 3;
    public static final int REGEXP_GROUP_HOUR = 4;
    public static final int REGEXP_GROUP_MINUTE = 5;
    public static final int REGEXP_GROUP_SECOND = 6;
}
