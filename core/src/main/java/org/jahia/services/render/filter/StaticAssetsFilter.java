/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.utils.GWTInitializer;
import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.render.AssetsMapFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.cache.AggregateCacheFilter;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.Patterns;
import org.jahia.utils.ScriptEngineUtils;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.script.*;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Render filter that "injects" the static assets into the HEAD section of the
 * rendered HTML document.
 *
 * @author Sergiy Shyrkov
 */
public class StaticAssetsFilter extends AbstractFilter implements ApplicationListener<TemplatePackageRedeployedEvent>, InitializingBean {

    private static final FastHashMap RANK;
    static {
        RANK = new FastHashMap();
        RANK.put("inlinebefore", 0);
        RANK.put("css", 1);
        RANK.put("inlinecss", 2);
        RANK.put("javascript", 3);
        RANK.put("inlinejavascript", 4);
        RANK.put("inline", 5);
        RANK.put("unknown", 6);
        RANK.setFast(true);
    }

    private static final Pattern URL_PATTERN_1 = Pattern.compile("url\\( ");
    private static final Pattern URL_PATTERN_2 = Pattern.compile("url\\(\"(?!(/|http:|https:|data:))");
    private static final Pattern URL_PATTERN_3 = Pattern.compile("url\\('(?!(/|http:|https:|data:))");
    private static final Pattern URL_PATTERN_4 = Pattern.compile("url\\((?!(/|'|\"|http:|https:|data:))");

    private static final Pattern CLEANUP_REGEXP = Pattern.compile("<!-- jahia:temp value=\".*?\" -->");
    private static final String[] OPTIONAL_ATTRIBUTES = new String[]{"title", "rel", "media", "condition"};
    private static final String TARGET_TAG = "targetTag";
    private static final String STATIC_ASSETS = "staticAssets";

    // This path must be is sync with the static files servlet URL pattern in web.xml.
    private static final String GENERATED_RESOURCES_URL_PATH = "/generated-resources/";

    private static final Logger logger = LoggerFactory.getLogger(StaticAssetsFilter.class);

    private String jahiaContext = null;
    private boolean addLastModifiedDate = false;
    private File generatedResourcesFolder;
    private String ajaxResolvedTemplate;
    private String ajaxTemplate;
    private String ajaxTemplateExtension;
    private String ckeditorJavaScript = "/modules/ckeditor/javascript/ckeditor.js";
    private String resolvedTemplate;
    private ScriptEngineUtils scriptEngineUtils;
    private String template;
    private String templateExtension;
    private boolean aggregateAndCompress;
    private List<String> excludesFromAggregateAndCompress = new ArrayList<String>();
    private Set<String> ieHeaderRecognitions = new HashSet<String>();
    private boolean forceLiveIEcompatiblity;

    private static final Transformer LOW_CASE_TRANSFORMER = new Transformer() {

        @Override
        public Object transform(Object input) {
            return input != null ? input.toString().toLowerCase() : null;
        }
    };

