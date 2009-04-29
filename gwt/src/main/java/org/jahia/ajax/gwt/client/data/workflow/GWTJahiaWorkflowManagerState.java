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
package org.jahia.ajax.gwt.client.data.workflow;

import java.io.Serializable;
import java.util.Set;
import java.util.Map;
import java.util.List;

/**
 * User: rfelden
 * Date: 3 d�c. 2008 - 14:42:18
 */
public class GWTJahiaWorkflowManagerState implements Serializable {

    private List<String> availableLanguages ;
    private Map<String, Set<String>> checked;
    private Map<String, Set<String>> disabledChecks;
    private Map<String, String> titleForObjectKey ;
    private Map<String, Map<String, Set<String>>> batch ;

    public GWTJahiaWorkflowManagerState() {}

    public GWTJahiaWorkflowManagerState(Map<String, Set<String>> checked, Map<String, Set<String>> disabledChecks, Map<String, String> titleForObjectKey, Map<String, Map<String, Set<String>>> batch) {
        this.checked = checked ;
        this.disabledChecks = disabledChecks ;
        this.titleForObjectKey = titleForObjectKey ;
        this.batch = batch ;
    }

    public Map<String, Set<String>> getChecked() {
        return checked;
    }

    public Map<String, Set<String>> getDisabledChecks() {
        return disabledChecks;
    }

    public Map<String, String> getTitleForObjectKey() {
        return titleForObjectKey;
    }

    public void setTitleForObjectKey(Map<String, String> titleForObjectKey) {
        this.titleForObjectKey = titleForObjectKey;
    }

    public Map<String, Map<String, Set<String>>> getBatch() {
        return batch;
    }

    public List<String> getAvailableLanguages() {
        return availableLanguages;
    }

    public void setAvailableLanguages(List<String> availableLanguages) {
        this.availableLanguages = availableLanguages;
    }
}
