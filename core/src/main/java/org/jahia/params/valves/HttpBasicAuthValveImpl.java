/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 package org.jahia.params.valves;

import org.apache.commons.codec.binary.Base64;
import org.apache.jackrabbit.server.JahiaBasicCredentialsProvider;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Valve that uses Basic authentication to authenticate the user.
 * @author toto
 */
public class HttpBasicAuthValveImpl extends BaseAuthValve {

    private static final Logger logger = LoggerFactory.getLogger(HttpBasicAuthValveImpl.class);
    private JahiaUserManagerService userManagerService;

    @Override
    public void invoke(Object context, ValveContext valveContext) throws PipelineException {

        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }

        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Basic ")) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Header found : "+auth);
                }
                auth = auth.substring(6).trim();
                Base64 decoder = new Base64();
                String cred = new String(decoder.decode(auth.getBytes("UTF-8")));
                int colonInd = cred.indexOf(':');
                String user = cred.substring(0,colonInd);
                if (user != null && !user.contains(JahiaBasicCredentialsProvider.IMPERSONATOR)) {
                    String pass = cred.substring(colonInd+1);
                    JCRUserNode jcrUserNode = userManagerService.lookupUser(user);
                    if (jcrUserNode != null) {
                        if (jcrUserNode.verifyPassword(pass)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("User " + user + " authenticated.");
                            }
                            if (jcrUserNode.isAccountLocked()) {
                                logger.debug("Login failed. Account is locked for user " + user);
                                return;
                            }
                            authContext.getSessionFactory().setCurrentUser(jcrUserNode.getJahiaUser());
                            return;
                        } else {
                            logger.debug("User found but password verification failed for user : " + user);
                        }
                    } else {
                        logger.debug("User not found : "+user);
                    }
                }
            } catch (Exception e) {
                logger.debug("Exception thrown",e);
            }
        } else {
            logger.debug("No authorization header found");
        }
        valveContext.invokeNext(context);
    }

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }
}
