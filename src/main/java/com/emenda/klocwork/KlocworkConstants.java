
package com.emenda.klocwork;

public class KlocworkConstants {

    public static final String ICON_URL = "/plugin/klocwork/icons/klocwork-24.gif";
    public static final String DISPLAY_NAME = "Klocwork Server";

    public static final String KLOCWORK_BUILD_WRAPPER_DISPLAY_NAME =
        "Klocwork - Build Environment Settings";

    public static final String KLOCWORK_BUILD_SPEC_DISPLAY_NAME =
            "Klocwork - (step 1 - capture) Capture Build Information";
    public static final String KLOCWORK_SERVER_ANALYSIS_DISPLAY_NAME =
        "Klocwork - (step 2 - analysis) Full Integration Analysis";
    public static final String KLOCWORK_SERVER_LOAD_DISPLAY_NAME =
        "Klocwork - (step 3 - DB load) Full Integration Analysis";
    public static final String KLOCWORK_XSYNC_DISPLAY_NAME =
        "Klocwork - Cross-Project Issue Sync";
    public static final String KLOCWORK_QUALITY_GATEWAY_DISPLAY_NAME =
        "Klocwork - Quality Gateway";
    public static final String KLOCWORK_REPORT_PUBLISHER_DISPLAY_NAME =
        "Klocwork - Report Publisher";
    public static final String KLOCWORK_DESKTOP_BUILDER_NAME =
            "Klocwork - Incremental Diff Analysis";

    public static final String KLOCWORK_ISSUE_CRITICAL = "Critical";
    public static final String KLOCWORK_ISSUE_ERROR = "Error";
    public static final String KLOCWORK_ISSUE_WARNING = "Warning";
    public static final String KLOCWORK_ISSUE_REVIEW = "Review";
    public static final int MAX_CHART_HISTORY = 20;

    public static final String DEFAULT_BUILD_SPEC = "kwinject.out";
    public static final String DEFAULT_TABLES_DIR = "kwtables";
    public static final String DEFAULT_KWCHECK_REPORT_FILE = "kwcheck_report.xml";
    public static final String DEFAULT_DIFF_FILE_LIST = "diff_file_list.txt";

    public static final String KLOCWORK_URL = "KLOCWORK_URL";
    public static final String KLOCWORK_LICENSE_HOST = "KLOCWORK_LICENSE_HOST";
    public static final String KLOCWORK_LICENSE_PORT = "KLOCWORK_LICENSE_PORT";
    public static final String KLOCWORK_PROJECT = "KLOCWORK_PROJECT";
    public static final String KLOCWORK_LTOKEN = "KLOCWORK_LTOKEN";

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
