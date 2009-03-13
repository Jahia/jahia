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

//  JahiaSmalltextSharedLangField

package org.jahia.data.fields;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentSmallTextSharedLangField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.sharing.FieldSharingManager;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.TextHtml;

import java.util.*;

public class JahiaSmallTextSharedLangField extends JahiaField implements JahiaSimpleField, JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = 2178462896245203477L;
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaSmallTextSharedLangField.class);

    /**
     * @param ID
     * @param jahiaID
     * @param pageID
     * @param ctnid
     * @param fieldDefID
     * @param fieldType
     * @param connectType
     * @param fieldValue
     * @param rank
     * @param aclID
     * @param versionID
     * @param versionStatus
     * @param languageCode
     */
    public JahiaSmallTextSharedLangField(
            Integer ID,
            Integer jahiaID,
            Integer pageID,
            Integer ctnid,
            Integer fieldDefID,
            Integer fieldType,
            Integer connectType,
            String fieldValue,
            Integer rank,
            Integer aclID,
            Integer versionID,
            Integer versionStatus,
            String languageCode) {

        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType,
                fieldValue, rank, aclID, versionID, versionStatus, languageCode);

        if (isShared()) {
            this.languageCode = ContentField.SHARED_LANGUAGE;
        }

    } // end constructor


    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {
        switch (this.getConnectType()) {
            case (ConnectionTypes.LOCAL) :
                //this.setValue(this.getValue());
                if (!this.getValue().equals("<empty>")) {
                    this.objectItem = ExpressionMarker.getValue(this.getValue(), jParams);
                    if (jParams != null) {
                        this.objectItem = ResourceBundleMarker.getValue((String) this.objectItem, jParams.getLocale());
                    }
                    this.setRawValue(this.getValue());
                    this.setValue(FormDataManager.htmlEncode(this.getValue()));
                }
                break;
            case (ConnectionTypes.DATASOURCE) :
                if ((loadFlag & LoadFlags.DATASOURCE) != 0) {
                    this.setValue(FieldSharingManager.getInstance().getRemoteFieldValue(
                            this.getValue()));
                }
        }
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.SmallText_Field";
    }

    public boolean save(ProcessingContext jParams)
            throws JahiaException {
        // 0 for parentAclID in saveField, because field already exists
        //  -> field already has an aclID
        //  -> no need to create a new one

        ContentSmallTextSharedLangField contentField = (ContentSmallTextSharedLangField) ContentField.getField(getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentSmallTextSharedLangField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }

        final String value = getValue();
        final String savedValue = contentField.getValue(jParams);

        if (((value == null && savedValue == null && !isNew) || (value != null && getValue().equals(savedValue)))) {
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        if(logger.isDebugEnabled()) {
            logger.debug("getValue(): " + value);
            logger.debug("contentField.getValue(jParams): " + savedValue);
        }
        final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), getLanguageCode(), isNew);
        contentField.setText(value==null?"":value, saveRequest);

        if (getID() == 0) {
            setID(contentField.getID());
        }

        return true;
    }

    public String getFieldContent4Ranking() {
        String result = this.getValue();
        try {
            /* Before we can activate this we need a paramBean to be passed !
            ExpressionMarker marker =
                ExpressionMarker.parseMarkerValue(result);
            if (marker != null) {
                result = marker.getValue();
            }
            */
            ResourceBundleMarker marker =
                    ResourceBundleMarker.parseMarkerValue(result);
            if (marker != null) {
                result = (String) this.getObject();
            }
        } catch (Exception t) {
        }
        return result;
    }

    public String getIconNameOff() {
        return "smalltext";
    }

    public String getIconNameOn() {
        return "smalltext_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared() {
        return true;
    }

    /**
     * Copy the internal value of current language to another language.
     * Must be implemented by conctrete field for specific implementation.
     *
     * @param aField A same field in another language
     */
    public void copyValueInAnotherLanguage(JahiaField aField, ProcessingContext jParams)
            throws JahiaException {
        if (aField == null) {
            return;
        }
        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

    /**
     * Returns an Hashmap of language_code/value used by search index engine
     * the value is an array of String
     */
    public Map<String, String[]> getValuesForSearch() throws JahiaException {

        String lang = this.getLanguageCode();
        if (this.isShared()) {
            lang = ContentField.SHARED_LANGUAGE;
        }

        Map<String, List<String>> tempValues = new HashMap<String, List<String>>();

        for (String val : this.getValues()) {
            ResourceBundleMarker resMarker =
                    ResourceBundleMarker.parseMarkerValue(val);
            if (resMarker == null) {
                List<String> vals = tempValues.get(lang);
                if (vals == null) {
                    vals = new ArrayList<String>();
                }
                if (val == null) {
                    val = "";
                }
                vals.add(val);
                tempValues.put(lang, vals);
            } else {
                JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(this.getJahiaID());
                List<SiteLanguageSettings> siteLanguageSettings = site.getLanguageSettings();
                if (siteLanguageSettings != null) {
                    for (SiteLanguageSettings curSetting : siteLanguageSettings) {
                        if (curSetting.isActivated()) {
                            Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
                            String value = resMarker.getValue(tempLocale);
                            if (value == null) {
                                value = "";
                            }
                            List<String> vals = tempValues.get(tempLocale.toString());
                            if (vals == null) {
                                vals = new ArrayList<String>();
                            }
                            vals.add(value);
                            tempValues.put(tempLocale.toString(), vals);
                        }
                    }
                }
            }
        }
        Map<String, String[]> values = new HashMap<String, String[]>();
        
        for (Map.Entry<String, List<String>> entry : tempValues.entrySet()) {
            values.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]) );
        }
        return values;
    }

    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {

        EntryLoadRequest loadRequest = (EntryLoadRequest) context.getEntryLoadRequest().clone();
        loadRequest.getLocales().clear();
        loadRequest.getLocales().add(LanguageCodeConverters.languageCodeToLocale(languageCode));
        List<String> values = new ArrayList<String>();
        EntryLoadRequest savedEntryLoadRequest =
            context.getSubstituteEntryLoadRequest();
        try {
            context.setSubstituteEntryLoadRequest(loadRequest);
            Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(languageCode);
            for (String val : this.getValues()) {
                ResourceBundleMarker resMarker = expand ? ResourceBundleMarker
                        .parseMarkerValue(val) : null;
                if (resMarker == null) {
                    // expression marker
                    ExpressionMarker exprMarker = expand ? ExpressionMarker
                            .parseMarkerValue(val, context) : null;
                    if (exprMarker != null) {
                        try {
                            val = exprMarker.getValue();
                        } catch (Exception t) {
                        }
                    }

                    if (val == null) {
                        val = "";
                    }
                    values.add(val);
                } else {
                    String value = resMarker.getValue(tempLocale);

                    if (value == null) {
                        value = "";
                    }
                    values.add(value);
                }
            }
        } catch (Exception t) {
            logger.debug(t);
        } finally {
            context.setSubstituteEntryLoadRequest(savedEntryLoadRequest);
        }

        String[] strArray = new String[values.size()];
        int i = 0;
        for (String value : values) {
            strArray[i] = TextHtml.html2text(value);
            i++;
        }

        return strArray;
    }

}
