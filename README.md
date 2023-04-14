# Jenkins Klocwork Community Plugin

The Klocwork Community Plugin for Jenkins enables automated Klocwork analyses within Jenkins.

### Supported features:
 * Basic freestyle/matrix jobs
 * [Job-DSL](https://wiki.jenkins-ci.org/display/JENKINS/Job+DSL+Plugin)
 * [Pipelines](https://wiki.jenkins-ci.org/display/JENKINS/Pipeline+Plugin)

More information and up-to-date documentation can be found here: [Jenkins Plugin Documentation](https://docs.roguewave.com/en/klocwork/2020/jenkinsci), [Klocwork's Documentation](https://docs.roguewave.com/en/klocwork/current/)

### Technical support

The plugin is maintained by Klocwork Team. For any questions visit [Technical support](https://techsupport.roguewave.com/)

### Latest features:

* Issue citing for CI-Analysis jobs
* New build step names. (Make sure to update your pipeline scripts to match the changes. Use Pipeline Snippet Generator to retrieve updated names)
* Pipeline support
* Job-DSL support
* Klocwork Cross-Project Synchronisation
* Incremental Diff Analysis & Quality Gateway


### Changelog

#### Version 2023.1
* General Update: Added ability to choose RLM as a license service provider
* Bug fix: fixed authentication problem when citing loaded defects through analysis results tool

#### Version 2022.4
* No changes

#### Version 2022.3
* No changes

#### Version 2022.2
* General Update: added support for the License Provider option when using with latest Klocwork tools (2022.2 and up)

#### Version 2022.1
* General Update: added optional 'Working Directory' text box in 'Klocwork Step 1 - Capture Build Information' step widget
* Bug fix: corrected 'Load Analysis Results' step pipeline script generation to always include required values

#### Version 2021.3
* General Update: added missing relevant buildspec creation tools to the plugin
* General Update: changed report chart to use interpolation plot instead of discrete trend

#### Version 2020.3.1
* General Update: fixed potential XXE vulnerability in XML-parser

#### Version 2020.3
* Feature: Added user authorization for issue citing
* Feature: Added ability to show only changed file in Post Analysis - Build Failure Condition Step
* Bug fix: Fixed Japanese translation encodings for some of the text fields
* Bug fix: Fixed the job failing when changed files list contains non-existent file. Error message is logged instead.  

#### Version 2020.2
* Feature: Added issue citing through CI-analysis report
* General Update: New build step names: 


    Klocwork - Step 1 (CI/Full) - Capture Build Information
    Klocwork - Step 2 (Full) - Run Analysis                                      <-replaces Analysis Full
    Klocwork - Step 2 (CI) - Run Differentail Analysis                           <-replaces Incremental Diff Analysis
    Klocwork - Step 3 (Full) - Load Analysis Results                             <-replaces DB Load
    
    Klocwork - Post Analysis (Full) - Cross-Project Issue Sync (Optional)    
    Klocwork - Post Analysis (Full/CI) - Build Failure Conditions (Optional)     <-replaces Quality Gateway

* General Update: Changed several class names to match standardized build step names
* General Update: Added internationalization
* General Update: Analysis support by 'kwcheck' removed. Analysis support only by 'kwciagent' added.
* General Update: Removed deprecated features
* Bug fix: Fixed problem with 'kwmaven' not working properly
* Bug fix: Fix KW Dashboard not showing on job page for pipeline jobs
* General Update: changed version enumeration format to match Klocwork release versions

#### Version 2.5.2
* General Update: switched pom.xml to use jenkins https. This change does not impact existing configuration or behaviour. 

#### Version 2.5.1
* Bug Fix: Resolved issue where the diff analysis command line is too long.  The diff file is now passed as an argument file, rather than each file on the command line. Also skipped the check internally between the diff file and the build specification as kwciagent does this itself and also provides output to skipped files. This change does not impact existing configuration or behaviour. 

#### Version 2.5.0
* Feature: Added option to the advanced section of the integration analysis step, that if the specified project does not exist it will be created or duplicated from another. This change does not impact existing configuration or behaviour (off by default), however to make use of the new feature extra pipeline configuration or GUI configuration is needed. 

#### Version 2.4.9
* Bug Fix: Update to resolve issue with the Klocwork server url links. Projects with special characters such as spaces or projects that had been renamed would generate invalid url links. No impact or change to configuration should be needed.

#### Version 2.4.8
* Bug Fix: Update to resolve issue with severities in Japanese language. No impact or change to configuration should be needed.

#### Version 2.4.7
* Bug Fix: Resolved issue introduced in 2.4.6 where global server/install settings were not being saved. No impact or change to configuration should be needed.
* Bug Fix: Resolved issue where the trend graph and Klocwork link introduced in 2.4.3 were not showing in pipeline jobs. No impact or change to configuration should be needed.
* Bug Fix: Resolved issue when generating a pipeline snippet on step 'klocworkIntegrationStep2'. No impact or change to configuration should be needed.

#### Version 2.4.6
* Feature: Added the ability to use with jcasc plugin. No impact or change to configuration should be needed.

#### Version 2.4.5
* Bug Fix: Resolved issue in which when reading the ltoken file the resource was not closed. No impact or change to configuration should be needed.

#### Version 2.4.4
* Bug Fix: Post-build step "Klocwork Gateway". The query string sent to the server was being incorrectly formatted due to errors in the code. No impact or change to configuration should be needed.

#### Version 2.4.3
* New feature: Added trend graph and link to Klocwork review to the main product page. Configuration can be found in the integration step for loading. This does impact pipeline configuration
* Important note: This version requires an update to the pipeline scripts running integration builds to continue to run. Please find examples below:

Pipeline before 2.4.3:

    klocworkIntegrationStep2 ( [ tablesDir: "${KLOCWORK_TABLES}", buildName: "", additionalOpts: "" ])

Pipeline 2.4.3 without trend chart:

    klocworkIntegrationStep2 (
                serverConfig:[
                    tablesDir: "${KLOCWORK_TABLES}",
                    buildName: "",
                    additionalOpts: ""
                ]
    )

Pipeline 2.4.3 with trend chart:

    klocworkIntegrationStep2 (
                serverConfig:[
                    tablesDir: "${KLOCWORK_TABLES}", 
                    buildName: "", 
                    additionalOpts: "" 
                ],
                reportConfig:[
                   displayChart: true,
                   chartHeight: "",
                   chartWidth: "",
                   query: "status:Analyze"
               ]
     )

#### Version 2.4.2
* Bug fix: Dashboard feature caused a could "not serialize" bug in some environments. Implemented Serializable for KlocworkIssue. No impact to existing configuration expected.

#### Version 2.4.1
* Bug fix: Dashboard feature caused a could "not serialize" bug in some environments. Implemented Serializable for KlocworkSeverities and KlocworkStatuses. No impact to existing configuration expected.

#### Version 2.4.0
* Bug fix: Dashboard feature caused a could "not serialize" bug in some environments. Implemented Serializable for KlocworkXMLReportParser and KlocworkXMLReportParserIssueList. No impact to existing configuration expected.
* Bug fix Fixed pipeline snippet editor not showing the correct snippet for quality gate. No impact to existing configuration expected.

#### Version 2.3.9
* New feature: Added parameter to Klocwork CI gateway to "report results". This creates a link on the build/project page to "Klocwork Dashboard" which allows issues to be seen. No impact to existing configuration expected.
* New feature: Changed step Klocwork CI gateway to allow multiple quality gates similar to the integration build. No impact to existing configuration expected.
* New feature: Changed step Klocwork CI gateway to include filtering on severities and statuses. No impact to existing configuration expected.

#### Version 2.3.8
* New feature: Added parameter to Klocwork CI gateway to allow the build to fail as unstable rather than failure. No impact to existing configuration expected.

#### Version 2.3.7
* Minor bug fix: Updated file handling to use UTF-8 to support use in Japan

#### Version 2.3.6
* New feature: added an option to the quality gate to stop the build from continuing if the gate failed a condition
* Major bug fix: fixed backwards compatibility issue with quality gate in pipelines when referring to old config using old terms, when using old config this cause the quality gate to be skipped.

#### Version 2.3.5
* Minor bug fix: resolved issue where the quality gate config wouldn't show in job configuration page.

#### Version 2.3.4
* New feature: Added in support to use kwciagent as the diff analysis tool
* Re-factored plug-in to refer to Ci in the diff analysis
* Minor bug fix: fixed issue were build command got encapsulated in quotes
* Added a config file for the pipeline step for the build specification generation step, this enables the step to appear in the syntax generator.

#### Version 2.3.3
* Minor bug fix: resolved issue in diff analysis where files/paths were in a different case on windows between the build specification and file system were being ignored.
* Minor bug fix: resolved issue on windows were the git change list command was incorrectly encapsulating the command with double quotes.

#### Version 2.3.2
* Added the Klocwork tools kwgradlew and kwgradle to the drop down selection for the build specification step

#### Version 2.3.1
* Minor bug fix: resolved conflict with other plug-ins using older versions of org.joda.time

#### Version 2.3
* New feature: added a new step for generating a build specification file to be run before the analysis steps

#### Version 2.2.3
* New feature: added an option to the integration build step to disable the running of kwdeploy.

#### Version 2.2.2
* Changed how the output from kwciagent was generated to reduce console noise and generate a consistent xml
* Minor bug fix: Fixed an issue with windows agents were env vars were not injected correctly

#### Version 2.2.1
* Minor bug fix: updated deprecated build step to make use build name setting

#### Version 2.2
* New feature: support for Java diff analysis
* Major bug fix: resolved issue were global license server settings were passed to integration analysis step even if blank
* Major bug fix: updated the integration quality gate query to allow users to specify grouping:on if they wish

#### Version 2.1
* Minor bug fix: resolved potential NPD when checking threshold values
* Minor bug fix: updated the wiki link in the plugin pom.xml
* Major bug fix: updated Klocwork api library to make use of Jenkins environment variables

#### Version 2.0
* Simplified creation of Klocwork static analysis jobs or tasks
* Supports Klocwork full, incremental and incremental diff analysis
* Allows job success criteria to be connected to static analysis results via a Quality Gateway
* Provides per-checkin CI analysis of just the changed files for minimum analysis times
* Support for Jenkins Pipelines
* Support for Job-DSL

#### Version 1.24.6
* Merged various changes and bug fixes from Emenda
* Added support for use of variables in configuration
* Updated the version of kwjlib for Klocwork API usage
* Updated the global settings so that it uses the Klocwork tool in the path

#### Version 1.18
* Fix reoponed JENKINS-29673 - Klocwork plugin does not work in a master/slave configuration

#### Version 1.17
* Fix JENKINS-29673 - Klocwork plugin does not work in a master/slave configuration

#### Version 1.16.3
* Avoid errors when building on slaves
* Avoid error 500 when restarting jenkins
* Avoid NPE from old version of config.xml which doesn't contain the tag <webAPI>

#### Version 1.16.2
* Clean code by applying IDEA format and by removing unused code
* Use SCM features for changelog

#### Version 1.16.1
* Changing Jenkins version to be Java 7 compliant

#### Version 1.16
* Enabled multiple build specification files
* Removed Dashboard portlet component to also remove dependency on dashboard view plugin
* Added SSL support
* Build graph previously incorrectly removed, now re-instated
* Fixed Multiconfiguration project bug in Publisher

#### Version 1.15
* Enabled embedded Klocwork results in Klocwork v9.6 and later.
* Achieved using kwjlib to retrieve issues from Klocwork Web API, which are
put into an XML file by KloXMLGenerator and then parsed as before

#### Version 1.14.1
* Allowing Klocwork publisher to appear in maven job

#### Version 1.14
* Fixed NullPointerException and added compiler options

#### Version 1.13
* Fix reopened JENKINS-12861 - klocwork - update to recognize 9.5.x xml schema

#### Version 1.12
* Fix JENKINS-12861 - klocwork - update to recognize 9.5.x xml schema

#### Version 1.11
* Merge pull request - Add compatibility with Klocwork v9.6

#### Version 1.10
* Fix JENKINS-13668 - Klocwork Plugin Fails if no KW Issues

#### Version 1.9
* Fix JENKINS-14005 - Major issue when Klocwork reporting is enabled

#### Version 1.8.1
* Allow publisher part to appear in maven jobs.

#### Version 1.8
* Fixed JENKINS-13743 - Jenkins 1.463 + Klocwork plugin - crashes when saving configuration

#### Version 1.7
* Fixed JENKINS-12535 - Link to the line which contains the klocwork error instead of just linking to the file.

#### Version 1.6
* Update to latest DTkit versions

#### Version 1.5
* Updated version number. Provided variable ${BUILD_ID} when specifying Klocwork tables directory such that a unique directory can be used for each build.
* Added functionality to specify options for kwbuildproject. Useful for changing location of tables directory, adding compiler options, etc.

#### Version 1.4
* Updated xml parser to work with reports generated by Klocwork v9.5. New features: per-build pie chart showing number of issues and their state, a link to view the build.log, link to view the parse_errors.log and a link to access Klocwork Review within Jenkins. Now to test on Windows.

#### Version 1.3
* Fixed JENKINS-10773
* Fixed JENKINS-10775
* Fixed JENKINS-10776

#### Version 1.2
* Fixed JENKINS-10455

#### Version 1.1
* Fixed JENKINS-10379 - Allowed statuses for Klocwork report are incorrect
* Fixed args parsing

#### Version 1.0
* Initial version
