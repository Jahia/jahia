/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
            String requirePermissions = script.getTemplate().getProperties().getProperty("requirePermissions");
            if (requirePermissions != null) {
                String[] perms = requirePermissions.split(" ");
                for (String perm : perms) {
                    if (!node.hasPermission(perm)) {
                        throw new AccessDeniedException();
                    }
                }
            }
        } else {
            throw new TemplateNotFoundException("Unable to resolve script: "+resource.getResolvedTemplate()+" asked template was "+renderContext.getMainResource().getResolvedTemplate());
        }
        if (!"studiomode".equals(renderContext.getEditModeConfigName())) {
            if (node.hasProperty("j:requiredMode")) {
                String req = node.getProperty("j:requiredMode").getString();
                if (renderContext.isContributionMode() && !req.equals("contribute")) {
                    throw new AccessDeniedException();
                } else if (!renderContext.isContributionMode() && !req.equals("normal")) {
                    throw new AccessDeniedException();
                }

            }
            if (node.hasProperty("j:requiredPermissions")) {
                Value[] values = node.getProperty("j:requiredPermissions").getValues();
                for (Value value : values) {
                    if (!node.hasPermission(((JCRValueWrapperImpl) value).getNode().getName())) {
                        throw new AccessDeniedException();
                    }
                }
            }
        }
        return null;
    }

}
