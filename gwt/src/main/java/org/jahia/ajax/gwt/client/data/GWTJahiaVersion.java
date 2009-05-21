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
package org.jahia.ajax.gwt.client.data;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 28 avr. 2008
 * Time: 15:20:03
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaVersion extends BaseModelData implements Serializable {

    public final static String VERSION_LABEL = "versionLabel";
    public final static String WORKFLOW_STATE = "workflowState";
    public final static String AUTHOR = "author";
    public final static String DATE = "versionDate";
    public final static String LANG = "languageCode";

    final static public int ACTIVE_WORKFLOW_STATE = 1;
    final static public int STAGING_WORKFLOW_STATE = 2;
    final static public int WAITING_WORKFLOW_STATE = 3;
    final static public int VERSIONED_WORKFLOW_STATE = 0;
    final static public int DELETED_WORKFLOW_STATE = -1;

    private String versionableUUID;
    private long date;
    private String name;
    private String readableName;
    private String lang;
    private String[] versionRowData;
    private String[] availableTranslations;
    private int workflowState;

    public GWTJahiaVersion() {
        super();
    }

    public GWTJahiaVersion(String versionableUUID, long date, String name, String readableName, String lang,
                   String[] versionRowData, String[] availableTranslations) {
        this.versionableUUID = versionableUUID;
        this.date = date;
        this.name = name;
        this.readableName = readableName;
        this.lang = lang;
        this.versionRowData = versionRowData;
        this.availableTranslations = availableTranslations;
    }

    public String getVersionableUUID() {
        return versionableUUID;
    }

    public void setVersionableUUID(String versionableUUID) {
        this.versionableUUID = versionableUUID;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReadableName() {
        return readableName;
    }

    public void setReadableName(String readableName) {
        this.readableName = readableName;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String[] getVersionRowData() {
        return versionRowData;
    }

    public void setVersionRowData(String[] versionRowData) {
        this.versionRowData = versionRowData;
    }

    public String[] getAvailableTranslations() {
        return availableTranslations;
    }

    public void setAvailableTranslations(String[] availableTranslations) {
        this.availableTranslations = availableTranslations;
    }

    public int getWorkflowState() {
        return workflowState;
    }

    public void setWorkflowState(int workflowState) {
        this.workflowState = workflowState;
    }

    public boolean isLive(){
        return this.workflowState == ACTIVE_WORKFLOW_STATE;
    }

    public boolean isStaging(){
        return this.workflowState > ACTIVE_WORKFLOW_STATE;
    }

    public boolean isArchived(){
        return this.workflowState < ACTIVE_WORKFLOW_STATE;
    }
}
