/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data;

import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Dec 8, 2008
 * Time: 2:31:45 PM
 * To change this template use File | Settings | File Templates.
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
