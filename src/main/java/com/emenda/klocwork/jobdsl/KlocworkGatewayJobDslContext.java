package com.emenda.klocwork;

import java.util.ArrayList;
import java.util.List;

import javaposse.jobdsl.dsl.Context;
import static javaposse.jobdsl.plugin.ContextExtensionPoint.executeInContext;

import com.emenda.klocwork.config.KlocworkGatewayDesktopConfig;
import com.emenda.klocwork.config.KlocworkGatewayServerConfig;

class KlocworkGatewayJobDslContext implements Context {

	KlocworkGatewayDesktopConfig klocworkDesktopGateway;
	List<KlocworkGatewayServerConfig> passFailConfigs = new ArrayList<KlocworkGatewayServerConfig>();

	public void klocworkIncrementalGateway(String threshold, String reportFile){
		klocworkDesktopGateway = new KlocworkGatewayDesktopConfig(threshold, reportFile);
	}

	public void klocworkIntegrationGateway(String jobResult, String query,
										String threshold, String conditionName){
		KlocworkGatewayServerConfig passFailConfig = new KlocworkGatewayServerConfig(jobResult, query, threshold, conditionName);
		passFailConfigs.add(passFailConfig);
	}


}
