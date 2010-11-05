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

package org.jahia.services.render.filter.cache;

import net.htmlparser.jericho.*;
import net.sf.ehcache.*;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Module content caching filter.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateCacheFilter.class);
    private ModuleCacheProvider cacheProvider;
    private PageGeneratorQueue generatorQueue;

    public static final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->");
    public static final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");
    private static final Pattern CLEANUP_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->\n|\n<!-- /cache:include -->");

    public static final Set<String> notCacheableFragment = new HashSet<String>(512);
    private static final String RESOURCES = "resources";
    private static final String TEMPLATE = "template";

    public static void clearNotCacheableFragmentCache() {
        notCacheableFragment.clear();
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<String>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        if (script != null) {
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));

            if (Boolean.valueOf(script.getTemplate().getProperties().getProperty(
                    "cache.additional.key.useMainResourcePath", "false"))) {
                resource.getModuleParams().put("module.cache.additional.key",
                        renderContext.getMainResource().getNode().getPath());
            }
        }
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);

        if (debugEnabled) {
            logger.debug("Cache filter for key " + key);
        }
        Element element = null;
        final Cache cache = cacheProvider.getCache();
        final boolean cacheable = !notCacheableFragment.contains(key);
        String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",
                renderContext.getMainResource().getNode().getPath() + renderContext.getMainResource().getTemplate());
        if (cacheable) {
            try {
                if (debugEnabled) {
                    logger.debug("Try to get content from cache for node with key: " + perUserKey);
                }
                element = cache.get(perUserKey);
            } catch (LockTimeoutException e) {
                logger.warn("Error while rendering " + renderContext.getMainResource() + e.getMessage(), e);
            }
        }
        if (element != null && element.getValue() != null) {
            return returnFromCache(renderContext, resource, debugEnabled, displayCacheInfo, servedFromCache, key,
                    element, cache, perUserKey);
        } else {
            if (cacheable) {
                // Use CountLatch as not found in cache
                CountDownLatch countDownLatch = avoidParallelProcessingOfSamePage(perUserKey);
                if (countDownLatch == null) {
                    element = cache.get(perUserKey);
                    if (element != null && element.getValue() != null) {
                        return returnFromCache(renderContext, resource, debugEnabled, displayCacheInfo, servedFromCache,
                                key, element, cache, perUserKey);
                    }
                }
            }
            return null;
        }
    }

    private String returnFromCache(RenderContext renderContext, Resource resource, boolean debugEnabled,
                                   boolean displayCacheInfo, Set<String> servedFromCache, String key, Element element,
                                   Cache cache, String perUserKey) {
        if (debugEnabled) {
            logger.debug("Content retrieved from cache for node with key: " + perUserKey);
        }
        String cachedContent = (String) ((CacheEntry) element.getValue()).getObject();
        cachedContent = aggregateContent(cache, cachedContent, renderContext);

        if (renderContext.getMainResource() == resource) {
            cachedContent = removeEsiTags(cachedContent);
        }
        servedFromCache.add(key);
        if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
            return appendDebugInformation(renderContext, key, cachedContent, element);
        } else {
            return cachedContent;
        }
    }

    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        if (script != null) {
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));
        }
        resource.getDependencies().add(resource.getNode().getPath());
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache.contains(key)) {
            return previousOut;
        }
        if (key.contains("_mr_")) {
            resource.getDependencies().add(renderContext.getMainResource().getNode().getPath());
            if(script!=null && Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.mainResource.flushParent", "false"))) {
                resource.getDependencies().add(renderContext.getMainResource().getNode().getParent().getPath());
            }
        }
        final boolean cacheable = !notCacheableFragment.contains(key);
        final Cache cache = cacheProvider.getCache();
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",renderContext.getMainResource().getNode().getPath()+renderContext.getMainResource().getTemplate());
        if (debugEnabled) {
            logger.debug("Generating content for node : " + perUserKey);
        }
        try {
            if (cacheable) {
                String cacheAttribute = (String) renderContext.getRequest().getAttribute("expiration");
                Long expiration = cacheAttribute != null ? Long.valueOf(cacheAttribute) : Long.valueOf(
                        script != null ? script.getTemplate().getProperties().getProperty("cache.expiration",
                                "-1") : "-1");
                final Cache dependenciesCache = cacheProvider.getDependenciesCache();
                Set<String> depNodeWrappers = resource.getDependencies();
                for (String path : depNodeWrappers) {

                    Element element1 = dependenciesCache.get(path);
                    Set<String> dependencies;
                    if (element1 != null) {
                        dependencies = (Set<String>) element1.getValue();
                    } else {
                        dependencies = new LinkedHashSet<String>();
                    }
                    dependencies.add(perUserKey);
                    dependenciesCache.put(new Element(path, dependencies));
                }
                // append cache:include tag
                String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(previousOut).replaceAll(
                        "</esi:include>");
                cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll(
                        "<esi:include src=\"$1\">");
                if (debugEnabled) {
                    logger.debug("Storing for key: " + key + " content:\n" + cachedRenderContent);
                }
                Source source = new Source(cachedRenderContent);
                // This will remove all blank line and drastically reduce data in memory
                source = new Source((new SourceFormatter(source)).toString());
                List<StartTag> esiIncludeTags = source.getAllStartTags("esi:include");
                if (debugEnabled) {
                    displaySegments(esiIncludeTags);
                }
                // We will remove container content here has we do not want to store them twice in memory
                OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);
                final String output = outputDocument.toString();
                cachedRenderContent = surroundWithCacheTag(key, output);
                CacheEntry<String> cacheEntry = new CacheEntry<String>(cachedRenderContent);
                cacheEntry.setProperty(RESOURCES, renderContext.getStaticAssets());
                cacheEntry.setProperty(TEMPLATE, renderContext.getRequest().getAttribute("previousTemplate"));
                Element cachedElement = new Element(perUserKey, cacheEntry);
                if (expiration > 0) {
                    cachedElement.setTimeToLive(expiration.intValue() + 1);
                    String hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template",
                            "hidden.load");
                    Element hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                    if (hiddenElement != null) {
                        hiddenElement.setTimeToLive(expiration.intValue() + 1);
                        cache.put(hiddenElement);
                    }
                    hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template", "hidden.footer");
                    hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                    if (hiddenElement != null) {
                        hiddenElement.setTimeToLive(expiration.intValue() + 1);
                        cache.put(hiddenElement);
                    }
                    hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template", "hidden.header");
                    hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                    if (hiddenElement != null) {
                        hiddenElement.setTimeToLive(expiration.intValue() + 1);
                        cache.put(hiddenElement);
                    }
                }
                if (expiration != 0) {
                    cache.put(cachedElement);
                } else {
                    cachedElement = new Element(perUserKey, null);
                    cache.put(cachedElement);
                    notCacheableFragment.add(key);
                }
                if (debugEnabled) {
                    logger.debug("Store in cache content of node with key: " + perUserKey);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String path : depNodeWrappers) {
                        stringBuilder.append(path).append("\n");
                    }
                    logger.debug("Dependencies of " + perUserKey + " : \n" + stringBuilder.toString());
                }
                if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
                    return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, previousOut),
                            cachedElement);
                }
            }
            if (displayCacheInfo && !previousOut.contains("<body") && previousOut.trim().length() > 0) {
                return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, previousOut), null);
            }
            if (renderContext.getMainResource() == resource) {
                return removeEsiTags(previousOut);
            } else {
                return surroundWithCacheTag(key, previousOut);
            }
        } catch (Exception e) {
            cache.put(new Element(perUserKey, null));
            throw e;
        }
    }

    private String aggregateContent(Cache cache, String cachedContent, RenderContext renderContext) {
        // aggregate content
        Source htmlContent = new Source(cachedContent);
        List<? extends Tag> esiIncludeTags = htmlContent.getAllStartTags("esi:include");
        if (esiIncludeTags.size() > 0) {
            OutputDocument outputDocument = new OutputDocument(htmlContent);
            for (Tag esiIncludeTag : esiIncludeTags) {
                StartTag segment = (StartTag) esiIncludeTag;
                if(logger.isDebugEnabled()){
                    logger.debug(segment.toString());
                }
                String cacheKey = segment.getAttributeValue("src").replaceAll("_perUser_",
                                                                              renderContext.getUser().getUsername());
                String mrCacheKey = cacheKey.replaceAll("_mr_",renderContext.getMainResource().getNode().getPath()+renderContext.getMainResource().getTemplate());
                logger.debug("Check if " + cacheKey + " is in cache");
                if (cache.isKeyInCache(mrCacheKey)) {
                    final Element element = cache.get(mrCacheKey);
                    if (element != null && element.getValue() != null) {
                        logger.debug(cacheKey + " has been found in cache");
                        final CacheEntry cacheEntry = (CacheEntry) element.getValue();
                        String content = (String) cacheEntry.getObject();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Document replace from : "+segment.getStartTagType()+" to "+segment.getElement().getEndTag().getEndTagType()+ " with "+content);
                        }
                        if(!cachedContent.equals(content)) {
                            String aggregatedContent = aggregateContent(cache, content, renderContext);
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                aggregatedContent);
                        } else {
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                content);
                        }
                        if (cacheEntry.getProperty(RESOURCES) != null) {
                            renderContext.addStaticAsset((Map<String, Set<String>>) cacheEntry.getProperty(RESOURCES));
                        }
                        if (cacheEntry.getProperty(TEMPLATE) != null) {
                            renderContext.getRequest().setAttribute("previousTemplate", cacheEntry.getProperty(TEMPLATE));
                        }
                        else {logger.warn("resource not found"); }
                    } else {
                        cache.put(new Element(mrCacheKey, null));
                        logger.debug("Missing content : " + cacheKey);
                        generateContent(renderContext, outputDocument, segment, cacheKey);
                    }
                } else {
                    logger.debug("Missing content : " + mrCacheKey);
                    generateContent(renderContext, outputDocument, segment, cacheKey);

                }
            }
            return outputDocument.toString();
        }
        return cachedContent;
    }

    private void generateContent(RenderContext renderContext, OutputDocument outputDocument, StartTag segment,
                                 String cacheKey) {
        // if missing data call RenderService after creating the right resource
        final CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
        try {
            Map<String, String> keyAttrbs = cacheKeyGenerator.parse(cacheKey);
            final JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession(keyAttrbs.get(
                    "workspace"), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                                                                                              renderContext.getFallbackLocale()).getNode(
                    keyAttrbs.get("path"));

            renderContext.getRequest().removeAttribute("areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));

            String content = RenderService.getInstance().render(new Resource(node, keyAttrbs.get("templateType"),
                                                                             keyAttrbs.get("template"),
                                                                             Resource.CONFIGURATION_MODULE),
                                                                renderContext);
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), content);
        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String surroundWithCacheTag(String key, String output) {
        String cachedRenderContent;
        StringBuilder builder = new StringBuilder();
        builder.append("<!-- cache:include src=\"");
        builder.append(key);
        builder.append("\" -->\n");
        builder.append(output);
        builder.append("\n<!-- /cache:include -->");
        cachedRenderContent = builder.toString();
        return cachedRenderContent;
    }

    public void setCacheProvider(ModuleCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    public void setGeneratorQueue(PageGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    private String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                          Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Key: </span><span>");
        stringBuilder.append(key);
        stringBuilder.append("</span><br/>");
        if (cachedElement != null && cachedElement.getValue() != null) {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment has been created at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getCreationTime())));
            stringBuilder.append("</span><br/>");
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment will expire at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getExpirationTime())));
            stringBuilder.append("</span>");
            stringBuilder.append("<form action=\"").append(renderContext.getURLGenerator().getContext()).append(
                    "/flushKey.jsp\" method=\"post\"");
            stringBuilder.append("<input type=\"hidden\" name=\"keyToFlush\" value=\"").append(key).append("\"");
            stringBuilder.append("<button type=\"submit\"title=\"Flush it\">Flush It</button>");
            stringBuilder.append("</form>");
        } else {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment Not Cacheable</span><br/>");
        }
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }

    private static void displaySegments(Iterable<StartTag> segments) {
        for (StartTag segment : segments) {
            System.out.println("\n-------------------------------------------------------------------------------");
            System.out.println(segment.getDebugInfo());
            System.out.println(segment.getElement().getContent());
            System.out.println(segment.getAttributeValue("src"));
            System.out.println(segment.isUnregistered());
        }
        System.out.println("*******************************************************************************\n");
    }

    private static OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : segments) {
            outputDocument.replace(segment.getElement().getContent(), "");
        }
        return outputDocument;
    }

    public static String removeEsiTags(String content) {
        return StringUtils.isNotEmpty(content) ? CLEANUP_REGEXP.matcher(content).replaceAll("") : content;
    }

    @Override
    public void handleError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.handleError(renderContext, resource, chain, e);
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        if (script != null) {
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));

            if (Boolean.valueOf(script.getTemplate().getProperties().getProperty(
                    "cache.additional.key.useMainResourcePath", "false"))) {
                resource.getModuleParams().put("module.cache.additional.key",
                        renderContext.getMainResource().getNode().getPath());
            }
        }
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);

        final Cache cache = cacheProvider.getCache();
        String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",
                renderContext.getMainResource().getNode().getPath() + renderContext.getMainResource().getTemplate());
        cache.put(new Element(perUserKey,null));
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        if (script != null) {
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.perUser", "false")));
            chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                    script.getTemplate().getProperties().getProperty("cache.mainResource", "false")));

            if (Boolean.valueOf(script.getTemplate().getProperties().getProperty(
                    "cache.additional.key.useMainResourcePath", "false"))) {
                resource.getModuleParams().put("module.cache.additional.key",
                        renderContext.getMainResource().getNode().getPath());
            }
        }
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
        String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername()).replaceAll("_mr_",
                renderContext.getMainResource().getNode().getPath() + renderContext.getMainResource().getTemplate());
        Map<String, CountDownLatch> countDownLatchMap = generatorQueue.getGeneratingPages();
        synchronized (countDownLatchMap) {
            CountDownLatch countDownLatch = countDownLatchMap.get(perUserKey);
            if (countDownLatch != null) {
                generatorQueue.getGeneratingPages().remove(perUserKey);
                countDownLatch.countDown();
            }
        }
    }

    private CountDownLatch avoidParallelProcessingOfSamePage(String key) throws Exception {
        CountDownLatch latch = null;
        boolean mustWait = true;
        Map<String, CountDownLatch> generatingPages = generatorQueue.getGeneratingPages();
        synchronized (generatingPages) {
            latch = (CountDownLatch) generatingPages.get(key);
            if (latch == null) {
                latch = new CountDownLatch(1);
                generatingPages.put(key, latch);
                mustWait = false;
            }
        }
        if (mustWait) {
            try {
                if (!latch.await(generatorQueue
                        .getPageGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    throw new Exception("Server is overloaded");
                }
                latch = null;
            } catch (InterruptedException ie) {
                logger.debug("The waiting thread has been interrupted :", ie);
                throw new Exception(ie);
            }
        }
        return latch;
    }
}