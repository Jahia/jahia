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
//  JahiaFileFieldWrapper
//  YG      17.07.2001

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.jahia.data.ConnectionTypes;
import org.jahia.data.FormDataManager;
import org.jahia.data.files.JahiaFile;
import org.jahia.data.files.JahiaFileField;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.fields.ContentFileField;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.services.content.JCRStoreService;
import org.jahia.sharing.FieldSharingManager;

public class JahiaFileFieldWrapper extends JahiaField implements JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = -6004418441639789868L;
    
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaFileFieldWrapper.class);

    /**
     * constructor
     * YG    17.07.2001
     */
    public JahiaFileFieldWrapper(Integer ID,
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


        JahiaFileField fField;
        JahiaFile file = new JahiaFile(-1, // filemanager id
                -1, // folder id
                "", // upload user
                "", // realname
                "",    // storage name
                0,    // modif date
                0,    // size
                "",    // type
                "",    // title
                "",   // descr
                String.valueOf(ServicesRegistry.getInstance()
                        .getJahiaVersionService().getCurrentVersionID()),  // version
                JahiaFile.STATE_ACTIVE);
        fField = new JahiaFileField(file, new Properties());
        fField.setID(-1);
        fField.setDownloadUrl("#");
        setObject(fField);
    } // end constructor


    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest)
            throws JahiaException {
        ContentFileField contentFileField =
                (ContentFileField) ContentFileField.getField(getID());
        String val = contentFileField.getValue(jParams, loadRequest);
        this.setValue(FormDataManager.htmlEncode(val));

        switch (this.getConnectType()) {
            case (ConnectionTypes.LOCAL) :
                //this.setValue(this.getValue());
//                if (!this.getValue().equals("<empty>")) {
                this.setRawValue(this.getValue());
                this.setValue(FormDataManager.htmlEncode(this.getValue()));
//                }
                break;
            case (ConnectionTypes.DATASOURCE) :
                if ((loadFlag & LoadFlags.DATASOURCE) != 0) {
                    this.setValue(FieldSharingManager.getInstance().getRemoteFieldValue(
                            this.getValue()));
                }
        }

        JahiaFileField fField;

        //if (((loadFlag & LoadFlags.FILE) != 0) || loadFlag == -1) {
        if (jParams != null) {
            if (jParams.getSiteID() == this.getJahiaID()) {
                fField = JahiaWebdavBaseService.getInstance().getJahiaFileField(jParams, val);
            } else {
                fField = JahiaWebdavBaseService.getInstance().getJahiaFileField(jParams, ServicesRegistry.getInstance().getJahiaSitesService().getSite(getSiteID()), jParams.getUser(), val);
            }
        } else {
            fField = JahiaWebdavBaseService.getInstance().getJahiaFileField(ServicesRegistry.getInstance().getJahiaSitesService().getSite(getSiteID()), val);
        }

        this.setObject(fField);
        //}
    }

