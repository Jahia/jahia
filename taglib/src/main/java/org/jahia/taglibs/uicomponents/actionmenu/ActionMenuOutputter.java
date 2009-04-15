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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.uicomponents.actionmenu;

import org.jahia.data.beans.*;
import org.jahia.data.JahiaData;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.content.ObjectKey;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.utils.JahiaObjectCreator;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuLabelProvider;
import org.jahia.ajax.gwt.templates.components.actionmenus.server.helper.ActionMenuServiceHelper;
import org.jahia.ajax.gwt.client.widget.actionmenu.actions.ActionMenuIcon;
import org.jahia.ajax.usersession.userSettings;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.pages.JahiaPage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;
import org.apache.log4j.Logger;

import javax.servlet.jsp.PageContext;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 11 f�vr. 2008 - 17:50:53
 */
public class ActionMenuOutputter {

    private static final transient Logger logger = Logger.getLogger(ActionMenuOutputter.class);
    public static final String DEFAULT_CSS = "actionMenuWrap";
    public static final String CONTAINER_DEFAULT_CSS = "containerWrap";
    public static final String CONTAINERLIST_DEFAULT_CSS = "containerListWrap";

    // parameters to check if action menu has to be displayed
    private ProcessingContext processingContext;
    private ContentBean contentObject;

    // default attributes
    private PageContext pageContext;
    private String contentObjectName;
    private String objectKey;
    private int actionType = ActionMenuIcon.CONTAINERLIST_UPDATE;

    // custom attributes
    private String bundleName = null;
    private String namePostFix = null;
    private String labelKey = null;
    private String iconStyle = null ;
    private boolean toolbarView = true ;

    /**
     * Construct a action menu without customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey) {
        this.processingContext = processingContext;
        this.contentObject = contentObject;
        this.pageContext = context;
        this.contentObjectName = objectName;
        this.objectKey = objectKey;
    }

    /**
     * Construct a action menu without customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param actionType        add, edit container, edit container list
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, int actionType) {
        this(processingContext, context, contentObject, objectName,  objectKey) ;
        this.actionType = actionType ;
    }

    /**
     * Construct a action menu with customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param bundleName        the custom resource bundle to use
     * @param namePostFix       the key to retrieve label extension
     * @param labelKey          the label to add after the action menu icon
     * @param iconStyle         the icon style
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, String bundleName, String namePostFix, String labelKey, String iconStyle) {
        this(processingContext, context, contentObject, objectName,  objectKey) ;
        this.bundleName = bundleName;
        this.namePostFix = namePostFix;
        this.labelKey = labelKey;
        this.iconStyle = iconStyle ;
    }

    /**
     * Construct a action menu with customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param actionType        add, edit container, edit container list
     * @param bundleName        the custom resource bundle to use
     * @param namePostFix       the key to retrieve label extension
     * @param labelKey          the label to add after the action menu icon
     * @param iconStyle         the icon style
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, int actionType, String bundleName, String namePostFix, String labelKey, String iconStyle) {
        this(processingContext, context, contentObject, objectName,  objectKey, actionType) ;
        this.bundleName = bundleName;
        this.namePostFix = namePostFix;
        this.labelKey = labelKey;
        this.iconStyle = iconStyle ;
    }

     public String getOutput(boolean enableToolbarView) throws ClassNotFoundException, JahiaException {
         this.toolbarView = enableToolbarView ;
         return getOutput() ;
     }

    /**
     * Generate the HTML output given the constructor parameters. The generated div will then be filled by GWT.
     *
     * @return HTML code
     * @throws ClassNotFoundException sthg bad happened
     * @throws org.jahia.exceptions.JahiaException
     *                                sthg bad happened
     */
    public String getOutput() throws ClassNotFoundException, JahiaException {
        final RequestBean requestBean = (RequestBean) pageContext.findAttribute("currentRequest");

        if (requestBean == null) {
            return "";
        }

        // if non edit mode, useless
        if (!requestBean.isEditMode()) {
            return "";
        }

        // check if a content bean has been passed
        if (contentObject == null && contentObjectName != null) {
            contentObject = (ContentBean) pageContext.findAttribute(contentObjectName);
        }

        // if unexisting content, useless as well
        if (contentObject == null && objectKey == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("nothing for " + contentObjectName);
            }
            return "";
        }

        // set the content bean
        else if (contentObject == null) {
            contentObject = JahiaObjectCreator.getContentBeanFromObjectKey(objectKey, processingContext);
        }

        // generate the object key string
        else if (objectKey == null) {
            objectKey = contentObject.getBeanType() + ObjectKey.KEY_SEPARATOR + contentObject.getID();
        }

        boolean picker = false ;

        // various checks
        // get the content object type
        String contentType = objectKey.substring(0, objectKey.indexOf(ObjectKey.KEY_SEPARATOR));
        String type = null ;
        // container list case
        if (contentType.equals(ContainerListBean.TYPE)) {
            final ContainerListBean listBean = (ContainerListBean) contentObject;
            final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
            if (listBean.getFullSize() == 0 &&
                    aclService.getSiteActionPermission("engines.languages." + processingContext.getLocale().toString(),
                            processingContext.getUser(),
                            JahiaBaseACL.READ_RIGHTS, processingContext.getSiteID()) <= 0) {
                logger.debug("empty list / no rights for the current language");
                return "";
            }
            type = ActionMenuLabelProvider.CONTAINER_LIST ;
        } else {
            if (contentType.equals(ContainerBean.TYPE)) {
                type = ActionMenuLabelProvider.CONTAINER ;
            } else if (contentType.equals(FieldBean.TYPE)) {
                type = ActionMenuLabelProvider.FIELD ;
            } else if (contentType.equals(PageBean.TYPE)) {
                type = ActionMenuLabelProvider.PAGE ;
            }
            picker = contentObject.isPicker() ;
        }

