/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ApplicationsManagerProvider {
    /**
     * Creates an instance of an application entry point definition. This
     * does not need to create the entry in the database, just the memory
     * instance, which is dependent on the provider.
     *
     * @param entryPointDefinition EntryPointDefinition
     * @throws JahiaException
     * @returns the created instance for the entry point definition.
     */
    public abstract EntryPointInstance createEntryPointInstance(
            EntryPointDefinition entryPointDefinition)
            throws JahiaException;

    public abstract PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID,
                                                   JahiaUser jahiaUser,
                                                   HttpServletRequest httpServletRequest,
                                                   HttpServletResponse httpServletResponse,
                                                   ServletContext servletContext, String workspaceName) throws JahiaException;

    public List<EntryPointDefinition> getAppEntryPointDefinitions(ApplicationBean appBean)
            throws JahiaException;

    public void start()
            throws JahiaInitializationException;

    public void stop();

}
