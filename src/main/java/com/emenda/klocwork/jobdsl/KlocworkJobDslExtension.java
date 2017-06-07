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
    klocworkWrapper("KlocworkServerConfigName","KlocworkInstallationName","KlocworkProjectName","BuildSpecificationFileName")
  }
  steps {
    shell("kwinject make")

    klocworkDesktopBuilder(){
      klocworkDesktopConfig("LocalProjectDirectory", true, "ReportFile", "AdditionalOptions", true){
        klocworkDiffAnalysisConfig("DiffFileListFileName", "git", "GitPreviousCommit")
      }
    }

    klocworkServerAnalysis("TablesDirectory", true, true, "ImportConfig", "AdditionalOptions")

    klocworkServerDBLoad("TablesDirectory", "BuildName", "AdditionalOptions")

    klocworkXSyncBuilder(true, "03-00-0000 00:00:00", "ProjectFilter", true, true, true, true, true, true, true, true,"AdditionalOptions")
  }
  publishers{
    klocworkQualityGateway(true, true){
      klocworkIncrementalDiffGateway("2", "ReportFile")
      klocworkFullIntegrationGateway("unstable", "severity:Critical", "2", "ConditionName")
    }
  }
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
    public Object klocworkDesktopBuilder(Runnable closure){
		KlocworkDesktopConfigJobDslContext context =  new KlocworkDesktopConfigJobDslContext();
		executeInContext(closure, context);

		return new KlocworkDesktopBuilder(context.klocworkDesktopConfig);
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
    public Object klocworkServerDBLoad(String tablesDir, String buildName, String additionalOptions) {
        return new KlocworkServerLoadBuilder(
            new KlocworkServerLoadConfig(tablesDir, buildName, additionalOptions));
    }

    @DslExtensionMethod(context = StepContext.class)
    public Object klocworkXSyncBuilder(boolean dryRun, String projectRegexp, String lastSync,
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
            new KlocworkGatewayConfig(enableServerGateway, context.passFailConfigs,
											enableDesktopGateway, context.klocworkDesktopGateway));
	}

}
