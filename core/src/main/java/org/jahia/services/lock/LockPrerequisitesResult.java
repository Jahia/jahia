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
