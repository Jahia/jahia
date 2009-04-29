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
package org.jahia.ajax.gwt.client.data.node;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 11, 2008
 * Time: 2:09:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeUsage implements Serializable {
    private int id;
    private int version;
    private int workflow;
    private String extendedWorkflow ;
    private String lang;
    private String pageTitle;
    private String url;

    public GWTJahiaNodeUsage() {
    }

    public GWTJahiaNodeUsage(int id, int version, int workflow, String extendedWorkflow, String lang, String pageTitle, String url) {
        this.id = id;
        this.version = version;
        this.workflow = workflow;
        this.extendedWorkflow = extendedWorkflow ;
        this.lang = lang;
        this.pageTitle = pageTitle;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getWorkflow() {
        return workflow;
    }

    public void setWorkflow(int workflow) {
        this.workflow = workflow;
    }

    public String getExtendedWorkflow() {
        return extendedWorkflow ;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getPageTitle() {
        return pageTitle;
    }

    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
