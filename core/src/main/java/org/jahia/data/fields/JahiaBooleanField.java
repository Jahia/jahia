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

//  JahiaBooleanField
//  YG      08.08.2001

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.HashMap;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.fields.ContentBooleanField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.sharing.FieldSharingManager;

public class JahiaBooleanField extends JahiaField implements JahiaSimpleField, JahiaAllowApplyChangeToAllLangField {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaBooleanField.class);


    /**
     * constructor
     * YG
     */
    public JahiaBooleanField(Integer ID,
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
                    this.setRawValue(this.getValue());
                    this.setValue(FormDataManager.htmlEncode(this.getValue()));

                    if (this.getValue() != null && !this.getValue().equals("") && (this.getValue().equals("true") ||
                            this.getValue().equals("false"))) {
                        this.setObject(Boolean.valueOf(this.getValue()));
                    } else {
                        this.setObject(Boolean.TRUE);
                        this.setValue("");
                    }
                }
                break;
            case (ConnectionTypes.DATASOURCE) :
                if ((loadFlag & LoadFlags.DATASOURCE) != 0) {
                    this.setValue(FieldSharingManager.getInstance().getRemoteFieldValue(
                            this.getValue()));

                    if (this.getValue() != null && !this.getValue().equals("")) {
                        this.setObject(Boolean.valueOf(this.getValue()));
                    } else {
                        this.setObject(Boolean.TRUE);
                    }
                }
        }

    }

    public boolean save(ProcessingContext jParams) throws JahiaException {
        //setValue( FormDataManager.encode( getValue() ) );

        // 0 for parentAclID in saveField, because field already exists
        //  -> field already has an aclID
        //  -> no need to create a new one

        // deprecated
        // ServicesRegistry.getInstance().getJahiaFieldService().saveField( theField, 0, jParams );
        ContentBooleanField contentField = (ContentBooleanField) ContentField.getField(getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentBooleanField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList(), new HashMap());
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

        final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), getLanguageCode(), isNew);
        try {
            contentField.setValue((Boolean.valueOf(getValue())).booleanValue(), saveRequest);
            //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getctnid(), jParams.getUser());

            if (getID() == 0) {
                setID(contentField.getID());
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.Boolean_Field";
    }

    public String getFieldContent4Ranking() {
        return this.getValue();
    }

    public String getIconNameOff() {
        return "01_off";
    }

    public String getIconNameOn() {
        return "01_on";
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
