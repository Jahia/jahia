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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.registries.ServicesRegistry;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.File;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

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
        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        JahiaTemplatesPackage templatesPackage = (JahiaTemplatesPackage) pageContext.getAttribute("currentModule", PageContext.REQUEST_SCOPE);
        addResources(renderContext, templatesPackage, type, resources);
        nodetype = null;
        node = null;
        return super.doEndTag();
    }

    private void addResources(RenderContext renderContext, JahiaTemplatesPackage aPackage, String type,
                              String resources) {
        final Set<String> links = renderContext.getExternalLinks(type);
        String[] strings = resources.split(",");

        List<JahiaTemplatesPackage> packages = new ArrayList<JahiaTemplatesPackage>();
        packages.add(aPackage);
        for (String s : aPackage.getDepends()) {
            packages.add(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(s));
        }

        for (String resource : strings) {
            if (resource.startsWith("/") || resource.startsWith("http://")) {
                renderContext.addExternalLink(type, resource);
            } else {
                for (JahiaTemplatesPackage pack : packages){
                    String path = pageContext.getServletContext().getRealPath(pack.getRootFolderPath());
                    File f = new File(path + "/" + type + "/" + resource);
                    if (f.exists()) {
                        boolean found = false;
                        if (links != null) {
                            for (String link : links) {
                                if (link.endsWith(f.getName())) {
                                    found = true;
                                }
                            }
                        }
                        if (!found) {
                            renderContext.addExternalLink(type,renderContext.getRequest().getContextPath() + pack.getRootFolderPath() + "/" + type + "/" + f.getName());
                            break;
                        }
                    }
                }
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