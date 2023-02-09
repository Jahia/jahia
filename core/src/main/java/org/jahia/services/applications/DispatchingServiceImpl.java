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

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author not attributable
 * @version 1.0
 */

public class DispatchingServiceImpl extends DispatchingService {

    /** logging */
    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(DispatchingServiceImpl.class);

    private static DispatchingServiceImpl singletonInstance = null;

    private static final String ENTRYPOINT_ID_SEPARATOR = "_";

    private Map dispatchingProviders = new LinkedHashMap();

    private ApplicationsManagerService applicationsManager;

    protected DispatchingServiceImpl () {
    }

    /**
     * return the singleton instance
     */
    public static synchronized DispatchingServiceImpl getInstance () {
        if (singletonInstance == null) {
            singletonInstance = new DispatchingServiceImpl();
        }

        return singletonInstance;
    }

    public void setApplicationsManager(ApplicationsManagerService applicationsManager) {
        this.applicationsManager = applicationsManager;
    }

    public void start() throws JahiaInitializationException {}

    public void stop() {}

    public String getAppOutput (int fieldID, String entryPointIDStr, JahiaUser jahiaUser,
                                HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                ServletContext servletContext, String workspaceName)
        throws JahiaException {

        String fieldIDStr = Integer.toString(fieldID);
        String entryPointUniqueIDStr = fieldIDStr + ENTRYPOINT_ID_SEPARATOR + entryPointIDStr;

        EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(entryPointIDStr, workspaceName);
        if (entryPointInstance == null) {
            if(entryPointIDStr != null){
                logger.error("Couldn't find application entry point with ID=" + entryPointIDStr);
            }else{
               logger.debug(" --> no selected application.");
            }
            return "";
        }

        ApplicationBean appBean = applicationsManager.getApplicationByContext(entryPointInstance.getContextName());

        if (appBean == null) {
            logger.error("Couldn't find application with appID=" + entryPointIDStr);
            return "";
        }

        DispatchingProvider dispatcher = (DispatchingProvider) dispatchingProviders.get(appBean.getType());
        if (dispatcher == null) {
            logger.error("Found no dispatching that corresponds to application type [" + appBean.getType() + "]");
            return "";
        }

        // we deactivate the HTML page cache to avoid storing the entry screen of
        // the application (first call on a page since it was flushed) in the
        // HTML cache. The side effect of this is that any page that contains
        // a web application is *NEVER* cached.
//        jParams.setCacheExpirationDelay(0);

        // Now let's check the request URL to see if the target application is this one or not.

        Object url = httpServletRequest.getAttribute("url");
        httpServletRequest.setAttribute("url",null);  // todo : should we put something here ?

        String renderResult = null;
        if(httpServletRequest!=null) {

            // now we can render the output of the application.

            // we check the request, because we want to make sure this
            // method is called only once.
            if (httpServletRequest.getAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr) == null) {
                // process the action now
                renderResult = dispatcher.render(entryPointInstance, entryPointInstance.getID(), jahiaUser, httpServletRequest, httpServletResponse, servletContext, workspaceName);
                httpServletRequest.setAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr, renderResult);
            } else {
                renderResult = (String) httpServletRequest.getAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr);
            }
        }

        httpServletRequest.setAttribute("url",url);

        return renderResult;

    }

    public Map getDispatchingProviders() {
        return dispatchingProviders;
    }

    public void setDispatchingProviders(Map dispatchingProviders) {
        this.dispatchingProviders = dispatchingProviders;
    }

}
