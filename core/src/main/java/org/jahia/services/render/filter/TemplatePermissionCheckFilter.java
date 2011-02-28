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

package org.jahia.services.render.filter;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRValueWrapperImpl;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.scripting.Script;

import javax.jcr.AccessDeniedException;
import javax.jcr.Value;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 8, 2009
 * Time: 11:54:06 AM
 * 
 */
public class TemplatePermissionCheckFilter extends AbstractFilter {

    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Script script = (Script) renderContext.getRequest().getAttribute("script");
        JCRNodeWrapper node = resource.getNode();
        if (script != null) {
            String requirePermissions = script.getView().getProperties().getProperty("requirePermissions");
            if (requirePermissions != null) {
                chain.pushAttribute(renderContext.getRequest(),"cache.dynamicRolesAcls",Boolean.TRUE);
                String[] perms = requirePermissions.split(" ");
                for (String perm : perms) {
                    if (!node.hasPermission(perm)) {
                        return "";
                    }
                }
            }
        } else {
            throw new TemplateNotFoundException("Unable to resolve script: "+resource.getResolvedTemplate());
        }
        if (!renderContext.isEditMode()) {
            if (node.hasProperty("j:requiredMode")) {
                String req = node.getProperty("j:requiredMode").getString();
                if (!renderContext.isContributionMode() && req.equals("contribute")) {
                    throw new AccessDeniedException("Content can only be accessed in contribute");
                } else if (!renderContext.isEditMode() && req.equals("edit")) {
                    throw new AccessDeniedException("Content can only be accessed in edit");
                } else if (!renderContext.isLiveMode() && req.equals("live")) {
                    throw new AccessDeniedException("Content can only be accessed in live");
                }
            }
        }
        if (!"studiomode".equals(renderContext.getEditModeConfigName())) {
            if (node.hasProperty("j:requiredPermissions")) {
                chain.pushAttribute(renderContext.getRequest(),"cache.dynamicRolesAcls",Boolean.TRUE);
                Value[] values = node.getProperty("j:requiredPermissions").getValues();
                for (Value value : values) {
                    if (!renderContext.getMainResource().getNode().hasPermission(((JCRValueWrapperImpl) value).getNode().getName())) {
                        return "";
                    }
                }
            }
            if (node.hasProperty("j:requireLoggedUser") && node.getProperty("j:requireLoggedUser").getBoolean()) {
                if (!renderContext.isLoggedIn()) {
                    return "";
                }
            }
            if (node.hasProperty("j:requirePrivilegedUser") && node.getProperty("j:requirePrivilegedUser").getBoolean()) {
                if (!renderContext.getUser().isMemberOfGroup(0,"privileged")) {
                    return "";
                }
            }

        }
        return null;
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
}
