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
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.InsertionSortedMap;

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


    public boolean isPicker() {
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