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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.node;

import com.extjs.gxt.ui.client.data.BaseModel;

import java.util.Date;

/**
 *
 * User: toto
 * Date: Mar 16, 2009
 * Time: 5:16:40 PM
 *
 */
public class GWTJahiaNodeVersion extends BaseModel {
    private String uuid;

    public GWTJahiaNodeVersion() {
    }

    public GWTJahiaNodeVersion(String uuid, String version, Date date, String label, String workspace, GWTJahiaNode node) {
        setUUID(uuid);
        setVersionNumber(version);
        setDate(date);
        setLabel(label);
        setNode(node);
        setWorkspace(workspace);
    }

    public GWTJahiaNodeVersion(String workspace, GWTJahiaNode node) {
        setWorkspace(workspace);
        setNode(node);
    }

    public String getVersionNumber() {
        return get("versionNumber");
    }

    public void setVersionNumber(String versionNumber) {
        set("versionNumber", versionNumber);
    }

    public Date getDate() {
        return get("date");
    }

    public void setDate(Date date) {
        set("date", date);
    }

    public GWTJahiaNode getNode() {
        return get("node");
    }

    public void setNode(GWTJahiaNode node) {
        set("node", node);
    }

    public String getAuthor() {
        return get("author");
    }

    public void setAuthor(String author) {
        set("author", author);
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label", label);
    }

    public String getWorkspace() {
        return get("workspace");
    }

    public void setWorkspace(String workspace) {
        set("workspace", workspace);
    }

    public String getUrl() {
        return get("url");
    }

    public void setUrl(String url) {
        set("url", url);
    }
}
