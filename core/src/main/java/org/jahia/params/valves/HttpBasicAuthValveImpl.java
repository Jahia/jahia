/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
                            // Do not store HTTP Basic auth into session
                            authContext.setShouldStoreAuthInSession(false);
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
