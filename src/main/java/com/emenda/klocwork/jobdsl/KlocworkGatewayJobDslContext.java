package com.emenda.klocwork;

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;

import com.emenda.klocwork.config.KlocworkDesktopGateway;
import com.emenda.klocwork.config.KlocworkPassFailConfig;

class KlocworkGatewayJobDslContext implements Context {
	
	KlocworkDesktopGateway klocworkDesktopGateway;
	List<KlocworkPassFailConfig> passFailsConfig = new ArrayList<KlocworkPassFailConfig>();
	
	public void klocworkDesktopGateway(String threshold){
		klocworkDesktopGateway = new KlocworkDesktopGateway(threshold);
	}
	
	public void klocworkPassFailConfig(String jobResult, String query, 
										String threshold, String conditionName){
		KlocworkPassFailConfig passFailConfig = new KlocworkPassFailConfig(jobResult, query, threshold, conditionName);
		passFailsConfig.add(passFailConfig);
	}
	
	
}