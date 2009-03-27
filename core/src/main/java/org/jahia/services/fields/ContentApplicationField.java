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

package org.jahia.services.fields;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.data.ConnectionTypes;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.ajax.gwt.client.core.JahiaType;

import java.util.*;

/**
 * @author NK
 */
public class ContentApplicationField extends ContentField {

    private static final long serialVersionUID = 4073168079780862548L;
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContentApplicationField.class);
    private static long uniqueValue = 0;

    protected ContentApplicationField (Integer ID,
                                       Integer jahiaID,
                                       Integer pageID,
                                       Integer ctnid,
                                       Integer fieldDefID,
                                       Integer fieldType,
                                       Integer connectType,
                                       Integer aclID,
                                       List<ContentObjectEntryState> activeAndStagingEntryStates,
                                       Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super (ID.intValue (), jahiaID.intValue (), pageID.intValue (), ctnid.intValue (),
                fieldDefID.intValue (),
                fieldType.intValue (), connectType.intValue (), aclID.intValue (),
                activeAndStagingEntryStates, activeAndStagedDBValues);
    }

    public static synchronized ContentApplicationField createApplication (int siteID,
                                                                          int pageID,
                                                                          int containerID,
                                                                          int fieldDefID,
                                                                          int parentAclID,
                                                                          int aclID,
                                                                          String appID,
                                                                          ProcessingContext jParams)
            throws JahiaException {
        ContentApplicationField result =
                (ContentApplicationField) ContentField.createField (siteID, pageID,
                        containerID, fieldDefID,
                        ContentFieldTypes.APPLICATION,
                        ConnectionTypes.LOCAL,
                        parentAclID, aclID);
        EntrySaveRequest saveRequest = new EntrySaveRequest (jParams.getUser (),
                ContentField.SHARED_LANGUAGE, true);

        result.setAppID (appID, saveRequest);
        return result;
    }


    /**
     * Gets the String representation of this field. In case of an Application,
     * it will be the output of the application, in case of a bigtext it will
     * be the full content of the bigtext, etc. This is called by the public
     * method getValue of ContentField, which does the entryState resolving
     * This method should call getDBValue to get the DBValue
     */
    public String getValue (ProcessingContext jParams, ContentObjectEntryState entryState)
            throws JahiaException {

        String appID = getDBValue (entryState);
        if (appID.equals ("<empty>")) {
            return "";
        }

        logger.debug ("Dispatching to application for appID=" + appID + "...");

        String tmpValue = "";

        if (jParams != null) {
            if (jParams instanceof ParamBean) {
                int id = this.getID();
                String contextId = getProperty("contextId");
                if (contextId != null) {
                    id = Integer.parseInt(contextId);
                }

                if (jParams.settings().isPortletAJAXRenderingActivated() &&
                    !jParams.getUser().getUsername().equals(JahiaUserManagerService.GUEST_USERNAME)) {
                    tmpValue = "<div id=\""+ uniqueValue++ +"\" windowID=\""+id+
                            "\" entryPointInstanceID=\""+appID+"\" " +
                            JahiaType.JAHIA_TYPE + "=\"" + JahiaType.PORTLET_RENDER +
                            "\" pathInfo=\""+jParams.getPathInfo()+
                            "\" queryString=\""+jParams.getQueryString()+
                            "\"></div>";
                } else {
                    tmpValue = ServicesRegistry.getInstance ().
                            getApplicationsDispatchService ().
                            getAppOutput (id, appID, (ParamBean) jParams);
                }

            } else {
                // we are not in the context of an HTTP request, so we cannot dispatch
                // to the web application. This happens for example in the case of
                // the background indexation of the search engine.
                tmpValue="";
            }
        }
        if (tmpValue != null) {
            try {
                tmpValue = (new RE ("</?html>", RE.MATCH_CASEINDEPENDENT)).
                        subst (tmpValue, "");
            } catch (RESyntaxException e) {
                logger.debug (".getValue, exception : " + e.toString ());
            }
        } else {
            tmpValue = "";
        }
        return tmpValue;
    }

    public String getAppID(ContentObjectEntryState entryState) throws JahiaException {
        String appID = getDBValue (entryState);
        if (appID.equals ("<empty>")) {
            return null;
        }
        try {
            return appID;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Sets the String representation of this field.
     * This method should call preSet.
     */
    public void setAppID (String value, EntrySaveRequest saveRequest) throws JahiaException {
        preSet (value, saveRequest);
        logger.debug ("Saving application field...");
        postSet(saveRequest);

    }

    public void unsetValue (EntrySaveRequest saveRequest) throws JahiaException {
        preSet ("", saveRequest);
        postSet(saveRequest);
    }    

    /**
     * get the Value that will be added to the search engine for this field.
     * for a bigtext it will be the content of the bigtext, for an application
     * the string will be empty!
     * Do not return null, return an empty string instead.
     *
     * @param jParams the jParam containing the loadVersion and locales
     */
    public String getValueForSearch (ProcessingContext jParams, ContentObjectEntryState entryState)
            throws JahiaException {
        //return FormDataManager.formDecode(getDBValue(entryState));
        return getDBValue (entryState);
    }

    /**
     * This method is called when there is a workflow state change
     * Such as  staged mode -> active mode (validation), active -> inactive (for versioning)
     * and also staged mode -> other staged mode (workflow)
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     * @param jParams        ProcessingContext object used to get information about the user
     *                       doing the request, the current locale, etc...
     *
     * @return null if the entry state change wasn't an activation, otherwise it
     *         returns an object that contains the status of the activation (whether
     *         successfull, partial or failed, as well as messages describing the
     *         warnings during the activation process)
     */
    public ActivationTestResults changeEntryState (ContentObjectEntryState fromEntryState,
                                                   ContentObjectEntryState toEntryState,
                                                   ProcessingContext jParams,
                                                   StateModificationContext stateModifContext)
            throws JahiaException {
        return new ActivationTestResults ();
    }

    protected ActivationTestResults isContentValidForActivation (
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext) throws JahiaException {
        /** @todo to be implemented */
        return new ActivationTestResults ();
    }

    //--------------------------------------------------------------------------
    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared () {
        return true;
    }

    protected boolean isEntryInitialized (ContentObjectEntryState curEntryState)
        throws JahiaException {
        String entryValue = getDBValue(curEntryState);
        if (entryValue == null) {
            return false;
        }
        return !entryValue.equals("") &&
               !entryValue.equals("<empty>");
    }


}
