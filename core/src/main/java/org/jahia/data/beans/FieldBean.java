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

package org.jahia.data.beans;

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaDateField;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.gui.GuiBean;
import org.jahia.gui.HTMLToolBox;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.InsertionSortedMap;
import org.jahia.ajax.usersession.userSettings;
import org.jahia.bin.Jahia;

import java.util.*;


/**
 * <p>Title: Field JavaBean compliant JahiaField facade</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber, Xavier Lawrence
 * @version 1.0
 */

public class FieldBean extends ContentBean {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(FieldBean.class);

    public static final String TYPE = "ContentField";

    private JahiaField jahiaField;
    private ContentField contentField;
    private Properties properties;
    private Map<String, ActionURIBean> actionURIs;
    private boolean completelyLocked = false;
    private boolean independantWorkflow = false;
    private boolean independantWorkflowInitialized = false;
    private int groupWorkflowState = 0;
    private boolean groupWorkflowStateInitialized = false;
    
    private PageBean originalParentPage = null;

    private final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private final WorkflowService workflowService = servicesRegistry.getWorkflowService();
    private final ImportExportService importExportService = servicesRegistry.getImportExportService();
    private final LockService lockRegistry = servicesRegistry.getLockService();

    public FieldBean() {
    }

    static {
        registerType(ContentField.class.getName(), FieldBean.class.getName());
    }


    public FieldBean(final JahiaField jahiaField, final ProcessingContext processingContext) {
        this.jahiaField = jahiaField;
        this.processingContext = processingContext;
    }

    public static AbstractJahiaObjectBean getChildInstance(
            final JahiaObject jahiaObject,
            final ProcessingContext processingContext) {
        final ContentField contentField = (ContentField) jahiaObject;
        try {
            return new FieldBean(contentField.getJahiaField(
                    processingContext.getEntryLoadRequest()), processingContext);
        } catch (JahiaException je) {
            logger.error("Error while converting content container to jahia container", je);
            return null;
        }
    }

    public JahiaField getJahiaField() {
        return jahiaField;
    }

    public String getValue() {
        return jahiaField.getValue();
    }

    public Object getObject() {
        final Object fieldObject = jahiaField.getObject();
        if (fieldObject == null) {
            return null;
        }
        if (fieldObject.getClass() == JahiaPage.class) {
            return new PageBean((JahiaPage) fieldObject, processingContext);
        }
        return fieldObject;
    }

    public int getID() {
        return jahiaField.getID();
    }

    public String getBeanType() {
        return TYPE;
    }

    public String getHighLightDiffValue() {
        return jahiaField.getHighLightDiffValue(processingContext);
    }

    public JahiaFieldDefinition getDefinition() {
        try {
            return jahiaField.getDefinition();
        } catch (JahiaException je) {
            logger.error("Error retrieving field definition for field " + getID() + ":", je);
            return null;
        }
    }

    public int getDefinitionID() {
        return getDefinition().getID();
    }

    public String getName() {
        try {
            return jahiaField.getDefinition().getName();
        } catch (JahiaException je) {
            logger.error("Error retrieving field definition for field " + getID() + ":", je);
            return null;
        }
    }

    public String getTitle() {
        try {
            return jahiaField.getDefinition().getTitle(processingContext.getLocale());
        } catch (JahiaException je) {
            logger.error("Error retrieving field definition for field " + getID() + ":", je);
            return null;
        }
    }

    public int getContainerID() {
        return jahiaField.getctnid();
    }

    public ContentField getContentField() {
        if (contentField == null) {
            contentField = jahiaField.getContentField();
        }
        return contentField;
    }

    public String getAnchor() {
        return jahiaField.getAnchor();
    }

    public JahiaBaseACL getACL() {
        return jahiaField.getACL();
    }

    public Properties getProperties() {
        if (properties != null) {
            return properties;
        }
        properties = new Properties();
        final Map<Object, Object> fieldProps = jahiaField.getProperties();
        final Iterator<?> propNameEnum = fieldProps.keySet().iterator();
        while (propNameEnum.hasNext()) {
            final String curPropName = (String) propNameEnum.next();
            final String curPropValue = (String) fieldProps.get(curPropName);
            properties.setProperty(curPropName, curPropValue);
        }
        return properties;
    }