    @SuppressWarnings("unchecked")
    private static final Comparator<String> ASSET_COMPARATOR = ComparatorUtils.transformedComparator(null, new Transformer() {

        @Override
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

    private static void atomicMove(File src, File dest) throws IOException {
        if (src.exists()) {
            // perform the file move
            try {
                Files.move(Paths.get(src.toURI()), Paths.get(dest.toURI()),
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                logger.warn("Unable to move the file {} into {}. Copying it instead.", src, dest);
                try {
                    FileUtils.copyFile(src, dest);
                } finally {
                    FileUtils.deleteQuietly(src);
                }
            }
        }
    }

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        String out = previousOut;

        Source source = new Source(previousOut);

        Map<String, Map<String, Map<String, Map<String, String>>>> assetsByTarget = new LinkedHashMap<String, Map<String, Map<String,Map<String,String>>>>();

        List<StartTag> esiResourceTags = source.getAllStartTags("jahia:resource");
        Set<String> keys = new HashSet<String>();
        for (StartTag esiResourceTag : esiResourceTags) {
            Map<String, Map<String, Map<String, String>>> assets;
            String targetTag = esiResourceTag.getAttributeValue(TARGET_TAG);
            if (targetTag == null) {
                targetTag = "HEAD";
            } else {
                targetTag = targetTag.toUpperCase();
            }

            if (!assetsByTarget.containsKey(targetTag)) {
                assets = LazySortedMap.decorate(TransformedSortedMap.decorate(new TreeMap<String, Map<String, Map<String, String>>>(ASSET_COMPARATOR),
                        LOW_CASE_TRANSFORMER, NOPTransformer.INSTANCE), new AssetsMapFactory());
                assetsByTarget.put(targetTag,assets);
            } else {
                assets = assetsByTarget.get(targetTag);
            }

            String type = esiResourceTag.getAttributeValue("type");
            String path = esiResourceTag.getAttributeValue("path");
            path = URLDecoder.decode(path, "UTF-8");
            Boolean insert = Boolean.parseBoolean(esiResourceTag.getAttributeValue("insert"));
            String key = esiResourceTag.getAttributeValue("key");

            // get options
            Map<String, String> optionsMap = getOptionMaps(esiResourceTag);

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

        OutputDocument outputDocument = new OutputDocument(source);

        if (renderContext.isAjaxRequest()) {
            String templateContent = getAjaxResolvedTemplate();
            if (templateContent != null) {
                for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> entry : assetsByTarget.entrySet()) {
                    renderContext.getRequest().setAttribute(STATIC_ASSETS, entry.getValue());
                    Element element = source.getFirstElement(TARGET_TAG);
                    final EndTag tag = element != null ? element.getEndTag() : null;
                    ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(ajaxTemplateExtension);
                    ScriptContext scriptContext = new AssetsScriptContext();
                    final Bindings bindings = scriptEngine.createBindings();
                    bindings.put(TARGET_TAG, entry.getKey());
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
                            out = staticsAsset + "\n" + previousOut;
                        }
                    }
                }
            }
        } else if (resource.getContextConfiguration().equals("page")) {
            if (renderContext.isEditMode()) {
                if (renderContext.getServletPath().endsWith("frame")) {
                    boolean doParse = true;
                    if (renderContext.getEditModeConfig().getSkipMainModuleTypesDomParsing() != null) {
                        for (String nt : renderContext.getEditModeConfig().getSkipMainModuleTypesDomParsing()) {
                            doParse = !resource.getNode().isNodeType(nt);
                            if (!doParse) {
                                break;
                            }
                        }
                    }
                    List<Element> bodyElementList = source.getAllElements(HTMLElementName.BODY);
                    if (bodyElementList.size() > 0) {
                        Element bodyElement = bodyElementList.get(bodyElementList.size() - 1);
                        EndTag bodyEndTag = bodyElement.getEndTag();
                        outputDocument.replace(bodyEndTag.getBegin(), bodyEndTag.getBegin() + 1, "</div><");

                        bodyElement = bodyElementList.get(0);

                        StartTag bodyStartTag = bodyElement.getStartTag();
                        outputDocument.replace(bodyStartTag.getEnd(), bodyStartTag.getEnd(), "\n" +
                                "<div jahiatype=\"mainmodule\""
                                + " path=\"" +
                                resource.getNode().getPath() +
                                "\" locale=\"" +
                                resource.getLocale() + "\"" +
                                " template=\"" +
                                (resource.getTemplate() != null ? resource.getTemplate() : "default") +
                                "\"" + " nodetypes=\"" +
                                ConstraintsHelper.getConstraints(
                                        renderContext.getMainResource().getNode()) +
                                "\"" + ">");
                        if (doParse) {
                            outputDocument.replace(bodyStartTag.getEnd() - 1, bodyStartTag.getEnd()," jahia-parse-html=\"true\">");
                        }
                    }
                }
            }
            if (!assetsByTarget.containsKey("HEAD")) {
                addResources(renderContext, resource, source, outputDocument, "HEAD", new HashMap<String, Map<String, Map<String, String>>>());
            }
            for (Map.Entry<String, Map<String, Map<String, Map<String, String>>>> entry : assetsByTarget.entrySet()) {
                String targetTag = entry.getKey();
                Map<String, Map<String, Map<String, String>>> assets = entry.getValue();
                addResources(renderContext, resource, source, outputDocument, targetTag, assets);
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
        String s = outputDocument.toString();
        s = removeTempTags(s);
        return s.trim();
    }

    private void addResources(RenderContext renderContext, Resource resource, Source source, OutputDocument outputDocument, String targetTag, Map<String, Map<String, Map<String, String>>> assets) throws IOException, ScriptException {
        renderContext.getRequest().setAttribute(STATIC_ASSETS, assets);
        Element element = source.getFirstElement(targetTag);
        String templateContent = getResolvedTemplate();
        if(element == null) {
            logger.warn("Trying to add resources to output but didn't find {} tag", targetTag);
            return;
        }
        if (templateContent != null) {
            final EndTag headEndTag = element.getEndTag();
            ScriptEngine scriptEngine = scriptEngineUtils.scriptEngine(templateExtension);
            ScriptContext scriptContext = new AssetsScriptContext();
            final Bindings bindings = scriptEngine.createBindings();

            bindings.put("contextJsParameters", getContextJsParameters(assets, renderContext));

            if (aggregateAndCompress && resource.getWorkspace().equals("live")) {
                Map<String, Map<String, String>> cssAssets = assets.get("css");
                if (cssAssets != null) {
                    assets.put("css", aggregate(cssAssets, "css"));
                }
                Map<String, Map<String, String>> javascriptAssets = assets.get("javascript");
                if (javascriptAssets != null) {
                    Map<String, Map<String, String>> scripts = new LinkedHashMap<String, Map<String, String>>(javascriptAssets);
                    Map<String, Map<String, String>> newScripts = aggregate(javascriptAssets, "js");
                    assets.put("javascript", newScripts);
                    scripts.keySet().removeAll(newScripts.keySet());
                    assets.put("aggregatedjavascript", scripts);
                }
            } else if (addLastModifiedDate) {
                addLastModified(assets);
            }

            bindings.put(TARGET_TAG, targetTag);
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
                        "\n" + AggregateCacheFilter.removeCacheTags(staticsAsset) + "\n<");
            }
        }
        // workaround for ie9 in gxt/gwt
        // renderContext.isEditMode() means that gwt is loaded, for contribute, edit or studio
        if (isEnforceIECompatibilityMode(renderContext)) {
            int idx = element.getBegin() + element.toString().indexOf(">");
            String str = ">\n<meta http-equiv=\"X-UA-Compatible\" content=\""
                    + SettingsBean.getInstance().getInternetExplorerCompatibility() + "\"/>";
            outputDocument.replace(idx, idx + 1, str);
        }
        if ((renderContext.isPreviewMode()) && !Boolean.valueOf((String) renderContext.getRequest().getAttribute(
                "org.jahia.StaticAssetFilter.doNotModifyDocumentTitle"))) {
            for (Element title : element.getAllElements(HTMLElementName.TITLE)) {
                int idx = title.getBegin() + title.toString().indexOf(">");
                String str = Messages.getInternal("label.preview", renderContext.getUILocale());
                str = ">" + str + " - ";
                outputDocument.replace(idx, idx + 1, str);
            }
        }
    }

