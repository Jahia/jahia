/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.PublicContentFieldEntryState;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.LanguageCodeConverters;

/**
 * Used to hold a set of versioned JahiaField instance in multiple language for a given
 * field id.
 *
 * @author Khue Nguyen
 */
public class JahiaVersioningContentFieldFacade {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaVersioningContentFieldFacade.class);

    private int fieldID = -1;

    private List versioningEntryStates;

    private Map fields;

    //--------------------------------------------------------------------------

    /**
     * Constructor for existing Field only
     *
     * @param fieldID, the unique field identifier
     * @param loadFlag
     * @param jParams
     * @param locales, the list of locales
     */
    public JahiaVersioningContentFieldFacade(int fieldID,
                                             int loadFlag,
                                             ProcessingContext jParams,
                                             List locales)
            throws JahiaException {
        this.fieldID = fieldID;
        this.fields = new HashMap();
        this.versioningEntryStates = new ArrayList();
        instanceFields(loadFlag, jParams, locales);
    }

    //--------------------------------------------------------------------------
    public Iterator getFields() {
        return fields.values().iterator();
    }

    //--------------------------------------------------------------------------

    /**
     * @param languageCode
     */
    public Iterator getFields(String languageCode) {
        int size = this.versioningEntryStates.size();
        List result = new ArrayList();
        for (int i = 0; i < size; i++) {
            ContentObjectEntryState entryState =
                    (ContentObjectEntryState) this.versioningEntryStates.get(i);
            if (entryState.getLanguageCode().equals(ContentField.SHARED_LANGUAGE)
                    || entryState.getLanguageCode().equals(languageCode)) {
                JahiaField aField = (JahiaField) this.fields.get(entryState);
                if (aField != null) {
                    result.add(aField);
                }
            }
        }
        return result.iterator();
    }

    //--------------------------------------------------------------------------

    /**
     * Return a field for a entryLoadRequest using resolve entry state mechanism.
     *
     * @param entryLoadRequest
     */
    public JahiaField getField(EntryLoadRequest entryLoadRequest) {

        logger.debug("EntryLoadRequest :" + entryLoadRequest.toString());

        Locale locale = entryLoadRequest.getFirstLocale(true);
        logger.debug("EntryLoadRequest locale :" + locale.toString());

        ContentObjectEntryState entryState =
                (ContentObjectEntryState) ServicesRegistry.getInstance()
                        .getJahiaVersionService()
                        .resolveEntry(this.versioningEntryStates,
                                entryLoadRequest);

        if (entryState != null) {
            logger.debug("Resolved entryState :" + entryState.toString());
        } else {
            return null;
        }
        JahiaField field = null;
        if (entryState != null) {
            field =
                    (JahiaField) fields.get(new PublicContentFieldEntryState(entryState));
        }

        if (field != null) {
            logger.debug("Returned entryState :" + entryState.toString());
            logger.debug("Field Value :" + field.getValue()
                    + ", langCode=" + field.getLanguageCode());
        } else {
            logger.debug("Returned entryState is null ");
        }
        return field;
    }

    //--------------------------------------------------------------------------
    private void instanceFields(int loadFlag,
                                ProcessingContext jParams, List locales)
            throws JahiaException {

        ContentField contentField = ContentField.getField(fieldID);
        EntryLoadRequest elr = null;

        // we ensure to load all versioning entry state.
        contentField.getEntryState(EntryLoadRequest.VERSIONED);

        Iterator entryStates = contentField.getEntryStates().iterator();
        ContentObjectEntryState entryState = null;
        while (entryStates.hasNext()) {
            entryState = (ContentObjectEntryState) entryStates.next();

            PublicContentFieldEntryState entryStateKey =
                    new PublicContentFieldEntryState(entryState);

            List entryLocales = new ArrayList();
            entryLocales.add(LanguageCodeConverters
                    .languageCodeToLocale(entryState.getLanguageCode()));
            elr = new EntryLoadRequest(entryState.getWorkflowState(),
                    entryState.getVersionID(),
                    entryLocales);

            JahiaField field = ServicesRegistry.getInstance()
                    .getJahiaFieldService()
                    .loadField(fieldID,
                            loadFlag,
                            jParams,
                            elr);
            if (field != null) {
                field.setRawValue(field.getValue());
                fields.put(entryStateKey, field);
                this.versioningEntryStates.add(entryStateKey);
            }

        }
    }

}
