/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import net.htmlparser.jericho.*;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Factory;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.collections.map.LazySortedMap;
import org.apache.commons.collections.map.TransformedSortedMap;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.SetFactory;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Render filter that "injects" the static assets into the HEAD section of the
 * rendered HTML document.
 *
 * @author Sergiy Shyrkov
 */
public class StaticAssetsFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent> {


    private static final Transformer LOW_CASE_TRANSFORMER = new Transformer() {
        public Object transform(Object input) {
            return input != null ? input.toString().toLowerCase() : null;
        }
    };

    private static final FastHashMap RANK;
    static {
        RANK = new FastHashMap();
        RANK.put("css", Integer.valueOf(1));
        RANK.put("inlinecss", Integer.valueOf(2));
        RANK.put("javascript", Integer.valueOf(3));
        RANK.put("inlinejavascript", Integer.valueOf(4));
        RANK.put("inline", Integer.valueOf(5));
        RANK.put("unknown", Integer.valueOf(6));
        RANK.setFast(true);
    }

    @SuppressWarnings("unchecked")
    private static final Comparator<String> ASSET_COMPARATOR = ComparatorUtils
            .transformedComparator(null, new Transformer() {
                public Object transform(Object input) {
                    Integer rank = null;
                    if (input != null) {
                        rank = (Integer) RANK.get(input.toString());
                    }

                    return rank != null ? rank : RANK.get("unknown");
                }
            });


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
    
    private String ajaxResolvedTemplate;

    private String ajaxTemplate;
    private String ajaxTemplateExtension;
    private String resolvedTemplate;

    private ScriptEngineUtils scriptEngineUtils;

    private String template;
    private String templateExtension;

    private static final Pattern CLEANUP_RESOURCE_REGEXP = Pattern.compile(
            "<jahia:resource .*/>");


    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        String out = previousOut;

        Source source = new Source(previousOut);

        @SuppressWarnings("unchecked")
        Map<String, Set<String>> assets = LazySortedMap.decorate(
                TransformedSortedMap.decorate(new TreeMap<String, Set<String>>(ASSET_COMPARATOR), LOW_CASE_TRANSFORMER, NOPTransformer.INSTANCE), new SetFactory());

        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> assetsOptions = LazyMap.decorate(
                new HashMap<String, Map<String, Object>>(), new Factory() {
                    public Object create() {
                        return new HashMap<String, Object>();
                    }
                });

        List<StartTag> esiResourceTags = source.getAllStartTags("jahia:resource");
        Set<String> keys = new HashSet<String>();
        for (StartTag esiResourceTag : esiResourceTags) {
            String type = esiResourceTag.getAttributeValue("type");
            String path = esiResourceTag.getAttributeValue("path");
            path = URLDecoder.decode(path, "UTF-8");
            Boolean insert = Boolean.parseBoolean(esiResourceTag.getAttributeValue("insert"));
            String resourceS = esiResourceTag.getAttributeValue("resource");
            String title = esiResourceTag.getAttributeValue("title");
            String key = esiResourceTag.getAttributeValue("key");
            Set<String> stringSet = assets.get(type);
            if (stringSet == null) {
                stringSet = new LinkedHashSet<String>();
            }
            if (insert) {
                LinkedHashSet<String> my = new LinkedHashSet<String>();
                my.add(path);
                my.addAll(stringSet);
                stringSet = my;
            } else {
                if ("".equals(key) || !keys.contains(key)) {
                    stringSet.add(path);
                    keys.add(key);
                }
            }
            assets.put(type, stringSet);
            if (title != null && !"".equals(title.trim())) {
                Map<String, String> stringMap = assetsOptions.get(resourceS);
                if (stringMap == null) {
                    stringMap = new HashMap<String, String>();
                    assetsOptions.put(resourceS, stringMap);
                }
                stringMap.put("title", title);
            }
        }

