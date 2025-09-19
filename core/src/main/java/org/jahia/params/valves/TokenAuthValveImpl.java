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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.id.IdentifierGenerator;
import org.apache.commons.id.IdentifierGeneratorFactory;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Valve that uses tokens to authenticate the user.
 * @author toto
 * @deprecated not used, we don't recommend this valve for security reasons, use PAT (personal api token) module instead
 */
@Deprecated(since = "8.2.3.0", forRemoval = true)
public class TokenAuthValveImpl extends BaseAuthValve {

    private static IdentifierGenerator idGen = IdentifierGeneratorFactory.newInstance().uuidVersionFourGenerator();

    private static Map<String, JahiaUser> map = new ConcurrentHashMap<String, JahiaUser>();

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
