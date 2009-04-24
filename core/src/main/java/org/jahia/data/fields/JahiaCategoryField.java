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

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.jahia.services.categories.Category;
import org.jahia.services.fields.ContentCategoryField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.sharing.FieldSharingManager;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.engines.shared.Category_Field;

import java.util.*;

/**
 * User: Serge Huber
 * Date: 23 ao�t 2005
 * Time: 09:30:06
 * Copyright (C) Jahia Inc.
 */
public class JahiaCategoryField extends JahiaField implements JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = 3669579811889812977L;
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaCategoryField.class);

    public JahiaCategoryField(
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
                    if (!Category_Field.NOSELECTION_MARKER.equals(getValue())) {
                        this.objectItem = ExpressionMarker.getValue(this.getValue(), jParams);
                        if (jParams != null) {
                            this.objectItem = ResourceBundleMarker.getValue((String) this.objectItem, jParams.getLocale());
                        }
                        this.setRawValue(this.getValue());
                        this.setValue(FormDataManager.htmlEncode(this.getValue()));
                    } else {
                        this.objectItem = this.getValue();
                        this.setRawValue(this.getValue());
                        this.setValue(this.getValue());
                    }
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
        return "org.jahia.engines.shared.Category_Field";
    }

    public boolean save(ProcessingContext jParams) throws JahiaException {
        // 0 for parentAclID in saveField, because field already exists
        //  -> field already has an aclID
        //  -> no need to create a new one

        //ServicesRegistry.getInstance().getJahiaFieldService().saveField( theField, 0, jParams );
        ContentCategoryField contentField = (ContentCategoryField) ContentField.getField(getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentCategoryField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }

        final String value = getValue();
        String savedValue = contentField.getValue(jParams);
        if (savedValue != null && savedValue.equals("")) {
            savedValue = Category_Field.NOSELECTION_MARKER;
        }
        if (((value == null && savedValue == null && !isNew) || (value != null && getValue().equals(savedValue)))) {
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        /*
        if (contentField.hasActiveEntries() && getValue() != null && getValue().equals(contentField.getValue(jParams))) {
            return true;
        }*/
        final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), getLanguageCode(), isNew);
        contentField.setCategories(getValue(), saveRequest);

        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getctnid(), jParams.getUser());
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
        return "category";
    }

    public String getIconNameOn() {
        return "category_on";
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
     *
     * @return a Map of language_code/value pairs used by search index engine
     */
    public Map<String, String[]> getValuesForSearch() throws JahiaException {

        Map<String, List<String>> tempValues = new HashMap<String, List<String>>();
        List<String> fieldRawValues = new ArrayList<String>();
        String[] strVals = getValue() != null
                && Category_Field.NOSELECTION_MARKER.equals(getValue()) ? EMPTY_STRING_ARRAY
                : this.getValues();
        if (strVals != null) {
            fieldRawValues.addAll(Arrays.asList(strVals));
        }

        for (String curFieldRawValue : fieldRawValues) {
            if (curFieldRawValue.trim().length() > 0) {
                JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(this.getJahiaID());
                List<SiteLanguageSettings> siteLanguageSettings = site
                    .getLanguageSettings();
                if (siteLanguageSettings != null) {
                    for (SiteLanguageSettings curSetting : siteLanguageSettings) {
                        if (curSetting.isActivated()) {
                            Locale tempLocale = LanguageCodeConverters
                                .languageCodeToLocale(curSetting.getCode());
                            Category curCategory = Category.getCategory(
                                curFieldRawValue, null);
                            if (curCategory == null) {
                                logger
                                    .warn("Couldn't find category "
                                            + curFieldRawValue
                                            + " when indexing field, ignoring entry...");
                                continue;
                            }
                            String value = curCategory.getTitle(tempLocale);
                            if (value == null || value.length() == 0) {
                                value = curCategory.getKey();
                            }
                            List<String> vals = tempValues.get(tempLocale
                                .toString());
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

    /**
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {

        if (getValue() != null && Category_Field.NOSELECTION_MARKER.equals(getValue())) {
            return EMPTY_STRING_ARRAY;
        }
        
        List<String> fieldRawValues = new ArrayList<String>();
        String[] strVals = this.getValues();
        if (strVals != null) {
            fieldRawValues.addAll(Arrays.asList(strVals));
        }

        Locale tempLocale = LanguageCodeConverters
            .languageCodeToLocale(languageCode);
        List<String> vals = new ArrayList<String>();
        for (String val : fieldRawValues) {
            if (val.trim().length() > 0) {
                Category curCategory = Category.getCategory(val, null);
                if (curCategory == null) {
                    logger.warn("Couldn't find category " + val
                            + " when indexing field, ignoring entry...");
                    continue;
                }
                val = curCategory.getTitle(tempLocale);
                if (val == null || val.length() == 0) {
                    val = curCategory.getKey();
                }
                vals.add(val);
            }
        }
      
        return vals.toArray(new String[vals.size()]);
    }

}
