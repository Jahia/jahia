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
 package org.jahia.params.valves;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Valve that uses tokens to authenticate the user.
 * @author toto
 */
public class TokenAuthValveImpl extends BaseAuthValve {

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    private static Map<String, JahiaUser> map = new HashMap<String, JahiaUser>();

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        if (!isEnabled()) {
            valveContext.invokeNext(context);
            return;
        }
        
        AuthValveContext authContext = (AuthValveContext) context;
        HttpServletRequest request = authContext.getRequest();

        if (request.getHeader("jahiatoken") != null) {
            JahiaUser jahiaUser = map.remove(request.getHeader("jahiatoken"));
            if (jahiaUser != null) {
                authContext.getSessionFactory().setCurrentUser(jahiaUser);
                return;
            }
        }
        valveContext.invokeNext(context);
    }

    public static String addToken(JahiaUser user) {
        String s = idGen.nextIdentifier().toString();
        map.put(s,user);
        return s;
    }
}
