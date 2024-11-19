/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 *
 * User: loom
 * Date: Dec 8, 2008
 * Time: 2:31:45 PM
 *
 */
public class GWTJahiaPortletOutputBean implements Serializable {

    private String htmlOutput;
    private boolean inIFrame = false;
    private boolean inContentPortlet = false;
    private long delayedIFrameResizeTime = 1000;
    private String iFrameHeight = null;
    private String iFrameWidth = null;
    private List<String> scriptsWithSrc = new ArrayList<String>();
    private List<String> scriptsWithCode = new ArrayList<String>();

    public String getHtmlOutput() {
        return htmlOutput;
    }

    public void setHtmlOutput(String htmlOutput) {
        this.htmlOutput = htmlOutput;
    }

    public boolean isInIFrame() {
        return inIFrame;
    }

    public void setInIFrame(boolean inIFrame) {
        this.inIFrame = inIFrame;
    }

    public boolean isInContentPortlet() {
        return inContentPortlet;
    }

    public void setInContentPortlet(boolean inContentPortlet) {
        this.inContentPortlet = inContentPortlet;
    }

    public String getIFrameHeight() {
        return iFrameHeight;
    }

    public void setIFrameHeight(String iFrameHeight) {
        this.iFrameHeight = iFrameHeight;
    }

    public String getIFrameWidth() {
        return iFrameWidth;
    }

    public void setIFrameWidth(String iFrameWidth) {
        this.iFrameWidth = iFrameWidth;
    }

    public long getDelayedIFrameResizeTime() {
        return delayedIFrameResizeTime;
    }

    public void setDelayedIFrameResizeTime(long delayedIFrameResizeTime) {
        this.delayedIFrameResizeTime = delayedIFrameResizeTime;
    }

    public List<String> getScriptsWithSrc() {
        return scriptsWithSrc;
    }

    public void setScriptsWithSrc(List<String> scriptsWithSrc) {
        this.scriptsWithSrc = scriptsWithSrc;
    }

    public List<String> getScriptsWithCode() {
        return scriptsWithCode;
    }

    public void setScriptsWithCode(List<String> scriptsWithCode) {
        this.scriptsWithCode = scriptsWithCode;
    }
}
