/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thalesgroup.hudson.plugins.klocwork.util;

import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.model.AbstractBuild;
import java.util.Map;

/**
 *
 * @author Yoan Poigneau
 */
public class UserAxisConverter {
    
    //News in 1.17. Replace ${var}  by the corresponding user defined axis of a matrix project in the ProjectName Field.
    public static String AxeConverter(AbstractBuild<?, ?> build, String varToConvert)
    {

        if(build.getProject().getClass().getName().equals(MatrixConfiguration.class.getName()))
	{
            MatrixConfiguration matrix=(MatrixConfiguration)build.getProject();
            Combination currentAxes=matrix.getCombination();
            String newVar=varToConvert;
            for(Map.Entry var : currentAxes.entrySet())
            {
                if(varToConvert.contains("${"+var.getKey()+"}"))
                {
                   newVar=varToConvert.replace("${"+var.getKey()+"}",var.getValue().toString());
                }
            }
            return newVar;
        }
        return varToConvert;
    }
    
}