//    public void load(int loadFlag, ProcessingContext jParams)
//    throws JahiaException
//    {
//        logger.debug("loading field : " + this.getID() );
//
//        ContentFileField contentFileField =
//                (ContentFileField)ContentFileField.getField(getID());
//        String val = contentFileField.getValue(jParams);
//        this.setValue(FormDataManager.htmlEncode(val));
//
//        int fileFieldID = -1;
//        try {
//            fileFieldID = Integer.parseInt(this.getValue());
//        } catch ( Exception t ){
//            // this can fail in the case of a default value.
//        }
//        JahiaFileField fField = JahiaFileFieldsManager.getInstance()
//                              .getJahiaFileField(fileFieldID);
//
//        // set the download url
//        String dUrl = "#";
//        if (fField != null)
//        {
//            //System.out.println("JahiaFieldBaseService::loadField() fField is not null and id=" + fField.getFileID()  );
//            if ( fField.getFileID() != -1 )
//            {
//                this.setObject(fField);
//                List params = new ArrayList();
//                params.add("actionFileDownload");
//                params.add(this);
//                dUrl = ((Filemanager_Engine)EnginesRegistry.getInstance().
//                    getEngine("filemanager")).renderLink(jParams, params);
//            }
//
//            fField.setDownloadUrl(dUrl);
//            this.setObject(fField);
//        }
//    }


    /**
     * save the information specific to a type
     * save the Object in relation with the field
     * a JahiaPage or a file, for example.
     */
    public boolean save(ProcessingContext jParams) throws JahiaException {
        ContentFileField contentFileField = (ContentFileField) ContentFileField.getField(getID());
        boolean isNew = false;
        if (contentFileField == null) {
            contentFileField = (ContentFileField) ContentFieldTools.getInstance().createContentFieldInstance(0,getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentFileField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }

        JahiaFileField fField = (JahiaFileField) this.getObject();
        // ugly hack to recreate a fField object if the field has been cloned without object value
        if ("".equals(fField.getStorageName()) && getValue() != null && !"<empty>".equals(getValue()) && !"".equals(getValue())) {
            fField = JCRStoreService.getInstance().getFileNode(getValue(), jParams.getUser()).getJahiaFileField(); 
        }
        String value = contentFileField.getValue(jParams);
        if (value == null || "<empty>".equals(value)) { value =""; }
        if (!isNew && fField != null && fField.getStorageName() != null &&
                (fField.getStorageName()).equals(value)) {
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        if (this.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            // if the current field is active, a staging entry is going to be created.
            // we force insert a new jahia file field for the staging entry.
            fField.setID(-1);
        }
        EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), this.getLanguageCode(), isNew);
        contentFileField.setFile(fField, saveRequest);
        //ServicesRegistry.getInstance().getJahiaSearchService().indexContainer(this.getctnid(), jParams.getUser());
        if (getID() == 0) {
            setID(contentFileField.getID());
        }

        JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        fieldLinkManager.deleteReferencesForField(contentFileField.getID(), languageCode, EntryLoadRequest.STAGING_WORKFLOW_STATE);
        if (fField.isDownloadable()) {
            String path = fField.getStorageName();
            fieldLinkManager.createFieldReference(contentFileField.getID(), jahiaID, languageCode, EntryLoadRequest.STAGING_WORKFLOW_STATE, JahiaFieldXRefManager.FILE+path);
        }
        return true;
    }

//    public boolean save(ProcessingContext jParams)
//    throws JahiaException {
//
//        logger.debug("Save File Field...value : " + getValue());
//
//        JahiaFileField fField = (JahiaFileField)this.getObject();
//
//        if ( fField != null ){
//
//            if ( this.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE ){
//                // if the current field is active, a staging entry is going to be created.
//                // we force insert a new jahia file field for the staging entry.
//                fField.setID(-1);
//            }
//            JahiaFileFieldsManager.getInstance().insertJahiaFileField(fField);
//            ContentFileField contentFileField = (ContentFileField) ContentFileField.getField(getID());
//            EntrySaveRequest saveRequest =
//                    new EntrySaveRequest(jParams.getUser(), this.getLanguageCode());
//            contentFileField.setFile(fField, saveRequest);
//            ServicesRegistry.getInstance().getJahiaSearchService().indexField(getID(),jParams);
//        }
//        return true;
//    }

    public String getEngineName() {
        return "org.jahia.engines.shared.DAVFile_Field";
    }

    public String getFieldContent4Ranking() {
        String fieldInfo = "";
        JahiaFileField fle = (JahiaFileField) this.getObject();
        if (fle != null) {
            fieldInfo = fle.getFileFieldTitle();
        } else {
            fieldInfo = this.getValue().substring(getValue().lastIndexOf('/') + 1);
        }
        return fieldInfo;
    }

    public String getIconNameOff() {
        return "file";
    }

    public String getIconNameOn() {
        return "file_on";
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
        // check the field is in different language first.
        if (this.getLanguageCode() != aField.getLanguageCode()) {
            aField.setValue(this.getValue()); // force create a new Jahia File Filed
            aField.setRawValue(this.getRawValue());
            JahiaFileField fField =
                    (JahiaFileField) this.getObject();
            aField.setObject(fField);
        }
    }

    /**
     * Copy the internal value of this field to the passed field.
     * Should be implemented by concrete field for specific implementation.
     * <p/>
     * Can be used to restore versioned value to a staging field.
     *
     * @param aField A same field in another language
     */
    public void copyValueToAnotherField(JahiaField aField, ProcessingContext jParams) throws JahiaException {
        if (aField == null) {
            return;
        }
        // check the field is in different language first.
        aField.setValue(this.getValue()); // force create a new Jahia File Filed
        aField.setRawValue(this.getRawValue());
        JahiaFileField fField =
                (JahiaFileField) this.getObject();
        aField.setObject(fField);
    }
    
    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {
        return EMPTY_STRING_ARRAY;
    }
}