    public String getRawValue() {
        return jahiaField.getRawValue();
    }

    public int getSiteID() {
        return jahiaField.getSiteID();
    }

    public int getFieldType() {
        return jahiaField.getType();
    }

    /**
     * @deprecated Use getFieldType() instead
     */
    public int getType() {
        return jahiaField.getType();
    }

    public String getIconNameOff() {
        return jahiaField.getIconNameOff();
    }

    public String getIconNameOn() {
        return jahiaField.getIconNameOn();
    }

    public int getAclID() {
        return jahiaField.getAclID();
    }

    public int getConnectType() {
        return jahiaField.getConnectType();
    }

    public int getFieldDefinitionID() {
        return jahiaField.getFieldDefID();
    }

    public String getLanguageCode() {
        return jahiaField.getLanguageCode();
    }

    public int getPageID() {
        return jahiaField.getPageID();
    }

    public int getRank() {
        return jahiaField.getRank();
    }

    public int getVersionID() {
        return jahiaField.getVersionID();
    }

    public int getWorkflowState() {
        return jahiaField.getWorkflowState();
    }

    public Map<String, ActionURIBean> getActionURIBeans() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        return actionURIs;
    }

    public boolean isCompletelyLocked() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        return completelyLocked;
    }

    public boolean isPartiallyLocked() {
        if (actionURIs == null) {
            buildActionURIs();
        }
        if (!completelyLocked) {
            boolean partiallyLocked = false;
            for (final Map.Entry<String, ActionURIBean> curActionURIEntry : actionURIs.entrySet()) {
                final ActionURIBean curActionURIBean = curActionURIEntry.getValue();
                if (curActionURIBean.isLocked()) {
                    partiallyLocked = true;
                }
            }
            return partiallyLocked;
        } else {
            return false;
        }
    }

    public boolean isIndependantWorkflow() {

        if (!independantWorkflowInitialized) {
            try {
                final ContentField theField = jahiaField.getContentField();
                final int mode = workflowService.getWorkflowMode(theField);
                // Set independantWorkflow variable
                independantWorkflow = (mode != WorkflowService.LINKED);
                independantWorkflowInitialized = true;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return independantWorkflow;
    }

    public int getGroupWorkflowState() {

        if (!groupWorkflowStateInitialized) {
            try {
                final ContentField theField = jahiaField.getContentField();
                final Map<String, Integer> languagesStates = workflowService.getLanguagesStates(
                        theField);
                Integer languageState = languagesStates.get(
                        processingContext.getLocale().toString());
                final Integer sharedLanguageState = languagesStates.get(
                        ContentObject.SHARED_LANGUAGE);
                if (languageState != null && languageState.intValue() != -1) {
                    if (sharedLanguageState != null &&
                            languageState.intValue() < sharedLanguageState.intValue()) {
                        languageState = sharedLanguageState;
                    }
                } else if (languageState == null) {
                    languageState = sharedLanguageState;
                }
                // Set groupWorkflowState variable
                groupWorkflowState = (languageState != null) ? languageState.intValue() : 1;
                groupWorkflowStateInitialized = true;
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return groupWorkflowState;
    }

    public boolean isActionURIsEmpty() {
        if (!processingContext.getOperationMode().equals(ProcessingContext.EDIT)) {
            return true;
        }
        if (actionURIs == null) {
            buildActionURIs();
        }
        return actionURIs.isEmpty();
    }
    
    public PageBean getOriginalParentPage() {
    	if (originalParentPage == null) {
			try {
				JahiaPage parentJahiaPage = null;
				if (jahiaField.getPageID() == processingContext.getPageID()) {
					parentJahiaPage = processingContext.getPage();
				} else {
					final ContentPage parentContentPage = ContentPage
							.getPage(jahiaField.getPageID());
					parentJahiaPage = parentContentPage
							.getPage(processingContext);
				}
				originalParentPage = new PageBean(parentJahiaPage,
						processingContext);
			} catch (JahiaException je) {
				logger.error("Error while loading parent page "
						+ jahiaField.getPageID() + " for field "
						+ jahiaField.getID(), je);
			}
		}
		return originalParentPage;
	}

    public ContentBean getParent() {
        if (jahiaField.getctnid() == 0) {
            try {
                final ContentPage parentContentPage = ContentPage.getPage(jahiaField.
                        getPageID());
                final JahiaPage parentJahiaPage = parentContentPage.getPage(processingContext);
                return new PageBean(parentJahiaPage, processingContext);
            } catch (JahiaException je) {
                logger.error("Error while loading parent page " +
                        jahiaField.getPageID() + " for field " +
                        jahiaField.getID(), je);
            }
            return null;
        }

        try {
            final ContentContainer parentContentContainer = ContentContainer.
                    getContainer(jahiaField.getctnid());
            final JahiaContainer parentJahiaContainer = parentContentContainer.getJahiaContainer(processingContext, processingContext.getEntryLoadRequest());
            return new ContainerBean(parentJahiaContainer, processingContext);
        } catch (JahiaException je) {
            logger.error("Error while loading parent container " + jahiaField.getctnid() + " for field " + jahiaField.getID(), je);
        }
        return null;

    }

    private void buildActionURIs() {
        actionURIs = new InsertionSortedMap<String, ActionURIBean>();
        final GuiBean guiBean = new GuiBean(processingContext);
        final HTMLToolBox htmlToolBox = new HTMLToolBox(guiBean, processingContext);
        completelyLocked = true;
        try {
            final ContentField theField = jahiaField.getContentField();
            String curURL = guiBean.drawUpdateFieldUrl(theField);
            String curLauncherURI = htmlToolBox.drawUpdateFieldLauncher(theField);
            ActionURIBean curActionURIBean = new ActionURIBean("update", curURL, curLauncherURI);
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_FIELD_TYPE, jahiaField.getID());
            final JahiaUser user = processingContext.getUser();
            if (!lockRegistry.isAcquireable(lockKey, user, user.getUserKey())) {
                curActionURIBean.setLocked(true);
            } else {
                completelyLocked = false;
            }
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                curActionURIBean.setReleaseable(true);
            }
            if ((curActionURIBean.getUri() != null) && (!"".equals(curActionURIBean.getUri()))) {
                actionURIs.put(curActionURIBean.getName(), curActionURIBean);
            }

            // If the field is displayed as an absolute, add a link to the source page where it has beed declared
            // unless the workflow icon is already displayed next to it
            if (theField.getPageID() != processingContext.getPageID()
                    && (!isIndependantWorkflow() || !(org.jahia.settings.SettingsBean.getInstance()
                            .isDevelopmentMode()
                            || processingContext.getSessionState()
                                    .getAttribute(userSettings.WF_VISU_ENABLED) != null || Jahia
                            .getSettings().isWflowDisp()))) {
                curURL = processingContext.composePageUrl(theField.getPageID(),
                        processingContext.getLocale().toString());
                final StringBuffer buff = new StringBuffer();
                buff.append("document.location.href='");
                buff.append(curURL);
                buff.append("'");
                curActionURIBean = new ActionURIBean("source", buff.toString(),
                        buff.toString());
                if (curActionURIBean.getUri() != null
                        && curActionURIBean.getUri().length() > 0) {
                    actionURIs
                            .put(curActionURIBean.getName(), curActionURIBean);
                }
            }

        } catch (JahiaException je) {
            logger.error("Error while retrieving action URI map for field " + getID(), je);
        }
    }

    public boolean isPicker() {
        try {
            return importExportService.isPicker(jahiaField.getContentField());
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public ContentObject getContentObject() {
        return getContentField();
    }

    public String getJCRPath() throws JahiaException {
        throw new JahiaException("Not implemented yet","Not implemented yet",JahiaException.APPLICATION_ERROR,
            JahiaException.ERROR_SEVERITY);
    }

    public Date getDateValue() {
        if (!(jahiaField instanceof JahiaDateField)) {
            throw new UnsupportedOperationException(
                    "The field is not an instance of JahiaDateField");
        }
        Object value = jahiaField.getObject();
        return value != null ? new Date(Long.parseLong(value.toString()))
                : null;
    }

}