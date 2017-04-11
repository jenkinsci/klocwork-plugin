package com.emenda.klocwork;

import com.emenda.klocwork.KlocworkBuildWrapper;
import com.emenda.klocwork.KlocworkDesktopBuilder;
import com.emenda.klocwork.KlocworkServerAnalysisBuilder;
import com.emenda.klocwork.KlocworkServerLoadBuilder;
import com.emenda.klocwork.KlocworkXSyncBuilder;

import com.emenda.klocwork.config.KlocworkDesktopConfig;
import com.emenda.klocwork.config.KlocworkDiffAnalysisConfig;
import com.emenda.klocwork.config.KlocworkServerAnalysisConfig;
import com.emenda.klocwork.config.KlocworkServerLoadConfig;
import com.emenda.klocwork.config.KlocworkXSyncConfig;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.wrapper.WrapperContext;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/*
job("DSL-KW-Test") {
label "VM_Linux"
scm {
git("https://github.com/jlarfors/git.git")
}
wrappers {
klocworkWrapper("xubuntu","","git","kwinject.out")
}
steps {
shell("kwinject make NO_EXPAT=YesPlease")
klocworkDesktopBuilder("",false, "", "", true, "", "", "")
klocworkServerAnalysis("",
    true, true, "",
    "")

klocworkServerDBLoad("", "")

klocworkXSyncBuilder(true, "03-00-0000 00:00:00", "git",
    true, true,
    true, true, true,
    true, true, true,
    "")
}
*/

@Extension(optional = true)
public class KlocworkJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = WrapperContext.class)
    public Object klocworkWrapper(String serverConfig, String installConfig,
                    String serverProject, String buildSpec) {
        return new KlocworkBuildWrapper(serverConfig, installConfig,
                        serverProject, buildSpec);
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkDesktopBuilder(String projectDir, boolean cleanupProject,
                                        String reportFile, String additionalOptions,
                                        boolean incrementalAnalysis,
                                        // arguments for KlocworkDiffAnalysisConfig
                                        String diffType, String gitPreviousCommit,
                                        String diffFileList) {
        // TODO: add support for DSL Contexts to make this cleaner...
        return new KlocworkDesktopBuilder(
            new KlocworkDesktopConfig(projectDir, cleanupProject, reportFile,
                additionalOptions, incrementalAnalysis,
                    new KlocworkDiffAnalysisConfig(diffType, gitPreviousCommit,
                        diffFileList)));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkServerAnalysis(String tablesDir,
            boolean incrementalAnalysis, boolean ignoreCompileErrors,
            String importConfig, String additionalOptions) {
        return new KlocworkServerAnalysisBuilder(
            new KlocworkServerAnalysisConfig(tablesDir,
                incrementalAnalysis, ignoreCompileErrors, importConfig,
                additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkServerDBLoad(String buildName, String additionalOptions) {
        return new KlocworkServerLoadBuilder(
            new KlocworkServerLoadConfig(buildName, additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkXSyncBuilder(boolean dryRun, String lastSync, String projectRegexp,
                                        boolean statusAnalyze, boolean statusIgnore,
                                        boolean statusNotAProblem, boolean statusFix,
                                        boolean statusFixInNextRelease, boolean statusFixInLaterRelease,
                                        boolean statusDefer, boolean statusFilter,
                                        String additionalOptions) {
        return new KlocworkXSyncBuilder(
            new KlocworkXSyncConfig(dryRun, lastSync, projectRegexp,
                statusAnalyze, statusIgnore,
                statusNotAProblem, statusFix, statusFixInNextRelease,
                statusFixInLaterRelease, statusDefer, statusFilter,
                additionalOptions));
    }

    // TODO: add context support for conditions to handle List...
    // @DslExtensionMethod(context = PublisherContext.class)
    // public Object KlocworkQualityGateway() {
    //     return null;
    // }

}
