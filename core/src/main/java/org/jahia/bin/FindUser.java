/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;

import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.bin.errors.DefaultErrorHandler;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller for performing user search.
 * 
 * @author Sergiy Shyrkov
 */
public class FindUser extends BaseFindController {

    private static final Logger logger = LoggerFactory.getLogger(FindUser.class);

    public static String getFindUserServletPath() {
        // TODO move this into configuration
        return "/cms/findUser";
    }

    protected Set<String> displayProperties = new HashSet<String>(Arrays.asList("j:lastName", "j:firstName"));

    protected Set<String> searchProperties = new HashSet<String>(Arrays.asList("j:lastName", "j:firstName", "username"));

    protected JahiaUserManagerService userService;

    protected Properties buildSearchCriteria(String queryTerm) {
        Properties props = new Properties();
        for (String key : searchProperties) {
            props.put(key, queryTerm);
        }

        return props;
    }

    protected void handle(HttpServletRequest request, HttpServletResponse response)
            throws JahiaForbiddenAccessException, IOException, JSONException {
        checkUserLoggedIn();
        checkUserAuthorized();

        String queryTerm = getParameter(request, "q");
        if (queryTerm.length() < 1 || queryTerm.replace("*", "").trim().length() < 1) {
            throw new JahiaBadRequestException("Please specify more exact term for user search");
        }
        Properties searchCriterias = buildSearchCriteria(queryTerm);

        if (logger.isDebugEnabled()) {
            logger.debug("Performing user search using criteria: {}", searchCriterias);
        }

        Set<Principal> result = userService.searchUsers(searchCriterias);

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} matching users", result.size());
        }

        writeResults(result, request, response);
    }

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        long startTime = System.currentTimeMillis();
        String sessionId = null;
        try {
            if (logger.isInfoEnabled()) {
                sessionId = request.getSession().getId();
            }
            if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
                handle(request, response);
            } else if (request.getMethod().equals("OPTIONS")) {
                response.setHeader("Allow", "GET, OPTIONS, POST");
            } else {
                response.sendError(SC_METHOD_NOT_ALLOWED);
            }
        } catch (Exception e) {
            DefaultErrorHandler.getInstance().handle(e, request, response);
        } finally {
            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder(100);
                sb.append("Rendered [").append(request.getRequestURI());
                JahiaUser user = JCRTemplate.getInstance().getSessionFactory().getCurrentUser();
                if (user != null) {
                    sb.append("] user=[").append(user.getUsername());
                }
                sb.append("] ip=[").append(request.getRemoteAddr()).append("] sessionID=[")
                        .append(sessionId).append("] in [")
                        .append(System.currentTimeMillis() - startTime).append("ms]");
                logger.info(sb.toString());
            }
        }

        return null;
    }

    public void setDisplayProperties(Set<String> displayProperties) {
        this.displayProperties = displayProperties;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.userService = jahiaUserManagerService;
    }

    public void setSearchProperties(Set<String> searchFields) {
        this.searchProperties = searchFields;
    }

    protected JSONObject toJSON(JahiaUser user) throws JSONException {
        JSONObject json = new JSONObject();

        json.put("userKey", user.getUserKey());
        json.put("username", user.getUsername());

        for (String key : displayProperties) {
            String value = user.getProperty(key);
            if (value != null) {
                json.put(key, value);
            }
        }
        return json;
    }

    protected void writeResults(Set<Principal> users, HttpServletRequest request,
            HttpServletResponse response) throws IOException, JSONException {

        response.setContentType("application/json; charset=UTF-8");
        JSONArray results = new JSONArray();

        int count = 0;
        for (Principal user : users) {
            JSONObject jsonUser = toJSON((JahiaUser) user);
            results.put(jsonUser);
            count++;
            if (count >= hardLimit) {
                logger.info("{} users were found, limiting results to {}", users.size(), hardLimit);
                break;
            }
        }

        results.write(response.getWriter());
    }

}