    private Map<String, String> getOptionMaps(StartTag esiResourceTag) {
        Map<String, String> optionsMap = null;

        for (String attributeName : OPTIONAL_ATTRIBUTES) {
            String attribute = esiResourceTag.getAttributeValue(attributeName);
            if (attribute != null) {
                attribute = attribute.trim();
                if (!attribute.isEmpty()) {
                    // create options map if it doesn't exist already
                    if (optionsMap == null) {
                        optionsMap = new HashMap<>(OPTIONAL_ATTRIBUTES.length);
                    }

                    optionsMap.put(attributeName, attribute);
                }
            }
        }


        return optionsMap != null ? optionsMap : Collections.<String, String>emptyMap();
    }

    private void addLastModified(Map<String, Map<String, Map<String, String>>> assets) throws IOException {
        for (Map.Entry<String, Map<String, Map<String, String>>> assetsEntry : assets.entrySet()) {
            if (assetsEntry.getKey().equals("css") || assetsEntry.getKey().equals("javascript")) {
                Map<String, Map<String, String>> newMap = new LinkedHashMap<String, Map<String, String>>();
                for (Map.Entry<String, Map<String, String>> entry : assetsEntry.getValue().entrySet()) {
                    org.springframework.core.io.Resource r = getResource(getKey(entry.getKey()));
                    if (r != null) {
                        newMap.put(entry.getKey() + "?" + r.lastModified(), entry.getValue());
                    } else {
                        newMap.put(entry.getKey(), entry.getValue());
                    }
                }
                assetsEntry.getValue().clear();
                assetsEntry.getValue().putAll(newMap);
            }
        }
    }

