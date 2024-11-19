/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Add some resources to the head tag of the HTML.
 *
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 27 oct. 2009
 */
public class AddResourcesTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -552052631291168495L;
    private transient static Logger logger = LoggerFactory.getLogger(AddResourcesTag.class);
    private boolean insert;
    private boolean async;
    private boolean defer;
    private String type;
    private String resources;
    private String title;
    private String key;
    private String targetTag;
    private String rel;
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
        org.jahia.services.render.Resource currentResource =
                (org.jahia.services.render.Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);

        boolean isVisible = true;

        try {
            isVisible = getRenderContext().getEditModeConfig() == null || getRenderContext().isVisible(currentResource.getNode());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        if (isVisible) {
            addResources(getRenderContext());
        }
        resetState();
        return super.doEndTag();
    }

    protected boolean addResources(RenderContext renderContext) {
        if (logger.isDebugEnabled()) {
            logger.debug("Site : " + renderContext.getSite() + " type : " + type + " resources : " + resources);
        }
        if (bodyContent != null) {
            try {
                pageContext.getOut().print(bodyContent.getString());
                return true;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (renderContext == null) {
            logger.warn("No render context found. Unable to add a resoure");
            return false;
        }

        final Map<String, String> mapping = getStaticAssetMapping();

        Set<String> strings = new LinkedHashSet<String>();
        for (String sourceResource : Patterns.COMMA_WHITESPACE.split(resources.trim())) {
            String replacement = mapping.get(sourceResource);
            if (replacement != null) {
                for (String r : StringUtils.split(replacement, " ")) {
                    strings.add(r);
                }
            } else {
                strings.add(sourceResource);
            }
        }

        Set<JahiaTemplatesPackage> packages = new TreeSet<JahiaTemplatesPackage>(TemplatePackageRegistry.TEMPLATE_PACKAGE_COMPARATOR);
        final JCRSiteNode site = renderContext.getSite();
        JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        if (site.getPath().startsWith("/sites/")) {
            for (String s : site.getInstalledModulesWithAllDependencies()) {
                final JahiaTemplatesPackage templatePackageById = templateManagerService.getTemplatePackageById(s);
                if (templatePackageById != null) {
                    packages.add(templatePackageById);
                }
            }
        } else if (site.getPath().startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageById(site.getName());
            if (aPackage != null) {
                packages.add(aPackage);
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!packages.contains(depend)) {
                        packages.add(depend);
                    }
                }
            }

        }

        StringBuilder builder = new StringBuilder();
        for (String resource : strings) {
            resource = resource.trim();
            if (resource.startsWith("/") || resource.startsWith("http://") || resource.startsWith("https://")) {
                writeResourceTag(type, resource, resource);
            } else {
                String relativeResourcePath = "/" + type + "/" + resource;
                for (JahiaTemplatesPackage pack : packages) {
                    if (pack.resourceExists(relativeResourcePath)) {
                        // we found it
                        String path = pack.getRootFolderPath() + relativeResourcePath;
                        String contextPath = renderContext.getRequest().getContextPath();
                        String pathWithContext = contextPath.isEmpty() ? path : contextPath + path;

                        // apply mapping
                        String mappedPath = mapping.get(path);
                        if (mappedPath != null) {
                            for (String mappedResource : StringUtils.split(mappedPath, " ")) {
                                path = mappedResource;
                                pathWithContext = !path.startsWith("http://") && !path.startsWith("https://") ? (contextPath
                                        .isEmpty() ? path : contextPath + path) : path;
                                writeResourceTag(type, pathWithContext, resource);
                            }
                        } else {
                            writeResourceTag(type, pathWithContext, resource);
                        }

                        if (builder.length() > 0) {
                            builder.append(",");
                        }
                        builder.append(pathWithContext);
                        break;
                    }
                }
            }
        }
        if (var != null && !"".equals(var.trim())) {
            pageContext.setAttribute(var, builder.toString());
        }
        return true;
    }

    public void setType(String type) {
        this.type = type != null ? type.toLowerCase() : null;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }

    public void setDefer(boolean defer) {
        this.defer = defer;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public void setTargetTag(String targetTag) {
        this.targetTag = targetTag;
    }

    public void setRel(String rel) {
        this.rel = rel;
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
        targetTag = null;
        rel = null;
        media = null;
        condition = null;
        var = null;
        super.resetState();
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getStaticAssetMapping() {
        return (Map<String, String>) SpringContextSingleton.getBean(
                "org.jahia.services.render.StaticAssetMappingRegistry");
    }

    private void writeResourceTag(String type, String path, String resource) {
        StringBuilder builder = new StringBuilder();
        builder.append("<jahia:resource type=\"");
        builder.append(type != null ? type : "").append("\"");
        boolean isTypeInline = StringUtils.equals(type,"inline");
        if (!isTypeInline) {
            try {
                builder.append(" path=\"").append(URLEncoder.encode(path != null ? path : "", "UTF-8")).append("\"");
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        builder.append(" insert=\"").append(insert).append("\"");
        if (targetTag != null) {
            builder.append(" targetTag=\"").append(targetTag).append("\"");
        }
        if (rel != null) {
            builder.append(" rel=\"").append(rel).append("\"");
        }
        if (media != null) {
            builder.append(" media=\"").append(media).append("\"");
        }
        if (condition != null) {
            builder.append(" condition=\"").append(condition).append("\"");
        }
        builder.append(" resource=\"").append(resource != null ? resource : "").append("\"");
        builder.append(" async=\"").append(async).append("\"");
        builder.append(" defer=\"").append(defer).append("\"");
        builder.append(" title=\"").append(title != null ? title : "").append("\"");
        builder.append(" key=\"").append(key != null ? key : "").append("\"");
        if (!isTypeInline) {
            builder.append(" />\n");
        } else {
            builder.append(">");
            builder.append(path);
            builder.append("</jahia:resource>\n");
        }
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
