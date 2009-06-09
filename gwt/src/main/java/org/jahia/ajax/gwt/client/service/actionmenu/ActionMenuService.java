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
package org.jahia.ajax.gwt.client.service.actionmenu;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;

import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.actionmenu.actions.GWTJahiaAction;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingDetails;
import org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing.GWTJahiaTimebasedPublishingState;
import org.jahia.ajax.gwt.client.data.actionmenu.workflow.GWTJahiaWorkflowState;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalState;
import org.jahia.ajax.gwt.client.data.actionmenu.GWTJahiaGlobalStateKey;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffState;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 22 janv. 2008 - 11:50:45
 */
public interface ActionMenuService extends RemoteService {

    /**
     * Utility/Convenience class.
     * Use JahiaService.App.getInstance() to access static instance of MyServiceAsync
     */
    public static class App {
        private static ActionMenuServiceAsync ourInstance = null;

        public static synchronized ActionMenuServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint()+"actionmenus/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (ActionMenuServiceAsync) GWT.create(ActionMenuService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }
            return ourInstance;
        }
    }

    /**
     * Retrieve the global state (ACL difference, workflow state and timebased publishing state) for
     * a given object.
     *
     * @param page the current page
     * @param objectKey the object key
     * @param wfKey the workflow object key
     * @param languageCode the current language
     * @return a wrapper containing all needed states
     */
    public GWTJahiaGlobalState getGlobalStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode) ;

    /**
     * Retrieve the global state (ACL difference, workflow state and timebased publishing state) for
     * given objects.
     *
     * @param page the current page
     * @param key list of object keys
     * @return a list of wrappers containing all needed states
     */
    public List<GWTJahiaGlobalState> getGlobalStateForObject(GWTJahiaPageContext page, List<GWTJahiaGlobalStateKey> keys) ;

    /**
     * Retrieve the full workflow state (a code corresponding to a given image).
     *
     * @param page the current page
     * @param objectKey the object to retrieve workflow
     * @param wfKey the workflow object key
     * @param languageCode a language code (can be null)
     * @return the workflow state as a 'xyz' String
     */
    public GWTJahiaWorkflowState getWorkflowStateForObject(GWTJahiaPageContext page, String objectKey, String wfKey, String languageCode) ;

    /**
     * Return the url to the timebased publishing icon.
     *
     * @param page the current page
     * @param objectKey the content object key
     * @return a wrapper containing the state
     */
    public GWTJahiaTimebasedPublishingState getTimebasedPublishingState(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Retrieve timebased publishing details for a given content object represented by its state wrapper.
     *
     * @param page the current page
     * @param state the state wrapper of the content object
     * @return the details concerning this object's timebased publishing
     */
    public GWTJahiaTimebasedPublishingDetails getTimebasedPublishingDetails(GWTJahiaPageContext page, GWTJahiaTimebasedPublishingState state) ;

    /**
     * Retrieve the ACL status of a given object if requested and if its parents are different.
     *
     * @param page the current page
     * @param objectKey the content object key
     * @return the acl diff status as a wrapper
     */
    public GWTJahiaAclDiffState getAclDiffState(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Retrieve ACL difference details for a given content object represented by its object key.
     *
     * @param page the current page
     * @param objectKey the key of the content object
     * @return the details concerning this object's ACL difference
     */
    public GWTJahiaAclDiffDetails getAclDiffDetails(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Checks if the action menu is available for the current content object. It can be either a ContainerList, a Container,
     * a Page or a Field. Return a string containing the label to attach to the action menu if wanted (empty otherwise), or null if the menu is not available.
     *
     * @param page the page on which the object is defined
     * @param objectKey the key of the content object
     * @param bundleName the bundle name if required (can be null)
     * @param labelKey the label key if required (can be null)
     * @return a string if available, null otherwise
     */
    public String isActionMenuAvailable(GWTJahiaPageContext page, String objectKey, String bundleName, String labelKey) ;

    /**
     * Get all the available actions for a given object.
     *
     * @param page the current page
     * @param objectKey the objectkey to retrieve the actions
     * @param bundleName the custom bundle to use
     * @param namePostFix the postfix to add to action labels
     * @param enableAddItem true to include the container list 'add' item
     * @return a list of actions
     */
    public List<GWTJahiaAction> getAvailableActions(GWTJahiaPageContext page, String objectKey, String bundleName, String namePostFix) ;

    /**
     * Check if the clipboard contains data.
     *
     * @return true if empty, false otherwise
     */
    public Boolean clipboardIsEmpty() ;

    /**
     * Copy the selected object in the clipboard.
     *
     * @param page the current page
     * @param objectKey the content object key
     * @return true if copy succeeded, false otherwise
     */
    public Boolean clipboardCopy(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Paste the clipboard content into the selected object.
     *
     * @param page the current page
     * @param objectKey the target object key
     * @return true if paste succeeded, false otherwise
     */
    public Boolean clipboardPaste(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Paste the clipboard content into the selected object as a reference (linked copy).
     *
     * @param page the current page
     * @param objectKey the target object key
     * @return true if paste succeeded, false otherwise
     */
    public Boolean clipboardPasteReference(GWTJahiaPageContext page, String objectKey) ;

    /**
     * Hack to avoid a rpc issue (blank method)
     * @param action the parameter (type in fact) causing trouble
     */
    public void hack(GWTJahiaAction action);

}
