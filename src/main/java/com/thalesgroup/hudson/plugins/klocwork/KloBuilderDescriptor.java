/*******************************************************************************
 * Copyright (c) 2011 Thales Corporate Services SAS                             *
 * Author : Aravindan Mahendran                                                 *
 *                                                                              *
 * Permission is hereby granted, free of charge, to any person obtaining a copy *
 * of this software and associated documentation files (the "Software"), to deal*
 * in the Software without restriction, including without limitation the rights *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell    *
 * copies of the Software, and to permit persons to whom the Software is        *
 * furnished to do so, subject to the following conditions:                     *
 *                                                                              *
 * The above copyright notice and this permission notice shall be included in   *
 * all copies or substantial portions of the Software.                          *
 *                                                                              *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,*
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN    *
 * THE SOFTWARE.                                                                *
 *******************************************************************************/

package com.thalesgroup.hudson.plugins.klocwork;

import com.thalesgroup.hudson.plugins.klocwork.model.KloInstallation;
import com.thalesgroup.hudson.plugins.klocwork.model.KloOption;
import hudson.CopyOnWrite;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KloBuilderDescriptor extends BuildStepDescriptor<Builder> {


    public KloBuilderDescriptor() {
        super(KloBuilder.class);
        load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
        // TODO Auto-generated method stub
        return true;
    }

    @CopyOnWrite
    private volatile KloInstallation[] installations = new KloInstallation[0];

    public String getHelpFile() {
        return "/plugin/klocwork/help.html";
    }

    public String getDisplayName() {
        return "Klocwork - Invoke klocwork command (deprecated)";
    }

    public KloInstallation[] getInstallations() {
        return installations;
    }

    @Override
    public Builder newInstance(StaplerRequest req, JSONObject formData)
            throws hudson.model.Descriptor.FormException {

        KloBuilder builder = req.bindJSON(KloBuilder.class, formData);
        // KloOption[] kloOptions = new KloOption[0];
		// KloOption[] compilerOptions = new KloOption[0];
        // kloOptions = req.bindParametersToList(KloOption.class,
        //        "kloOption.").toArray(new KloOption[0]);

        // builder.setKloOptions(kloOptions);

		// compilerOptions = req.bindParametersToList(KloOption.class,
		//		"compilerOption.").toArray(new KloOption[0]);

		// builder.setCompilerOptions(compilerOptions);

        return builder;
    }

    public boolean configure(StaplerRequest req, JSONObject json) {
        installations = req.bindParametersToList(KloInstallation.class,
                "klocwork.").toArray(new KloInstallation[0]);
        save();
        return true;
    }

    public FormValidation doCheckKwCommand(@QueryParameter String value) {
        String kwCommand = Util.fixEmptyAndTrim(value);
        if (kwCommand == null || kwCommand.isEmpty()) {
            return FormValidation.error("command is mandatory");
        } else {
            return FormValidation.ok();
        }
    }

	public FormValidation doCheckBuildName(@QueryParameter String value) {
        String buildName = Util.fixEmptyAndTrim(value);
        if (buildName == null || buildName.isEmpty()) {
            return FormValidation.ok();
        } else {
			Pattern pattern = Pattern.compile("\\$\\{([A-Za-z0-9._-]+)\\}");
			Matcher matcher = pattern.matcher(value);
			while (matcher.find()) {
				return FormValidation.ok();

			}
            return FormValidation.warning("Warning: Cannot overwrite Klocwork build names on server. Please use environment variable such as ${BUILD_NUMBER} to ensure different name across different builds.");
        }
    }
}
