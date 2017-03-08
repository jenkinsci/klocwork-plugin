package com.emenda.klocwork;

import com.emenda.klocwork.KlocworkBuildWrapper;
import com.emenda.klocwork.KlocworkDesktopBuilder;
import com.emenda.klocwork.KlocworkServerAnalysisBuilder;
import com.emenda.klocwork.KlocworkXSyncBuilder;

import com.emenda.klocwork.config.KlocworkDesktopConfig;
import com.emenda.klocwork.config.KlocworkDiffAnalysisConfig;
import com.emenda.klocwork.config.KlocworkServerAnalysisConfig;
import com.emenda.klocwork.config.KlocworkXSyncConfig;

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
    public Object klocworkServerBuilder(String buildName, String tablesDir, boolean incrementalAnalysis, boolean ignoreCompileErrors, String additionalOptions) {
        return new KlocworkServerAnalysisBuilder(new KlocworkServerAnalysisConfig(buildName, tablesDir, incrementalAnalysis, ignoreCompileErrors, "", additionalOptions));
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
