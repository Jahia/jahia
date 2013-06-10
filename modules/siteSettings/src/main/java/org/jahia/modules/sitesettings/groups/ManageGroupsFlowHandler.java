/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.modules.sitesettings.groups;

import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroupManagerProvider;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.utils.i18n.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;

/**
 * Web flow handler for group management actions.
 * 
 * @author Sergiy Shyrkov
 */
public class ManageGroupsFlowHandler implements Serializable {

    private static final long serialVersionUID = -425326961938017713L;

    private transient JahiaGroupManagerService groupManagerService;

    @SuppressWarnings("deprecation")
    public boolean addGroup(GroupModel group, MessageContext context) {
        Locale locale = LocaleContextHolder.getLocale();
        if (groupManagerService.createGroup(group.getSiteId(), group.getGroupname(), null, false) != null) {
            context.addMessage(new MessageBuilder()
                    .info()
                    .defaultText(
                            Messages.getInternal("label.group", locale) + " '" + group.getGroupname() + "' "
                                    + Messages.getInternal("message.successfully.created", locale)).build());
            return true;
        } else {
            context.addMessage(new MessageBuilder().info()
                    .defaultText(Messages.getInternal("siteSettings.groups.create.failed", locale)).build());
            return false;
        }
    }

    /**
     * Returns a list of all group providers currently registered.
     * 
     * @return a list of all group providers currently registered
     */
    public List<? extends JahiaGroupManagerProvider> getProviders() {
        return groupManagerService.getProviderList();
    }

    private int getSiteId(RequestContext ctx) {
        return ((RenderContext) ctx.getExternalContext().getRequestMap().get("renderContext")).getSite().getID();
    }

    /**
     * Returns an empty (newly initialized) search criteria bean.
     * 
     * @return an empty (newly initialized) search criteria bean
     */
    public SearchCriteria initCriteria(RequestContext ctx) {
        return new SearchCriteria(getSiteId(ctx));
    }

    /**
     * Returns an empty (newly initialized) group bean.
     * 
     * @return an empty (newly initialized) group bean
     */
    public GroupModel initGroup(RequestContext ctx) {
        return new GroupModel(getSiteId(ctx));
    }

    /**
     * Performs the group search with the specified search criteria and returns the list of matching groups.
     * 
     * @param searchCriteria
     *            current search criteria
     * @return the list of groups, matching the specified search criteria
     */
    public Set<Principal> search(SearchCriteria searchCriteria) {
        Set<Principal> searchResult = PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(), searchCriteria.getSiteId(),
                searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                searchCriteria.getProviders());
        return searchResult;
    }

    @Autowired
    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

}