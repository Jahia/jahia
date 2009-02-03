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

 package org.jahia.services.applications;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.InsertionSortedMap;

import javax.servlet.http.HttpServletRequest;
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
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(DispatchingServiceImpl.class);

    private static DispatchingServiceImpl singletonInstance = null;

    private static final String ENTRYPOINT_ID_SEPARATOR = "_";

    private Map dispatchingProviders = new InsertionSortedMap();

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

    public String getAppOutput (int fieldID, String entryPointIDStr, ParamBean jParams)
        throws JahiaException {

        String fieldIDStr = Integer.toString(fieldID);
        String entryPointUniqueIDStr = fieldIDStr + ENTRYPOINT_ID_SEPARATOR + entryPointIDStr;

        EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(entryPointIDStr);
        if (entryPointInstance == null) {
            if(entryPointIDStr != null){
                logger.error("Couldn't find application entry point with ID=" + entryPointIDStr);
            }else{
               logger.debug("ID=" + entryPointIDStr+" --> no selected application.");
            }
            return "";
        }

        ApplicationBean appBean = applicationsManager.getApplication (entryPointInstance.getContextName());

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

        HttpServletRequest request = jParams.getRequest();
        String renderResult = null;
        if(request!=null) {

            // now we can render the output of the application.

            // we check the request, because we want to make sure this
            // method is called only once.
            if (request.getAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr) == null) {
                // process the action now
                renderResult = dispatcher.render(entryPointInstance, entryPointInstance.getID(), jParams);
                request.setAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr, renderResult);
            } else {
                renderResult = (String) request.getAttribute("org.jahia.applications.renderAlreadyProcessed." + entryPointUniqueIDStr);
            }
        }
        return renderResult;

    }

    public Map getDispatchingProviders() {
        return dispatchingProviders;
    }

    public void setDispatchingProviders(Map dispatchingProviders) {
        this.dispatchingProviders = dispatchingProviders;
    }

}
