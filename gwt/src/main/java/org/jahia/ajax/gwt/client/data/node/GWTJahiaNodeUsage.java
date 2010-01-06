/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 11, 2008
 * Time: 2:09:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeUsage extends BaseModelData implements Serializable {

    public GWTJahiaNodeUsage() {}

    public GWTJahiaNodeUsage(int id, int version, int workflow, String extendedWorkflow, String lang, String pageTitle, String url) {
        setId(id);
        setVersion(version);
        setWorkflow(workflow);
        setExtendedWorkflow(extendedWorkflow);
        setLang(lang);
        setPageTitle(pageTitle);
        setUrl(url);
    }

    public GWTJahiaNodeUsage(String identifier, String url) {
        setUrl(url);
        setIdentifier(identifier);
    }

    public void setIdentifier(String identifier) {
        set("identifier", identifier);
    }

    public String getIdentifier() {
        return get("identifier");
    }
    
    public int getId() {
        Integer idInt = get("id");
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return -1;
        }
    }

    public void setId(int id) {
        set("id", Integer.valueOf(id));
    }

    public int getVersion() {
        Integer idInt = get("version");
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return -1;
        }
    }

    public void setVersion(int version) {
        set("version", Integer.valueOf(version));
    }

    public int getWorkflow() {
        Integer idInt = get("workflow");
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return -1;
        }
    }

    public void setWorkflow(int workflow) {
        set("workflow", Integer.valueOf(workflow));
    }

    public String getExtendedWorkflow() {
        return get("extendedWorkflow");
    }

    public void setExtendedWorkflow(String extendedWorkflow) {
        set("extendedWorkflow", extendedWorkflow);
    }

    public String getLang() {
        return get("lang");
    }

    public void setLang(String lang) {
        set("lang", lang);
    }

    public String getPageTitle() {
        return get("pageTitle");
    }

    public void setPageTitle(String pageTitle) {
        set("pageTitle", pageTitle);
    }

    public String getUrl() {
        return get("url");
    }

    public void setUrl(String url) {
        set("url", url);
    }

    public String getVersionName() {
        return get("versionName");
    }

    public void setVersionName(String versionName) {
        set("versionName", versionName);
    }
}
