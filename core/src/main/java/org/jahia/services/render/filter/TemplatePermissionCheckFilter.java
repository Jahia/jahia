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

package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.utils.Patterns;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Performs accessibility check for the content: permissions, required mode etc.
 *  
 * @author Thomas Draier
 */
public class TemplatePermissionCheckFilter extends AbstractFilter {
    
    class RequiredModeException extends AccessDeniedException {

        private static final long serialVersionUID = -984310772102680834L;

        private Locale locale;

        private String localizedMessage;

        private String mode;

        RequiredModeException(String mode, Locale locale) {
            super("Content can only be accessed in " + mode);
            this.mode = mode;
            this.locale = locale;
        }

        @Override
        public String getLocalizedMessage() {
            if (localizedMessage == null && locale != null) {
                if (locale != null) {
                    localizedMessage = JahiaResourceBundle.getJahiaInternalResource("message.requiredMode." + mode,
                            locale, getMessage());
                } else {
                    return super.getLocalizedMessage();
                }
            }
            return localizedMessage;
        }
    }

    public String prepare(RenderContext renderContext, final Resource resource, RenderChain chain) throws Exception {
        Script script = (Script) renderContext.getRequest().getAttribute("script");
        JCRNodeWrapper node = resource.getNode();
        if (script != null) {
            String requirePermissions = script.getView().getProperties().getProperty("requirePermissions");
            if(requirePermissions==null) {
                requirePermissions = script.getView().getDefaultProperties().getProperty("requirePermissions");
            }
            if (requirePermissions != null) {
                chain.pushAttribute(renderContext.getRequest(),"cache.dynamicRolesAcls",Boolean.TRUE);
                if (requirePermissions.indexOf(' ') != -1) {
                    String[] perms = Patterns.SPACE.split(requirePermissions);
                    for (String perm : perms) {
                        if (!hasPermission(node, perm)) {
                            return "";
                        }
                    }
                } else if (!hasPermission(node, requirePermissions)) {
                    return "";
                }
            }
        } else {
            throw new TemplateNotFoundException("Unable to resolve script: "+resource.getResolvedTemplate());
        }

        boolean invert = node.hasProperty("j:invertCondition") && node.getProperty("j:invertCondition").getBoolean();

        if (!renderContext.isEditMode()) {
            if (node.hasProperty("j:requiredMode")) {
                String req = node.getProperty("j:requiredMode").getString();
                if (!renderContext.getMode().equals(req) && !invert) {
                    throw new RequiredModeException(req, renderContext.getMainResourceLocale());
                }
            }
        }
        if (!"studiomode".equals(renderContext.getEditModeConfigName())) {
            JahiaUser aliasedUser = JCRSessionFactory.getInstance().getCurrentAliasedUser();

            if (node.hasProperty("j:requiredPermissions")) {
                chain.pushAttribute(renderContext.getRequest(),"cache.dynamicRolesAcls",Boolean.TRUE);

                final Value[] values = node.getProperty("j:requiredPermissions").getValues();
                final List<String> perms = JCRTemplate.getInstance().doExecuteWithSystemSession(null, new JCRCallback<List<String>>() {
                    public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        List<String> permissionNames = new ArrayList<String>();
                        for (Value value : values) {
                            permissionNames.add(session.getNodeByUUID(value.getString()).getName());
                        }
                        return permissionNames;
                    }
                });
                JCRNodeWrapper contextNode = renderContext.getAjaxResource() != null ? renderContext.getAjaxResource().getNode() : renderContext.getMainResource().getNode();
                try {
                    if (node.hasProperty("j:contextNodePath")) {
                        String contextPath = node.getProperty("j:contextNodePath").getString();
                        if (!StringUtils.isEmpty(contextPath)) {
                            if (contextPath.startsWith("/")) {
                                contextNode = JCRSessionFactory.getInstance().getCurrentUserSession().getNode(contextPath);
                            } else {
                                contextNode = contextNode.getNode(contextPath);
                            }
                        }
                    }
                } catch (PathNotFoundException e) {
                    return invert ? null : "";
                }
                for (String perm : perms) {
                    if (!contextNode.hasPermission(perm)) {
                        return invert ? null : "";
                    }
                }

                if (aliasedUser != null) {
                    if (!JCRTemplate.getInstance().doExecuteWithUserSession(aliasedUser.getUsername(), node.getSession().getWorkspace().getName(),
                            new JCRCallback<Boolean>() {
                                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                    JCRNodeWrapper aliasedNode = session.getNode(resource.getNode().getPath());
                                    for (String perm : perms) {
                                        if (!aliasedNode.hasPermission(perm)) {
                                            return false;
                                        }
                                    }
                                    return true;
                                }
                            }
                    )) {
                        return invert ? null : "";
                    }
                }

            }
            if (node.hasProperty("j:requireLoggedUser") && node.getProperty("j:requireLoggedUser").getBoolean()) {
                if (!renderContext.isLoggedIn()) {
                    return invert ? null : "";
                }
                if (aliasedUser != null) {
                    if (JahiaUserManagerService.isGuest(aliasedUser)) {
                        return invert ? null : "";
                    }
                }
            }
            if (node.hasProperty("j:requirePrivilegedUser") && node.getProperty("j:requirePrivilegedUser").getBoolean()) {
                if (!renderContext.getUser().isMemberOfGroup(0,JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) {
                    return invert ? null : "";
                }
                if (aliasedUser != null) {
                    if (!aliasedUser.isMemberOfGroup(0, JahiaGroupManagerService.PRIVILEGED_GROUPNAME)) {
                        return invert ? null : "";
                    }
                }
            }
        }
        return invert ? "" : null;
    }

    private boolean hasPermission(JCRNodeWrapper node, String perm) {
        if (perm.indexOf('|') == -1) {
            return node.hasPermission(perm);
        } else {
            String[] perms = Patterns.PIPE.split(perm);
            for (String p : perms) {
                if (node.hasPermission(p)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        String out = super.execute(previousOut, renderContext, resource, chain);
        JCRNodeWrapper node = resource.getNode();
        if (node.hasProperty("j:requiredMode")) {
            String req = node.getProperty("j:requiredMode").getString();
            if (!renderContext.isLiveMode() && req.equals("live")) {
                out = "<div style=\"position:relative;overflow:hidden\"><div style=\"position:absolute; opacity:0.5; width:100%; height:100%\" class=\"area-liveOnly\"></div>"+ out +"</div>";
            }
        }

        return out;
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.getContentForError(renderContext, resource, chain, e);
        if (Resource.CONFIGURATION_PAGE.equals(resource.getContextConfiguration())) {
            return null;
        }
        try {
            // Handle case of required mode
            if(e instanceof AccessDeniedException && renderContext.getMode().equals("preview") && resource.getNode().hasProperty("j:requiredMode")) {
                // Returns a fragment with an error comment
                return "<p>" + e.getLocalizedMessage() + "</p>";
            }
        } catch (Exception e1) {
            return null;
        }
        return null;
    }
}
