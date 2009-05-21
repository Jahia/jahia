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
//  JahiaDateField
//  YG      08.08.2001

package org.jahia.data.fields;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.containers.ContainerFacadeInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentDateField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.sharing.FieldSharingManager;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.validation.ValidationError;
import org.apache.lucene.document.DateTools;

import java.util.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class JahiaDateField extends JahiaField implements JahiaAllowApplyChangeToAllLangField, JahiaSimpleField {

    private static final long serialVersionUID = 4232615921522548444L;
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaDateField.class);

    /**
     * constructor
     * YG
     */
    public JahiaDateField(Integer ID,
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

        if (fieldValue != null
                && fieldValue.toLowerCase().indexOf("jahia_calendar") != -1) {
            JahiaDateFieldUtil.DateValues defaultValue = JahiaDateFieldUtil
                    .parseDateFieldDefaultValue(fieldValue, languageCode);
            if (defaultValue != null) {
                this.fieldValue = defaultValue.getValueString();
                this.fieldRawValue = defaultValue.getValueString();
                this.objectItem = defaultValue.getValueObject();
            } else {
                this.fieldValue = "";
                this.fieldRawValue = "";
                this.objectItem = "";
            }
        }

        if (isShared()) {
            this.languageCode = ContentField.SHARED_LANGUAGE;
        }

    } // end constructor

    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {
        switch (this.getConnectType()) {
            case (ConnectionTypes.LOCAL):
                // this.setValue(this.getValue());
                if (!"<empty>".equals(this.getValue())) {

                    String defValue = "";
                    if (this.pageID != 0) {
                        defValue = this.getDefinition().getDefaultValueAsNull();
                        // If defvalue is null try to get a defaultValue as all fields with the same name
                        // must have the same properties to avoid them being considered as different.
                        // See the warning message in console top to date ensure your definitions are right.
                        if (defValue == null)
                            defValue = getDefinition().getDefaultValue();
                    } else {
                        defValue = this.getDefinition().getDefaultValue();
                    }

                    if (this.getValue() != null && this.getValue().length() > 0
                            && !this.getValue().equals("null")) {
                        try {
                            long timeValue = Long.parseLong(this.getValue());
                            this.setObject(this.getValue());
                            this.setValue(JahiaDateFieldUtil.getDateFormat(
                                    defValue, jParams.getLocale()).format(
                                    new Date(timeValue)));
                            this.setRawValue(this.getValue());
                        } catch (NumberFormatException e) {
                            this.setValue("");
                            this.setObject("");
                            break;
                        }
                    } else {
                        this.setValue("");
                        this.setObject("");
                        break;
                    }
                }
                break;
            case (ConnectionTypes.DATASOURCE):
                if ((loadFlag & LoadFlags.DATASOURCE) != 0) {
                    this.setValue(FieldSharingManager.getInstance().
                            getRemoteFieldValue(
                            this.getValue()));
                    this.setObject(this.getValue());
                }
        }

    }

    public boolean save(ProcessingContext jParams) throws JahiaException {

        String oldValue = this.fieldValue;

        setValue((String) getObject());
        //System.out.println("########### Save - Value: "+theField.getValue());
        // 0 for parentAclID in saveField, because field already exists
        //  -> field already has an aclID
        //  -> no need to create a new one

        // deprecated
        //ServicesRegistry.getInstance().getJahiaFieldService().saveField( theField, 0, jParams );

        ContentDateField contentField = (ContentDateField) ContentField.getField(getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentDateField) ContentFieldTools.getInstance().createContentFieldInstance(0, getJahiaID(), getPageID(), getctnid(),
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

        try {
            final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), getLanguageCode(), isNew);
            contentField.setValue(getValue(), saveRequest);

            if (getID() == 0) {
                setID(contentField.getID());
            }
            //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(
            //    this.getctnid(), jParams.getUser());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // restore all value
            this.setValue(oldValue);
            return false;
        }
        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.Date_Field";
    }

    public String getFieldContent4Ranking() {
        return this.getValue();
    }

    public String getIconNameOff() {
        return "date_off";
    }

    public String getIconNameOn() {
        return "date_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
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
    public void copyValueInAnotherLanguage(JahiaField aField,
                                           ProcessingContext jParams)
            throws JahiaException {
        if (aField == null) {
            return;
        }
        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context) throws JahiaException {

        String val = (String) this.getObject();
        if (val == null) {
            val = "";
        } else {
            try {
                long longVal = Long.parseLong(val);
                int dateRounding = ServicesRegistry.getInstance().getJahiaSearchService().getDateRounding();
                if (dateRounding > 0 && longVal > 0) {
                    if (dateRounding >= 1440) {
                        longVal = DateTools.round(longVal, DateTools.Resolution.DAY);
                    } else if (dateRounding >= 60) {
                        longVal = DateTools.round(longVal, DateTools.Resolution.HOUR);
                    } else {
                        longVal = DateTools.round(longVal, DateTools.Resolution.MINUTE);
                    }
                }
                val = String.valueOf(longVal);
            } catch (Exception t) {
                logger.debug("Exception rounding date field");
            }
        }

        return new String[]{val};
    }

    @Override
    public ValidationError validate(
            ContainerFacadeInterface jahiaContentContainerFacade,
            EngineLanguageHelper elh,
            ProcessingContext ctx) throws JahiaException {
        final ValidationError result = super.validate(
                jahiaContentContainerFacade, elh, ctx);
        if (result != null)
            return result;

        final String fieldValue = getValue();
        if (fieldValue != null && fieldValue.length() > 0) {
            final String dateFormat = this.getDefinition().getDefaultValue();
            try {
                final SimpleDateFormat fmt = JahiaDateFieldUtil
                        .getDateFormatForParsing(dateFormat, elh.getCurrentLocale());
                fmt.parse(fieldValue);

            } catch (final ParseException ex) {
                return new ValidationError(this,
                        "The date you typed is not in the right format",
                        "org.jahia.data.fields.JahiaDateField.unparsableDate",
                        new String[] { fieldValue, dateFormat });
            }
        }
        return null;
    }
}
