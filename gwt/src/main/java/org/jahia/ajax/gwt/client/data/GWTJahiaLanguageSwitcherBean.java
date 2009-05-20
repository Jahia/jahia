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

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 7 nov. 2008
 * Time: 16:30:09
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaLanguageSwitcherBean implements Serializable{
    private Map<String,GWTLanguageSwitcherLocaleBean> availableLanguages;
    private Map<String,String> workflowStates;

    public GWTJahiaLanguageSwitcherBean() {
    }

    public GWTJahiaLanguageSwitcherBean(Map<String,GWTLanguageSwitcherLocaleBean> availableLanguages, Map<String, String> workflowStates) {
        this.availableLanguages = availableLanguages;
        this.workflowStates = workflowStates;
    }

    public Map<String,GWTLanguageSwitcherLocaleBean> getAvailableLanguages () {
        return availableLanguages;
    }

    public void setAvailableLanguages (Map<String,GWTLanguageSwitcherLocaleBean> availableLanguages) {
        this.availableLanguages = availableLanguages;
    }

    public Map<String, String> getWorkflowStates () {
        return workflowStates;
    }

    public void setWorkflowStates (Map<String, String> workflowStates) {
        this.workflowStates = workflowStates;
    }    
}
