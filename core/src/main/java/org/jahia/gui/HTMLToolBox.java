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
//
//  HTMLToolBox
//  EV      15.12.2000
//  NK      14.05.2001 	Prefix each popup window's name with the session id
//			to resolve session conflict beetween different sites
//			opened in different explorer
//  NK      22.05.2001 	Container and field update popup's names are composed with their id too !!!
//			This is needed when to close and reopen the update popup with data. ( resolv refresh problem with JahiaOpenWindow() javascript function )
//

package org.jahia.gui;

import org.apache.log4j.Logger;
import org.apache.commons.codec.binary.Base64;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentPageKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.JahiaObject;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.ContentBean;
import org.jahia.data.beans.PageBean;
import org.jahia.data.beans.portlets.PortletModeBean;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.data.beans.portlets.WindowStateBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.engines.selectpage.SelectPage_Engine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.PageInfoInterface;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.settings.SettingsBean;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.ajax.gwt.client.core.JahiaType;

import javax.servlet.jsp.JspWriter;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modified and cleaned by Xavier Lawrence
 * MC modified and cleaned Xavier Lawrence
 *
 * @version $Id$
 */
public class HTMLToolBox {

    public static final String IMAGE_DIRECTORY = "/engines/images/actions";

    private static final transient Logger logger = Logger.getLogger(HTMLToolBox.class);

    private static final int JS_WINDOW_WIDTH = 1020;
    private static final int JS_WINDOW_HEIGHT = 730;

    private static String previousSessionID = "";
    private static String previousCleanSessionID = "";

    private final ProcessingContext processingContext;
    private final GuiBean gui;

    /**
     * ID generator for AJAX domIds
     */
    static Random generator = new Random();
    public static final String ID_SEPARATOR = "_--_";
    //    public static final Map resourceBundleStore = new ConcurrentHashMap();
    public static final Map lockIconStore = new ConcurrentHashMap();

    /**
     * constructor
     * EV    15.12.2000
     */
    public HTMLToolBox(final GuiBean gui, final ProcessingContext processingContext) {
        this.gui = gui;
        this.processingContext = processingContext;
    } // end constructor

    public HTMLToolBox(final GuiBean gui) {
        this.gui = gui;
        this.processingContext = gui.params();
    }

    protected HTMLToolBox() {
        this.gui = null;
        this.processingContext = null;
    }

