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
//
//  JahiaApplicationsService
//  EV      29.11.2000
//
//  getAppOutput( fieldID, appID, params )
//

package org.jahia.services.applications;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This service generates the dispatching and aggregation on an application.
 * This functionality is central to the portal behavior of Jahia, allowing it
 * to display multiple applications on a web page and interact with them
 * simultaneously.
 *
 * @author Serge Huber
 * @author Eric Vassalli
 * @version 1.0
 */
public abstract class DispatchingService extends JahiaService {

    /**
     * Dispatches processing to an application, and retrieves it's output for
     * Jahia to aggregate
     *
     * @param fieldID     identifier of Jahia's field
     * @param entryPointIDStr application identifier passed as a String (converted
     *                    from an integer)
     * @param jahiaUser the Jahia user that will be passed to the portlet when dispatching
     * @param httpServletRequest the request object that will be passed to the portlet
     * @param httpServletResponse
     * @param servletContext
     * @param workspaceName the workspace name against which to check for mode and various other permissions
     *
     * @throws JahiaException generated if there was a problem dispatching,
     *                        during processing of the application, or when recuperating the application's
     *                        output.
     * @return String containing the output of the application
     */
    public abstract String getAppOutput (int fieldID, String entryPointIDStr, JahiaUser jahiaUser, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext, String workspaceName)
            throws JahiaException;

} // end JahiaApplicationsService