        renderContext.getRequest().setAttribute("staticAssets", assets);
        renderContext.getRequest().setAttribute("staticAssetsOptions", assetsOptions);

        OutputDocument outputDocument = new OutputDocument(source);

        if (renderContext.isAjaxRequest()) {
            String templateContent = getAjaxResolvedTemplate(); 
            if (templateContent != null) {
                Element element = source.getFirstElement();
                final EndTag tag = element != null ? element.getEndTag() : null;
                ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(ajaxTemplateExtension);
                ScriptContext scriptContext = new AssetsScriptContext();
                final Bindings bindings = scriptEngine.createBindings();
                bindings.put("renderContext", renderContext);
                bindings.put("resource", resource);
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
        } else if (resource.getContextConfiguration().equals("page")) {
            if (renderContext.isEditMode()) {
                // Add static div for edit mode
                List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
                Set<String> javascript = assets.get("javascript");
                if (javascript == null) {
                    assets.put("javascript", (javascript = new HashSet<String>()));
                }
                javascript.add(renderContext.getRequest().getContextPath() + "/modules/assets/javascript/jquery.min.js");
                javascript.add(renderContext.getRequest().getContextPath() + "/modules/assets/javascript/jquery.Jcrop.js");

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
                    ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(templateExtension);
                    ScriptContext scriptContext = new AssetsScriptContext();
                    final Bindings bindings = scriptEngine.createBindings();
                    bindings.put("renderContext", renderContext);
                    bindings.put("resource", resource);
                    bindings.put("contextPath", renderContext.getRequest().getContextPath());
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
                String header = renderContext.getRequest().getHeader("user-agent");
                if (header!=null && header.contains("MSIE")) {
                    int idx = element.getBegin() + element.toString().indexOf(">");
                    String str = ">\n<meta http-equiv=\"X-UA-Compatible\" content=\"IE=8\">";
                    outputDocument.replace(idx,idx + 1,str);
                }
                if (renderContext.isContributionMode() || renderContext.isPreviewMode()) {
                    for (Element title : element.getAllElements(HTMLElementName.TITLE)) {
                        int idx = title.getBegin() + title.toString().indexOf(">");
                        String str = renderContext.isContributionMode()?JahiaResourceBundle.getJahiaInternalResource("label.contribute",renderContext.getUILocale()):
                                JahiaResourceBundle.getJahiaInternalResource("label.preview",renderContext.getUILocale());
                        str = "> " + str  + " - ";
                        outputDocument.replace(idx, idx + 1,str);
                    }
                }

            }
            out = outputDocument.toString();
        }

        out = CLEANUP_RESOURCE_REGEXP.matcher(out).replaceAll("");
        return out.trim();
    }

    protected String getAjaxResolvedTemplate() throws IOException {
        if (ajaxResolvedTemplate == null) {
            ajaxResolvedTemplate = WebUtils.getResourceAsString(ajaxTemplate);
            if (ajaxResolvedTemplate == null) {
                logger.warn("Unable to lookup template at {}", ajaxTemplate);
            }
        }
        return ajaxResolvedTemplate;
    }

    protected String getResolvedTemplate() throws IOException {
        if (resolvedTemplate == null) {
            resolvedTemplate = WebUtils.getResourceAsString(template);
            if (resolvedTemplate == null) {
                logger.warn("Unable to lookup template at {}", template);
            }
        }
        return resolvedTemplate;
    }

    public void setAjaxTemplate(String ajaxTemplate) {
        this.ajaxTemplate = ajaxTemplate;
        if (ajaxTemplate != null) {
            ajaxTemplateExtension = StringUtils.substringAfterLast(ajaxTemplate, ".");
        }
    }

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }

    public void setTemplate(String template) {
        this.template = template;
        if (template != null) {
            templateExtension = StringUtils.substringAfterLast(template, "."); 
        }
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            ajaxResolvedTemplate = null;
            resolvedTemplate = null;
        }
    }
}
