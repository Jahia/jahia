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
package org.jahia.data.beans;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.PropertiesInterface;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.InsertionSortedMap;

import java.util.*;

/**
 * <p>Title: Container JavaBean compliant JahiaContainer facade</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version $Id$
 */

public class ContainerBean extends ContentBean implements PropertiesInterface {

    private static final transient Logger logger = Logger.getLogger(ContainerBean.class);

    public static final String TYPE = "ContentContainer";

    private JahiaContainer jahiaContainer;
    private ContentContainer contentContainer;
    private Map<String, FieldBean> fields;
    private Map<String, ActionURIBean> actionURIs;
    private boolean completelyLocked;
    private boolean independantWorkflow;
    private boolean independantWorkflowInitialized = false;
    private int groupWorkflowState = 0;
    private boolean groupWorkflowStateInitialized = false;

    static {
        registerType(ContentContainer.class.getName(), ContainerBean.class.getName());
    }

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private final ImportExportService importExportService = servicesRegistry.getImportExportService();
    private final LockService lockRegistry = servicesRegistry.getLockService();

    public ContainerBean() {
    }

    public ContainerBean(final JahiaContainer aJahiaContainer,
                         final ProcessingContext aProcessingContext) {
        this.jahiaContainer = aJahiaContainer;
        this.processingContext = aProcessingContext;
    }

    public static AbstractJahiaObjectBean getChildInstance(final JahiaObject jahiaObject,
                                                           final ProcessingContext processingContext) {
        final ContentContainer contentContainer = (ContentContainer) jahiaObject;
        try {
            return new ContainerBean(contentContainer.getJahiaContainer(
                    processingContext, processingContext.getEntryLoadRequest()), processingContext);
        } catch (JahiaException je) {
            logger.error("Error while converting content container to jahia container", je);
            return null;
        }
    }

    public int getID() {
        return jahiaContainer.getID();
    }

    public String getBeanType() {
        return TYPE;
    }

    public JahiaContainer getJahiaContainer() {
        return jahiaContainer;
    }

    public ContentContainer getContentContainer() {
        if (contentContainer == null) {
            contentContainer = jahiaContainer.getContentContainer();
        }
        return contentContainer;
    }

    public JahiaContainerDefinition getDefinition() {
        try {
            return jahiaContainer.getDefinition();
        } catch (JahiaException je) {
            logger.error(
                    "Error while accessing container list definition for container " +
                            getID() + ":", je);
            return null;
        }
    }

    public int getDefinitionID() {
        return getDefinition().getID();
    }

    public FieldBean getField(final String name) {
        try {
            final JahiaField jahiaField = jahiaContainer.getField(name);
            if (jahiaField == null) {
                return null;
            }
            return new FieldBean(jahiaField, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while retrieving field " + name +
                    " for container " + getID() + ":", je);
            return null;
        }
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName the field name
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName)
            throws JahiaException {
        return getFieldValue(fieldName, false, null);
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName
     * @param allowDiffVersionHighlight
     * @return
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName,
                                final boolean allowDiffVersionHighlight,
                                final ProcessingContext jParams)
            throws JahiaException {
        final FieldBean theField = this.getField(fieldName);
        if (theField != null) {
            if (!allowDiffVersionHighlight) {
                return theField.getValue();
            } else {
                return theField.getHighLightDiffValue();
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName    the field name
     * @param defaultValue
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName,
                                final String defaultValue,
                                final boolean allowDiffVersionHighlight,
                                final ProcessingContext jParams)
            throws JahiaException {
        final String value = (String)
                getFieldValue(fieldName, allowDiffVersionHighlight, jParams);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName    the field name
     * @param defaultValue
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldValue(final String fieldName, final String defaultValue)
            throws JahiaException {
        final String value = (String) getFieldValue(fieldName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    //-------------------------------------------------------------------------

    /**
     * get a field value
     *
     * @param fieldName the field name
     * @throws JahiaException a critical jahia exception if field not found
     */
    public Object getFieldObject(final String fieldName)
            throws JahiaException {
        final FieldBean theField = this.getField(fieldName);
        if (theField != null) {
            return theField.getObject();
        }
        return null;
    }


    public Map<String, FieldBean> getFields() {
        if (fields != null) {
            return fields;
        }
        fields = new TreeMap<String, FieldBean>();
        final Iterator<JahiaField> fieldEnum = jahiaContainer.getFields();
        while (fieldEnum.hasNext()) {
            final JahiaField curJahiaField = fieldEnum.next();
            final FieldBean curFieldBean = new FieldBean(curJahiaField, processingContext);
            fields.put(curFieldBean.getName(), curFieldBean);
        }
        return fields;
    }

    public ContainerListBean getContainerList(final String name) {
        try {
            final JahiaContainerList jahiaContainerList = jahiaContainer.
                    getContainerList(name);
            if (jahiaContainerList == null) {
                return null;
            }
            return new ContainerListBean(
                    jahiaContainerList, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while retrieving sub container list " + name +
                    " for container " + getID() + ":", je);
            return null;
        }
    }

    public int getSiteID() {
        return jahiaContainer.getSiteID();
    }

    public int getNbFields() {
        return jahiaContainer.getNbFields();
    }

    public int getContainerDefinitionID() {
        return jahiaContainer.getctndefid();
    }

    public JahiaBaseACL getACL() {
        return jahiaContainer.getACL();
    }

    public int getAclID() {
        return jahiaContainer.getAclID();
    }

    public int getPageID() {
        return jahiaContainer.getPageID();
    }

    public int getContainerListID() {
        return jahiaContainer.getListID();
    }

    public int getVersionID() {
        return jahiaContainer.getVersionID();
    }

    public int getWorkflowState() {
        return jahiaContainer.getWorkflowState();
    }

    public String getLanguageCode() {
        return jahiaContainer.getLanguageCode();
    }

    public int getRank() {
        return jahiaContainer.getRank();
    }

    public Properties getProperties() {
        return jahiaContainer.getProperties();
    }

    public void setProperties(final Properties properties) {
        jahiaContainer.setProperties(properties);
    }

    public String getProperty(final String propertyName) {
        return jahiaContainer.getProperty(propertyName);
    }

    public void setProperty(final String propertyName, final String propertyValue) {
        jahiaContainer.setProperty(propertyName, propertyValue);
    }

    public ContentBean getParent() {
        return getParentContainerList();
    }

    public ContainerListBean getParentContainerList() {
        int containerListID = getContainerListID();
        try {
            final ContentContainerList parentContainerList = ContentContainerList.
                    getContainerList(containerListID);
            final JahiaContainerList parentJahiaContainerList = parentContainerList.
                    getJahiaContainerList(processingContext,
                            processingContext.getEntryLoadRequest());
            return new ContainerListBean(parentJahiaContainerList, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while trying to retrieve parent container list ID=" + containerListID, je);
        }
        return null;
    }

    public boolean isPicker() {
        return false;
    }

    public ContentObject getContentObject() {
        return getContentContainer();
    }

    public String getJCRPath() throws JahiaException {
        return getContentContainer().getJCRPath(processingContext);
    }

}
