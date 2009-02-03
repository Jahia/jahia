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

 package org.jahia.params.valves;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 d�c. 2004
 * Time: 13:03:08
 * To change this template use File | Settings | File Templates.
 */
public class HttpBasicAuthValveImpl implements Valve {
    private static final transient Logger logger = Logger
            .getLogger(HttpBasicAuthValveImpl.class);

    public HttpBasicAuthValveImpl() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        HttpServletRequest request = ((ParamBean)processingContext).getRequest();
        String auth = request.getHeader("Authorization");
        if (auth != null) {
            try {
                logger.debug("Header found : "+auth);
                auth = auth.substring(6).trim();
                Base64 decoder = new Base64();
                String cred = new String(decoder.decode(auth.getBytes("UTF-8")));
                int colonInd = cred.indexOf(':');
                String user = cred.substring(0,colonInd);
                String pass = cred.substring(colonInd+1);

                JahiaSite site = (JahiaSite) request.getSession().getAttribute(ProcessingContext.SESSION_SITE);
                JahiaUser jahiaUser = null;
                jahiaUser = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMember(site.getID(), user);
                if (jahiaUser != null) {
                    if (jahiaUser.verifyPassword(pass)) {
                        logger.debug("User " + user + " authenticated.");
                        processingContext.setTheUser(jahiaUser);
                        return;
                    } else {
                        logger.debug("User found but incorrect password : " + user);
                    }
                } else {
                    logger.debug("User not found : "+user);                        
                }
            } catch (Exception e) {
                logger.debug("Exception thrown",e);
            }
        } else {
            logger.debug("No authorization header found");
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
    }
}
