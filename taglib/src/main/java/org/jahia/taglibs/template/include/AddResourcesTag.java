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

package org.jahia.taglibs.template.include;

import org.slf4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;

/**
 * Add some resources to the head tag of the HTML.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 27 oct. 2009
 */
public class AddResourcesTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -552052631291168495L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddResourcesTag.class);
    private boolean insert;
    private String type;
    private String resources;
    private String title;
    private String key;
    private String media;
    private String condition;
    private String var;

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        JahiaTemplatesPackage templatesPackage = (JahiaTemplatesPackage) pageContext.getAttribute("currentModule", PageContext.REQUEST_SCOPE);
        addResources(getRenderContext(), templatesPackage);
        resetState();
        return super.doEndTag();
    }

    protected void addResources(RenderContext renderContext, JahiaTemplatesPackage aPackage) {
        logger.debug("Package : " + aPackage.getName() + " type : " + type + " resources : " + resources);
        if (bodyContent != null) {
            try {
                pageContext.getOut().print(bodyContent.getString());
                return;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (renderContext == null) {
            logger.warn("No render context found. Unable to add a resoure");
            return;
        }

        final Map<String, String> mapping = getStaticAssetMapping();

        String[] strings = resources.split(",");

        List<String> lookupPaths = new LinkedList<String>();
        lookupPaths.add(aPackage.getRootFolderPath() + "/" + type + "/");
        for (JahiaTemplatesPackage pack : aPackage.getDependencies()) {
            lookupPaths.add(pack.getRootFolderPath() + "/" + type + "/");
        }
        StringBuilder builder = new StringBuilder();
        for (String resource : strings) {
            boolean found = false;
            resource = resource.trim();
            if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://")) {
                found = true;
                resource = mapping.containsKey(resource) ? mapping.get(resource) : resource;
                writeResourceTag(type, resource, resource);
            } else {
                for (String lookupPath : lookupPaths) {
                    String path = lookupPath + resource;
                    String pathWithContext = renderContext.getRequest().getContextPath() + path;
                    try {
                        if (pageContext.getServletContext().getResource(path) != null) {
                            // we found it

                            // apply mapping
                            if (mapping.containsKey(path)) {
                                path = mapping.get(path);
                                pathWithContext = !path.startsWith("http://") && !path.startsWith("https://") ? renderContext.getRequest().getContextPath() + path : path;
                            }
                            writeResourceTag(type, pathWithContext, resource);
                            found = true;
                            if (builder.length() > 0) {
                                builder.append(",");
                            }
                            builder.append(pathWithContext);
                            break;
                        }
                    } catch (MalformedURLException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
            if (!found) {
                logger.warn("Unable to find resource '" + resource + "' in: " + lookupPaths);
            }
        }
        if (var != null && !"".equals(var.trim())) {
            pageContext.setAttribute(var, builder.toString());
        }
    }

    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : null;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public int doAfterBody() throws JspException {
        if (bodyContent != null) {
            String asset = getBodyContent().getString();
            getBodyContent().clearBody();
            writeResourceTag(type != null ? type : "inline", asset, null);
        }
        return super.doAfterBody();
    }


    @Override
    protected void resetState() {
        insert = false;
        resources = null;
        type = null;
        title = null;
        media = null;
        condition = null;
        super.resetState();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getStaticAssetMapping() {
        return (Map<String, String>) SpringContextSingleton.getBean("org.jahia.services.render.StaticAssetMappingRegistry");
    }

    private void writeResourceTag(String type, String path, String resource) {
        StringBuilder builder = new StringBuilder();
        builder.append("<jahia:resource type=\"");
        builder.append(type != null ? type : "").append("\"");
        try {
            builder.append(" path=\"").append(URLEncoder.encode(path != null ? path : "", "UTF-8")).append("\"");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        builder.append(" insert=\"").append(insert).append("\"");
        if (media != null) {
            builder.append(" media=\"").append(media).append("\"");
        }
        if (condition != null) {
            builder.append(" condition=\"").append(condition).append("\"");
        }
        builder.append(" resource=\"").append(resource != null ? resource : "").append("\"");
        builder.append(" title=\"").append(title != null ? title : "").append("\"");
        builder.append(" key=\"").append(key != null ? key : "").append("\"");
        builder.append(" />\n");
//        builder.append("<!-- cache:resource type=\"");
//        builder.append(type != null ? type : "").append("\"");
//        try {
//            builder.append(" path=\"").append(URLEncoder.encode(path != null ? path : "", "UTF-8")).append("\"");
//        } catch (UnsupportedEncodingException e) {
//            logger.error(e.getMessage(), e);
//        }
//        builder.append(" insert=\"").append(insert).append("\"");
//        builder.append(" resource=\"").append(resource != null ? resource : "").append("\"");
//        builder.append(" title=\"").append(title != null ? title : "").append("\"");
//        builder.append(" key=\"").append(key != null ? key : "").append("\"");
//        builder.append(" -->\n");
//        builder.append("\n<!-- /cache:resource -->\n");
        try {
            pageContext.getOut().print(builder.toString());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Sets the title to be used for the asset if applicable.
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
