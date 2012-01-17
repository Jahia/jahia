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

package org.jahia.services.render.filter;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import com.yahoo.platform.yui.org.mozilla.javascript.ErrorReporter;
import com.yahoo.platform.yui.org.mozilla.javascript.EvaluatorException;
import net.htmlparser.jericho.*;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.FastHashMap;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.functors.NOPTransformer;
import org.apache.commons.collections.map.LazySortedMap;
import org.apache.commons.collections.map.TransformedSortedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.render.AssetsMapFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.SimpleScriptContext;
import javax.servlet.ServletContext;
import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Render filter that "injects" the static assets into the HEAD section of the
 * rendered HTML document.
 *
 * @author Sergiy Shyrkov
 */
public class StaticAssetsFilter extends AbstractFilter implements ApplicationListener<TemplatePackageRedeployedEvent> {


    private static final Transformer LOW_CASE_TRANSFORMER = new Transformer() {
        public Object transform(Object input) {
            return input != null ? input.toString().toLowerCase() : null;
        }
    };

    private static final FastHashMap RANK;

    static {
        RANK = new FastHashMap();
        RANK.put("inlinebefore", Integer.valueOf(0));
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

    private boolean aggregateAndCompress;
    private List<String> excludesFromAggregateAndCompress = new ArrayList<String>();
    
    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        String out = previousOut;

        Source source = new Source(previousOut);

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Map<String, String>>> assets = LazySortedMap.decorate(
                TransformedSortedMap.decorate(new TreeMap<String, Map<String, Map<String, String>>>(ASSET_COMPARATOR), LOW_CASE_TRANSFORMER, NOPTransformer.INSTANCE), new AssetsMapFactory());

        List<StartTag> esiResourceTags = source.getAllStartTags("jahia:resource");
        Set<String> keys = new HashSet<String>();
        for (StartTag esiResourceTag : esiResourceTags) {
            String type = esiResourceTag.getAttributeValue("type");
            String path = esiResourceTag.getAttributeValue("path");
            String media = esiResourceTag.getAttributeValue("media");
            String condition = esiResourceTag.getAttributeValue("condition");
            path = URLDecoder.decode(path, "UTF-8");
            Boolean insert = Boolean.parseBoolean(esiResourceTag.getAttributeValue("insert"));
            String resourceS = esiResourceTag.getAttributeValue("resource");
            String title = esiResourceTag.getAttributeValue("title");
            String key = esiResourceTag.getAttributeValue("key");
            Map<String, String> optionsMap = new HashMap<String, String>();

            // Manage Options
            if (title != null && !"".equals(title.trim())) {
                optionsMap.put("title", title);
            }
            if (media != null && !"".equals(media.trim())) {
                optionsMap.put("media", media);
            }
            if (condition != null && !"".equals(condition.trim())) {
                optionsMap.put("condition", condition);
            }

            Map<String, Map<String, String>> stringMap = assets.get(type);
            if (stringMap == null) {
                Map<String, Map<String, String>> assetMap = new LinkedHashMap<String, Map<String, String>>();
                stringMap = assets.put(type, assetMap);
            }

            if (insert) {
                Map<String, Map<String, String>> my = new LinkedHashMap<String, Map<String, String>>();
                my.put(path, optionsMap);
                my.putAll(stringMap);
                stringMap = my;
            } else {
                if ("".equals(key) || !keys.contains(key)) {
                    Map<String, Map<String, String>> my = new LinkedHashMap<String, Map<String, String>>();
                    my.put(path, optionsMap);
                    stringMap.putAll(my);
                    keys.add(key);
                }
            }
            assets.put(type, stringMap);
        }

        renderContext.getRequest().setAttribute("staticAssets", assets);

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
                Map<String, Map<String, String>> javascript = assets.get("javascript");
                if (javascript == null) {
                    assets.put("javascript", (javascript = new HashMap<String, Map<String, String>>()));
                }
                javascript.put(renderContext.getRequest().getContextPath() + "/modules/assets/javascript/jquery.min.js", null);
                javascript.put(renderContext.getRequest().getContextPath() + "/modules/assets/javascript/jquery.Jcrop.js", null);

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

                    if (resource.getWorkspace().equals("live") && aggregateAndCompress) {
                        assets.put("css", aggregate(assets.get("css"), "css"));
                        assets.put("javascript", aggregate(assets.get("javascript"), "js"));
                    }

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
                if (!renderContext.isPreviewMode() && !renderContext.isLiveMode() && header != null && header.contains("MSIE")) {
                    int idx = element.getBegin() + element.toString().indexOf(">");
                    String str = ">\n<meta http-equiv=\"X-UA-Compatible\" content=\"IE=8\">";
                    outputDocument.replace(idx, idx + 1, str);
                }
                if (renderContext.isContributionMode() || renderContext.isPreviewMode()) {
                    for (Element title : element.getAllElements(HTMLElementName.TITLE)) {
                        int idx = title.getBegin() + title.toString().indexOf(">");
                        String str = renderContext.isContributionMode() ? JahiaResourceBundle.getJahiaInternalResource("label.contribute", renderContext.getUILocale()) :
                                JahiaResourceBundle.getJahiaInternalResource("label.preview", renderContext.getUILocale());
                        str = "> " + str + " - ";
                        outputDocument.replace(idx, idx + 1, str);
                    }
                }

            }
            out = outputDocument.toString();
        }

        // Clean all jahia:resource tags
        source = new Source(out);
        esiResourceTags = (new Source(out)).getAllStartTags("jahia:resource");
        outputDocument = new OutputDocument(source);
        for (StartTag segment : esiResourceTags) {
            outputDocument.replace(segment,"");
        }
        return outputDocument.toString().trim();
    }

    private Map<String, Map<String, String>> aggregate(Map<String, Map<String, String>> map, String type) throws IOException {
        List<Map.Entry<String, Map<String, String>>> entries = new ArrayList<Map.Entry<String, Map<String, String>>>(map.entrySet());
        Map<String, Map<String, String>> newCss = new LinkedHashMap<String, Map<String, String>>();

        int i = 0;
        for (; i < entries.size(); ) {
            long filesDates = 0;
            ServletContext context = JahiaContextLoaderListener.getServletContext();

            List<String> pathsToAggregate = new ArrayList<String>();
            Vector<InputStream> files = new Vector<InputStream>();
            for (; i < entries.size(); i++) {
                Map.Entry<String, Map<String, String>> entry = entries.get(i);
                File file = new File(context.getRealPath(entry.getKey()));
                if (file.exists() && entry.getValue().isEmpty() && !excludesFromAggregateAndCompress.contains(entry.getKey())) {
                    pathsToAggregate.add(entry.getKey());
                    files.add(new FileInputStream(file));
                    long lastModified = file.lastModified();
                    if (filesDates < lastModified) {
                        filesDates = lastModified;
                    }
                } else {
                    // CSS has options - will not be aggregated and added at the end
                    break;
                }
            }
            if (!pathsToAggregate.isEmpty()) {
                String aggregatedKey = generateAggregateName(pathsToAggregate);

                String minifiedAggregatedPath = "/resources/" + aggregatedKey + ".min." + type;
                String minifiedAggregatedRealPath = context.getRealPath(minifiedAggregatedPath);
                File minifiedAggregatedFile = new File(minifiedAggregatedRealPath);

                if (!minifiedAggregatedFile.exists() || minifiedAggregatedFile.lastModified() < filesDates) {
                    new File(context.getRealPath("/resources")).mkdirs();

                    List<String> minifiedPaths = new ArrayList<String>();
                    for (String path : pathsToAggregate) {
                        File f = new File(context.getRealPath(path));
                        String minifiedPath = "/resources/" + path.replace('/','_') + ".min." + type;
                        File minifiedFile = new File(context.getRealPath(minifiedPath));
                        if (!minifiedFile.exists() || minifiedFile.lastModified() < f.lastModified()) {
                            Reader reader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                            Writer writer = new OutputStreamWriter(new FileOutputStream(context.getRealPath(minifiedPath)), "UTF-8");
                        
                            if (type.equals("css")) {
                                CssCompressor compressor = new CssCompressor(reader);
                                compressor.compress(writer, -1);
                            } else if (type.equals("js")) {
                                JavaScriptCompressor compressor = null;
                                try {
                                    compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
                                        public void warning(String message, String sourceName,
                                                            int line, String lineSource, int lineOffset) {
                                            if (line < 0) {
                                                logger.debug(message);
                                            } else {
                                                logger.debug(line + ':' + lineOffset + ':' + message);
                                            }
                                        }

                                        public void error(String message, String sourceName,
                                                          int line, String lineSource, int lineOffset) {
                                            if (line < 0) {
                                                logger.error(message);
                                            } else {
                                                logger.error(line + ':' + lineOffset + ':' + message);
                                            }
                                        }

                                        public EvaluatorException runtimeError(String message, String sourceName,
                                                                               int line, String lineSource, int lineOffset) {
                                            error(message, sourceName, line, lineSource, lineOffset);
                                            return new EvaluatorException(message);
                                        }
                                    });
                                    compressor.compress(writer, -1, true, true, false, false);
                                } catch (EvaluatorException e) {
                                    logger.error("Error when minifying " + path, e);
                                    IOUtils.closeQuietly(reader);
                                    IOUtils.closeQuietly(writer);
                                    reader = new InputStreamReader(new FileInputStream(f), "UTF-8");
                                    writer = new OutputStreamWriter(new FileOutputStream(context.getRealPath(minifiedPath)), "UTF-8");
                                    IOUtils.copy(reader, writer);
                                }
                            } else {
                                IOUtils.copy(reader, writer);
                            }
                            IOUtils.closeQuietly(reader);
                            IOUtils.closeQuietly(writer);
                        }
                        minifiedPaths.add(minifiedPath);
                    }

                    try {
                        OutputStream outMerged = new FileOutputStream(minifiedAggregatedRealPath);
                        for (String minifiedFile : minifiedPaths) {
                            InputStream is = new FileInputStream(context.getRealPath(minifiedFile));
                            if (type.equals("css")) {
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                IOUtils.copy(is, stream);
                                IOUtils.closeQuietly(is);
                                String s = stream.toString("UTF-8");

                                s = s.replace("url( ", "url(");
                                s = s.replace("url(\"", "url(\".." + StringUtils.substringBeforeLast(pathsToAggregate.get(minifiedPaths.indexOf(minifiedFile)), "/") + "/");
                                s = s.replace("url('", "url('.." + StringUtils.substringBeforeLast(pathsToAggregate.get(minifiedPaths.indexOf(minifiedFile)), "/") + "/");
                                s = s.replaceAll("url\\(([^'\"])", "url(.." + StringUtils.substringBeforeLast(pathsToAggregate.get(minifiedPaths.indexOf(minifiedFile)), "/") + "/$1");
                                is = new ByteArrayInputStream(s.getBytes("UTF-8"));
                            }
                            IOUtils.copy(is, outMerged);
                            IOUtils.closeQuietly(is);
                        }
                        IOUtils.closeQuietly(outMerged);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                newCss.put(minifiedAggregatedPath, new HashMap<String, String>());
            }
            if (i < entries.size()) {
                newCss.put(entries.get(i).getKey(), entries.get(i).getValue());
                i++;
            }
        }
        return newCss;
    }

    public String generateAggregateName(List<String> m) {
        StringBuilder sb = new StringBuilder();
        for (String s1 : m) {
            sb.append(s1);
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] digest = digester.digest(sb.toString().getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte aDigest : digest) {
                hexString.append(Integer.toHexString(0xFF & aDigest));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    public void setAggregateAndCompress(boolean aggregateAndCompress) {
        this.aggregateAndCompress = aggregateAndCompress;
    }

    public void setExcludesFromAggregateAndCompress(List<String> skipAggregation) {
        this.excludesFromAggregateAndCompress = skipAggregation;
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        ajaxResolvedTemplate = null;
        resolvedTemplate = null;
    }

}
