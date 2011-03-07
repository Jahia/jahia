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

import net.htmlparser.jericho.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLGenerator;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * Render filter that "injects" the static assets into the HEAD section of the
 * rendered HTML document.
 *
 * @author Sergiy Shyrkov
 */
public class StaticAssetsFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent> {

    class AssetsScriptContext extends SimpleScriptContext {
        private Writer writer = null;

        /**
         * {@inheritDoc}
         */
        @Override
        public Writer getWriter() {
            if (writer == null) {
                writer = new StringWriter();
            }
            return writer;
        }
    }
    
    private static Logger logger = LoggerFactory.getLogger(StaticAssetsFilter.class);
    
    private static String getTemplateContent(String path) throws IOException {
        String content = null;
        InputStream is = null;
        try {
            is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
            if (is != null) {
                content = IOUtils.toString(is);
            } else {
                logger.warn("Unable to lookup template at {}", path);
            }
        } finally {
            IOUtils.closeQuietly(is);
        }

        return content;
    }
    private String ajaxResolvedTemplate;

    private String ajaxTemplate;
    private String resolvedTemplate;

    private ScriptEngineUtils scriptEngineUtils;

    private String template;

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        String out = previousOut;
        Source source = new Source(previousOut);
        OutputDocument outputDocument = new OutputDocument(source);
        if (renderContext.isAjaxRequest()) {
            String templateContent = getAjaxResolvedTemplate(); 
            if (templateContent != null) {
                Element element = source.getFirstElement();
                final EndTag tag = element != null ? element.getEndTag() : null;
                String extension = StringUtils.substringAfterLast(ajaxTemplate, ".");
                ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(extension);
                ScriptContext scriptContext = new AssetsScriptContext();
                final Bindings bindings = scriptEngine.createBindings();
                bindings.put("renderContext", renderContext);
                bindings.put("resource", resource);
                bindings.put("url", new URLGenerator(renderContext, resource));
                scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                // The following binding is necessary for Javascript, which doesn't offer a console by default.
                bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                scriptEngine.eval(templateContent, scriptContext);
                StringWriter writer = (StringWriter) scriptContext.getWriter();
                final String staticsAsset = writer.toString();

                if (StringUtils.isNotBlank(staticsAsset)) {
                    if (tag != null) {
                        outputDocument.replace(tag.getBegin(), tag.getBegin() + 1, "\n" + staticsAsset + "\n<");
                        out = outputDocument.toString();
                    } else {
                        out = previousOut + "\n" + staticsAsset;
                    }
                }
            }
        } else {
            if (renderContext.isEditMode() && resource.getContextConfiguration().equals("page")) {
                // Add static div for edit mode
                List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
                if (bodyElementList.size() > 0) {
                    Element bodyElement = bodyElementList.get(bodyElementList.size() - 1);

                    EndTag bodyEndTag = bodyElement.getEndTag();
                    outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1, "</div><");

                    bodyElement = bodyElementList.get(0);

                    StartTag bodyStartTag = bodyElement.getStartTag();
                    outputDocument.replace(bodyStartTag.getEnd(), bodyStartTag.getEnd(), "\n" +
                                                                                         "<div class=\"jahia-template-gxt editmode-gxt\" jahiatype=\"editmode\" id=\"editmode\"" +
                                                                                         " config=\"" +
                                                                                         renderContext.getEditModeConfigName() +
                                                                                         "\"" + " path=\"" +
                                                                                         resource.getNode().getPath() +
                                                                                         "\" locale=\"" +
                                                                                         resource.getLocale() + "\"" +
                                                                                         " template=\"" +
                                                                                         resource.getResolvedTemplate() +
                                                                                         "\"" + " nodetypes=\"" +
                                                                                         ConstraintsHelper.getConstraints(
                                                                                                 renderContext.getMainResource().getNode()) +
                                                                                         "\"" + ">");
                }
            }
            List<Element> headElementList = source.getAllElements(HTMLElementName.HEAD);
            for (Element element : headElementList) {
                String templateContent = getResolvedTemplate(); 
                if (templateContent != null) {
                    final EndTag headEndTag = element.getEndTag();
                    String extension = StringUtils.substringAfterLast(template, ".");
                    ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(extension);
                    ScriptContext scriptContext = new AssetsScriptContext();
                    final Bindings bindings = scriptEngine.createBindings();
                    bindings.put("renderContext", renderContext);
                    bindings.put("resource", resource);
                    bindings.put("url", new URLGenerator(renderContext, resource));
                    scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    scriptEngine.eval(templateContent, scriptContext);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    final String staticsAsset = writer.toString();

                    if (StringUtils.isNotBlank(staticsAsset)) {
                        outputDocument.replace(headEndTag.getBegin(), headEndTag.getBegin() + 1,
                                "\n" + AggregateCacheFilter.removeEsiTags(staticsAsset) + "\n<");
                    }
                }
            }
            out = outputDocument.toString();
        }

        return out.trim();
    }

    private String getAjaxResolvedTemplate() throws IOException {
        if (ajaxResolvedTemplate == null) {
            ajaxResolvedTemplate = getTemplateContent(ajaxTemplate);
        }
        return ajaxResolvedTemplate;
    }

    private String getResolvedTemplate() throws IOException {
        if (resolvedTemplate == null) {
            resolvedTemplate = getTemplateContent(template);
        }
        return resolvedTemplate;
    }

    public void setAjaxTemplate(String ajaxTemplate) {
        this.ajaxTemplate = ajaxTemplate;
    }

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            ajaxResolvedTemplate = null;
            resolvedTemplate = null;
        }
    }
}
