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

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rendering controller for the edit mode.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 *
 * @see org.jahia.bin.Render
 */
public class Contribute extends Render {
    private static final long serialVersionUID = 7621770855694958651L;
    private static Logger logger = LoggerFactory.getLogger(Contribute.class);

    
    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setContributionMode(true);
        context.setServletPath(getContributeServletPath());
        return context;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp,
            RenderContext renderContext, Resource resource, long startTime)
            throws RepositoryException, RenderException, IOException {
        overrideResourceNodeType(resource, req);
        super.doGet(req, resp, renderContext, resource, startTime);
    }
    
    public static String getContributeServletPath() {
        // TODO move this into configuration
        return "/cms/contribute";
    }

    protected boolean hasAccess(JCRNodeWrapper node) {
        if (node == null) {
            logger.error("Site key is null.");
            return false;
        }
        try {
            if(Constants.LIVE_WORKSPACE.equals(node.getSession().getWorkspace().getName())) {
                logger.error("Someone have tried to access the live repository in contribute mode");
                return false;
            }
        } catch (RepositoryException e) {
            return false;
        }
        return node.hasPermission("contributeModeAccess");
    }

    @Override
    protected boolean isDisabled() {
        return settingsBean.isDistantPublicationServerMode();
    }

    protected void overrideResourceNodeType(Resource resource, HttpServletRequest req) {
        if (resource == null || resource.getResourceNodeType() != null) {
            return;
        }

        String resourceNodeType = req.getParameter("resourceNodeType");
        if (StringUtils.isEmpty(resourceNodeType)) {
            return;
        }

        try {
            resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(
                    resourceNodeType));
            if (logger.isDebugEnabled()) {
                logger.debug("Set resource node type for path {} to {}", resource.getNode()
                        .getPath(), resourceNodeType);
            }
        } catch (NoSuchNodeTypeException e) {
            logger.warn("Unable to find node type " + resourceNodeType
                    + ". Skip overriding resource node type.", e);
        }
    }

}