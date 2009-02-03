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

package org.jahia.ajax.gwt.templates.components.actionmenus.server.helper;

import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.lock.LockService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.engines.containerlistproperties.ContainerListProperties_Engine;
import org.jahia.engines.updatecontainer.UpdateContainer_Engine;
import org.jahia.engines.pages.PageProperties_Engine;
import org.jahia.engines.updatefield.UpdateField_Engine;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 17:27:33
 */
public class ActionMenuServiceHelper {

    // ================================================================
    //    COMMON METHODS
    // ================================================================
    
    /**
     * Retrieve initial setting concerning a given property.
     *
     * @param therequest the current request
     * @param settingName the property name
     * @param isDevMode dev mode enabled
     * @return the setting value
     */
    public static boolean getUserInitialSettingForDevMode(final HttpServletRequest therequest, final String settingName, final boolean isDevMode) {
        boolean ret = isDevMode ;
        String settingValue = (String) therequest.getSession().getAttribute(settingName);
        if (settingValue != null) {
            ret = Boolean.valueOf(settingValue);
        } else if (ret) {
            therequest.getSession().setAttribute(settingName, String.valueOf(ret));
        }
        return true ; //ret; TODO remove this useless "devmode" status for action menu display
    }

    /**
     *
     *
     * @param contentContainerList the content container list to update
     * @param jParams the processing context
     * @param checkLock check the lock prior to engine opening
     * @param focusedFieldId the field to open (0 if none)
     * @param screen the screen to display (can be null, bypassed if field > 0)
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     * @return the engine url
     */
    public static String drawContainerListPropertiesLauncher(final ProcessingContext jParams, final ContentContainerList contentContainerList, final boolean checkLock, final int focusedFieldId, final String screen) throws JahiaException {
        if (contentContainerList == null) {
            return "";
        }
        if (checkLock) {
            final int containerListID = contentContainerList.getID();
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINERLIST_TYPE, containerListID);
            final JahiaUser user = jParams.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return "";
            }
        }

        String updateContainerListURL = drawContainerListPropertiesUrl(jParams, contentContainerList, focusedFieldId);
        if (updateContainerListURL.length() > 0) {
            if (focusedFieldId > 0) {
                // also add focused field ID or screen if needed
                updateContainerListURL = new StringBuffer().append(updateContainerListURL).append("&fid=").append(focusedFieldId).toString();
            } else if (screen != null && screen.length() > 0) {
                updateContainerListURL = new StringBuffer().append(updateContainerListURL).append("&gotoscreen=").append(screen).toString();
            }
        }

        return updateContainerListURL;
    }

    /**
     * @param jParams the processing context
     * @param contentContainerList the content container list
     * @param focusedFieldId the field to update
     * @return the url to the engine
     * @throws JahiaException sthg bad happened
     */
    public static String drawContainerListPropertiesUrl(ProcessingContext jParams, final ContentContainerList contentContainerList, int focusedFieldId) throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        String url = "";
        if (aclService.getSiteActionPermission("engines.actions.update",
                jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + jParams.getLocale().toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0) {
            url = ActionMenuURIFormatter.drawUrlCheckWriteAccess(jParams, ContainerListProperties_Engine.ENGINE_NAME, contentContainerList, false, false);
        }
        if (focusedFieldId > 0 && url.length() > 0) {
            url = new StringBuffer(url.length() + 16).append(url).append("&fid=").append(focusedFieldId).toString();
        }

        return url;
    }

    /**
     *
     *
     * @param contentContainer the content container to update
     * @param jParams the processing context
     * @param checkLock check the lock prior to engine opening
     * @param focusedFieldId the field to open (0 if none)
     * @param screen the screen to display (can be null, bypassed if field > 0)
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     * @return the engine url
     */
    public static String drawUpdateContainerLauncher(final ProcessingContext jParams, final ContentContainer contentContainer, final boolean checkLock, final int focusedFieldId, final String screen) throws JahiaException {
        if (contentContainer == null) {
            return "";
        }
        if (checkLock) {
            final int containerID = contentContainer.getID();
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_CONTAINER_TYPE, containerID);
            final JahiaUser user = jParams.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return "";
            }
        }

        String updateContainerURL = drawUpdateContainerUrl(jParams, contentContainer, focusedFieldId);
        if (updateContainerURL.length() > 0) {
            if (focusedFieldId > 0) {
                // also add focused field ID or screen if needed
                updateContainerURL = new StringBuffer().append(updateContainerURL).append("&fid=").append(focusedFieldId).toString();
            } else if (screen != null && screen.length() > 0) {
                updateContainerURL = new StringBuffer().append(updateContainerURL).append("&gotoscreen=").append(screen).toString();
            }
        }

        return updateContainerURL;
    }

    /**
     * @param jParams the processing context
     * @param contentContainer the content container
     * @param focusedFieldId the field to update
     * @return the url to the engine
     * @throws JahiaException sthg bad happened
     */
    public static String drawUpdateContainerUrl(ProcessingContext jParams, final ContentContainer contentContainer, int focusedFieldId) throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        String url = "";
        if (aclService.getSiteActionPermission("engines.actions.update",
                jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + jParams.getLocale().toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0) {
            url = ActionMenuURIFormatter.drawUrlCheckWriteAccess(jParams, UpdateContainer_Engine.ENGINE_NAME, contentContainer, false, false);
        }
        if (focusedFieldId > 0 && url.length() > 0) {
            url = new StringBuffer(url.length() + 16).append(url).append("&fid=").append(focusedFieldId).toString();
        }

        return url;
    }

    /**
     *
     * @param jParams the processing context
     * @param checkLock check the lock prior to engine opening
     * @param pageId the page id
     * @param screen the screen to open (can be null)
     * @return the engine url
     * @throws org.jahia.exceptions.JahiaException sthg bad happened
     */
    public static String drawPagePropertiesLauncher(ProcessingContext jParams, final boolean checkLock, final int pageId, final String screen) throws JahiaException {
        if (pageId < 1) {
            return "" ;
        }
         if (checkLock) {
            final LockService lockRegistry = ServicesRegistry.getInstance().getLockService();
            final LockKey lockKey = LockKey.composeLockKey(LockKey.UPDATE_PAGE_TYPE, jParams.getPageID());
            final JahiaUser user = jParams.getUser();
            if (!lockRegistry.canRelease(lockKey, user, user.getUserKey())) {
                return "";
            }
        }
        String url = drawPagePropertiesUrl(jParams, pageId);
        if (url != null && url.length() > 0 && screen != null && screen.length() > 0) {
            url = new StringBuffer(url.length() + 25).append(url).append("&gotoscreen=").append(screen).toString();
        }
        return url ;
    }

    /**
     * Return the page prperties engine url.
     *
     * @param jParams the processing context
     * @param pageID the page id
     * @return the engine url
     * @throws JahiaException sthg bad happened
     */
    public static String drawPagePropertiesUrl(ProcessingContext jParams, final int pageID) throws JahiaException {
        final int oldPageID = jParams.getPageID();
        if (oldPageID != pageID) jParams.changePage(pageID);
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        final String result;
        if (aclService.getSiteActionPermission("engines.actions.update",
                jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + jParams.getLocale().toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0) {
            result = ActionMenuURIFormatter.drawUrlCheckWriteAccess(jParams, PageProperties_Engine.ENGINE_NAME, jParams.getPage(), false, false);
        } else {
            result = "";
        }
        if (oldPageID != pageID) jParams.changePage(oldPageID);
        return result;
    }

    /**
     * Returns the URL of the field update window
     *
     * @param jParams the processing context
     * @param contentField the field to update
     * @param screen the screen to open (can be null)
     * @return the engine url
     * @throws JahiaException sthg bad happened
     */
    public static String drawUpdateFieldLauncher(final ProcessingContext jParams, final ContentField contentField, final String screen) throws JahiaException {
        final StringBuffer buffer = new StringBuffer();
        buffer.append(drawUpdateFieldUrl(jParams, contentField));
        if (screen != null && screen.length() > 0) {
            buffer.append("&gotoscreen=");
            buffer.append(screen);
        }
        return buffer.toString() ;
    }

    /**
     * @param jParams the processing context
     * @param contentField the field to update
     * @return the engine url
     * @throws JahiaException sthg bad happened
     */
    public static String drawUpdateFieldUrl(ProcessingContext jParams, final ContentField contentField)
            throws JahiaException {
        final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        if (aclService.getSiteActionPermission("engines.actions.update",
                jParams.getUser(), JahiaBaseACL.READ_RIGHTS,
                jParams.getSiteID()) > 0 &&
                aclService.getSiteActionPermission("engines.languages." + jParams.getLocale().toString(),
                        jParams.getUser(),
                        JahiaBaseACL.READ_RIGHTS,
                        jParams.getSiteID()) > 0) {
            return ActionMenuURIFormatter.drawUrlCheckWriteAccess(jParams, UpdateField_Engine.ENGINE_NAME, contentField, false, false);
        } else {
            return "";
        }
    }

    /**
     * Return a char array containin the binary representation of a given integer
     * between 0 and 7 filling beginning with '0's.
     * @param nb the number to transform
     * @return the binary char array (size = 3)
     */
    public static char[] getBinaryCharArrayFromInt(int nb) {
        StringBuilder reqType = new StringBuilder(Integer.toBinaryString(nb)) ;
            if (reqType.length() == 1) {
                reqType.insert(0, "00") ;
            } else if (reqType.length() == 2) {
                reqType.insert(0, "0") ;
            }
            char[] chars = new char[3] ;
            reqType.getChars(0, 3, chars, 0);
        return chars ;
    }

}
