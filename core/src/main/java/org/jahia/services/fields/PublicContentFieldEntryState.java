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
package org.jahia.services.fields;

import org.jahia.services.version.ContentObjectEntryState;

/**
 * This class is just a bean holding one "entry state information" entity
 * concerning a single field
 *
 * @author NK
 */
public class PublicContentFieldEntryState extends ContentObjectEntryState {

    public PublicContentFieldEntryState (int workflowState,
                                         int versionID,
                                         String languageCode) {
        super (workflowState, versionID, languageCode);
    }

    public PublicContentFieldEntryState (ContentObjectEntryState contentFieldEntryState) {
        super (contentFieldEntryState.getWorkflowState (),
                contentFieldEntryState.getVersionID (),
                contentFieldEntryState.getLanguageCode ());
    }

    /**
     * Needed when a PublicContentFieldEntryState is used as a Map key
     */
    public boolean equals (Object cfe) {
        if (cfe instanceof PublicContentFieldEntryState) {
            if ((((PublicContentFieldEntryState) cfe).getWorkflowState () == getWorkflowState ()) &&
                    (((PublicContentFieldEntryState) cfe).getVersionID () == getVersionID ()) &&
                    (((PublicContentFieldEntryState) cfe).getLanguageCode ().equals (
                            getLanguageCode ())))
                return true;
        }
        return false;
    }
}