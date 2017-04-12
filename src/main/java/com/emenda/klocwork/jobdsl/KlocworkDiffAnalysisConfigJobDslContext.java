package com.emenda.klocwork;

import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;

import com.emenda.klocwork.config.KlocworkDiffAnalysisConfig;

class KlocworkDiffAnalysisConfigJobDslContext implements Context {
	
	KlocworkDiffAnalysisConfig klocworkDiffAnalysisConfig;
	
	public void klocworkDiffAnalysisConfig(String diffFileList, String diffType, String gitPreviousCommit){
		
		klocworkDiffAnalysisConfig = new KlocworkDiffAnalysisConfig(diffType, gitPreviousCommit, diffFileList);
	}
	
	
}