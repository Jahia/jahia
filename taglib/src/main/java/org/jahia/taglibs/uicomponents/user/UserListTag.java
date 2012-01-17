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

package org.jahia.taglibs.uicomponents.user;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspWriter;

import org.slf4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerProvider;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * <p>Easy tag that generates the HTML listing of Jahia Users.</p>
 * <p>the tag use the query attribute, if available, to execute the search. By default the query is "*=*" (all).<br/>
 * the designer can set a list of comma (or space) separated name-value pairs inside the criteria attribute to finely filter
 * the results against your set of criteria.<br>
 * By default, the number of results is limited to 10 first results, the scope to jahiaDB </p>
 *
 * @author dominique (joe) pillot
 * @version $Id: $
 */
@SuppressWarnings("serial")
public class UserListTag extends AbstractJahiaTag {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserListTag.class);
    private static JahiaUserManagerService userservice = ServicesRegistry.getInstance().getJahiaUserManagerService();

    private int displaylimit = 10;
    private String query = "*";
    private String scope = "jahia_db";
    private String criteria = null;
    private String backCall = null;
    private String styleClass = null;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final ProcessingContext jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");

        //we check jParams
        if (jParams == null) {
            logger.debug("JParams is not available or null!");
            return SKIP_BODY;
        }

        //the list of providers
        List<? extends JahiaUserManagerProvider> thelist = userservice.getProviderList();//the list of all available providers
        final Set<Principal> searchResults = new HashSet<Principal>();
        Properties searchParameters = new Properties();
        StringBuffer sb = new StringBuffer();
        int siteID = jParams.getSiteID();
        if (query == null || query.equalsIgnoreCase("")) query = "*";//default query
        if (query.indexOf("*") == -1) query += "*";

        // search criteria
        searchParameters.setProperty("*", query);
        // add criteria
        if (criteria != null && !criteria.equalsIgnoreCase("")) {
            String delimitor = " ";
            if (getCriteria().trim().indexOf(",") != -1) delimitor = ",";
            StringTokenizer tk = new StringTokenizer(getCriteria().trim(), delimitor);
            while (tk.hasMoreTokens()) {
                String token = tk.nextToken().trim();
                String p = token.substring(0, token.indexOf("="));
                String v = token.substring(token.indexOf("=") + 1);

                if (!v.substring(v.length() - 1, v.length()).equalsIgnoreCase("*")) v += "*";
                logger.debug("adding criteria " + p + " = " + v);
                searchParameters.setProperty(p, v);
            }
        }


        if (thelist.size() == 1 || getScope().equalsIgnoreCase("all") || getScope().equalsIgnoreCase("everywhere")) {
            logger.debug("searching all");
            //we search on the current siteID domain
            searchResults.addAll(userservice.searchUsers(searchParameters));
        } else {
            String delimitor = " ";
            if (getScope().indexOf(",") != -1) delimitor = ",";
            for (JahiaUserManagerProvider curProvider : thelist) {
                StringTokenizer tk = new StringTokenizer(scope.trim(), delimitor);
                while (tk.hasMoreTokens()) {
                    String token = tk.nextToken();
                    if (curProvider.getKey().trim().toLowerCase().indexOf(token.toLowerCase()) != -1) {
                        logger.debug("searching in " + curProvider.getKey());
                        searchResults.addAll(userservice.searchUsers(curProvider.getKey(), searchParameters));
                    }
                }
            }
        }

        int count = 0;
        for (Principal searchResult : searchResults) {
            if (count > displaylimit) {
                break;
            }
            JahiaUser user = (JahiaUser) searchResult;
            if (getBackCall() != null) {
                sb.append("<a href=\"").append(backCall.replaceAll("userid", "'" + user.getUserKey() + "'")).append(
                        "\"");
                if (styleClass != null && !"".equals(styleClass.trim())) {
                    sb.append(" class=\"").append(styleClass).append("\"");
                }
                sb.append(">");
            }
            sb.append(user.getUsername()).append("<br/>");
            if (getBackCall() != null) {
                sb.append("</a>");
            }
            count++;
        }
        try {
            out.println(sb.toString());
        } catch (IOException e) {
            logger.error("Error while rendering the user list tag", e);
        }
        return SKIP_BODY;
    }

    // ACCESSORS

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        logger.debug("setting query to " + query);
        this.query = query;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        logger.debug("setting scope to " + scope);
        this.scope = scope;
    }

    public int getDisplayLimit() {
        return displaylimit;
    }

    public void setDisplayLimit(int limit) {
        logger.debug("setting limit to " + limit);
        this.displaylimit = limit;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        logger.debug("setting criteria to " + criteria);
        this.criteria = criteria;
    }

    public String getBackCall() {
        return backCall;
    }

    public void setBackCall(String backCall) {
        this.backCall = backCall;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public void setStyleClass(String styleClass) {
        this.styleClass = styleClass;
    }
}

