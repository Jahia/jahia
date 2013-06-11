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
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerProvider;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.jcr.JCRGroup;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ManageGroupsFlowHandler.class);

    private static final long serialVersionUID = -425326961938017713L;

    private transient JahiaGroupManagerService groupManagerService;

    /**
     * Performs the creation of a new group for the site.
     * 
     * @param group
     *            the group model object with the data for the new group
     * @param context
     *            the message context object
     * @return <code>true</code> if the group was successfully added; <code>false</code> otherwise
     */
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
            context.addMessage(new MessageBuilder()
                    .error()
                    .defaultText(
                            Messages.format(Messages.get("resources.JahiaSiteSettings",
                                    "siteSettings.groups.errors.create.failed", locale), group.getGroupname())).build());
            return false;
        }
    }

    /**
     * Duplicates the selected group.
     * 
     * @param selectedGroup
     *            a group to be copied
     * @param newGroup
     *            the new group model
     * @param context
     *            the message context object
     * @return <code>true</code> if the group was successfully copied; <code>false</code> otherwise
     */
    public void copyGroup(JahiaGroup selectedGroup, GroupModel newGroup, MessageContext context) {
        Locale locale = LocaleContextHolder.getLocale();
        // create new group
        @SuppressWarnings("deprecation")
        JahiaGroup grp = groupManagerService.createGroup(newGroup.getSiteId(), newGroup.getGroupname(), null, false);
        if (grp == null) {
            context.addMessage(new MessageBuilder()
                    .error()
                    .defaultText(
                            Messages.format(Messages.get("resources.JahiaSiteSettings",
                                    "siteSettings.groups.errors.create.failed", locale), newGroup.getGroupname()))
                    .build());
        } else {
            context.addMessage(new MessageBuilder()
                    .info()
                    .defaultText(
                            Messages.getInternal("label.group", locale) + " '" + newGroup.getGroupname() + "' "
                                    + Messages.getInternal("message.successfully.created", locale)).build());
            // copy membership
            Collection<Principal> members = selectedGroup.getMembers();
            if (members.size() > 0) {
                grp.addMembers(members);
            }
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

    private boolean isReadOnly(JahiaGroup grp) {
        if (groupManagerService.getProvider(grp.getProviderName()).isReadOnly()) {
            return true;
        }
        if (grp instanceof JCRGroup) {
            try {
                return ((JCRGroup) grp).getNode(JCRSessionFactory.getInstance().getCurrentUserSession()).isNodeType(
                        "jmix:systemNode");
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Looks up the specified group by key.
     * 
     * @param selectedGroup
     *            the group key
     * @return up the specified group by key
     */
    public JahiaGroup lookupGroup(String selectedGroup) {
        return groupManagerService.lookupGroup(selectedGroup);
    }

    /**
     * Performs the removal of the specified groups for the site.
     * 
     * @param selectedGroup
     *            a key of the group to be removed
     * @param context
     *            the message context object
     * @return <code>true</code> if the group was successfully removed; <code>false</code> otherwise
     */
    public void removeGroup(String selectedGroup, MessageContext context) {
        JahiaGroup grp = lookupGroup(selectedGroup);
        if (isReadOnly(grp)) {
            context.addMessage(new MessageBuilder()
                    .error()
                    .defaultText(
                            Messages.get("resources.JahiaSiteSettings", "siteSettings.groups.errors.reservedGroup",
                                    LocaleContextHolder.getLocale())).build());

            return;
        } else {
            Locale locale = LocaleContextHolder.getLocale();
            if (groupManagerService.deleteGroup(grp)) {
                context.addMessage(new MessageBuilder()
                        .info()
                        .defaultText(
                                Messages.getInternal("label.group", locale) + " '" + grp.getGroupname() + "' "
                                        + Messages.getInternal("message.successfully.removed", locale)).build());
            } else {
                context.addMessage(new MessageBuilder()
                        .error()
                        .defaultText(
                                Messages.format(Messages.get("resources.JahiaSiteSettings",
                                        "siteSettings.groups.errors.remove.failed", locale), grp.getGroupname()))
                        .build());
            }
        }
    }

    /**
     * Performs the group search with the specified search criteria and returns the list of matching groups.
     * 
     * @param searchCriteria
     *            current search criteria
     * @return the list of groups, matching the specified search criteria
     */
    public Set<Principal> search(SearchCriteria searchCriteria) {
        Set<Principal> searchResult = PrincipalViewHelper.getGroupSearchResult(searchCriteria.getSearchIn(),
                searchCriteria.getSiteId(), searchCriteria.getSearchString(), searchCriteria.getProperties(),
                searchCriteria.getStoredOn(), searchCriteria.getProviders());
        return searchResult;
    }

    @Autowired
    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }
}