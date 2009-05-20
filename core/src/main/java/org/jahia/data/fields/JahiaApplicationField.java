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
//  JahiaApplicationField
//  YG      17.07.2001

package org.jahia.data.fields;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.pluto.PortletWindow;
import org.jahia.data.ConnectionTypes;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.fields.ContentApplicationField;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;

public class JahiaApplicationField extends JahiaField implements JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = -1503368255523653387L;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaApplicationField.class);

    /**
     * constructor
     * YG    17.07.2001
     */
    public JahiaApplicationField(Integer ID, Integer jahiaID, Integer pageID, Integer ctnid, Integer fieldDefID, Integer fieldType, Integer connectType, String fieldValue, Integer rank, Integer aclID, Integer versionID, Integer versionStatus, String languageCode) {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType, fieldValue, rank, aclID, versionID, versionStatus, languageCode);

        if (isShared()) {
            this.languageCode = ContentField.SHARED_LANGUAGE;
        }

    } // end constructor


    public void load(int loadFlag, ProcessingContext jParams, EntryLoadRequest loadRequest) throws JahiaException {
        // this.setObject(this.getValue()); // to save the app id... used in engines

        try {
            logger.debug("Loading application field...");

            String entryPointID = null;
            try {
                if (this.getValue().length() > 0 && !this.getValue().equals("<empty>")) {
                    entryPointID = this.getValue();
                }
            } catch (NumberFormatException nfe) {
                logger.debug("Error while parsing application id to convert to integer", nfe);
            }
            EntryPointInstance entryPointInstance = null;
            if (entryPointID != null) {
                entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(entryPointID);
                if (entryPointInstance == null) {
                    if (logger.isDebugEnabled())
                        logger.debug("User " + jParams.getUser().getName() + " could not load the portlet instance :" + entryPointID);
                    entryPointInstance = null;
                }
            }
            if (entryPointInstance != null) {
                ApplicationBean appBean = ServicesRegistry.getInstance().
                        getApplicationsManagerService().
                        getApplication(entryPointInstance.getContextName());
                if (appBean != null) {
                    String defName = entryPointInstance.getDefName();
                    int separatorPos = defName.indexOf("###");
                    String portletWindowID = Integer.toString(getID());
                    if (separatorPos != -1) {
                        String portletDefName = defName.substring(0, separatorPos);
                        String portletEntityID = defName.substring(separatorPos + "###".length());
                        defName = portletDefName;
                        portletWindowID = portletEntityID;
                    } else {
                        int plutoSeperatorPos = defName.indexOf(".");
                        if (plutoSeperatorPos != -1) {
                            String portletContext = defName.substring(0, plutoSeperatorPos);
                            String portletDefName = defName.substring(plutoSeperatorPos + ".".length());
                            defName = portletDefName;
                            portletWindowID = entryPointInstance.getID();
                        }
                    }
                    EntryPointDefinition entryPointDefinition = appBean.getEntryPointDefinitionByName(defName);
                    if (jParams instanceof ParamBean) {
                        PortletWindow window = ServicesRegistry.getInstance().getApplicationsManagerService().getPortletWindow(entryPointInstance, portletWindowID, (ParamBean) jParams);
                        PortletWindowBean portletWindowBean = new PortletWindowBean(jParams, window);
                        portletWindowBean.setEntryPointInstance(entryPointInstance);
                        portletWindowBean.setEntryPointDefinition(entryPointDefinition);
                        setObject(portletWindowBean);
                    } else setObject(null);
                } else {
                    setObject(null);
                }

            } else {
                setObject(null);
            }

            // Todo remove this code and move it to getvalue
            ContentApplicationField contentApplicationField = (ContentApplicationField) ContentApplicationField.getField(getID());
            logger.debug("value=" + getValue());

            String val = contentApplicationField.getValue(jParams, loadRequest);

            this.setValue(val);
            // this.setValue(JahiaTools.replacePattern(FormDataManager.htmlEncode(val), "&#64;", "@"));

        } catch (Exception t) {
            logger.error("Problem loading application field value for field " + getID(), t);
            this.setValue("Exception loading application data");
        }
    }

    public boolean save(ProcessingContext jParams) throws JahiaException {
        ContentApplicationField contentField = (ContentApplicationField) ContentField.getField(this.getID());
        boolean isNew = false;
        if (contentField == null) {
            contentField = (ContentApplicationField) ContentFieldTools.getInstance().createContentFieldInstance(0, getJahiaID(), getPageID(), getctnid(), getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }


        switch (this.getConnectType()) {
            case (ConnectionTypes.LOCAL):

                if ((getValue() == null || getValue().equals("<empty>") || getValue().equals(contentField.getValue(jParams))) && !isNew) {
                    return true;
                }

                final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), this.getLanguageCode(), isNew);
                String appID = null;
                try {
                    appID = this.getValue();
                } catch (NumberFormatException e) {
                    contentField.unsetValue(saveRequest);

                    if (getID() == 0) {
                        setID(contentField.getID());
                    }

                    return true;
                }
                final ApplicationsManagerService service = ServicesRegistry.getInstance().getApplicationsManagerService();
                EntryPointInstance app = service.getEntryPointInstance(appID);

                if (app != null) {

                    // check if the app has changed
                    PortletWindowBean oldAppID = (PortletWindowBean) this.getObject();
                    if ((oldAppID != null) && !oldAppID.getEntryPointInstanceID().equals(appID)) {
                        // App has changed, so delete old groups
                        service.deleteApplicationGroups(oldAppID.getEntryPointInstance());
                    }

                    // Create new groups on this field ( only if not exists )
                    service.createApplicationGroups(app);
                }
                jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
                logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

                contentField.setAppID(appID, saveRequest);
                break;
        }

        // update field ID
        if (isNew) {
            setID(contentField.getID());
            logger.debug("Create new Field ID: " + this.getID());
        }

        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.Application_Field";
    }

    public String getFieldContent4Ranking() throws JahiaException {
        String fieldInfo = "";
        PortletWindowBean app = (PortletWindowBean) this.getObject();

        if (app != null) {
            fieldInfo = app.getEntryPointInstance().getDefName();
        } else {
            fieldInfo = this.getValue();
        }
        return fieldInfo;
    }

    public String getIconNameOff() {
        return "application";
    }

    public String getIconNameOn() {
        return "application_on";
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
    public void copyValueInAnotherLanguage(JahiaField aField, ProcessingContext jParams) throws JahiaException {
        if (aField == null) {
            return;
        }

        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

}
