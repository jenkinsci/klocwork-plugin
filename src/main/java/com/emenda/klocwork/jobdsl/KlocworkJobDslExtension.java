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
import com.emenda.klocwork.config.KlocworkGatewayDesktopConfig;
import com.emenda.klocwork.config.KlocworkGatewayConfig;

import hudson.Extension;
import javaposse.jobdsl.dsl.RequiresPlugin;
import javaposse.jobdsl.dsl.helpers.wrapper.WrapperContext;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.dsl.helpers.step.StepContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;

/*
job("DSL-KW-Test") {
  wrappers {
    klocworkWrapper("KlocworkServerConfigName","KlocworkInstallationName","KlocworkProjectName")
  }
  steps {
    shell("kwinject make")

    klocworkIncremental(){
      analysisConfig("BuildSpecification", "LocalProjectDirectory", true, "ReportFile", "AdditionalOptions", true){
        diffConfig("DiffFileListFileName", "git", "GitPreviousCommit")
      }
    }

    klocworkIntegrationStep1("BuildSpecification", "TablesDirectory", true, true, "ImportConfig", "AdditionalOptions")

    klocworkIntegrationStep2("TablesDirectory", "BuildName", "AdditionalOptions")

    klocworkIssueSync(true, "03-00-0000 00:00:00", "ProjectFilter", true, true, true, true, true, true, true, true,"AdditionalOptions")
  }
  publishers{
    klocworkQualityGateway(true, true){
      klocworkIncrementalGateway("2", "ReportFile")
      klocworkIntegrationGateway("unstable", "severity:Critical", "2", "ConditionName")
    }
  }
}
*/

@Extension(optional = true)
public class KlocworkJobDslExtension extends ContextExtensionPoint {

    @DslExtensionMethod(context = WrapperContext.class)
    public Object klocworkWrapper(String serverConfig, String installConfig,
                    String serverProject) {
        return new KlocworkBuildWrapper(serverConfig, installConfig,
                        serverProject);
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkIncremental(Runnable closure){
		KlocworkDesktopConfigJobDslContext context =  new KlocworkDesktopConfigJobDslContext();
		executeInContext(closure, context);

		return new KlocworkDesktopBuilder(context.klocworkDesktopConfig);
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkIntegrationStep1(String buildSpec, String tablesDir,
            boolean incrementalAnalysis, boolean ignoreCompileErrors,
            String importConfig, String additionalOptions) {
        return new KlocworkServerAnalysisBuilder(
            new KlocworkServerAnalysisConfig(buildSpec, tablesDir,
                incrementalAnalysis, ignoreCompileErrors, importConfig,
                additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkIntegrationStep2(String tablesDir, String buildName, String additionalOptions) {
        return new KlocworkServerLoadBuilder(
            new KlocworkServerLoadConfig(tablesDir, buildName, additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkIssueSync(boolean dryRun, String projectRegexp, String lastSync,
                                        boolean statusAnalyze, boolean statusIgnore,
                                        boolean statusNotAProblem, boolean statusFix,
                                        boolean statusFixInNextRelease, boolean statusFixInLaterRelease,
                                        boolean statusDefer, boolean statusFilter,
                                        String additionalOptions) {
        return new KlocworkXSyncBuilder(
            new KlocworkXSyncConfig(dryRun, projectRegexp, lastSync,
                statusAnalyze, statusIgnore,
                statusNotAProblem, statusFix, statusFixInNextRelease,
                statusFixInLaterRelease, statusDefer, statusFilter,
                additionalOptions));
    }


	@DslExtensionMethod(context = PublisherContext.class)
    public Object klocworkQualityGateway(boolean enableServerGateway, boolean enableDesktopGateway, Runnable closure){

		KlocworkGatewayJobDslContext context = new KlocworkGatewayJobDslContext();
		executeInContext(closure, context);

		return new KlocworkGatewayPublisher(
            new KlocworkGatewayConfig(enableServerGateway, context.gatewayServerConfigs,
											enableDesktopGateway, context.klocworkDesktopGateway));
	}

}
