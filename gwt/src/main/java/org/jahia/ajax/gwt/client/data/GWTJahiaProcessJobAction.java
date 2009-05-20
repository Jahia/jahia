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

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 26, 2008
 * Time: 4:30:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaProcessJobAction extends BaseModelData implements Serializable {
    private String key;
    private Set<String> langs;
    private String action;
    private Map<String, String> workflowStateForLanguage;

    public GWTJahiaProcessJobAction() {
    }

    public GWTJahiaProcessJobAction(String key, Set<String> langs, String action, Map<String, String> wfStates) {
        this.key = key;
        this.langs = langs;
        this.action = action;
        this.workflowStateForLanguage = wfStates ;
    }

    public String getKey() {
        return key;
    }

    public Set<String> getLangs() {
        return langs;
    }

    public String getAction() {
        return action;
    }

    public Map<String, String> getWorkflowStateForLanguage() {
        if (workflowStateForLanguage != null) {
            return workflowStateForLanguage;
        } else {
            return new HashMap<String, String>() ;
        }
    }
}
