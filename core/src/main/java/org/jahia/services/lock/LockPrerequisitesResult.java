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
package org.jahia.services.lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Title: Jahia locking system implementation.</p>
 * <p>Description:
 * In the case it is impossible to obtain a lock, this class store the lock
 * prerequisites result in an array list.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
public class LockPrerequisitesResult implements Serializable {

    private List readOnlyTabs;
    private List disabledTabs;
    private List resultsList;
    private List languages;
    private boolean showDetails;

    public LockPrerequisitesResult() {
        this.resultsList = new ArrayList();
        this.readOnlyTabs = new ArrayList();
        this.disabledTabs = new ArrayList();
        this.languages = new ArrayList();
        this.showDetails = false;
    }

    protected void put(final LockKey lockKey) {
        this.resultsList.add(lockKey);
    }

    public List getResultsList() {
        return this.resultsList;
    }

    public LockKey getFirstLockKey() {
        if (resultsList.size() == 0) return null;
        else return (LockKey) resultsList.get(0);
    }

    public void setShowDetails(final boolean showDetails) {
        this.showDetails = showDetails;
    }

    public boolean shouldShowDetails() {
        return showDetails;
    }

    public int size() {
        return this.resultsList.size();
    }

    public List getReadOnlyTabs() {
        return readOnlyTabs;
    }

    public void addReadOnlyTab(final String readOnlyTab) {
        readOnlyTabs.add(readOnlyTab);
    }

    public List getDisabledTabs() {
        return disabledTabs;
    }

    public void addDisabledTab(final String disabledTab) {
        disabledTabs.add(disabledTab);
    }

    public List getLanguages() {
        return languages;
    }

    public void addLanguage(final String locale) {
        languages.add(locale);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("LockPrerequisitesResult: ");
        buff.append(resultsList);
        buff.append(", readOnly: ");
        buff.append(readOnlyTabs);
        buff.append(", disabled: ");
        buff.append(disabledTabs);
        return buff.toString();
    }
}
