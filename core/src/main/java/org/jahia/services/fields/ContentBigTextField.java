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


public class ContentBigTextField extends ContentField {

    private static final long serialVersionUID = 844954881445411548L;

    private static final org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger(ContentBigTextField.class);

    protected ContentBigTextField(final Integer ID,
                                  final Integer jahiaID,
                                  final Integer pageID,
                                  final Integer ctnid,
                                  final Integer fieldDefID,
                                  final Integer fieldType,
                                  final Integer connectType,
                                  final Integer aclID,
                                  List<ContentObjectEntryState> activeAndStagingEntryStates,
                                  Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType, aclID,
                activeAndStagingEntryStates, activeAndStagedDBValues);
    }

    //--------------------------------------------------------------------------
    public static synchronized ContentBigTextField createBigText(final int siteID,
                                                                 final int pageID,
                                                                 final int containerID,
                                                                 final int fieldDefID,
                                                                 final int parentAclID,
                                                                 final int aclID,
                                                                 final String text,
                                                                 final ProcessingContext jParams)
            throws JahiaException {

        final ContentBigTextField result =
                (ContentBigTextField) ContentField.createField(siteID, pageID,
                        containerID, fieldDefID,
                        ContentFieldTypes.BIGTEXT,
                        ConnectionTypes.LOCAL,
                        parentAclID, aclID);
        final EntrySaveRequest saveRequest =
                new EntrySaveRequest(jParams.getUser(),
                        jParams.getLocale().toString(), true);

        result.setText(text, saveRequest, null);
        return result;
    }

    //--------------------------------------------------------------------------

    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entryState resolving
     * This method should call getDBValue to get the DBValue
     */
    public String getValue(final ProcessingContext jParams,
                           final ContentObjectEntryState entryState)
            throws JahiaException {

        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * Sets the String representation of this field.
     * This method should call preSet.
     */
    public void setText(final String value, final EntrySaveRequest saveRequest, final String defaultValue)
            throws JahiaException {
    }

    public ActivationTestResults changeEntryState(final ContentObjectEntryState fromEntryState,
                                                  final ContentObjectEntryState toEntryState,
                                                  final ProcessingContext jParams,
                                                  final StateModificationContext stateModifContext)
            throws JahiaException {
        return null;
    }

    //--------------------------------------------------------------------------

    /**
     * If URL Integrity and WAI compliance are activated (or simply 1 of them),
     * warning messages generated by the URL integrity checker and the WAI
     * validator cannot be ignored and the field cannot be validated.
     */
    protected ActivationTestResults isContentValidForActivation(
            final Set<String> languageCodes,
            final ProcessingContext jParams,
            final StateModificationContext stateModifContext)
            throws JahiaException {
        final ActivationTestResults results = new ActivationTestResults();
        return results;
    }


    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared() {
        return false;
    }

    protected void purgeContent()
            throws JahiaException {
        // not necessary since already done in site admin interface, but we
        // should implement this if we want to be able to do atomic purging.
    }

}
