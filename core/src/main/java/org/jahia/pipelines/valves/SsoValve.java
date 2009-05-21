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