        try {
            if (contentObject.getID() > 0 && !contentObject.getACL().getPermission(processingContext.getUser(), JahiaBaseACL.WRITE_RIGHTS)) {
                // if the user doesn't have Write access on the object, don't display the GUI
                return "";
            }
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            return "";
        }

        // workflow stuff
        boolean isDevMode = org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode() ;
        HttpServletRequest theRequest = (HttpServletRequest)pageContext.getRequest() ;
        Boolean displayWorkflowStates = ActionMenuServiceHelper.getUserInitialSettingForDevMode(theRequest, userSettings.WF_VISU_ENABLED, isDevMode);
        if (!isDevMode) {
            try {
                String value = (String) theRequest.getSession().getAttribute(userSettings.WF_VISU_ENABLED);
                displayWorkflowStates = value != null ? Boolean.valueOf(value) : null;
                if (displayWorkflowStates == null) {
                    displayWorkflowStates = org.jahia.settings.SettingsBean.getInstance().isWflowDisp();
                }
            } catch (final IllegalStateException e) {
                logger.error(e, e);
            }
        }
        final boolean showWorkflow = displayWorkflowStates && contentObject.isIndependantWorkflow();
        String wfKey = null ;
        if (showWorkflow && (!PageBean.TYPE.equals(contentType))) {
            String wfKeyTemp = objectKey ;
            if (ContainerBean.TYPE.equals(contentType)) {
                final ContentContainer cont = (ContentContainer) contentObject.getContentObject();
                try {
                    int jahiaPageID = -1;
                    final Iterator<JahiaField> en = cont.getJahiaContainer(processingContext, processingContext.getEntryLoadRequest()).getFields();
                    while (en.hasNext()) {
                        final JahiaField field = (JahiaField) en.next();
                        if (field.getType() == FieldTypes.PAGE) {
                            final JahiaPageField pageField = (JahiaPageField) field;
                            final JahiaPage dest = (JahiaPage) pageField.getObject();
                            if (dest == null) continue;
                            jahiaPageID = dest.getID();
                            if (jahiaPageID > 0) break;
                        }
                    }
                    if (jahiaPageID > 0) {
                        wfKeyTemp = new StringBuilder(PageBean.TYPE).append("_").append(jahiaPageID).toString() ;
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            } else {
                wfKeyTemp = new StringBuilder(contentType).append(ObjectKey.KEY_SEPARATOR).append(contentObject.getID()).toString();
            }
            wfKey = wfKeyTemp ;
        }

        ContentBean parentContentObject = contentObject.getParent();
        if (parentContentObject != null && parentContentObject.isPicker()) {
            return "";
        }

        // generate a unique ID
        String uid = new StringBuilder(objectKey).append(Constants.UID_SEPARATOR).append(System.currentTimeMillis() % Constants.MODULO).append("_").append(Math.random()).toString() ;

        // generate the div that will receive the action menu
        StringBuilder buf = new StringBuilder();
        String objClass = objectKey.substring(0, objectKey.indexOf("_")) ;
        buf.append("<div class=\"action-menu-icons ").append(objClass).append("\" ").append(JahiaType.JAHIA_TYPE).append("=\"").append(JahiaType.ACTION_MENU).append("\" ");
        if (wfKey != null) {
            buf.append("wfkey=\"").append(wfKey).append("\" ");
        }
        if (bundleName != null) {
            buf.append("bundlename=\"").append(bundleName).append("\" ");
        }
        if (namePostFix != null) {
            buf.append("namepostfix=\"").append(namePostFix).append("\" ");
        }
        if (iconStyle != null) {
            buf.append("iconstyle=\"").append(iconStyle).append("\" ");
        } else {
            String icon = null ;
            if (actionType == ActionMenuIcon.CONTAINER_ADD) {
                icon = "addContainer" ;
            } else if (actionType == ActionMenuIcon.CONTAINER_EDIT) {
                if (picker) {
                    icon = "editPickerContainer" ;
                } else {
                    icon = "editContainer" ;
                }
            }
            if (icon != null) {
                buf.append("iconstyle=\"").append(icon).append("\" ");
            }
        }
        buf.append("actionType=\"").append(String.valueOf(actionType)).append("\" ");
        if (!toolbarView) {
            buf.append("toolbarview=\"false\" ");
        }
        if (labelKey != null) {
            final JahiaData jData = (JahiaData) pageContext.getRequest().getAttribute("org.jahia.data.JahiaData");
            labelKey = ActionMenuLabelProvider.getIconLabel(bundleName, labelKey, type, jData.getProcessingContext());
            buf.append("labelkey=\"").append(labelKey).append("\" ");
        }
        buf.append("id=\"").append(uid).append("\"></div>\n") ;

        // end tag
        return buf.toString();
    }

}
