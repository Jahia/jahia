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
