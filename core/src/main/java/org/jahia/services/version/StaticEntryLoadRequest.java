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
//
//  DJ 12/04/02
//

package org.jahia.services.version;

import java.util.List;
import java.util.Locale;

/**
 * Class describing which entry of a field/container/containerlist to
 * load
 */
public class StaticEntryLoadRequest extends EntryLoadRequest {
    public StaticEntryLoadRequest(int workflowState, int versionID, List<Locale> locales) {
        super(workflowState, versionID, locales);
    }

    public StaticEntryLoadRequest(int workflowState, int versionID, List<Locale> locales, boolean withMarkedForDeletion) {
        super(workflowState, versionID, locales, withMarkedForDeletion);
    }

    public StaticEntryLoadRequest(EntryLoadRequest sourceRequest) {
        super(sourceRequest);
    }

    public StaticEntryLoadRequest(EntryStateable entryState) {
        super(entryState);
    }

    public StaticEntryLoadRequest(EntryStateable entryState, boolean withMarkedForDeletion) {
        super(entryState, withMarkedForDeletion);
    }

    public void setLocales(List<Locale> locales) {
        throw new IllegalStateException();
    }

    public void setFirstLocale(String languageCode) {
        throw new IllegalStateException();
    }

    public void setWithMarkedForDeletion(boolean withMarkedForDeletion) {
        throw new IllegalStateException();
    }

    public void setVersionID(int versionID) {
        throw new IllegalStateException();
    }
    
    public void setWithDeleted(boolean withDeleted) {
        throw new IllegalStateException();
    }    
    
    public void setWorkflowState(int ws) {
        throw new IllegalStateException();
    }    
    
    public void setCompareMode(boolean compareMode) {
        throw new IllegalStateException();
    }    

}
