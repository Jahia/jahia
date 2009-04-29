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
import org.jahia.ajax.usersession.userSettings;
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
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;
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

    private final ProcessingContext jParams;
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
    public HTMLToolBox(final GuiBean gui, final ProcessingContext jParams) {
        this.gui = gui;
        this.jParams = jParams;
    } // end constructor

    public HTMLToolBox(final GuiBean gui) {
        this.gui = gui;
        this.jParams = gui.params();
    }

    protected HTMLToolBox() {
        this.gui = null;
        this.jParams = null;
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
            return buff.append("window.location = '").
                    append(Jahia.getSsoValve().getRedirectUrl(jParams)).
                    append("'").toString();
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
            return buff.append("window.location = '").
                    append(Jahia.getSsoValve().getRedirectUrl(jParams)).
                    append("'").toString();
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
        name.append(cleanSessionID(jParams.getSessionID()));
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
        name.append(cleanSessionID(jParams.getSessionID()));
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
        name.append(cleanSessionID(jParams.getSessionID()));
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
            name.append(cleanSessionID(jParams.getSessionID()));
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
                final JahiaUser user = jParams.getUser();
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
        name.append(cleanSessionID(jParams.getSessionID()));
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
            final JahiaUser user = jParams.getUser();
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
        final JahiaUser user = jParams.getUser();

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
        name.append(cleanSessionID(jParams.getSessionID()));
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

        name.append(cleanSessionID(jParams.getSessionID()));
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
                 if (contentContainerList.getChilds(jParams.getUser(), jParams.getEntryLoadRequest(), jParams.EDIT).size() == 0) {
            out = "";
                 }*/

        // #ifdef LOCK
        if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, listID);
            final JahiaUser user = jParams.getUser();
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
                append(jParams.getPage().getID());
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
        if (jParams.getPage() != null) {
            name.append(jParams.getPage().getID());
        }
        name.append(cleanSessionID(jParams.getSessionID()));
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
                basePage).append(cleanSessionID(jParams.getSessionID())).append(
                "'," + JS_WINDOW_WIDTH + "," + JS_WINDOW_HEIGHT + ")").toString();
    }

    /**
     *
     */
    public String drawShowReportLauncher(final String key) throws JahiaException {
        final StringBuffer buff = new StringBuffer();
        final StringBuffer name = new StringBuffer();
        name.append("workflow_showReport_");
        if (jParams.getPage() != null) {
            name.append(jParams.getPage().getID());
        }
        final String workflowURL = jParams.composeEngineUrl("workflow", "?screen=showReport&objectkey=" + key);
        name.append(cleanSessionID(jParams.getSessionID()));
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
        return drawPagePropertiesLauncher(false, jParams.getPageID(), null);
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
        name.append(cleanSessionID(jParams.getSessionID()));
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
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, jParams.getPageID());
            final JahiaUser user = jParams.getUser();
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
        name.append(cleanSessionID(jParams.getSessionID()));
        return drawLauncher(gui.drawUpdateTemplateUrl(theTemplate),
                name.toString());
    }

    public String drawUpdateCategoryLauncher(final Category category) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("updateCategory_");
        if (category != null) {
            name.append(category.getObjectKey().getIDInType());
        }
        name.append(cleanSessionID(jParams.getSessionID()));
        return drawLauncher(gui.drawUpdateCategoryUrl(category),
                name.toString());
    }

    public String drawAddSubCategoryLauncher(final String parentCategoryKey) throws JahiaException {
        final StringBuffer name = new StringBuffer();
        name.append("addCategory_");
        if (parentCategoryKey != null) {
            name.append(parentCategoryKey);
        }
        name.append(cleanSessionID(jParams.getSessionID()));
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
                callback, jParams.getSiteID(), jParams.getSite()
                        .getHomePageID());
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
     *@param homepageID @return The select page URL.
     * @throws JahiaException
     */
    public String drawSelectPageLauncher(final String operation,
                                         final int parentPageID,
                                         final int pageID, String callback, int siteID, int homepageID)
            throws JahiaException {
        final StringBuffer name = new StringBuffer("selectPage_");
        if (jParams.getPage() != null) {
            name.append(jParams.getPage().getID());
        }
        final Map params = new HashMap();
        params.put(SelectPage_Engine.OPERATION, operation);
        params.put(SelectPage_Engine.PARENT_PAGE_ID, new Integer(parentPageID));
        params.put(SelectPage_Engine.PAGE_ID, new Integer(pageID));
        params.put(SelectPage_Engine.SITE_ID, new Integer(siteID));
        params.put(SelectPage_Engine.HOMEPAGE_ID, new Integer(homepageID));
        params.put("callback", callback);
        name.append(cleanSessionID(jParams.getSessionID()));
        final String selectPageURL = EnginesRegistry.getInstance().getEngineByBeanName("selectPageEngine").renderLink(
                jParams, params);
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
        if (!ProcessingContext.EDIT.equals(jParams.getOperationMode())) {
            return null;
        }
        // Draw launcher without controling the lock.
        final Class objectClass = contentObject.getClass();
        if (objectClass == ContentContainerList.class) {
            if (LockKey.ADD_CONTAINER_TYPE.equals(lockKey.getType())) {
                final ContentContainerList contentContainerList = (
                        ContentContainerList) contentObject;
                final JahiaContainerList jahiaContainerList = contentContainerList.
                        getJahiaContainerList(jParams, jParams.getEntryLoadRequest());
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
                launcher = drawPagePropertiesLauncher(false, jParams.getPageID(), null);
            }
        }
        final LockService lockRegistry = ServicesRegistry.getInstance().
                getLockService();
        if (lockRegistry.isAlreadyAcquired(lockKey)) {
            final JahiaUser user = jParams.getUser();
            if (lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return jParams.composeReleaseLockURL(lockKey);
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
        if (jParams.getPage() != null) {
            name.append(jParams.getPage().getID());
        }
        name.append(cleanSessionID(jParams.getSessionID()));
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
     * Generates the HTML for the beginning of an action menu, including the
     * box around the object if the useFieldSet boolean is true.
     *
     * @param contentObject  the content object for which to generate the
     *                       action menu
     * @param lockIcon       the name of the lock icon. If null a default name
     *                       of "lock_grey.gif" is used.
     * @param actionIcon     the name of the action icon. If null a default name
     *                       of "action.gif" is used.
     * @param useFieldSet    use to specify when an HTML field set box should
     *                       be generated around the content object
     * @param namePostFix    a name to append to the resource names to be able
     *                       to generate access to varied resource for creating
     *                       different menus for the same type of objects. Example
     *                       could be "Text", or "_portlet", etc
     * @param resourceBundle the name of the resource bundle in which to
     *                       retrieve the resources.
     * @param out            the output JspWriter in which the HTML output will be
     *                       generated.
     * @throws IOException thrown if there was an error while writing to
     *                     the output (such as a socket that's been disconnected).
     */
    public void drawBeginActionMenu(final ContentBean contentObject,
                                    final String lockIcon,
                                    final String actionIcon,
                                    final boolean useFieldSet,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String labelKey,
                                    final JspWriter out)
            throws IOException {

        if (!drawingChecks(contentObject)) {
            return;
        }

        beginAjaxMenu(contentObject, lockIcon, actionIcon, useFieldSet, resourceBundle, namePostFix, labelKey, out);
    }

    /**
     * Another method to write action menus with StringBuffer instead of JspWriter
     *
     * @param contentObject
     * @param lockIcon
     * @param actionIcon
     * @param useFieldSet
     * @param namePostFix
     * @param resourceBundle
     * @param labelKey
     * @param out
     * @throws IOException
     */
    public void drawBeginActionMenu(final ContentBean contentObject,
                                    final String lockIcon,
                                    final String actionIcon,
                                    final boolean useFieldSet,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String labelKey,
                                    final StringBuffer out)
            throws IOException {

        if (!drawingChecks(contentObject)) {
            return;
        }

        beginAjaxMenu(contentObject, lockIcon, actionIcon, useFieldSet, resourceBundle, namePostFix, labelKey, out);
    }

    /**
     * Generates the HTML for the end of the action menu, closing the
     * box around the content object if we started one.
     *
     * @param contentObject  the content object for which to generate the
     *                       action menu
     * @param lockIcon       the name of the lock icon. If null a default name
     *                       of "lock.gif" is used.
     * @param actionIcon     the name of the action icon. If null a default name
     *                       of "action.gif" is used.
     * @param useFieldSet    use to specify when an HTML field set box should
     *                       be generated around the content object
     * @param namePostFix    a name to append to the resource names to be able
     *                       to generate access to varied resource for creating different menus for
     *                       the same type of objects. Example could be "Text", or "_portlet", etc
     * @param resourceBundle the name of the resource bundle in which to
     *                       retrieve the resources.
     * @param out            the output JspWriter in which the HTML output will be
     *                       generated.
     * @throws IOException thrown if there was an error while writing to
     *                     the output (such as a socket that's been disconnected).
     * @deprecated use drawEndActionMenu(final boolean useFieldSet,
     *             final JspWriter out) instead
     */
    public void drawEndActionMenu(final ContentBean contentObject,
                                  final String lockIcon,
                                  final String actionIcon,
                                  final boolean useFieldSet,
                                  final String namePostFix,
                                  final String resourceBundle,
                                  final String labelKey,
                                  final JspWriter out)
            throws IOException {

        if (drawingChecks(contentObject))
            endAjaxMenu(useFieldSet, out);
    }

    /**
     * Generates the HTML for the end of the action menu, closing the
     * box around the content object if we started one.
     *
     * @param contentObject the content object for which to generate the
     *                      action menu
     * @param useFieldSet   use to specify when an HTML field set box should
     *                      be generated around the content object
     * @param out           the output JspWriter in which the HTML output will be
     *                      generated.
     * @throws IOException thrown if there was an error while writing to
     *                     the output (such as a socket that's been disconnected).
     */
    public void drawEndActionMenu(final ContentBean contentObject,
                                  final boolean useFieldSet,
                                  final JspWriter out)
            throws IOException {

        if (drawingChecks(contentObject))
            endAjaxMenu(useFieldSet, out);
    }

    /**
     * Checks whether we must draw the action menu or not
     */
    private boolean drawingChecks(final ContentBean contentObject) {
        if (!ProcessingContext.EDIT.equals(jParams.getOperationMode())) {
            // if we are not in edit mode we don't display the GUI
            return false;
        }

        if (contentObject == null) {
            // No object -> No menu...
            return false;
        }

        try {
            if (jParams.getContentPage().isMarkedForDelete() &&
                    !org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) {
                return false;
            }

            // check for null: in old templates subcontainer lists are null
            if (contentObject.getContentObject() != null && contentObject.getContentObject().isMarkedForDelete() &&
                    !org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) {
                return false;
            }

            if (contentObject.getID() > 0 &&
                    !contentObject.getACL().getPermission(jParams.getUser(),
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

    protected Boolean getUserInitialSettingForDevMode(HttpServletRequest therequest,
                                                      String settingName) {
        final boolean isDevMode = org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode();
        String settingValue = (String) therequest.getSession().getAttribute(settingName);
        Boolean result = Boolean.valueOf(isDevMode);
        if (settingValue != null) {
            result = Boolean.valueOf(settingValue);
        } else if (isDevMode) {
            therequest.getSession().setAttribute(settingName, result.toString());
        }
        return result;
    }

    /**
     * Generates the HTML for the start of the action menu.
     *
     * @param contentObject  the content object for which to generate the
     *                       action menu
     * @param actionIcon     the name of the action icon. If null a default name
     *                       of "action.gif" is used.
     * @param useFieldSet    use to specify when an HTML field set box should
     *                       be generated around the content object
     * @param resourceBundle the name of the resource bundle in which to
     *                       retrieve the resources.
     * @param out            the output JspWriter in which the HTML output will be
     *                       generated.
     * @return The ID of the Action menu.
     * @throws IOException thrown if there was an error while writing to
     *                     the output (such as a socket that's been disconnected).
     */
    protected String beginAjaxMenu(final ContentBean contentObject,
                                   final String lockIcon,
                                   final String actionIcon,
                                   final boolean useFieldSet,
                                   final String resourceBundle,
                                   final String namePostFix,
                                   final String labelKey,
                                   final JspWriter out)
            throws IOException {

        StringBuffer buffer = new StringBuffer(256);
        String uniqueID = beginAjaxMenu(contentObject, lockIcon, actionIcon,
                useFieldSet, resourceBundle, namePostFix, labelKey, buffer);
        out.print(buffer.toString());

        return uniqueID;
    }

    /**
     * Another method to use StringBuffer instead of JspWriter
     *
     * @param contentObject
     * @param lockIcon
     * @param actionIcon
     * @param useFieldSet
     * @param resourceBundle
     * @param namePostFix
     * @param labelKey
     * @param out
     * @return
     * @throws IOException
     */
    protected String beginAjaxMenu(final ContentBean contentObject,
                                   final String lockIcon,
                                   final String actionIcon,
                                   final boolean useFieldSet,
                                   final String resourceBundle,
                                   final String namePostFix,
                                   final String labelKey,
                                   final StringBuffer out)
            throws IOException {

        if (logger.isDebugEnabled()) {
            logger.debug("beginAjaxMenu: " + contentObject + ", " + actionIcon +
                    ", " + useFieldSet + ", " + resourceBundle + ", " +
                    labelKey);
        }

        final int objectID = contentObject.getID();
        final String objectType = contentObject.getBeanType();
        final ContentBean parent = contentObject.getParent();
        final int parentID = (parent == null) ? 0 : parent.getID();
        final int definitionID = contentObject.getDefinitionID();
        final int pageID = jParams.getPageID();
        final HttpServletRequest therequest = ((ParamBean) jParams).getRequest();
        final String contextPath = Jahia.getContextPath();

        // to get flags to enable workflow and tbpublishing visu and checks
        // if dev mode is actived so all modules and semaphores are actived
        final boolean isDevMode = org.jahia.settings.SettingsBean.getInstance().isDevelopmentMode();
        Boolean displayWorkflowStates = getUserInitialSettingForDevMode(therequest, userSettings.WF_VISU_ENABLED);
        Boolean displayTimeBasedPublishing = getUserInitialSettingForDevMode(therequest, userSettings.TBP_VISU_ENABLED);
        Boolean aclDifferenceParam = getUserInitialSettingForDevMode(therequest, userSettings.ACL_VISU_ENABLED);
        if (!isDevMode) {
            try {
                String value = (String) therequest.getSession().getAttribute(userSettings.WF_VISU_ENABLED);
                displayWorkflowStates = value != null ? Boolean.valueOf(value) : null;
                if (displayWorkflowStates == null) {
                    displayWorkflowStates = Boolean.valueOf(org.jahia.settings.SettingsBean.getInstance().isWflowDisp());
                }
                value = (String) therequest.getSession().getAttribute(userSettings.TBP_VISU_ENABLED);
                displayTimeBasedPublishing = value != null ? Boolean.valueOf(value) : null;
                if (displayTimeBasedPublishing == null) {
                    displayTimeBasedPublishing = Boolean.valueOf(org.jahia.settings.SettingsBean.getInstance().isTbpDisp());
                }
                value = (String) therequest.getSession().getAttribute(userSettings.ACL_VISU_ENABLED);
                aclDifferenceParam = value != null ? Boolean.valueOf(value) : null;
                if (aclDifferenceParam == null) {
                    aclDifferenceParam = Boolean.valueOf(org.jahia.settings.SettingsBean.getInstance().isAclDisp());
                }
            } catch (final IllegalStateException e) {
                logger.error(e, e);
            }
        }
        //logger.debug("flagWorkFlowVisibitlity:"+flagWorkFlowVisibitlity+" flagTBPVisibitlity:"+flagTBPVisibitlity);

        final StringBuffer buff = new StringBuffer(100);

        final String picto = actionIcon == null ?
                buff.append(getURLImageContext()).append("/action.gif").toString() :
                actionIcon;
        buff.delete(0, buff.length());

        final String uniqueID = buildUniqueContentID(objectType, objectID,
                definitionID, parentID, pageID, lockIcon, useFieldSet, resourceBundle, namePostFix);

        // compute workflow stuff
        String objectKey = objectType + "_" + objectID;
        String realObjectKey = objectKey;
        boolean tbpCheckForPageLink = false;

        boolean showWorkflow = false;

        if (displayWorkflowStates.booleanValue()) {
            boolean workflowDisplayStatusForLinkedPages = org.jahia.settings.SettingsBean.getInstance().isWorkflowDisplayStatusForLinkedPages();

            // if linked workflow status display is enabled -> always display for pages
            // and in any case for objects with standalone workflow
            showWorkflow = workflowDisplayStatusForLinkedPages
                    && (PageBean.TYPE.equals(contentObject.getBeanType()) && ((PageBean) contentObject)
                    .getPageType() == PageInfoInterface.TYPE_DIRECT)
                    || contentObject.isIndependantWorkflow();

            // also check for container with a page field
            if (ContainerBean.TYPE.equals(contentObject.getBeanType())) {
                final ContentContainer cont = (ContentContainer) contentObject
                        .getContentObject();
                tbpCheckForPageLink = true;
                try {
                    final Iterator en = cont.getJahiaContainer(jParams,
                            jParams.getEntryLoadRequest()).getFields();
                    while (en.hasNext()) {
                        final JahiaField field = (JahiaField) en.next();
                        if (field.getType() == FieldTypes.PAGE) {
                            final JahiaPage dest = (JahiaPage) field
                                    .getObject();
                            if (dest != null && dest.getID() > 0) {
                                objectKey = PageBean.TYPE + "_" + dest.getID();
                                showWorkflow = workflowDisplayStatusForLinkedPages ? dest
                                        .getPageType() == PageInfoInterface.TYPE_DIRECT
                                        : showWorkflow;
                                break;
                            }
                        }
                    }
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        if (useFieldSet) {
            buff.append("<fieldset id=\"fieldset_").append(uniqueID);
            if (showWorkflow) {
                buff.append("\" class=\"workflow\">");
            } else {
                buff.append("\" class=\"unlocked\">");
            }
            out.append(buff.append("\n").toString());
            out.append("<legend align=\"right\">\n");
            buff.delete(0, buff.length());
        }

        if (showWorkflow && (!PageBean.TYPE.equals(objectType))) {

            buff.append(objectKey);

            try {
                    out.append("<a href=\"javascript:");
                    out.append(drawWorkflowLauncher(buff.toString()));
                    out.append("\"");
                    out.append("\">\n");
                    buff.delete(0, buff.length());

                    out.append("<img id=\"");
                    out.append("tbpState_");
                    out.append(realObjectKey);
                    out.append("\" border=\"0\" src=\"");
                    out.append(contextPath);
                    out.append("/ajaxaction/GetWorkflowState?params=/op/edit/pid/");
                    out.append(jParams.getPageID());
                    out.append("&key=");
                    out.append(objectKey);
                    out.append("\" />");
                    out.append("</a>\n");
            } catch (JahiaException je) {
                logger.error(je.getMessage(), je);
            }
        }

        // time based publishing status
        String tbpObjectKey = objectKey;
        if (displayTimeBasedPublishing.booleanValue() &&
                (ContainerBean.TYPE.equals(objectType) || PageBean.TYPE.equals(objectType))) {
            if (!tbpCheckForPageLink) {
                try {
                    ObjectKey objKey = ObjectKey.getInstance(tbpObjectKey);
                    if (ContainerBean.TYPE.equals(objectType)) {
                        final ContentContainer cont = (ContentContainer) contentObject.getContentObject();
                        final JahiaObjectManager jahiaObjectManager =
                                (JahiaObjectManager) SpringContextSingleton.getInstance()
                                        .getContext().getBean(JahiaObjectManager.class.getName());
                        JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager
                                .getJahiaObjectDelegate(objKey);
                        if (jahiaObjectDelegate.getRule() == null
                                || jahiaObjectDelegate.getRule().getInherited().booleanValue()) {
                            int jahiaPageID = -1;
                            final Iterator en = cont.getJahiaContainer(jParams, jParams.getEntryLoadRequest()).getFields();
                            while (en.hasNext()) {
                                final JahiaField field = (JahiaField) en.next();
                                if (field.getType() == FieldTypes.PAGE) {
                                    final JahiaPageField pageField = (JahiaPageField) field;

                                    final JahiaPage dest = (JahiaPage) pageField.getObject();
                                    if (dest == null) continue;

                                    jahiaPageID = dest.getID();
                                    //logger.debug("PageField page ID: " + jahiaPageID);
                                    if (jahiaPageID > 0) break;
                                }
                            }
                            if (jahiaPageID > 0) {
                                tbpObjectKey = PageBean.TYPE + "_" + jahiaPageID;
                            }
                        }
                    }
                } catch (Exception t) {
                    logger.debug("Error handling time based publishing for page link, use local rule", t);
                }

                try {
                    ObjectKey objKey = ObjectKey.getInstance(tbpObjectKey);
                    if (PageBean.TYPE.equals(objKey.getType())) {
                        ContentPage contentPage = (ContentPage)
                                ContentPage.getContentObjectInstance(objKey);
                        if (contentPage.getPageType(jParams.getEntryLoadRequest()) == JahiaPage.TYPE_LINK) {
                            int pageLinkId = contentPage.getPageLinkID(jParams);
                            if (pageLinkId > 0) {
                                ContentPage pageLink = ContentPage.getPage(pageLinkId);
                                if (pageLink != null) {
                                    final JahiaObjectManager jahiaObjectManager =
                                            (JahiaObjectManager) SpringContextSingleton.getInstance()
                                                    .getContext().getBean(JahiaObjectManager.class.getName());
                                    JahiaObjectDelegate jahiaObjectDelegate = jahiaObjectManager
                                            .getJahiaObjectDelegate(objKey);
                                    if (jahiaObjectDelegate.getRule() == null
                                            || jahiaObjectDelegate.getRule().getInherited().booleanValue()) {
                                        tbpObjectKey = pageLink.getObjectKey().toString();
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception t) {
                    logger.debug("Error handling time based publishing for page link, use local rule", t);
                }
            }
            //logger.debug("displaying TBP state");

            //todo port the code in ajax action here
            final String actionURL = contextPath + "/ajaxaction/GetTimeBasedPublishingState?params=/op/edit/pid/" +
                    jParams.getPageID() + "&key=" + tbpObjectKey;

            String serverURL = actionURL + "&displayDialog=true";
            String dialogTitle = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dialogTitle",
                    jParams.getLocale(), "Informational");
            StringBuffer cmdBuffer = new StringBuffer("handleTimeBasedPublishing(event,'");
            cmdBuffer.append(serverURL).append("','");
            cmdBuffer.append(tbpObjectKey).append("',").append("'/op/edit/pid/")
                    .append(jParams.getPageID()).append("','").append(dialogTitle).append("')");
            out.append("<img class=\"timeBasedPublishingState\" id=\"");
            out.append("img_");
            out.append(realObjectKey);
            out.append("\" border=\"0\" src=\"");
            out.append(actionURL);
            out.append("\" onClick=\"");
            out.append(cmdBuffer.toString());
            out.append("\" />\n");
        }
        final ContentObject obj = contentObject.getContentObject();
        if (aclDifferenceParam.booleanValue() && !
                obj.getObjectKey().toString().equals("ContentPage_" + jParams.getSite().getHomePageID()) &&
                (!obj.isAclSameAsParent() && obj.getACL().getACL().getEntries().size() > 0)) {
            String title = getResource(resourceBundle, "differentACLTitle");
            if (title == null || title.length() == 0) {
                title = "This object and its parent have different ACLs";
            }
            final StringBuffer spanBuffer = new StringBuffer();
            spanBuffer.append("<span title=\"");
            spanBuffer.append(title);
            spanBuffer.append("\" class=\"differentACL\">&nbsp;");

            try {
                final Class theClass = obj.getClass();
                final String launcher;
                if (theClass == ContentContainer.class) {
                    final JahiaContainer jahiaContainer = ((ContainerBean) contentObject).getJahiaContainer();
                    int contextualContainerListId = 0;
                    if (jahiaContainer != null) {
                        contextualContainerListId = jahiaContainer.getContextualContainerListID();
                    }
                    launcher = drawUpdateContainerLauncher((ContentContainer) obj, "rightsMgmt",
                            contextualContainerListId);
                } else if (theClass == ContentPage.class) {
                    launcher = drawPagePropertiesLauncher(false, obj.getID(), "rightsMgmt");
                } else if (obj instanceof ContentField) {
                    launcher = drawUpdateFieldLauncher((ContentField) obj, "rightsMgmt");
                } else {
                    launcher = "";
                }
                if (launcher.length() > 0) {
                    out.append("<a href=\"javascript:");
                    out.append(launcher);
                    out.append("\">");
                    out.append(spanBuffer.toString());
                    out.append("</span></a>");
                } else {
                    out.append(spanBuffer.toString());
                    out.append("</span>");
                }
            } catch (final JahiaException je) {
                logger.error(je, je);
            }
        }
        int contextualContainerListId = 0;
        if (contentObject instanceof ContainerBean) {
            final JahiaContainer jahiaContainer = ((ContainerBean) contentObject).getJahiaContainer();
            if (jahiaContainer != null) {
                contextualContainerListId = jahiaContainer.getContextualContainerListID();
            }
        }
        final String ajaxFunction = buildAjaxCall(objectType, objectID, definitionID, parentID, pageID, uniqueID,
                jParams.getLocale().toString(), contextualContainerListId);
        out.append("  <a id=\"button_");
        out.append(uniqueID);
        out.append("\" href=\"");
        out.append(ajaxFunction);
        out.append("\"\n");
        out.append("    onmouseover=\"buttonMouseover(event, '");
        out.append(uniqueID);
        out.append("')\"><img\n");

        String resource = getResource(resourceBundle, objectType + "Operations");

        //check for special status:picked/picker
        String pickedID;
        final ContentContainer cc;//the current container
        ContentObject pickedObject;// the source of the linked copy(the picked)
        try {
            cc = ContentContainer.getContainer(objectID);
            Set pickerObjects = null;
            if (cc != null) {
                pickerObjects = cc.getPickerObjects();
            }
            int pickerObjectsSize = 0;
            if (pickerObjects != null) {
                pickerObjectsSize = pickerObjects.size();
            }
            if (cc != null && pickerObjectsSize > 0) {
                //this container is picked
                resource = resource + " (" + getResource(resourceBundle, "numbercopies") + ":" + pickerObjectsSize + ")";
                buff.append(" src=\"").append(picto).append("\" alt=\"").append(resource).
                        append("\"").append(" title=\"").append(resource).append("\" border=\"0\"/>");

            } else if (cc != null && contentObject.isPicker() && cc.getPickedObject() != null) {
                // picker status of object
                pickedObject = cc.getPickedObject();
                pickedID = "" + pickedObject.getID();
                resource = resource + " (" + getResource(resourceBundle, "copylinkid") + ":" + pickedID + ")";

                //unused (relative to ability to display beside the action icon if needed)

                //pickedpageID=""+((ContentContainer)pickedObject).getPageID();

                buff.append(" src=\"").append(getURLImageContext()).append("/picker.gif").append("\" alt=\"").append(resource).
                        append("\"").append(" title=\"").append(resource).append("\" border=\"0\"/>");

            } else {
                // standard objects
                buff.append(" src=\"").append(picto).append("\" alt=\"").append(resource).
                        append("\"").append(" title=\"").append(resource).append("\" border=\"0\"/>");
            }
        } catch (JahiaException e) {
            logger.debug(e);
            // standard objects
            buff.append(" src=\"").append(picto).append("\" alt=\"").append(resource).
                    append("\"").append(" title=\"").append(resource).append("\" border=\"0\"/>");
        }

        out.append(buff.toString()).append("\n");
        buff.delete(0, buff.length());


        if (labelKey != null) {
            out.append("&nbsp;");
            out.append(getResource(resourceBundle, labelKey)).append("\n");
        }
        out.append("</a>\n");
        //display the url of the source(?)
        /*
        if(contentObject.isPicker() && !pickedID.equalsIgnoreCase("")) {
            out.print("&nbsp;<a href=\""
                    +Jahia.getContextPath()
                    + Jahia.getServletPath()
                    + "/op/edit/pid/" + pickedpageID+"\" title=\"to see the source of this linked copy\">["+pickedID+"]</a>");
        }
        */
        if (useFieldSet) {
            out.append("</legend>\n");
        }

        out.append("<div id=\"");
        out.append(uniqueID);
        out.append(
                "\" class=\"menu\" style=\"left: 161px; top: 265px; visibility: hidden;\">\n</div>\n");

        return uniqueID;
    }

    /**
     *
     */
    protected void endAjaxMenu(final boolean useFieldSet, final JspWriter out) throws IOException {
        if (useFieldSet) {
            out.println("</fieldset>");
        }
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
        buff.append(((ParamBean) jParams).getRequest().getContextPath()).append("', '");
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

        final Locale locale = jParams.getLocale();
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
        return jParams.getContextPath() + IMAGE_DIRECTORY;
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
        name.append(cleanSessionID(jParams.getSessionID()));
        return drawLauncher(gui.drawUpdateAppplicationUrl(applicationBean), name.toString());
    }

    /**
     *
     */
    public String drawRestoreContainerLauncher(final ContentContainer contentContainer)
            throws JahiaException {

        if (contentContainer == null) return "";
        final JahiaUser user = jParams.getUser();

        if (!contentContainer.checkWriteAccess(user, false, false)) return "";

        final int containerID = contentContainer.getID();
        final StringBuffer name = new StringBuffer();
        name.append("restoreContainer_");

        name.append(containerID);
        name.append(cleanSessionID(jParams.getSessionID()));
        final String restoreContainerURL = gui.drawRestoreContainerUrl(contentContainer);
        final StringBuffer buff = new StringBuffer();
        return restoreContainerURL.equals("") ? "" :
                buff.append("OpenJahiaScrollableWindow('").append(restoreContainerURL).
                        append("','").append(name.toString()).append("',").
                        append(790).append(",").append(340).append(")").toString();
    }
}
