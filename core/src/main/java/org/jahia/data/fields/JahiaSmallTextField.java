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
//  JahiaSmallTextField
//  YG      17.07.2001

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.HashMap;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.sharing.FieldSharingManager;

public class JahiaSmallTextField extends JahiaField implements JahiaSimpleField, JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = -696597708047166956L;
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaSmallTextField.class);

    /**
     * constructor
     * YG    17.07.2001
     */
    public JahiaSmallTextField(Integer ID,
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
                    String fieldValue = this.getValue();
                    this.setRawValue(fieldValue);
                    this.setValue(FormDataManager.htmlEncode(fieldValue));
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

    public boolean save(ProcessingContext jParams) throws JahiaException {
        // 0 for parentAclID in saveField, because field already exists
        //  -> field already has an aclID
        //  -> no need to create a new one
        boolean isNew = false;
        ContentSmallTextField contentField = (ContentSmallTextField) ContentField.getField(getID());
        if (contentField == null) {
            contentField = (ContentSmallTextField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }

        if (getValue() != null &&
                getValue().equals(contentField.getValue(jParams))) {
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        //ServicesRegistry.getInstance().getJahiaFieldService().saveField( theField, 0, jParams );
        final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), getLanguageCode(), isNew);

        /**
         if (contentField.hasActiveEntries() && getValue() != null && getValue().equals(contentField.getValue(jParams))) {
         return true;
         }*/
        contentField.setText(getValue(), saveRequest);

        if (getID() == 0) {
            setID(contentField.getID());
        }
        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getctnid(), jParams.getUser());
        return true;
    }

    public String getValue() {
        return fieldValue;
    }

    public String getFieldContent4Ranking() {
        return this.getValue();
    }

    public String getIconNameOff() {
        return "smalltext";
    }

    public String getIconNameOn() {
        return "smalltext_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return false;
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

}