    private Object getContextJsParameters(Map<String, Map<String, Map<String, String>>> assets, RenderContext ctx) {
        StringBuilder params = new StringBuilder(1024);
        params.append("{contextPath:\"").append(ctx.getRequest().getContextPath()).append("\",lang:\"")
                .append(ctx.getMainResourceLocale()).append("\",uilang:\"").append(ctx.getUILocale());
        try {
            params.append("\",siteUuid:\"").append(ctx.getSite() != null ? ctx.getSite().getIdentifier() : "''");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        params.append("\",wcag:").append(
                ctx.getSiteInfo() != null ? ctx.getSiteInfo().isWCAGComplianceCheckEnabled() : "false");

        Map<String, Map<String, String>> js = assets.get("javascript");
        if (js != null && js.containsKey(ckeditorJavaScript)) {
            String customCkeditorConfig = GWTInitializer.getCustomCKEditorConfig(ctx);
            if (customCkeditorConfig != null) {
                params.append(",ckeCfg:\"").append(customCkeditorConfig).append("\"");
            }
        }
        params.append(",ckeCfg:\"\"}");
        return params.toString();
    }

    private boolean isEnforceIECompatibilityMode(RenderContext renderContext) {

        if (!forceLiveIEcompatiblity && !renderContext.isEditMode()) {
            return false;
        }
        String header = renderContext.getRequest().getHeader("user-agent");
        if (header == null || header.length() == 0) {
            return false;
        }
        header = header.toLowerCase();
        for (String ieHeaderRecognition : getIeHeaderRecognitions()) {
            if (header.contains(ieHeaderRecognition)) {
                return true;
            }
        }
        return false;
    }

    public static String removeTempTags(String content) {
        if (StringUtils.isNotEmpty(content)) {
            return CLEANUP_REGEXP.matcher(content).replaceAll("");
        } else {
            return content;
        }
    }

    private Map<String, Map<String, String>> aggregate(Map<String, Map<String, String>> map, String type) throws IOException {

        List<Map.Entry<String, Map<String, String>>> entries = new ArrayList<Map.Entry<String, Map<String, String>>>(map.entrySet());
        Map<String, Map<String, String>> newCss = new LinkedHashMap<String, Map<String, String>>();

        int i = 0;
        for (; i < entries.size(); ) {
            long filesDates = 0;

            LinkedHashMap<String, org.springframework.core.io.Resource> pathsToAggregate = new LinkedHashMap<String,org.springframework.core.io.Resource>();

            for (; i < entries.size(); i++) {
                Map.Entry<String, Map<String, String>> entry = entries.get(i);
                String key = getKey(entry.getKey());
                org.springframework.core.io.Resource r = getResource(key);
                if (entry.getValue().isEmpty() && !excludesFromAggregateAndCompress.contains(key) && r != null) {
                    pathsToAggregate.put(key, r);
                    long lastModified = r.lastModified();
                    if (filesDates < lastModified) {
                        filesDates = lastModified;
                    }
                } else {
                    // CSS has options - will not be aggregated and added at the end
                    break;
                }
            }
            if (!pathsToAggregate.isEmpty()) {

                String aggregatedKey = generateAggregateName(pathsToAggregate.keySet());

                String minifiedAggregatedFileName = aggregatedKey + ".min." + type;
                String minifiedAggregatedRealPath = getFileSystemPath(minifiedAggregatedFileName);
                File minifiedAggregatedFile = new File(minifiedAggregatedRealPath);

                String minifiedAggregatedPath = GENERATED_RESOURCES_URL_PATH + minifiedAggregatedFileName;
                if (addLastModifiedDate) {
                    minifiedAggregatedPath += "?" + filesDates;
                }

                if (!minifiedAggregatedFile.exists() || minifiedAggregatedFile.lastModified() < filesDates) {
                    generatedResourcesFolder.mkdirs();

                    // aggregate minified resources

                    LinkedHashMap<String,String> minifiedFileNames = new LinkedHashMap<String, String>();
                    for (Map.Entry<String, org.springframework.core.io.Resource> entry : pathsToAggregate.entrySet()) {
                        final String path = entry.getKey();
                        String minifiedFileName = Patterns.SLASH.matcher(path).replaceAll("_") + ".min." + type;
                        final File minifiedFile = new File(getFileSystemPath(minifiedFileName));
                        final org.springframework.core.io.Resource f = entry.getValue();
                        if (!minifiedFile.exists() || minifiedFile.lastModified() < f.lastModified()) {
                            // minify the file
                            Reader reader = null;
                            Writer writer = null;
                            File tmpMinifiedFile = new File(minifiedFile.getParentFile(), minifiedFile.getName() + "." + System.nanoTime());
                            try {
                                reader = new InputStreamReader(f.getInputStream(), "UTF-8");
                                writer = new OutputStreamWriter(new FileOutputStream(tmpMinifiedFile), "UTF-8");
                                boolean compress = true;
                                if (compress && type.equals("css")) {
                                    String s = IOUtils.toString(reader);
                                    IOUtils.closeQuietly(reader);
                                    if (s.indexOf("url(") != -1) {
                                        String url = StringUtils.substringBeforeLast(path, "/") + "/";
                                        s = URL_PATTERN_1.matcher(s).replaceAll("url(");
                                        s = URL_PATTERN_2.matcher(s).replaceAll("url(\".." + url);
                                        s = URL_PATTERN_3.matcher(s).replaceAll("url('.." + url);
                                        s = URL_PATTERN_4.matcher(s).replaceAll("url(.." + url);
                                    }

                                    reader = new StringReader(s);

                                    CssCompressor compressor = new CssCompressor(reader);
                                    compressor.compress(writer, -1);
                                } else if (compress && type.equals("js")) {
                                    JavaScriptCompressor compressor = null;
                                    try {
                                        compressor = new JavaScriptCompressor(reader, new ErrorReporter() {
                                            public void warning(String message, String sourceName,
                                                                int line, String lineSource, int lineOffset) {
                                                if (!logger.isDebugEnabled()) {
                                                    return;
                                                }
                                                if (line < 0) {
                                                    logger.debug(message);
                                                } else {
                                                    logger.debug(line + ":" + lineOffset + ":" + message);
                                                }
                                            }

                                            public void error(String message, String sourceName,
                                                              int line, String lineSource, int lineOffset) {
                                                if (line < 0) {
                                                    logger.error(message);
                                                } else {
                                                    logger.error(line + ":" + lineOffset + ":" + message);
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
                                        reader = new InputStreamReader(f.getInputStream(), "UTF-8");
                                        writer = new OutputStreamWriter(new FileOutputStream(tmpMinifiedFile), "UTF-8");
                                        IOUtils.copy(reader, writer);
                                    }
                                } else {
                                    BufferedWriter bw = new BufferedWriter(writer);
                                    BufferedReader br = new BufferedReader(reader);
                                    String s = null;
                                    while ((s = br.readLine()) != null) {
                                        bw.write(s);
                                        bw.write("\n");
                                    }
                                    IOUtils.closeQuietly(bw);
                                    IOUtils.closeQuietly(br);
                                }
                            } finally {
                                IOUtils.closeQuietly(reader);
                                IOUtils.closeQuietly(writer);
                                atomicMove(tmpMinifiedFile, minifiedFile);
                            }
                        }
                        minifiedFileNames.put(path, minifiedFileName);
                    }

                    try {
                        File tmpMinifiedAggregatedFile = new File(minifiedAggregatedFile.getParentFile(), minifiedAggregatedFile.getName() + "." + System.nanoTime());
                        OutputStream outMerged = new BufferedOutputStream(new FileOutputStream(tmpMinifiedAggregatedFile));
                        InputStream is = null;
                        try {
                            for (Map.Entry<String, String> entry : minifiedFileNames.entrySet()) {
                                if (type.equals("js")) {
                                    outMerged.write("//".getBytes());
                                    outMerged.write(entry.getValue().getBytes());
                                    outMerged.write("\n".getBytes());
                                }
                                is = new FileInputStream(getFileSystemPath(entry.getValue()));
                                IOUtils.copy(is, outMerged);
                                if (type.equals("js")) {
                                    outMerged.write(";\n".getBytes());
                                }
                                IOUtils.closeQuietly(is);
                            }
                        } finally {
                            IOUtils.closeQuietly(outMerged);
                            IOUtils.closeQuietly(is);
                            atomicMove(tmpMinifiedAggregatedFile, minifiedAggregatedFile);
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                String ctx = Jahia.getContextPath();
                newCss.put(ctx.length() > 0 ?  (ctx + minifiedAggregatedPath) : minifiedAggregatedPath, new HashMap<String, String>());
            }
            if (i < entries.size()) {
                org.springframework.core.io.Resource r;
                if (addLastModifiedDate && ((r = getResource(getKey(entries.get(i).getKey()))) != null)) {
                    newCss.put(entries.get(i).getKey() + "?lastModified=" + r.lastModified(), entries.get(i).getValue());
                } else {
                    newCss.put(entries.get(i).getKey(), entries.get(i).getValue());
                }
                i++;
            }
        }
        return newCss;
    }

    private String getKey(String key) {
        if(Jahia.getContextPath().length() > 0 && key.startsWith(jahiaContext)) {
            key = key.substring(Jahia.getContextPath().length());
        }
        return key;
    }

    private org.springframework.core.io.Resource getResource(String key) {
        org.springframework.core.io.Resource r = null;

        String filePath = StringUtils.substringAfter(key.substring(1), "/");
        String moduleId = StringUtils.substringBefore(filePath, "/");
        filePath = StringUtils.substringAfter(filePath, "/");

        if (key.startsWith("/modules/")) {
            r = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(moduleId).getResource(filePath);
        } else if (key.startsWith("/files/")) {
            r = getResourceFromFile(moduleId, "/" + filePath);
        }
        return r;
    }

    private String getFileSystemPath(String minifiedAggregatedFileName) {
        return SettingsBean.getInstance().getJahiaGeneratedResourcesDiskPath() + File.separator + minifiedAggregatedFileName;
    }

    private org.springframework.core.io.Resource getResourceFromFile(String workspace, final String fFilePath) {
        try {
            final JCRNodeWrapper contentNode = JCRSessionFactory.getInstance().getCurrentUserSession(workspace).getNode(fFilePath);
            return new org.springframework.core.io.Resource() {
                @Override
                public boolean exists() {
                    return true;
                }

                @Override
                public boolean isReadable() {
                    return false;
                }

                @Override
                public boolean isOpen() {
                    return false;
                }

                @Override
                public URL getURL() throws IOException {
                    return null;
                }

                @Override
                public URI getURI() throws IOException {
                    return null;
                }

                @Override
                public File getFile() throws IOException {
                    return null;
                }

                @Override
                public long contentLength() throws IOException {
                    return contentNode.getFileContent().getContentLength();
                }

                @Override
                public long lastModified() throws IOException {
                    return contentNode.getLastModifiedAsDate().getTime();
                }

                @Override
                public org.springframework.core.io.Resource createRelative(String relativePath) throws IOException {
                    return null;
                }

                @Override
                public String getFilename() {
                    return contentNode.getName();
                }

                @Override
                public String getDescription() {
                    return null;
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return contentNode.getFileContent().downloadFile();
                }
            };
        } catch (RepositoryException e) {
            // Cannot get resource
        }
        return null;
    }

    private String generateAggregateName(Collection<String> m) {
        final StringBuilder sb = new StringBuilder(m.size() * 128);
        for (String s1 : m) {
            sb.append(s1);
        }
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] digest = digester.digest(sb.toString().getBytes("UTF-8"));
            final StringBuilder hexString = new StringBuilder(digest.length * 2);
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

    public void afterPropertiesSet() throws Exception {
        jahiaContext = Jahia.getContextPath() + "/";
        generatedResourcesFolder = new File(SettingsBean.getInstance().getJahiaGeneratedResourcesDiskPath());
    }

    public Set<String> getIeHeaderRecognitions() {
        return ieHeaderRecognitions;
    }

    public void setIeHeaderRecognitions(Set<String> ieHeaderRecognitions) {
        this.ieHeaderRecognitions = ieHeaderRecognitions;
    }

    public void setCkeditorJavaScript(String ckeditorJavaScript) {
        this.ckeditorJavaScript = ckeditorJavaScript;
    }

    public void setAddLastModifiedDate(boolean addLastModifiedDate) {
        this.addLastModifiedDate = addLastModifiedDate;
    }

    public void setForceLiveIEcompatiblity(boolean forceLiveIEcompatiblity) {
        this.forceLiveIEcompatiblity = forceLiveIEcompatiblity;
    }
}
