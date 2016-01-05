/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
