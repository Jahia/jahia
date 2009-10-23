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
package org.jahia.services.fields;

import org.jahia.data.ConnectionTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.services.version.StateModificationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContentSmallTextSharedLangField extends ContentField
        implements ContentSimpleField {
    private static final long serialVersionUID = -1676141338878161382L;
    private static org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger (ContentSmallTextSharedLangField.class);

    protected ContentSmallTextSharedLangField (Integer ID,
                                               Integer jahiaID,
                                               Integer pageID,
                                               Integer ctnid,
                                               Integer fieldDefID,
                                               Integer fieldType,
                                               Integer connectType,
                                               Integer aclID,
                                               List<ContentObjectEntryState> activeAndStagingEntryStates,
                                               Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (ID.intValue (), jahiaID.intValue (), pageID.intValue (), ctnid.intValue (), fieldDefID.intValue (),
                fieldType.intValue (), connectType.intValue (), aclID.intValue (), activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    public static ContentSmallTextSharedLangField createSmallText (int siteID,
                                                                                int pageID,
                                                                                int containerID,
                                                                                int fieldDefID,
                                                                                int parentAclID,
                                                                                int aclID,
                                                                                String text,
                                                                                ProcessingContext jParams)
            throws JahiaException {
        ContentSmallTextSharedLangField result =
                (ContentSmallTextSharedLangField) ContentField.createField (siteID, pageID,
                        containerID, fieldDefID,
                        ContentFieldTypes.SMALLTEXT_SHARED_LANG,
                        ConnectionTypes.LOCAL,
                        parentAclID, aclID);
        // EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), jParams.getLocale().toString());
        EntrySaveRequest saveRequest = new EntrySaveRequest (jParams.getUser (),
                ContentField.SHARED_LANGUAGE, true);
        result.setText (text, saveRequest);
        return result;
    }

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entry resolving
     * This method should call getDBValue to get the DBValue
     * Note that until setField() is called, getValue returns always the
     * same value, even if the content was set by a setter such as setText!!
     */
    public String getValue (ProcessingContext jParams, ContentObjectEntryState entryState)
            throws JahiaException {
        if (entryState == null) {
            return "";
        }
        String result = this.getDBValue (entryState);

        if (result == null || result.equals ("<empty>")) {
            result = new String ();
        }
        return result;
    }

    /**
     * Sets the String representation of this field.
     * This method should call preSet and postSet.
     */
    public void setText (String value, EntrySaveRequest saveRequest) throws JahiaException {
        if (!ContentField.SHARED_LANGUAGE.equals (saveRequest.getLanguageCode ())) {
            logger.debug ("Found non shared language in setting, enforcing shared language...");
            saveRequest.setLanguageCode (ContentField.SHARED_LANGUAGE);
        }
        preSet (value, saveRequest);
        postSet(saveRequest);
    }

    /**
     * This method is called when there is a workflow state change
     * Such as  staged mode -> active mode (validation), active -> inactive (for versioning)
     * and also staged mode -> other staged mode (workflow)
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     * @param jParams        ProcessingContext object used to get information about the user
     *                       doing the request, the current locale, etc...
     *
     * @return null if the entry state change wasn't an activation, otherwise it
     *         returns an object that contains the status of the activation (whether
     *         successfull, partial or failed, as well as messages describing the
     *         warnings during the activation process)
     */
    public ActivationTestResults changeEntryState (ContentObjectEntryState fromEntryState,
                                                   ContentObjectEntryState toEntryState,
                                                   ProcessingContext jParams,
                                                   StateModificationContext stateModifContext)
            throws JahiaException {
        return new ActivationTestResults ();
    }

    protected ActivationTestResults isContentValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        /** @todo to be implemented */
        return new ActivationTestResults ();
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

}
