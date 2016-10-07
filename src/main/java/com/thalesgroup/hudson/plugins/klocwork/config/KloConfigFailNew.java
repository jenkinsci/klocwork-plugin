/*******************************************************************************
 * Copyright (c) 2013 Emenda Software Ltd.                                      *
 * Author : Andreas Larfors                                                       *
 *		                                                                        *
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
 *                                                                              *
 *******************************************************************************/
package com.thalesgroup.hudson.plugins.klocwork.config;

import java.io.Serializable;
import org.kohsuke.stapler.DataBoundConstructor;

public class KloConfigFailNew implements Serializable {
    private boolean useFailNew;
    private boolean critical;
    private boolean error;
    private boolean warning;
    private boolean review;
    
    @DataBoundConstructor
    public KloConfigFailNew(boolean failNew,
                            boolean critical,
                            boolean error,
                            boolean warning,
                            boolean review) {
        super();
        this.useFailNew = !(failNew);
        this.critical = critical;
        this.error = error;
        this.warning = warning;
        this.review = review;
    }
    
    public boolean getUseFailNew() {
        return useFailNew;
    }
    public boolean getCritical() {
        return critical;
    }   
    public boolean getError() {
        return error;
    }   
    public boolean getWarning() {
        return warning;
    }   
    public boolean getReview() {
        return review;
    }   
}
