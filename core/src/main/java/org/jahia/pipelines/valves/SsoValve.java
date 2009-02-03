/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA SUSTAINABLE SOFTWARE LICENSE (JSSL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

 package org.jahia.pipelines.valves;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.pipelines.PipelineException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.security.license.LicenseActionChecker;

/**
 * <p>Title: Generic SSO auth valve</p>
 * <p>Description: authenticate users with a SSO server.</p>
 * <p>Copyright: Copyright (c) 2005 - Pascal Aubry</p>
 * <p>Company: University of Rennes 1</p>
 * @author Pascal Aubry
 * @version 1.0
 */

public abstract class SsoValve implements Valve {

    /** Logger instance */
    protected static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (SsoValve.class);

    /**
     * Retrieve the credentials from the request.
     * @param processingContext parameters
     * @return an object.
     * @throws Exception any exception
     */
    public abstract Object retrieveCredentials(ProcessingContext processingContext) throws Exception;

    /**
     * Validate the credentials.
     * @param credentials the crendentials.
     * @param paramBean
     * @return the id of user that was authenticated, or null if none.
     * @throws Exception any exception
     */
    public abstract String validateCredentials(Object credentials, ProcessingContext paramBean) throws JahiaException;

    /**
     * @see org.jahia.pipelines.valves.Valve#invoke(java.lang.Object, org.jahia.pipelines.valves.ValveContext)
     */
    public void invoke (Object context, ValveContext valveContext)
        throws PipelineException {

        if (!LicenseActionChecker.isAuthorizedByLicense("org.jahia.params.valves.SsoValve", 0)) {
            valveContext.invokeNext(context);
        }

        logger.debug("starting " + this.getClass().getName() + ".invoke()...");
        ProcessingContext processingContext = (ProcessingContext) context;

        // at first look if the user was previously authenticated
        JahiaUser sessionUser = null;
        sessionUser = (JahiaUser) processingContext.getSessionState().getAttribute(ProcessingContext.SESSION_USER);
        if (sessionUser != null && !sessionUser.getUsername().equals("guest")) {
            logger.debug("user '" + sessionUser.getUsername() + "' was already authenticated!");
            processingContext.setTheUser(sessionUser);
            return;
        }

        logger.debug("retrieving credentials...");
        Object credentials;
        try {
            credentials = retrieveCredentials(processingContext);
        } catch (Exception e) {
            logger.error(e);
            throw new PipelineException("exception was thrown while retrieving credentials!", e);
        }
        if (credentials == null) {
            logger.debug("no credentials found!");
            return;
        }
        logger.debug("credentials = " + credentials);

        logger.debug("validating credentials...");
        String uid;
        try {
            uid = validateCredentials(credentials, processingContext);
        } catch (Exception e) {
            throw new PipelineException("exception was thrown while validating credentials!", e);
        }
        if (uid == null) {
            logger.debug("credentials were not validated!");
        }
        logger.debug("uid = " + uid);

        logger.debug("checking user existence in Jahia database...");
        JahiaUser user = null;
        user = ServicesRegistry.getInstance ()
                .getJahiaSiteUserManagerService ()
                .getMember (processingContext.getSiteID (), uid);
        if (user == null) {
            throw new PipelineException("user '" + uid + "' was authenticated but not found in database!");
        }
        try {
            if (processingContext instanceof ParamBean) {
                ParamBean paramBean = (ParamBean) processingContext;
                paramBean.invalidateSession();
            }
        } catch (JahiaSessionExpirationException e) {
            logger.error(e.getMessage(), e);
        }
        // user has been successfully authenticated, note this in the current session.
        processingContext.getSessionState().setAttribute(ProcessingContext.SESSION_USER, user);

        // eventually set the Jahia user
        processingContext.setTheUser(user);
    }

    /**
     * Return the URL to redirect to for authentication.
     * @return a URL.
     * @throws JahiaInitializationException
     */
    public abstract String getRedirectUrl(ProcessingContext paramBean) throws JahiaException;

}
