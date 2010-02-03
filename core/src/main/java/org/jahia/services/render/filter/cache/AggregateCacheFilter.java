/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Limited. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import net.htmlparser.jericho.*;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.BlockingCache;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Module content caching filter.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter {
    private transient static Logger logger = Logger.getLogger(CacheFilter.class);
    private ModuleCacheProvider cacheProvider;

    public static final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile("<!-- cache:include src=\\\"(.*)\\\" -->");
    public static final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");
    private static final Pattern CLEANUP_REGEXP = Pattern.compile("<!-- cache:include src=\\\"(.*)\\\" -->\n|\n<!-- /cache:include -->");

    public static final Map<String, String> notCacheableFragment = new HashMap<String, String>(512);

    @Override
    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        if (renderContext.isLiveMode()) {
            final Script script = (Script) renderContext.getRequest().getAttribute("script");
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(script.getTemplate().getProperties().getProperty("cache.perUser","false")));

            boolean debugEnabled = logger.isDebugEnabled();
            boolean displayCacheInfo = Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
            String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);

            if (debugEnabled) {
                logger.debug("Cache filter for key " + key);
            }
            Element element = null;
            final BlockingCache cache = cacheProvider.getCache();
            final boolean cacheable = !notCacheableFragment.containsKey(key);
            String perUserKey = key.replaceAll("_perUser_", renderContext.getUser().getUsername());
            if (cacheable) {
                try {
                    element = cache.get(perUserKey);
                } catch (LockTimeoutException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
            if (element != null && element.getValue() != null) {
                if (debugEnabled) {
                    logger.debug("Getting content from cache for node : " + key);
                }
                String cachedContent = (String) ((CacheEntry) element.getValue()).getObject();
                cachedContent = aggregateContent(cache, cachedContent, renderContext);

                if (renderContext.getMainResource() == resource) {
                    cachedContent = removeEsiTags(cachedContent);
                }
                
                if (displayCacheInfo && !cachedContent.contains("<body") && cachedContent.trim().length() > 0) {
                    return appendDebugInformation(renderContext, key, cachedContent, element);
                } else {
                    return cachedContent;
                }
            } else {
                if (debugEnabled) {
                    logger.debug("Generating content for node : " + key);
                }
                String renderContent = chain.doFilter(renderContext, resource);

                resource.getDependencies().add(resource.getNode());

                if (cacheable) {
                    String cacheAttribute = (String) renderContext.getRequest().getAttribute("expiration");
                    Long expiration = cacheAttribute != null ? Long.valueOf(cacheAttribute) : Long.valueOf(
                            script.getTemplate().getProperties().getProperty("cache.expiration", "-1"));
                    final String currentPath = cacheProvider.getKeyGenerator().getPath(key);
                    Set<JCRNodeWrapper> depNodeWrappers = resource.getDependencies();
                    for (JCRNodeWrapper nodeWrapper : depNodeWrappers) {
                        String path = nodeWrapper.getPath();
                        Element element1 = cacheProvider.getDependenciesCache().get(path);
                        Set<String> dependencies;
                        if (element1 != null) {
                            dependencies = (Set<String>) element1.getValue();
                        } else {
                            dependencies = new LinkedHashSet<String>();
                        }
                        dependencies.add(key);
                        cacheProvider.getDependenciesCache().put(new Element(path, dependencies));
                    }
                    // append cache:include tag
                    String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(renderContent).replaceAll("</esi:include>");
                    cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll("<esi:include src=\"$1\">");
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
                    Element cachedElement = new Element(perUserKey, cacheEntry);
                    if (expiration > 0) {
                        cachedElement.setTimeToLive(expiration.intValue() + 1);
                        final String hiddenKey = cacheProvider.getKeyGenerator().replaceField(perUserKey, "template",
                                                                                              "hidden.load");
                        Element hiddenElement = cache.isKeyInCache(hiddenKey) ? cache.get(hiddenKey) : null;
                        if (hiddenElement != null) {
                            hiddenElement.setTimeToLive(expiration.intValue() + 1);
                            cache.put(hiddenElement);
                        }
                    }
                    if (expiration != 0) {
                        cache.put(cachedElement);
                    } else {
                        cachedElement = new Element(key, null);
                        cache.put(cachedElement);
                        notCacheableFragment.put(key, key);
                    }
                    if (debugEnabled) {
                        logger.debug("Caching content for node : " + key);
                        StringBuilder stringBuilder = new StringBuilder();
                        for (JCRNodeWrapper nodeWrapper : depNodeWrappers) {
                            stringBuilder.append(nodeWrapper.getPath()).append("\n");
                        }
                        logger.debug("Dependencies of " + key + " : \n" + stringBuilder.toString());
                    }
                    if (displayCacheInfo && !renderContent.contains("<body") && renderContent.trim().length() > 0) {
                        return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, renderContent),
                                                      cachedElement);
                    }
                }
                if (displayCacheInfo && !renderContent.contains("<body") && renderContent.trim().length() > 0) {
                        return appendDebugInformation(renderContext, key, surroundWithCacheTag(key, renderContent),
                                                      null);
                }
                if (renderContext.getMainResource() == resource) {
                    return removeEsiTags(renderContent);
                } else {
                    return surroundWithCacheTag(key, renderContent);
                }
            }
        }
        return chain.doFilter(renderContext, resource);
    }

    private String aggregateContent(BlockingCache cache, String cachedContent, RenderContext renderContext) {
        // aggregate content
        Source htmlContent = new Source(cachedContent);
        List<? extends Tag> esiIncludeTags = htmlContent.getAllStartTags("esi:include");
        if (esiIncludeTags.size() > 0) {
            OutputDocument outputDocument = new OutputDocument(htmlContent);
            for (Tag esiIncludeTag : esiIncludeTags) {
                StartTag segment = (StartTag) esiIncludeTag;
                String cacheKey = segment.getAttributeValue("src").replaceAll("_perUser_",renderContext.getUser().getUsername());
                if (cache.isKeyInCache(cacheKey)) {
                    final Element element = cache.get(cacheKey);
                    if (element != null && element.getValue() != null) {
                        String content = (String) ((CacheEntry) element.getValue()).getObject();
                        outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                               aggregateContent(cache, content, renderContext));
                    } else {
                        cache.put(new Element(cacheKey,null));
                        logger.debug("Missing content : "+cacheKey);
                        generateContent(renderContext, outputDocument, segment, cacheKey);
                    }
                } else {
                    logger.debug("Missing content : "+cacheKey);
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
                    "workspace"), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get(
                    "language")),renderContext.getFallbackLocale()).getNode(keyAttrbs.get("path"));
            String content = RenderService.getInstance().render(new Resource(node, keyAttrbs.get(
                    "templateType"), keyAttrbs.get("template"), keyAttrbs.get("template")), renderContext);
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), content);
        } catch (PathNotFoundException e) {
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), "");
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

    private String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                          Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Key: </span><span>");
        stringBuilder.append(key);
        stringBuilder.append("</span><br/>");
        if(cachedElement!=null && cachedElement.getValue()!=null) {
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment has been created at: </span><span>");
        stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(cachedElement.getCreationTime())));
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

    private static String removeEsiTags(String content) {
        return StringUtils.isNotEmpty(content) ? CLEANUP_REGEXP.matcher(content).replaceAll("") : content;
    }
}