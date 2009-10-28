/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.include;

import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 27 oct. 2009
 */
public class AddResourcesTag extends BodyTagSupport {
    private transient static Logger logger = Logger.getLogger(AddResourcesTag.class);
    private String nodetype;
    private JCRNodeWrapper node;
    private String type;
    private String resources;
    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            // Todo test if module is active
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext",
                                                                                   PageContext.REQUEST_SCOPE);
            ExtendedNodeType extendedNodeType = null;
            if (nodetype != null) {
                extendedNodeType = NodeTypeRegistry.getInstance().getNodeType(nodetype);
            } else if (node != null) {
                extendedNodeType = node.getPrimaryNodeType();
            }
            if (extendedNodeType != null) {
                final List<JahiaTemplatesPackage> aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackagesForModule(
                        extendedNodeType.getAlias().replace(":", "_"));
                if (aPackage != null && aPackage.size()>0) {
                    for (JahiaTemplatesPackage templatesPackage : aPackage) {
                        String path = pageContext.getServletContext().getRealPath(templatesPackage.getRootFolderPath());
                        addResources(renderContext, templatesPackage, path, type, resources);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new JspException(e);
        }
        nodetype = null;
        node = null;
        return super.doEndTag();
    }

    private void addResources(RenderContext renderContext, JahiaTemplatesPackage aPackage, String path, String type,
                              String resources) {

        String[] strings = resources.split(",");
        for (String resource : strings) {
            File f = new File(path + "/" + type+"/"+resource);
            if(f.exists()) {
                renderContext.addExternalLink(type, aPackage.getRootFolderPath() + "/" + type + "/" + f.getName());
            }
        }

    }

    public void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }
}