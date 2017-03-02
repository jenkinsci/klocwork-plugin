package com.emenda.emendaklocwork;

import com.emenda.emendaklocwork.KlocworkBuildWrapper;
import com.emenda.emendaklocwork.KlocworkDesktopBuilder;
import com.emenda.emendaklocwork.KlocworkServerAnalysisBuilder;
import com.emenda.emendaklocwork.KlocworkXSyncBuilder;

import com.emenda.emendaklocwork.config.KlocworkDesktopConfig;
import com.emenda.emendaklocwork.config.KlocworkDiffAnalysisConfig;
import com.emenda.emendaklocwork.config.KlocworkServerAnalysisConfig;
import com.emenda.emendaklocwork.config.KlocworkXSyncConfig;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.wrapper.WrapperContext;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;


@Extension(optional = true)
public class KlocworkJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = WrapperContext.class)
    public Object klocworkWrapper(String configName, String projectName, String buildSpecification) {
        return new KlocworkBuildWrapper(configName, "", projectName, buildSpecification);
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkDesktopBuilder(String projectDir, String reportFile, String additionalOpts) {
        return new KlocworkDesktopBuilder(new KlocworkDesktopConfig(projectDir, false,reportFile, additionalOpts, true, "9011", true, new KlocworkDiffAnalysisConfig("", "", "")));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkServerBuilder(String buildName, String tablesDir, boolean incrementalAnalysis, boolean ignoreReturnCodes, String additionalOptions) {
        return new KlocworkServerAnalysisBuilder(new KlocworkServerAnalysisConfig(buildName, tablesDir, incrementalAnalysis, ignoreReturnCodes, additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkXSyncBuilder(boolean dryRun, String lastSync, String projectRegexp,
                                        boolean statusAnalyze, boolean statusIgnore,
                                        boolean statusNotAProblem, boolean statusFix,
                                        boolean statusFixInNextRelease, boolean statusFixInLaterRelease,
                                        boolean statusDefer, boolean statusFilter,
                                        String additionalOpts) {
        return new KlocworkXSyncBuilder(new KlocworkXSyncConfig(dryRun, lastSync, projectRegexp, statusAnalyze, statusIgnore,
                                                    statusNotAProblem, statusFix, statusFixInNextRelease,
                                                    statusFixInLaterRelease, statusDefer, statusFilter,
                                                    additionalOpts));
    }

    @DslExtensionMethod(context = PublisherContext.class)
    public Object klocworkPublisher() {
        return null;
    }

}
