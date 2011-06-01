/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.commons.server.rpc;

import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.PagingLoadResult;

import org.slf4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaGroup;
import org.jahia.ajax.gwt.client.data.GWTJahiaUser;
import org.jahia.ajax.gwt.client.service.UserManagerService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.*;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.exceptions.JahiaException;

import java.security.Principal;
import java.util.*;

/**
 * GWT user management service.
 * User: hollis
 * Date: 25 juil. 2008
 * Time: 12:53:39
 */
public class UserManagerServiceImpl extends JahiaRemoteService implements UserManagerService {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(UserManagerServiceImpl.class);
    
    private JahiaUserManagerService userManagerService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaSitesService sitesService;

    public void setUserManagerService(JahiaUserManagerService userManagerService) {
        this.userManagerService = userManagerService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public BasePagingLoadResult<GWTJahiaUser> searchUsers(String match, int offset, int limit, List<Integer> siteIds) {
        try {
            Properties criterias = new Properties();
            criterias.setProperty("*", match);

            Set<Principal> users;
            List<GWTJahiaUser> result = new ArrayList<GWTJahiaUser>();
            users = userManagerService.searchUsers(criterias);
            if (users != null) {
                Iterator iterator = users.iterator();
                JahiaUser user;
                GWTJahiaUser data;
                while (iterator.hasNext()) {
                    user = (JahiaUser) iterator.next();
                    data = new GWTJahiaUser(user.getUsername(), user.getUserKey());
                    Properties p = user.getProperties();
                    for (Object o : p.keySet()) {
                        data.set((String) o, p.get(o));
                    }
                    data.setProvider(user.getProviderName());
                    result.add(data);
                }
            }
            Collections.sort(result, new Comparator<GWTJahiaUser>() {
                public int compare(GWTJahiaUser o1, GWTJahiaUser o2) {
                    return o1.getUsername().compareTo(o2.getUsername());
                }
            });
            int size = result.size();
            result = new ArrayList<GWTJahiaUser>(result.subList(offset, Math.min(size, offset + limit)));
            return new BasePagingLoadResult<GWTJahiaUser>(result, offset, size);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public BasePagingLoadResult<GWTJahiaGroup> searchGroups(String match, int offset, int limit, List<Integer> siteIds) {
        try {
            Properties criterias = new Properties();
            criterias.setProperty("groupname", match);

            List<Integer> sites = siteIds;
            if (sites == null || sites.size() == 0) {
                sites = new ArrayList<Integer>();
                sites.add(getSite().getID());
            }

            List<GWTJahiaGroup> result = new ArrayList<GWTJahiaGroup>();

            for (Integer siteId : sites) {
                Set groups = groupManagerService.searchGroups(siteId, criterias);
                if (groups != null) {
                    Iterator iterator = groups.iterator();
                    JahiaGroup group;
                    GWTJahiaGroup data;
                    while (iterator.hasNext()) {
                        group = (JahiaGroup) iterator.next();
                        if (!group.isHidden()) {
                            data = new GWTJahiaGroup(group.getGroupname(), group.getGroupKey());
                            if (group.getSiteID() > 0) {
                                JahiaSite jahiaSite = sitesService.getSite(group.getSiteID());
                                if (jahiaSite != null) {
                                    data.setSiteName(jahiaSite.getTitle());
                                }
                            }
                            data.setSiteId(siteId);
                            data.setProvider(group.getProviderName());
                            result.add(data);
                        }
                    }
                }
            }
            Collections.sort(result, new Comparator<GWTJahiaGroup>() {
                public int compare(GWTJahiaGroup o1, GWTJahiaGroup o2) {
                    return o1.getGroupname().compareTo(o2.getGroupname());
                }
            });
            int size = result.size();
            result = new ArrayList(result.subList(offset, Math.min(size, offset + limit)));
            BasePagingLoadResult<GWTJahiaGroup> pagingLoadResult = new BasePagingLoadResult<GWTJahiaGroup>(result, offset, size);
            return pagingLoadResult;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public String[] getFormattedPrincipal(String userkey, char type, String[] textpattern) {
        PrincipalViewHelper pvh = new PrincipalViewHelper(textpattern);
        Principal p;

        if (type == 'u') {
            p = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userkey);
        } else {
            p = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(userkey);
        }

        String principalTextOption = pvh.getPrincipalTextOption(p);
        principalTextOption = principalTextOption.replace("&nbsp;", " ");
        return new String[]{principalTextOption, pvh.getPrincipalValueOption(p)};
    }

    public BasePagingLoadResult<GWTJahiaUser> searchUsersInContext(String match, int offset, int limit, String context) {
        if (context != null) {
            List<Integer> list = new ArrayList<Integer>();;

            if (context.equals("currentSite")) {
                JCRSiteNode site = getSite();
                if (site != null) {
                    list.add(site.getID());
                }
            } else if (context.startsWith("site:")) {
                String sitekey = context.substring(5);
                try {
                    JahiaSite site = sitesService.getSiteByKey(sitekey);
                    if (site != null) {
                        list.add(site.getID());
                    }
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (context.startsWith("sharedOnly")) {
                list.add(0);
            }

            return searchUsers(match, offset, limit, list);
        }
        return null;
    }

    public BasePagingLoadResult<GWTJahiaGroup> searchGroupsInContext(String match, int offset, int limit, String context) {
        if (context != null) {
            List<Integer> list = new ArrayList<Integer>();;

            if (context.equals("currentSite")) {
                JCRSiteNode site = getSite();
                if (site != null) {
                    list.add(site.getID());
                }
            } else if (context.startsWith("site:")) {
                String sitekey = context.substring(5);
                try {
                    JahiaSite site = sitesService.getSiteByKey(sitekey);
                    if (site != null ) {
                        list.add(site.getID());
                    }
                } catch (JahiaException e) {
                    logger.error(e.getMessage(), e);
                }
            } else if (context.startsWith("sharedOnly")) {
                list.add(0);
            }

            return searchGroups(match, offset, limit, list);
        }
        return null;
    }

}
