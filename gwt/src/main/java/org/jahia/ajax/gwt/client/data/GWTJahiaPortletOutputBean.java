/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
