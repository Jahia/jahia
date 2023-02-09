/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.applications;

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.WebAppContext;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This Service is used to manage the jahia application definitions.
 *
 * @author Khue ng
 * @version 1.0
 */
public abstract class ApplicationsManagerService extends JahiaService {

    //--------------------------------------------------------------------------

    /**
     * return an Application Definition get directly from db
     *
     * @param appID the appID
     * @return ApplicationBean, the Application Definition
     */
    public abstract ApplicationBean getApplication(String appID)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * return an Application Definition looking at its context.
     *
     * @param context , the context
     * @return ApplicationBean, the Application Definition
     */
    public abstract ApplicationBean getApplicationByContext(String context)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * return all application Definitions
     *
     * @return Iterator an enumerations of ApplicationBean or null if empty
     */
    public abstract List<ApplicationBean> getApplications()
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * set an application Visible to users
     *
     * @param appID   the application identification
     * @param visible status
     * @return false on error
     */
    public abstract boolean setVisible(String appID, boolean visible)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * Add a new Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public abstract boolean addDefinition(ApplicationBean app)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * Save the Application Definition.
     * both in ApplicationsRegistry and in Persistance
     *
     * @param app the app Definition
     * @return false on error
     */
    public abstract boolean saveDefinition(ApplicationBean app)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * Removes an application from the persistant storage area and from registry.
     *
     * @param appID identifier of the application to remove from the persistant
     *              storage area
     * @throws org.jahia.exceptions.JahiaException
     *          if there was an error while removing the application
     *          data from persistant storage area
     */
    public abstract void removeApplication(String appID)
            throws JahiaException;

    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------

    /**
     * Get an ApplicationContext for a given application id
     *
     * @param id , the application id
     * @return the application context , null if not found
     */
    public abstract WebAppContext getApplicationContext(String id)
            throws JahiaException;

    //--------------------------------------------------------------------------

    /**
     * Get an ApplicationContext for a given context
     *
     * @param appBean , the application bean
     * @return the application context , null if not found
     */
    public abstract WebAppContext getApplicationContext(ApplicationBean
            appBean)
            throws JahiaException;

    /**
     * Creates an instance of an application entry point definition. This
     * creates the entry in the database.
     *
     * @param entryPointDefinition EntryPointDefinition
     * @throws JahiaException
     * @returns the created instance for the entry point definition.
     */
    public abstract EntryPointInstance createEntryPointInstance(
            EntryPointDefinition entryPointDefinition, String path)
            throws JahiaException;

    /**
     * Returns the entry point definitions corresponding to the application,
     * or an empty list if there was a problem retrieving them.
     *
     * @param appBean ApplicationBean the application for which to retrieve
     *                the entry point definitions
     * @return List a list of EntryPointDefinition objects, or an empty
     *         list if there was a problem retrieving the definitions.
     */
    public abstract List<EntryPointDefinition> getAppEntryPointDefinitions(ApplicationBean
            appBean);

    /**
     * Retrieves an EntryPointInstance object from the persistance system by
     * using it's ID as a search key
     *
     * @param epInstanceID int the unique identifier for the EntryPointInstance
     *                     object in the persistence system
     * @param workspaceName the name of the workspace in which to look for the entrypoint instance.
     * @return EntryPointInstance the object if found in the persistance system,
     *         or null otherwise.
     * @throws JahiaException thrown if there was an error communicating with
     *                        the persistence system.
     */
    public abstract EntryPointInstance getEntryPointInstance(String epInstanceID, String workspaceName)
            throws JahiaException;

    /**
     * Get the corresponding an entrypoint obejct
     *
     * @param node
     * @return
     */
    public abstract EntryPointInstance getEntryPointInstance(JCRPortletNode node) throws RepositoryException;

    /**
     * Removes an entry point instance from the persistance system.
     *
     * @param epInstanceID int the unique identifier for the entry point
     *                     instance
     * @throws JahiaException thrown if there was an error communicating with
     *                        the persistence system.
     */
    public abstract void removeEntryPointInstance(String epInstanceID)
            throws JahiaException;

    public abstract PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID,
                                                   JahiaUser jahiaUser,
                                                   HttpServletRequest httpServletRequest,
                                                   HttpServletResponse httpServletResponse,
                                                   ServletContext servletContext, String workspaceName)
            throws JahiaException;

    /**
     * Returns the list of portlet modes that are supported by Jahia
     *
     * @return List a list of PortletModeBean instances that contains
     *         the portlet modes supported by Jahia
     */
    public abstract List<PortletMode> getSupportedPortletModes();

    /**
     * Returns the list of window states that are supported by Jahia
     *
     * @return List a list of WindowStateBean instances that contain
     *         the window states supported by Jahia.
     */
    public abstract List<WindowState> getSupportedWindowStates();

} // end JahiaApplicationsService
