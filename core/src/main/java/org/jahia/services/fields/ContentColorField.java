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

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.data.ConnectionTypes;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntrySaveRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Xavier Lawrence
 * Date: 10 oct. 2006
 * Time: 16:27:55
 * To change this template use File | Settings | File Templates.
 */
public class ContentColorField extends ContentSmallTextField {

    private static final long serialVersionUID = 8796938054818487302L;

    protected ContentColorField(Integer ID,
                                Integer jahiaID,
                                Integer pageID,
                                Integer ctnid,
                                Integer fieldDefID,
                                Integer fieldType,
                                Integer connectType,
                                Integer aclID,
                                List<ContentObjectEntryState> activeAndStagingEntryStates,
                                Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType, aclID, activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    public static ContentColorField createColorField(int siteID,
                                                     int pageID,
                                                     int containerID,
                                                     int fieldDefID,
                                                     int parentAclID,
                                                     int aclID,
                                                     String text,
                                                     ProcessingContext jParams)
            throws JahiaException {
        ContentColorField result = (ContentColorField) ContentField.createField(siteID,
                pageID,
                containerID, fieldDefID,
                ContentFieldTypes.COLOR,
                ConnectionTypes.LOCAL,
                parentAclID, aclID);
        EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(),
                jParams.getLocale().toString(), true);

        result.setText(text, saveRequest);
        return result;
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return true;
    }
}
