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