    /**
     * Utility method to clean session ID to be used in javascript code mostly
     * Basically what it does is removed all non alpha numeric characters in the
     * session ID. This uses a unicode method so it WILL allow unicode allowed
     * alpha numeric character which might NOT work with Javascript flawlessly.
     * <p/>
     * This method also uses a very simple "cache" system that allows us not
     * to have to parse the whole string if called repeteadly with the same
     * session ID.
     *
     * @param sessionID the session ID to test for non alpha numeric characters
     * @return the "cleaned" session ID.
     */
    public static String cleanSessionID(final String sessionID) {
        if (sessionID.equals(previousSessionID)) {
            return previousCleanSessionID;
        }
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < sessionID.length(); i++) {
            final char curChar = sessionID.charAt(i);
            if (Character.isLetterOrDigit(curChar)) {
                result.append(curChar);
            }
        }
        previousSessionID = sessionID;
        previousCleanSessionID = result.toString();
        return result.toString();
    }

    //---------------------------------------------------------------- drawLogin

    /**
     * returns the URL allowing to open the login popup window
     *
     * @return
     * @throws JahiaException
     */
    public String drawLoginLauncher() throws JahiaException {
        // BEGIN [for CAS authentication]
        final StringBuffer buff = new StringBuffer();
        if (Jahia.usesSso()) {
            String loginUrl = Jahia.getSsoValve().getRedirectUrl(processingContext);
            if (loginUrl != null) {
                return buff.append("window.location = '").append(loginUrl).append("'").toString();
            }
        }
        // END [for CAS authentication]
        final String popupLoginURL = gui.drawPopupLoginUrl();
        return (popupLoginURL.equals("")) ? "" :
                buff.append("OpenJahiaWindow('").append(popupLoginURL).
                        append("','Login',450,500)").toString();
    }

    /**
     * @param destinationPageID
     * @return
     * @throws JahiaException
     */
    public String drawLoginLauncher(final int destinationPageID) throws JahiaException {
        // BEGIN [for CAS authentication]
        final StringBuffer buff = new StringBuffer();
        if (Jahia.usesSso()) {
            String loginUrl = Jahia.getSsoValve().getRedirectUrl(processingContext);
            if (loginUrl != null) {
                return buff.append("window.location = '").append(loginUrl).append("'").toString();
            }
        }
        // END [for CAS authentication]
        final String popupLoginURL = gui.drawPopupLoginUrl(destinationPageID);
        return (popupLoginURL.equals("")) ? "" :
                buff.append("OpenJahiaWindow('").append(popupLoginURL).
                        append("','Login',450,500)").toString();
    }

    /**
     *
     */
    public String drawLoginButton() throws JahiaException {
        return drawButton(drawLoginLauncher(), "login");
    }

    /**
     *
     */
    public String drawLoginButton(final int destinationPageID) throws JahiaException {
        return drawButton(drawLoginLauncher(destinationPageID), "login");
    }

    //--------------------------------------------------------------- drawLogout

    /**
     * returns the URL allowing to logout
     *
     * @return
     * @throws JahiaException
     */
    public String drawLogoutLauncher() throws JahiaException {
        return gui.drawLogoutUrl();
    }

    /**
     * @param destinationPageID
     * @return
     * @throws JahiaException
     */
    public String drawLogoutLauncher(final int destinationPageID) throws JahiaException {
        return gui.drawLogoutUrl();
    }

    /**
     *
     */
    public String drawLogoutButton() throws JahiaException {
        return drawLinkButton(gui.drawLogoutUrl(), "logout");
    }

    /**
     *
     */
    public String drawLogoutButton(final int destinationPageID) throws JahiaException {
        return drawLinkButton(gui.drawLogoutUrl(), "logout");
    }

    //---------------------------------------------------------- drawUpdateField

    /**
     * returns the URL of the field update window
     *
     * @param key The field's object key as a String
     * @return
     * @throws JahiaException
     */
    public String drawUpdateFieldLauncher(final String key) throws JahiaException, ClassNotFoundException {
        final StringBuffer name = new StringBuffer("updateField_");
        final ObjectKey objectKey = ObjectKey.getInstance(key);
        final ContentField contentField = (ContentField) JahiaObject.getInstance(objectKey);
        if (contentField != null) {
            name.append(contentField.getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawUpdateFieldUrl(contentField), name.toString());
    }

    /**
     * returns the URL of the field update window
     *
     * @param contentField
     * @return
     * @throws JahiaException
     */
    public String drawUpdateFieldLauncher(final ContentField contentField) throws JahiaException {
        final StringBuffer name = new StringBuffer("updateField_");
        if (contentField != null) {
            name.append(contentField.getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawUpdateFieldUrl(contentField), name.toString());
    }

    /**
     * returns the URL of the field update window
     *
     * @param contentField
     * @return
     * @throws JahiaException
     */
    public String drawUpdateFieldLauncher(final ContentField contentField, final String screen) throws JahiaException {
        final StringBuffer name = new StringBuffer("updateField_");
        if (contentField != null) {
            name.append(contentField.getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        final StringBuffer buffer = new StringBuffer();
        buffer.append(gui.drawUpdateFieldUrl(contentField));
        buffer.append("&gotoscreen=");
        buffer.append(screen);
        return drawLauncher(buffer.toString(), name.toString());
    }

    /**
     * @param theField
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateFieldLauncher(final JahiaField theField) throws JahiaException {
        if (theField == null) {
            return "";
        }
        final ContentField contentField = theField.getContentField();
        return drawUpdateFieldLauncher(contentField);
    }

    /**
     * returns the full link (�<a�>update</a>") used to open the update field window
     *
     * @param theField
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateFieldButton(final JahiaField theField) throws JahiaException {
        return drawButton(drawUpdateFieldLauncher(theField), "update");
    }

    // -------------------------------------------------------- drawAddContainer

    /**
     * returns the URL which opens the container add window
     *
     * @param jahiaContainerList
     */
    public String drawAddContainerLauncher(final JahiaContainerList jahiaContainerList) {
        return drawAddContainerLauncher(jahiaContainerList, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false);
    }

    /**
     * @param jahiaContainerList
     * @param width
     * @param height
     */
    public String drawAddContainerLauncher(final JahiaContainerList jahiaContainerList,
                                           final int width,
                                           final int height) {
        return drawAddContainerLauncher(jahiaContainerList, width, height, false);
    }

    /**
     * @param jahiaContainerList
     * @param width
     * @param height
     */
    private String drawAddContainerLauncher(final JahiaContainerList jahiaContainerList,
                                            final int width,
                                            final int height,
                                            final boolean checkLock) {
        String launcher;
        try {
            final StringBuffer name = new StringBuffer("addContainer_");
            name.append(jahiaContainerList.getID());
            name.append(cleanSessionID(processingContext.getSessionID()));
            final String addContainerURL = gui.drawAddContainerUrl(jahiaContainerList);
            final StringBuffer buff = new StringBuffer();
            launcher = addContainerURL.equals("") ? "" :
                    buff.append("OpenJahiaScrollableWindow('").append(addContainerURL).
                            append("','").append(name.toString()).append("',").
                            append(width).append(",").append(height).append(")").toString();
            // #ifdef LOCK
            if (checkLock) {
                final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
                final LockKey lockKey = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, jahiaContainerList.getID());
                final JahiaUser user = processingContext.getUser();
                if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                    launcher = "";
                }
            }
            // #endif
        } catch (JahiaException je) {
            logger.error("Cannot draw add container launcher", je);
            launcher = "";
        }
        return launcher;
    }

    //------------------------------------------------------ drawUpdateContainer

    /**
     * returns the URL of the container update window
     *
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final ContentContainer contentContainer) throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false, 0, null);
    }

    /**
     * returns the URL of the container update window
     *
     * @param contextualContainerListId This is the id of the container list from which this container is returned.
     *                                  This container list may be different (a container used to aggregate containers from other container list
     *                                  using Filters in exemple ) than the real unique parent container list of this container. it shoould come from
     *                                  <code>JahiaContainer.getContextualContainerListID</code>.
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final int contextualContainerListId,
                                              final ContentContainer contentContainer) throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false, 0, null, contextualContainerListId);
    }

    /**
     * returns the URL of the container update window
     *
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final ContentContainer contentContainer, final String screen)
            throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false, 0, screen);
    }

    /**
     * returns the URL of the container update window
     *
     * @param contentContainer
     * @param screen
     * @param contextualContainerListId This is the id of the container list from which this container is returned.
     *                                  This container list may be different (a container used to aggregate containers from other container list
     *                                  using Filters in exemple ) than the real unique parent container list of this container. it shoould come from
     *                                  <code>JahiaContainer.getContextualContainerListID</code>.
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final ContentContainer contentContainer, final String screen,
                                              final int contextualContainerListId)
            throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false, 0, screen, contextualContainerListId);
    }

    /**
     * returns the URL of the container update window
     *
     * @param contentContainer
     * @param focusedFieldId   the id of the field to focus on
     * @return the URL of the container update window
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final ContentContainer contentContainer, int focusedFieldId) throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, JS_WINDOW_WIDTH,
                JS_WINDOW_HEIGHT, false, focusedFieldId, null);
    }

    /**
     * @param contentContainer
     * @param width
     * @param height
     * @return
     * @throws JahiaException
     */
    public String drawUpdateContainerLauncher(final ContentContainer contentContainer,
                                              final int width,
                                              final int height)
            throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, width, height, false, 0, null);
    }

    /**
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateContainerLauncher(final JahiaContainer theContainer) throws JahiaException {
        if (theContainer != null) {
            final ContentContainer contentContainer = theContainer.
                    getContentContainer();
            return drawUpdateContainerLauncher(contentContainer,
                    JS_WINDOW_WIDTH,
                    JS_WINDOW_HEIGHT, false, 0, null, theContainer.getContextualContainerListID());
        } else {
            return "";
        }
    }

    /**
     * @param theContainer
     * @param width
     * @param height
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateContainerLauncher(final JahiaContainer theContainer,
                                              final int width,
                                              final int height)
            throws JahiaException {
        final ContentContainer contentContainer = theContainer.getContentContainer();
        return drawUpdateContainerLauncher(contentContainer, width, height, false, 0, null,
                theContainer.getContextualContainerListID());
    }

    /**
     * returns the full link (�<a�>update</a>") used to open the update container window
     *
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawUpdateContainerButton(final JahiaContainer theContainer) throws JahiaException {
        return drawButton(drawUpdateContainerLauncher(theContainer), "update");
    }

    /**
     *
     */
    private String drawUpdateContainerLauncher(final ContentContainer contentContainer,
                                               final int width,
                                               final int height,
                                               final boolean checkLock,
                                               final int focusedFieldId,
                                               final String screen)
            throws JahiaException {
        return drawUpdateContainerLauncher(contentContainer, width, height, checkLock, focusedFieldId, screen,
                0);
    }

    /**
     * @param contentContainer
     * @param width
     * @param height
     * @param checkLock
     * @param focusedFieldId
     * @param screen
     * @param contextualContainerListId This is the id of the container list from which this container is returned.
     *                                  This container list may be different (a container used to aggregate containers from other container list
     *                                  using Filters in exemple ) than the real unique parent container list of this container. it shoould come from
     *                                  <code>JahiaContainer.getContextualContainerListID</code>.
     * @return
     * @throws JahiaException
     */
    private String drawUpdateContainerLauncher(final ContentContainer contentContainer,
                                               final int width,
                                               final int height,
                                               final boolean checkLock,
                                               final int focusedFieldId,
                                               final String screen,
                                               final int contextualContainerListId)
            throws JahiaException {

        if (contentContainer == null) return "";

        final int containerID = contentContainer.getID();
        final StringBuffer name = new StringBuffer();
        name.append("updateContainer_");
        name.append(containerID);
        name.append(cleanSessionID(processingContext.getSessionID()));
        String updateContainerURL = gui.drawUpdateContainerUrl(contentContainer);
        if (updateContainerURL.length() > 0) {
            if (focusedFieldId > 0) {
                // also add focused field ID
                updateContainerURL = new StringBuffer(updateContainerURL.length() + 16).
                        append(updateContainerURL).append("&fid=").append(focusedFieldId).toString();
            } else if (screen != null && screen.length() > 0) {
                updateContainerURL = new StringBuffer(updateContainerURL.length() + 25).
                        append(updateContainerURL).append("&gotoscreen=").append(screen).toString();
            }
            if (contextualContainerListId != 0) {
                updateContainerURL = new StringBuffer(updateContainerURL.length() + 50).
                        append(updateContainerURL).append("&contextualContainerListId=")
                        .append(String.valueOf(contextualContainerListId)).toString();
            }
        }
        final StringBuffer buff = new StringBuffer();
        final String out = updateContainerURL.length() == 0 ? "" :
                buff.append("OpenJahiaScrollableWindow('").append(updateContainerURL).
                        append("','").append(name.toString()).append("',").append(width).
                        append(",").append(height).append(")").toString();
        // #ifdef LOCK
        if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, containerID);
            final JahiaUser user = processingContext.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return "";
            }
        }
        // #endif
        return out;
    }

    //------------------------------------------------------ drawDeleteContainer

    /**
     * returns only the URL of the previous window
     *
     * @param contentContainer
     * @return
     * @throws JahiaException
     */
    public String drawDeleteContainerLauncher(final ContentContainer contentContainer) throws JahiaException {
        return drawDeleteContainerLauncher(contentContainer, false);
    }

    /**
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawDeleteContainerLauncher(final JahiaContainer theContainer) throws JahiaException {
        if (theContainer == null) {
            return "";
        }
        final ContentContainer contentContainer = theContainer.getContentContainer();
        return drawDeleteContainerLauncher(contentContainer, false);
    }

    /**
     * returns the full link (�<a�>delete</a>") used to open the delete container window
     *
     * @param theContainer
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawDeleteContainerButton(final JahiaContainer theContainer) throws JahiaException {
        return drawButton(drawDeleteContainerLauncher(theContainer), "delete");
    }

    /**
     *
     */
    private String drawDeleteContainerLauncher(final ContentContainer contentContainer,
                                               boolean checkLock)
            throws JahiaException {
        return drawDeleteContainerLauncher(contentContainer, checkLock, 0);
    }

    /**
     * @param contentContainer
     * @param checkLock
     * @param contextualContainerListId This is the id of the container list from which this container is returned.
     *                                  This container list may be different (a container used to aggregate containers from other container list
     *                                  using Filters in exemple ) than the real unique parent container list of this container. it shoould come from
     *                                  <code>JahiaContainer.getContextualContainerListID</code>.
     * @return
     * @throws JahiaException
     */
    private String drawDeleteContainerLauncher(final ContentContainer contentContainer,
                                               boolean checkLock, int contextualContainerListId)
            throws JahiaException {

        if (contentContainer == null) return "";
        final JahiaUser user = processingContext.getUser();

        /** todo we removed this version of the action check because it was dreadfully
         * slow to check the rights on the whole sub tree, which could be very large.
         * We might want to do this check when opening the engine instead, or use AJAX
         * to indicate the background process is running.
         */
        // if(!contentContainer.checkWriteAccess(user,true,true)) return "";
        if (!contentContainer.checkWriteAccess(user, false, false)) return "";

        final int containerID = contentContainer.getID();
        final StringBuffer name = new StringBuffer();
        name.append("deleteContainer_");

        name.append(containerID);
        name.append(cleanSessionID(processingContext.getSessionID()));
        String deleteContainerURL = gui.drawDeleteContainerUrl(contentContainer);
        if (!"".endsWith(deleteContainerURL) && contextualContainerListId != 0) {
            deleteContainerURL += "&contextualContainerListId=" + String.valueOf(contextualContainerListId);
        }
        final StringBuffer buff = new StringBuffer();
        String out = deleteContainerURL.equals("") ? "" :
                buff.append("OpenJahiaScrollableWindow('").append(deleteContainerURL).
                        append("','").append(name.toString()).append("',").
                        append(JS_WINDOW_WIDTH).append(",").append(JS_WINDOW_HEIGHT).
                        append(")").toString();
        // #ifdef LOCK
        if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().
                    getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.DELETE_CONTAINER_TYPE, containerID);
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                out = "";
            }
        }
        // #endif
        return out;
    }

    // --------------------------------------------- drawContainerListProperties

    /**
     * @param contentContainerList
     * @return
     * @throws JahiaException
     */
    public String drawContainerListPropertiesLauncher(final ContentContainerList contentContainerList)
            throws JahiaException {
        return drawContainerListPropertiesLauncher(contentContainerList, false);
    }

    /**
     * @param theContainerList
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawContainerListPropertiesLauncher(final JahiaContainerList theContainerList) throws JahiaException {
        if (theContainerList.getID() == 0) {
            return "";
        }
        final ContentContainerList contentContainerList = ContentContainerList.
                getContainerList(theContainerList.getID());
        return drawContainerListPropertiesLauncher(contentContainerList, false);
    }

    /**
     * @param theContainerList
     * @return
     * @throws JahiaException
     * @deprecated
     */
    public String drawContainerListPropertiesButton(final JahiaContainerList theContainerList) throws JahiaException {
        return drawButton(drawContainerListPropertiesLauncher(theContainerList),
                "list properties");
    }

    private String drawContainerListPropertiesLauncher(final ContentContainerList contentContainerList,
                                                       final boolean checkLock)
            throws JahiaException {

        if (contentContainerList == null) return "";

        final int listID = contentContainerList.getID();

        final StringBuffer name = new StringBuffer();
        name.append("containerListProperties_");
        name.append(listID);

        name.append(cleanSessionID(processingContext.getSessionID()));
        final String containerListPropertiesURL = gui.drawContainerListPropertiesUrl(
                contentContainerList);
        String out = "";
        if (!"".equals(containerListPropertiesURL)) {
            final StringBuffer buff = new StringBuffer();
            out = buff.append("OpenJahiaScrollableWindow('").append(containerListPropertiesURL).
                    append("', '").append(name.toString()).append("', ").
                    append(JS_WINDOW_WIDTH).append(", ").append(JS_WINDOW_HEIGHT).
                    append(")").toString();
        }
        /*
         * We allow to display the properties buttons even there arent' any childs
         * to set rights
                 if (contentContainerList.getChilds(processingContext.getUser(), processingContext.getEntryLoadRequest(), processingContext.EDIT).size() == 0) {
            out = "";
                 }*/

        // #ifdef LOCK
        if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, listID);
            final JahiaUser user = processingContext.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                out = "";
            }
        }
        // #endif
        return out;
    }

    /**
     *
     */
    public String drawWorkflowLauncher() throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        buff.append(ContentPageKey.PAGE_TYPE).append("_").
                append(processingContext.getPage().getID());
        return drawWorkflowLauncher(buff.toString());
    }

    /**
     * @param basePage
     * @return
     */
    public String drawWorkflowLauncher(final int basePage) throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        buff.append(ContentPageKey.PAGE_TYPE).append("_").
                append(basePage);
        return drawWorkflowLauncher(buff.toString());
    }

    /**
     *
     */
    public String drawWorkflowLauncher(final String key) throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        final StringBuffer name = new StringBuffer();
        name.append("workflow_");
        if (processingContext.getPage() != null) {
            name.append(processingContext.getPage().getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        final String workflowURL = gui.drawWorkflowUrl(key);
        return ("".equals(workflowURL)) ? "" :
                buff.append("javascript:OpenJahiaScrollableWindow('").append(workflowURL).
                        append("','").append(name.toString()).append("',").
                        append(JS_WINDOW_WIDTH).append(",").append(JS_WINDOW_HEIGHT).
                        append(")").toString();
    }

    /**
     * Returns the link for the workflow engine with parameters.
     *
     * @param basePage        the base page Id
     * @param engineUrlParams parameters to be appended to the workflow engine
     *                        URL
     * @return the link for the workflow engine with parameters
     */
    public String drawWorkflowLauncher(int basePage, String engineUrlParams)
            throws JahiaException {
        String key = ContentPageKey.PAGE_TYPE + "_" + basePage;
        String workflowURL = gui.drawWorkflowUrl(key);

        return workflowURL.length() == 0 ? "" : new StringBuffer(64).append(
                "OpenJahiaScrollableWindow('").append(workflowURL)
                .append(engineUrlParams).append("','").append("workflow_").append(
                        basePage).append(cleanSessionID(processingContext.getSessionID())).append(
                        "'," + JS_WINDOW_WIDTH + "," + JS_WINDOW_HEIGHT + ")").toString();
    }

    /**
     *
     */
    public String drawShowReportLauncher(final String key) throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        final StringBuffer name = new StringBuffer();
        name.append("workflow_showReport_");
        if (processingContext.getPage() != null) {
            name.append(processingContext.getPage().getID());
        }
        final String workflowURL = processingContext.composeEngineUrl("workflow", "?screen=showReport&objectkey=" + key);
        name.append(cleanSessionID(processingContext.getSessionID()));
        return ("".equals(workflowURL)) ? "" :
                buff.append("javascript:OpenJahiaScrollableWindow('").append(workflowURL).
                        append("','").append(name.toString()).append("',").
                        append(JS_WINDOW_WIDTH).append(",").append(JS_WINDOW_HEIGHT).
                        append(");").toString();
    }

    //------------------------------------------------------- drawPageProperties

    /**
     * returns the URL allowing to open the current page properties window
     *
     * @return
     * @throws JahiaException
     */
    public String drawPagePropertiesLauncher() throws JahiaException {
        return drawPagePropertiesLauncher(false, processingContext.getPageID(), null);
    }

    /**
     * returns the URL allowing to open the current page properties window
     *
     * @return
     * @throws JahiaException
     */
    public String drawPagePropertiesLauncher(final int pageID) throws JahiaException {
        return drawPagePropertiesLauncher(false, pageID, null);
    }

    /**
     * returns the URL allowing to open the current page properties window
     *
     * @return
     * @throws JahiaException
     */
    public String drawPagePropertiesLauncher(final ContentPage page) throws JahiaException {
        return drawPagePropertiesLauncher(false, page.getID(), null);
    }

    /**
     *
     */
    private String drawPagePropertiesLauncher(final boolean checkLock,
                                              final int pageId,
                                              final String screen) throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        final StringBuffer name = new StringBuffer();
        name.append("pageProperties_");
        if (pageId > 0) {
            name.append(pageId);
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        String url = gui.drawPagePropertiesUrl(pageId);
        if (url != null && url.length() > 0 && screen != null && screen.length() > 0) {
            url = new StringBuffer(url.length() + 25).append(url).append("&gotoscreen=").append(screen).toString();
        }
        final String out = url.equals("") ? "" :
                buff.append("OpenJahiaScrollableWindow('").append(url).
                        append("','").append(name.toString()).append("',").append(JS_WINDOW_WIDTH).
                        append(",").append(JS_WINDOW_HEIGHT).append(")").toString();
        // #ifdef LOCK
        if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, processingContext.getPageID());
            final JahiaUser user = processingContext.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return "";
            }
        }
        // #endif
        return out;
    }

    /**
     * @return
     * @throws JahiaException
     */
    public String drawPagePropertiesButton() throws JahiaException {
        return drawButton(drawPagePropertiesLauncher(), "page properties");
    }

    // drawUpdateTemplateLauncher
    public String drawUpdateTemplateLauncher(final JahiaPageDefinition theTemplate) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("updateTemplate_");
        if (theTemplate != null) {
            name.append(theTemplate.getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawUpdateTemplateUrl(theTemplate),
                name.toString());
    }

    public String drawUpdateCategoryLauncher(final Category category) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("updateCategory_");
        if (category != null) {
            name.append(category.getObjectKey().getIDInType());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawUpdateCategoryUrl(category),
                name.toString());
    }

    public String drawAddSubCategoryLauncher(final String parentCategoryKey) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("addCategory_");
        if (parentCategoryKey != null) {
            name.append(parentCategoryKey);
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawAddSubCategoryUrl(parentCategoryKey),
                name.toString());
    }

    /**
     * Construct the select page URL.
     *
     * @param operation    The select page operation.
     * @param parentPageID The container parent page ID for new container creation.
     * @param pageID       The current page ID for update. N.B. This parameter is used
     *                     when the user will change a DIRECT Jahia page to a link type (URL or
     *                     page link). The DIRECT type cannot be changed to a link type on it's
     *                     self page. Set to -1 for page creation.
     * @param callback
     * @return The select page URL.
     * @throws JahiaException
     */
    public String drawSelectPageLauncher(final String operation,
                                         final int parentPageID,
                                         final int pageID, String callback)
            throws JahiaException {
        return drawSelectPageLauncher(operation, parentPageID, pageID,
                callback, processingContext.getSiteID(), processingContext.getSite()
                        .getHomePageID(), null);
    }

    /**
     * Construct the select page URL.
     *
     * @param operation    The select page operation.
     * @param parentPageID The container parent page ID for new container creation.
     * @param pageID       The current page ID for update. N.B. This parameter is used
     *                     when the user will change a DIRECT Jahia page to a link type (URL or
     *                     page link). The DIRECT type cannot be changed to a link type on it's
     *                     self page. Set to -1 for page creation.
     * @param callback
     * @param siteID
     * @param homepageID   @return The select page URL.
     * @throws JahiaException
     */
    public String drawSelectPageLauncher(final String operation,
                                         final int parentPageID,
                                         final int pageID, String callback, int siteID, int homepageID, String templates)
            throws JahiaException {
        final StringBuffer name = new StringBuffer("selectPage_");
        if (processingContext.getPage() != null) {
            name.append(processingContext.getPage().getID());
        }
        final Map params = new HashMap();
        params.put(SelectPage_Engine.OPERATION, operation);
        params.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(parentPageID));
        params.put(SelectPage_Engine.PAGE_ID, new Integer(pageID));
        params.put(SelectPage_Engine.SITE_ID, new Integer(siteID));
        params.put(SelectPage_Engine.HOMEPAGE_ID, new Integer(homepageID));
        if (templates != null) {
            params.put("templates", templates);
        }
        params.put("callback", callback);
        name.append(cleanSessionID(processingContext.getSessionID()));
        final String selectPageURL = EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine").renderLink(
                processingContext, params);
        final StringBuffer buff = new StringBuffer();
        return "".equals(selectPageURL) ? "" :
                buff.append("OpenJahiaScrollableWindow('").append(selectPageURL).
                        append("','").append(name.toString()).append("',").
                        append(JS_WINDOW_WIDTH).append(",").append(JS_WINDOW_HEIGHT).
                        append(")").toString();
    }

    /**
     * returns the URL value for the �action�  parameter of tag �form�; the input text field name must be �search�
     *
     * @return String
     * @throws JahiaException
     */
    public String drawSearchLauncher()
            throws JahiaException {
        //return drawLauncher(
        return gui.drawSearchUrl();
    }

    /**
     * returns the URL of the administration page
     */
    // MJ 21.03.2001
    public String drawAdministrationLauncher() throws JahiaException {
        return gui.drawAdministrationLauncher();
    }

    public String drawMySettingsLauncher() throws JahiaException {
        return gui.drawMySettingsUrl();
    }

    public String drawMySettingsLauncher(Object params) throws JahiaException {
        return gui.drawMySettingsUrl(params);
    }

    public String drawNewUserRegistrationLauncher() throws JahiaException {
        return gui.drawNewUserRegistrationUrl();
    }

    public String drawNewUserRegistrationLauncher(Object params)
            throws JahiaException {
        return gui.drawNewUserRegistrationUrl(params);
    }

    /**
     * returns the site map page URL
     *
     * @return String
     * @throws JahiaException
     */
    public String drawSiteMapLauncher() throws JahiaException {
        //return drawLauncher(
        return gui.drawSiteMapUrl();
    }

    // drawAddContainerButton

    /**
     * returns the full link (�<a�>add</a>") used to open the add popup
     */
    public String drawAddContainerButton(final JahiaContainerList theContainerList) throws JahiaException {
        return drawButton(drawAddContainerLauncher(
                theContainerList), "add");
    }

    // drawUpdateTemplateButton
    public String drawUpdateTemplateButton(final JahiaPageDefinition theTemplate) throws JahiaException {
        return drawButton(drawUpdateTemplateLauncher(
                theTemplate), "template");
    }

    // drawSearchButton
    // DJ 03.01.2001
    public String drawSearchButton() throws JahiaException {
        final StringBuffer html = new StringBuffer();
        final String theUrl = drawSearchLauncher();
//    	        theUrl += "','search!'," + JS_WINDOW_WIDTH + "," + JS_WINDOW_HEIGHT + ")";
        html.append("<form method=\"post\" action=\"").append(theUrl).append("\">\n");
        html.append("<input type=\"text\" name=\"search\" size=15 value=\"\">\n");
        html.append("<input type=submit value=\"Search\">");
//            html += "<input type="Button"
        html.append("</form>");

//            return drawButton( drawSearchLauncher(), html ) ;
        return html.toString();
    }

    /**
     *
     */
    public String drawSiteMapButton() throws JahiaException {
        return drawButton(drawSiteMapLauncher(), "site map");
    }

    /**
     * @param contentObject
     * @param lockKey
     * @return
     * @throws JahiaException
     */
    public String drawReleaseLockObjectLauncher(final ContentObject contentObject,
                                                final LockKey lockKey)
            throws JahiaException {
        // #ifdef LOCK
        String launcher = "";
        if (!ProcessingContext.EDIT.equals(processingContext.getOperationMode())) {
            return null;
        }
        // Draw launcher without controling the lock.
        final Class objectClass = contentObject.getClass();
        if (objectClass == ContentContainerList.class) {
            if (LockKey.ADD_CONTAINER_TYPE.equals(lockKey.getType())) {
                final ContentContainerList contentContainerList = (
                        ContentContainerList) contentObject;
                final JahiaContainerList jahiaContainerList = contentContainerList.
                        getJahiaContainerList(processingContext, processingContext.getEntryLoadRequest());
                launcher = drawAddContainerLauncher(jahiaContainerList,
                        JS_WINDOW_WIDTH, JS_WINDOW_HEIGHT, false);
            } else if (LockKey.UPDATE_CONTAINERLIST_TYPE.equals(lockKey.getType())) {
                launcher = drawContainerListPropertiesLauncher((
                        ContentContainerList) contentObject, false);
            }
        } else if (contentObject instanceof ContentField) {
            if (LockKey.UPDATE_FIELD_TYPE.equals(lockKey.getType())) {
                launcher = drawUpdateFieldLauncher((ContentField)
                        contentObject);
            }
        } else if (objectClass == ContentContainer.class) {
            if (LockKey.UPDATE_CONTAINER_TYPE.equals(lockKey.getType())) {
                launcher = drawUpdateContainerLauncher((ContentContainer)
                        contentObject, JS_WINDOW_WIDTH, JS_WINDOW_HEIGHT, false, 0, null);
            } else if (LockKey.DELETE_CONTAINER_TYPE.equals(lockKey.getType())) {
                launcher = drawDeleteContainerLauncher((ContentContainer)
                        contentObject, false);
            }
        } else if (objectClass == ContentPage.class) {
            if (LockKey.UPDATE_PAGE_TYPE.equals(lockKey.getType())) {
                launcher = drawPagePropertiesLauncher(false, processingContext.getPageID(), null);
            }
        }
        final LockService lockRegistry = ServicesRegistry.getInstance().
                getLockService();
        if (lockRegistry.isAlreadyAcquired(lockKey)) {
            final JahiaUser user = processingContext.getUser();
            if (lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return processingContext.composeReleaseLockURL(lockKey);
            } else {
                return "javascript:" + launcher;
            }
        }
        // #endif
        return null;
    }

    /**
     *
     */
    public String drawLockEngineLauncher(final LockKey lockKey) throws JahiaException {
        String out;
        // #ifdef LOCK
        final StringBuffer name = new StringBuffer("lock_");
        if (processingContext.getPage() != null) {
            name.append(processingContext.getPage().getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        final String lockURL = gui.drawLockUrl(lockKey);
        final StringBuffer buff = new StringBuffer();
        out = "".equals(lockURL) ? "" :
                buff.append("javascript:OpenJahiaScrollableWindow('").append(lockURL).
                        append("','").append(name.toString()).append("',").
                        append(JS_WINDOW_WIDTH).append(",").append(JS_WINDOW_HEIGHT).
                        append(")").toString();
        // #endif
        return out;
    }

    /**
     * drawLauncher
     * EV    15.12.2000
     */
    private String drawLauncher(final String url, final String windowName) {
        final StringBuffer html = new StringBuffer();
        // if url = "", this means that the engine didn't grant authorisation to render
        if (!url.equals("")) {
            html.append("OpenJahiaScrollableWindow('");
            html.append(url);
            html.append("','").append(windowName).append("',").append(JS_WINDOW_WIDTH).
                    append(",").append(JS_WINDOW_HEIGHT).append(")");
        }
        return html.toString();
    } // end drawLauncher

    /**
     * drawButton
     * EV    15.12.2000
     */
    private String drawButton(final String launcher, final String buttonLabel) {
        final StringBuffer html = new StringBuffer();
        // if launcher = "", this means that the engine didn't grant authorisation to render
        if (!launcher.equals("")) {
            html.append("<a href=\"javascript:");
            html.append(launcher);
            html.append("\">").append(buttonLabel).append("</a>");
        }
        return html.toString();
    } // end drawButton

    /**
     * drawLinkButton - Draw a link but without javascript!
     */
    private String drawLinkButton(final String launcher, final String buttonLabel) {
        final StringBuffer html = new StringBuffer();
        // if launcher = "", this means that the engine didn't grant authorisation to render
        if (!launcher.equals("")) {
            html.append("<a href=\"");
            html.append(launcher);
            html.append("\">").append(buttonLabel).append("</a>");
        }
        return html.toString();
    } // end drawButton

    /**
     * Generate an html anchor composed by the fieldID
     */
    public static String drawAnchor(final JahiaField theField) {

        if (theField == null) {
            return "";
        }

        final StringBuffer anchor = new StringBuffer("<a name=");
        anchor.append("field_");
        anchor.append(theField.getID());
        anchor.append("></a>");

        return anchor.toString();
    }

    /**
     * Generate an html anchor composed by the container list ID
     */
    public static String drawAnchor(final JahiaContainerList cList) {

        if (cList == null) {
            return "";
        }

        final StringBuffer anchor = new StringBuffer("<a name=");
        anchor.append("cList_");
        anchor.append(cList.getID());
        anchor.append("></a>");

        return anchor.toString();
    }

    /**
     * Generate an html anchor composed by the container definition id _ container ID
     */
    public static String drawAnchor(final JahiaContainer container) {
        return drawAnchor(container, false);
    }

    /**
     * Generate an html anchor composed by the container definition id _ container ID
     */
    public static String drawAnchor(final JahiaContainer container, final boolean anchorValueOnly) {
        if (container == null) {
            return "";
        }

        final StringBuffer buf = new StringBuffer();
        if (anchorValueOnly) {
            buf.append("ctn").append(container.getctndefid()).append("_").append(container.getID());
        } else {
            final String name = buf.append("ctn").append(container.getctndefid()).append("_").
                    append(container.getID()).toString();
            buf.delete(0, buf.length());
            buf.append("<a class=\"hidden\" id=\"").append(name).append("\" name=\"").append(name).append("\"></a>");
        }

        return buf.toString();
    }

    /**
     * Generate an html anchor composed by the page ID
     */
    public static String drawAnchor(final JahiaPage page) {

        if (page == null) {
            return "";
        }

        final StringBuffer anchor = new StringBuffer("<a name=");
        anchor.append("page_");
        anchor.append(page.getID());
        anchor.append("></a>");

        return anchor.toString();
    }

    /**
     * Checks whether we must draw the action menu or not
     */
    private boolean drawingChecks(final ContentBean contentObject) {
        if (!ProcessingContext.EDIT.equals(processingContext.getOperationMode())) {
            // if we are not in edit mode we don't display the GUI
            return false;
        }

        if (contentObject == null) {
            // No object -> No menu...
            return false;
        }

        try {
            if (processingContext.getContentPage().isMarkedForDelete() &&
                    !org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) {
                return false;
            }

            // check for null: in old templates subcontainer lists are null
            if (contentObject.getContentObject() != null && contentObject.getContentObject().isMarkedForDelete() &&
                    !org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) {
                return false;
            }

            if (contentObject.getID() > 0 &&
                    !contentObject.getACL().getPermission(processingContext.getUser(),
                            JahiaBaseACL.WRITE_RIGHTS)) {
                // if the user doesn't have Write access on the object, don't display the GUI
                return false;
            }
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            return false;
        }

        ContentBean parentContentObject = contentObject.getParent();
        return !(parentContentObject != null && parentContentObject.isPicker());
    }



    /**
     * Constructs the javascript function call for the AJAX action menu.
     */
    protected String buildAjaxCall(final String objectType,
                                   final int objectKey,
                                   final int definitionID,
                                   final int parentID,
                                   final int pageID,
                                   final String domID,
                                   final String lang,
                                   final int contextualContainerListId) {
        final StringBuffer buff = new StringBuffer();
        buff.append("javascript:getActionMenu('");
        buff.append(((ParamBean) processingContext).getRequest().getContextPath()).append("', '");
        buff.append(objectType).append("', ");
        buff.append(objectKey).append(", ");
        buff.append(definitionID).append(", ");
        buff.append(parentID).append(", ");
        buff.append(pageID).append(", '");
        buff.append(domID).append("', '");
        buff.append(lang).append("', ");
        ;
        buff.append(contextualContainerListId).append(")");
        return buff.toString();
    }

    /**
     *
     */
    public String getResource(final String resourceBundle,
                              final String resourceName) {
        ResourceBundle res;
        String resValue = null;

        final Locale locale = processingContext.getLocale();
        try {
            res = ResourceBundle.getBundle(resourceBundle, locale);
            resValue = res.getString(resourceName);
        } catch (MissingResourceException mre) {
            logger.warn("Error accessing resource " + resourceName +
                    " in bundle " + resourceBundle + " for locale " +
                    locale + ":" + mre.getMessage());
        }
        return resValue;
    }

    /**
     *
     */
    public String getURLImageContext() {
        return processingContext.getContextPath() + IMAGE_DIRECTORY;
    }

    protected String buildUniqueContentID(final String typeName,
                                          final int id,
                                          final int definitionID,
                                          final int parentID,
                                          final int pageID,
                                          final String lockIcon,
                                          final boolean useFieldSet,
                                          final String resourceBundle,
                                          final String namePostFix) {
        //just use the String's hashcode instead of real value
        Integer lockIconKey = (lockIcon != null) ? (new Integer(lockIcon.hashCode())) : null;
        if (lockIcon != null && !lockIconStore.containsKey(lockIconKey)) {
            lockIconStore.put(lockIconKey, lockIcon);
        }

//        Integer resourceBundleKey = (resourceBundle != null) ? (new Integer(resourceBundle.hashCode())) : null;
//        if (resourceBundle != null && !resourceBundleStore.containsKey(resourceBundleKey)) {
//            resourceBundleStore.put(resourceBundleKey, resourceBundle);
//        }

        final StringBuffer result = new StringBuffer();
        try {
            result.append(typeName)
                    .append(ID_SEPARATOR)
                    .append(id)
                    .append(ID_SEPARATOR)
                    .append(definitionID)
                    .append(ID_SEPARATOR)
                    .append(parentID)
                    .append(ID_SEPARATOR)
                    .append(pageID)
                    .append(ID_SEPARATOR)
                    .append(lockIconKey)
                    .append(ID_SEPARATOR)
                    .append(useFieldSet)
                    .append(ID_SEPARATOR)
                    .append(new String(new Base64().encode(resourceBundle.getBytes("UTF-8"))))
                    .append(ID_SEPARATOR)
                    .append(namePostFix)
                    .append(ID_SEPARATOR)
                    .append(generator.nextInt());
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) logger.debug("menuId is : " + result.toString());

        return result.toString();
    }

    /**
     *
     */
    /*public String buildUniqueContentID(final ContentBean bean, ProcessingContext processingContext) {
        final ContentBean parent = bean.getParent();
        final int parentID = (parent == null) ? 0 : parent.getID();
        return buildUniqueContentID(bean.getBeanType(), bean.getID(),
                bean.getDefinitionID(), parentID, processingContext.getPageID());
    }*/

    /**
     *
     */
    public void drawPortletModeList(final PortletWindowBean portletWindowBean,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String listCSSClass,
                                    final String currentCSSClass,
                                    final JspWriter out)
            throws IOException {
        final List portletModeBeansIterList = portletWindowBean.getPortletModeBeans();
        // draw mode links only if there is more than 1 mode
        if (portletModeBeansIterList.size() < 2) {
            return;
        }

        out.print("<ul class=\"");
        out.print(listCSSClass);
        out.print("\">\n");
        final Iterator portletModeBeansIter = portletWindowBean.getPortletModeBeans().iterator();
        while (portletModeBeansIter.hasNext()) {
            final PortletModeBean curPortletModeBean = (PortletModeBean)
                    portletModeBeansIter.next();
            if (curPortletModeBean.getName().equals(portletWindowBean.
                    getCurrentPortletModeBean().getName())) {
                out.print("<li class=\"");
                out.print(currentCSSClass);
                out.print("\">\n");
            } else {
                out.print("<li>");
            }
            final StringBuffer buff = new StringBuffer();
            buff.append("<a class=\"").append(curPortletModeBean.getName()).
                    append("\" title=\"").append(curPortletModeBean.getName()).
                    append("\" href=\"").append(curPortletModeBean.getURL()).
                    append("\">").append("<span>").append(getResource(resourceBundle,
                    "org.jahia.taglibs.html.portlets.portletmodes." +
                            curPortletModeBean.getName() + ".label" +
                            namePostFix)).append("</span></a>");
            out.print(buff.toString());
            out.println("</li>");
        }
        out.println("</ul>");
    }

    /**
     *
     */
    public void drawWindowStateList(final PortletWindowBean portletWindowBean,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String listCSSClass,
                                    final String currentCSSClass,
                                    final JspWriter out)
            throws IOException {

        out.print("<ul class=\"");
        out.print(listCSSClass);
        out.print("\">\n");
        final Iterator windowStateBeansIter = portletWindowBean.getWindowStateBeans().
                iterator();
        while (windowStateBeansIter.hasNext()) {
            final WindowStateBean curWindowStateBean = (WindowStateBean)
                    windowStateBeansIter.next();
            if (curWindowStateBean.getName().equals(portletWindowBean.
                    getCurrentWindowStateBean().getName())) {
                out.print("<li class=\"");
                out.print(currentCSSClass);
                out.print("\">\n");
            } else {
                out.print("<li>");
            }
            final StringBuffer buff = new StringBuffer();
            buff.append("<a class=\"").append(curWindowStateBean.getName()).
                    append("\" title=\"").append(curWindowStateBean.getName()).
                    append("\"").append("\" href=\"").append(curWindowStateBean.getURL()).
                    append("\">").append("<span>").append(
                    getResource(resourceBundle,
                            "org.jahia.taglibs.html.portlets.windowstates." +
                                    curWindowStateBean.getName() + ".label" +
                                    namePostFix)).append("</span></a>");
            out.print(buff.toString());
            out.println("</li>");
        }
        out.println("</ul>");
    }

    public String drawUpdateApplicationLauncher(final ApplicationBean applicationBean) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("updateApplication_");
        if (applicationBean != null) {
            name.append(applicationBean.getID());
        }
        name.append(cleanSessionID(processingContext.getSessionID()));
        return drawLauncher(gui.drawUpdateAppplicationUrl(applicationBean), name.toString());
    }


    /**
     * draw mashup node
     *
     * @param jcrPortletNode
     * @param ajaxRendering
     * @return
     */
    public void drawMashup(JCRPortletNode jcrPortletNode, boolean ajaxRendering, int windowId, final JspWriter out) throws JahiaException, IOException {
        if (!(processingContext instanceof ParamBean)) {
            logger.error("ProcessingContext is not instanceof ParamBean. Mashup can't be rendered");
            return;
        }
        ParamBean jParam = (ParamBean) processingContext;

        boolean computedAjaxRendering = ajaxRendering && processingContext.settings().isPortletAJAXRenderingActivated();

        String appID = null;
        try {
            appID = jcrPortletNode.getUUID();
        } catch (RepositoryException e) {
            throw new JahiaException("Error rendering mashup", "Error rendering mashup",
                    JahiaException.APPLICATION_ERROR, JahiaException.ERROR_SEVERITY, e);
        }

        logger.debug("Dispatching to portlet for appID=" + appID + "...");

        String portletOutput = "";
        if (computedAjaxRendering) {
            portletOutput = "<div id=\"" + windowId + "\" windowID=\"" + windowId +
                    "\" entryPointInstanceID=\"" + appID + "\" " +
                    JahiaType.JAHIA_TYPE + "=\"" + JahiaType.PORTLET_RENDER +
                    "\" pathInfo=\"" + processingContext.getPathInfo() +
                    "\" queryString=\"" + processingContext.getQueryString() +
                    "\"></div>";
        } else {
            portletOutput = ServicesRegistry.getInstance().getApplicationsDispatchService().getAppOutput(windowId, appID, jParam);
        }

        // remove <html> tags that can break the page
        if (portletOutput != null) {
            try {
                portletOutput = (new RE("</?html>", RE.MATCH_CASEINDEPENDENT)).subst(portletOutput, "");
            } catch (RESyntaxException e) {
                logger.debug(".getValue, exception : " + e.toString());
            }
        } else {
            portletOutput = "";
        }
        out.print(portletOutput);
    }
}
