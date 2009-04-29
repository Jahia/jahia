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
