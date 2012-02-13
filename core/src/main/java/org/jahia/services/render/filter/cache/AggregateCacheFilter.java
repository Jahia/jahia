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

package org.jahia.services.render.filter.cache;

import static org.jahia.api.Constants.JAHIA_REFERENCE_IN_FIELD_PREFIX;
import static org.jahia.api.Constants.JAHIAMIX_REFERENCES_IN_FIELD;

import net.htmlparser.jericho.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.LockTimeoutException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Template;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.jvm.ThreadMonitor;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Module content caching filter.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 8 janv. 2010
 */
public class AggregateCacheFilter extends AbstractFilter implements ApplicationListener<ApplicationEvent>, InitializingBean {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateCacheFilter.class);
    private ModuleCacheProvider cacheProvider;
    private ModuleGeneratorQueue generatorQueue;

    public static final Pattern ESI_INCLUDE_STARTTAG_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->");
    public static final Pattern ESI_INCLUDE_STOPTAG_REGEXP = Pattern.compile("<!-- /cache:include -->");
    private static final Pattern CLEANUP_REGEXP = Pattern.compile(
            "<!-- cache:include src=\\\"(.*)\\\" -->\n|\n<!-- /cache:include -->");
    private static final Pattern QUERYSTRING_REGEXP = Pattern.compile("(.*)(_qs\\[([^\\]]+)\\]_)(.*)");

    public static final Set<String> notCacheableFragment = new HashSet<String>(512);

    static private ThreadLocal<Set<CountDownLatch>> processingLatches = new ThreadLocal<Set<CountDownLatch>>();
    static private ThreadLocal<String> acquiredSemaphore = new ThreadLocal<String>();
    static private ThreadLocal<LinkedList<String>> userKeys = new ThreadLocal<LinkedList<String>>();
    private static long lastThreadDumpTime = 0L;
    private Byte[] threadDumpCheckLock = new Byte[0];
    public static final String FORM_TOKEN = "form_token";
    private Map<String,String> moduleParamsProperties;

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache == null) {
            servedFromCache = new HashSet<String>();
            renderContext.getRequest().setAttribute("servedFromCache", servedFromCache);
        }
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        boolean isBound = resource.getNode().isNodeType("jmix:bindedComponent");
        if (script != null) {
            Properties scriptProperties = script.getView().getProperties();
            Properties defaultScriptProperties = script.getView().getDefaultProperties();
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    scriptProperties.getProperty("cache.perUser")!=null?scriptProperties.getProperty("cache.perUser"):defaultScriptProperties.getProperty("cache.perUser", "false")));
            if(isBound) {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.TRUE);
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.valueOf(
                        scriptProperties.getProperty("cache.mainResource")!=null?scriptProperties.getProperty("cache.mainResource"):defaultScriptProperties.getProperty("cache.mainResource", "false")));
            }

            String requestParameters = scriptProperties.getProperty("cache.requestParameters");
            if (requestParameters == null) {
                requestParameters = defaultScriptProperties.getProperty("cache.requestParameters");
            }
            if (requestParameters != null && !"".equals(requestParameters.trim())) {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", requestParameters.split(","));
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", null);
            }
            if (Boolean.valueOf(scriptProperties.getProperty("cache.additional.key.useMainResourcePath")!=null?
                                scriptProperties.getProperty("cache.additional.key.useMainResourcePath"):defaultScriptProperties.getProperty("cache.additional.key.useMainResourcePath", "false"))) {

                ArrayList l = (ArrayList) resource.getModuleParams().get("module.cache.additional.key");
                if (l == null) {
                    l = new ArrayList();
                    resource.getModuleParams().put("module.cache.additional.key",l);
                }
                l.add(renderContext.getMainResource().getNode().getCanonicalPath());
            }
        }
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);

        if (debugEnabled) {
            logger.debug("Cache filter for key {}", key);
        }
        Element element = null;
        final Cache cache = cacheProvider.getCache();
        boolean cacheable = !notCacheableFragment.contains(key);
        if(renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v")!=null){
            cacheable = false;
        }
        if (renderContext.getRequest().getParameter("ec") != null && renderContext.getRequest().getParameter(
                "ec").equals(resource.getNode().getIdentifier())) {
            cacheable = false;
        }
        String perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList == null) {
            userKeysLinkedList = new LinkedList<String>();
            userKeys.set(userKeysLinkedList);
        }
        if(userKeysLinkedList.contains(perUserKey)) {
            return null;
        }
        userKeysLinkedList.add(0, perUserKey);
        if (cacheable) {
            try {
                if (debugEnabled) {
                    logger.debug("Try to get content from cache for node with key: {}", perUserKey);
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
                CountDownLatch countDownLatch = avoidParallelProcessingOfSameModule(perUserKey,
                        resource.getContextConfiguration(), renderContext.getRequest());
                if (countDownLatch == null) {
                    element = cache.get(perUserKey);
                    if (element != null && element.getValue() != null) {
                        return returnFromCache(renderContext, resource, debugEnabled, displayCacheInfo, servedFromCache,
                                key, element, cache, perUserKey);
                    }
                } else {
                    Set<CountDownLatch> latches = processingLatches.get();
                    if (latches == null) {
                        latches = new HashSet<CountDownLatch>();
                        processingLatches.set(latches);
                    }
                    latches.add(countDownLatch);
                }
            }
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private String returnFromCache(RenderContext renderContext, Resource resource, boolean debugEnabled,
                                   boolean displayCacheInfo, Set<String> servedFromCache, String key, Element element,
                                   Cache cache, String perUserKey) {
        if (debugEnabled) {
            logger.debug("Content retrieved from cache for node with key: {}", perUserKey);
        }
        CacheEntry<?> cacheEntry = (CacheEntry<?>) element.getValue();
        String cachedContent = (String) cacheEntry.getObject();
        cachedContent = aggregateContent(cache, cachedContent, renderContext,
                (Map<String, Serializable>) cacheEntry.getProperty("moduleParams"),(String) cacheEntry.getProperty("areaResource"), new Stack<String>());
        setResources(renderContext, cacheEntry);

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
        boolean isBound = resource.getNode().isNodeType("jmix:bindedComponent");
        final Script script = (Script) renderContext.getRequest().getAttribute("script");
        Properties scriptProperties = null;
        Properties defaultScriptProperties = null;
        if (script != null) {
            scriptProperties = script.getView().getProperties();
            defaultScriptProperties = script.getView().getDefaultProperties();
            chain.pushAttribute(renderContext.getRequest(), "cache.perUser", Boolean.valueOf(
                    scriptProperties.getProperty("cache.perUser")!=null?scriptProperties.getProperty("cache.perUser"):defaultScriptProperties.getProperty("cache.perUser", "false")));
            if(isBound) {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource", Boolean.TRUE);
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.mainResource",
                        Boolean.valueOf(scriptProperties.getProperty("cache.mainResource")!=null?scriptProperties.getProperty("cache.mainResource"):defaultScriptProperties.getProperty("cache.mainResource", "false")));
            }
            String requestParameters = scriptProperties.getProperty("cache.requestParameters");
            if (requestParameters == null) {
                requestParameters = defaultScriptProperties.getProperty("cache.requestParameters");
            }
            if (requestParameters != null && !"".equals(requestParameters.trim())) {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", requestParameters.split(","));
            } else {
                chain.pushAttribute(renderContext.getRequest(), "cache.requestParameters", null);
            }
        }
        resource.getDependencies().add(resource.getNode().getCanonicalPath());
        String key = cacheProvider.getKeyGenerator().generate(resource, renderContext);
        @SuppressWarnings("unchecked")
        Set<String> servedFromCache = (Set<String>) renderContext.getRequest().getAttribute("servedFromCache");
        if (servedFromCache.contains(key)) {
            return previousOut;
        }
        if (key.contains("_mr_")) {
            resource.getDependencies().add(renderContext.getMainResource().getNode().getCanonicalPath());
            if (scriptProperties != null && defaultScriptProperties!=null &&
                Boolean.valueOf(scriptProperties.getProperty("cache.mainResource.flushParent")!=null?scriptProperties.getProperty("cache.mainResource.flushParent"):defaultScriptProperties.getProperty("cache.mainResource.flushParent", "false"))) {
                try {
                    resource.getDependencies().add(renderContext.getMainResource().getNode().getParent().getCanonicalPath());
                } catch (ItemNotFoundException e) {
                }
            }
        }
        boolean cacheable = !notCacheableFragment.contains(key);
        if(renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v")!=null){
            cacheable = false;
        }
        if (renderContext.getRequest().getParameter("ec") != null && renderContext.getRequest().getParameter(
                "ec").equals(resource.getNode().getIdentifier())) {
            cacheable = false;
        }
        final Cache cache = cacheProvider.getCache();
        boolean debugEnabled = logger.isDebugEnabled();
        boolean displayCacheInfo = SettingsBean.getInstance().isDevelopmentMode() && Boolean.valueOf(renderContext.getRequest().getParameter("cacheinfo"));
        String perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
        /*if (Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.dynamicRolesAcls"))) {
            key = cacheProvider.getKeyGenerator().replaceField(key, "acls", "dynamicRolesAcls");
            chain.pushAttribute(renderContext.getRequest(), "cache.dynamicRolesAcls", Boolean.FALSE);
        }*/
        if (debugEnabled) {
            logger.debug("Generating content for node: {}", perUserKey);
        }
        try {
            if (cacheable) {
                addReferencesToDependencies(resource);
                String perUser = (String) renderContext.getRequest().getAttribute("perUser");
                if (perUser != null) {
                    // This content must be cached by user as it is defined in the options panel
                    // The value of the node property from the mixin jmix:cache are only checked in the TemplatesAttributesFilter
                    // So we need to recalculate the key as we were not aware that this content needed to be cached by user
                    // We need to store content with the previously calculate dcache to avoid lock up.
                    cache.put(new Element(perUserKey, null));
                    key = cacheProvider.getKeyGenerator().replaceField(key, "acls", DefaultCacheKeyGenerator.PER_USER);
                    perUserKey = replacePlaceholdersInCacheKey(renderContext, key);
                }
                String cacheAttribute = (String) renderContext.getRequest().getAttribute("expiration");
                Long expiration = cacheAttribute != null ? Long.valueOf(cacheAttribute) : Long.valueOf(
                        scriptProperties != null && defaultScriptProperties!=null ? (scriptProperties.getProperty("cache.expiration")!=null?scriptProperties.getProperty("cache.expiration"):defaultScriptProperties.getProperty("cache.expiration", "-1")) : "-1");
                final Cache dependenciesCache = cacheProvider.getDependenciesCache();
                Set<String> depNodeWrappers = resource.getDependencies();
                for (String path : depNodeWrappers) {
                    Element element1 = dependenciesCache.get(path);
                    Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String>emptySet();
                    Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                    newDependencies.addAll(dependencies);
                    if (newDependencies.add(perUserKey)) {
                        dependenciesCache.put(new Element(path, newDependencies));
                    }
                }
                resource.getDependencies().clear();
                final Cache regexpDependenciesCache = cacheProvider.getRegexpDependenciesCache();
                Set<String> regexpDepNodeWrappers = resource.getRegexpDependencies();
                for (String regexp : regexpDepNodeWrappers) {
                    Element element1 = regexpDependenciesCache.get(regexp);
                    Set<String> dependencies = element1 != null ? (Set<String>) element1.getValue() : Collections.<String> emptySet();
                    Set<String> newDependencies = new LinkedHashSet<String>(dependencies.size() + 1);
                    newDependencies.addAll(dependencies);
                    if (newDependencies.add(perUserKey)) {
                        regexpDependenciesCache.put(new Element(regexp, newDependencies));
                    }
                }
                resource.getRegexpDependencies().clear();

                // append cache:include tag
                String cachedRenderContent = ESI_INCLUDE_STOPTAG_REGEXP.matcher(previousOut).replaceAll(
                        "</esi:include>");
                cachedRenderContent = ESI_INCLUDE_STARTTAG_REGEXP.matcher(cachedRenderContent).replaceAll(
                        "<esi:include src=\"$1\">");

                if (debugEnabled) {
                    logger.debug("Storing for key: {}", key);
                }
                Source source = new Source(cachedRenderContent);
                // This will remove all blank line and drastically reduce data in memory
//                source = new Source((new SourceFormatter(source)).toString());
                List<StartTag> esiIncludeTags = source.getAllStartTags("esi:include");
                /*if (debugEnabled) {
                    displaySegments(esiIncludeTags);
                }*/
                // We will remove container content here has we do not want to store them twice in memory
                OutputDocument outputDocument = emptyEsiIncludeTagContainer(esiIncludeTags, source);
                String output = outputDocument.toString();
                cachedRenderContent = surroundWithCacheTag(key, output);
                CacheEntry<String> cacheEntry = new CacheEntry<String>(cachedRenderContent);

                if (resource.getFormInputs() != null) {
                    cacheEntry.setProperty(FORM_TOKEN, resource.getFormInputs());
                }
                LinkedHashMap<String, Object> moduleParams=null;
                for (String property : moduleParamsProperties.keySet()) {
                    if (resource.getNode().hasProperty(property)) {
                        if (moduleParams == null) {
                            moduleParams = new LinkedHashMap<String, Object>();
                        }
                        moduleParams.put(moduleParamsProperties.get(property),resource.getNode().getPropertyAsString(property));
                    }
                }
                if(moduleParams!=null && moduleParams.size()>0) {
                    cacheEntry.setProperty("moduleParams", moduleParams);
                }
                if(resource.getNode().isNodeType("jnt:area") || resource.getNode().isNodeType("jnt:mainResourceDisplay")) {
                    cacheEntry.setProperty("areaResource", resource.getNode().getIdentifier());
                }
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
                    logger.debug("Store in cache content of node with key: {}", perUserKey);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String path : depNodeWrappers) {
                        stringBuilder.append(path).append("\n");
                    }
                    logger.debug("Dependencies of {}:\n", perUserKey, stringBuilder.toString());
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

    private String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        Matcher m = QUERYSTRING_REGEXP.matcher(key);
        if (m.matches()) {
            Map parameterMap = renderContext.getRequest().getParameterMap();
            String qsString = m.group(2);
            String[] params = m.group(3).split(",");

            SortedMap<String,String> qs = new TreeMap<String, String>();
            for (String param : params) {
                param = param.trim();
                if (param.endsWith("*")) {
                    param = param.substring(0,param.length()-1);
                    for (Map.Entry o : (Iterable<? extends Map.Entry>) parameterMap.entrySet()) {
                        String k = (String) o.getKey();
                        if (k.startsWith(param)) {
                            qs.put(k, Arrays.toString((String[])o.getValue()));
                        }
                    }
                } else if (parameterMap.containsKey(param)) {
                    qs.put(param, Arrays.toString((String[])parameterMap.get(param)));
                }
            }
            key = key.replace(qsString,  qs.toString());
        }

        return DefaultCacheKeyGenerator.mainResourcePattern.matcher(DefaultCacheKeyGenerator.perUserPattern.matcher(key).replaceAll(
                renderContext.getUser().getUsername())).replaceAll(
                renderContext.getMainResource().getNode().getCanonicalPath() +
                renderContext.getMainResource().getResolvedTemplate());
    }

    /**
     * Checks if the node properties has references to other content items (links in rich text fields) and adds those as dependencies.
     *
     * @param resource
     *            the resource to update dependencies on
     * @throws RepositoryException
     *             in case of a repository error
     */
    private void addReferencesToDependencies(Resource resource) throws RepositoryException {
        if (resource.getNode().isNodeType(JAHIAMIX_REFERENCES_IN_FIELD)) {
            NodeIterator ni = resource.getNode().getNodes(JAHIA_REFERENCE_IN_FIELD_PREFIX);
            while (ni.hasNext()) {
                JCRNodeWrapper ref = (JCRNodeWrapper) ni.nextNode();
                try {
                    resource.getDependencies().add(ref.getProperty("j:reference").getString());
                } catch (PathNotFoundException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("j:reference property is not found on node {}", ref.getCanonicalPath());
                    }
                } catch (Exception e) {
                    logger.warn("Error adding dependency to node " + resource.getNode().getCanonicalPath(), e);
                }
            }
        }
    }

    private String aggregateContent(Cache cache, String cachedContent, RenderContext renderContext, Map<String, Serializable> moduleParams, String areaIdentifier, Stack<String> cacheKeyStack) {
        // aggregate content
        Source htmlContent = new Source(cachedContent);
        List<? extends Tag> esiIncludeTags = htmlContent.getAllStartTags("esi:include");
        if (esiIncludeTags.size() > 0) {
            OutputDocument outputDocument = new OutputDocument(htmlContent);
            for (Tag esiIncludeTag : esiIncludeTags) {
                StartTag segment = (StartTag) esiIncludeTag;
                if (logger.isDebugEnabled()) {
                    logger.debug(segment.toString());
                }
                String cacheKey = segment.getAttributeValue("src");
                CacheKeyGenerator keyGenerator = cacheProvider.getKeyGenerator();
                if (!cacheKey.contains(DefaultCacheKeyGenerator.PER_USER) && keyGenerator instanceof DefaultCacheKeyGenerator) {
                    DefaultCacheKeyGenerator defaultCacheKeyGenerator = (DefaultCacheKeyGenerator) keyGenerator;
                    try {
                        Map<String, String> keyAttrbs = keyGenerator.parse(cacheKey);
                        String[] split = keyAttrbs.get("acls").split("_p_");
                        String nodePath = "/"+StringUtils.substringAfter(split[1],"/");
                        String acls = defaultCacheKeyGenerator.getAclsKeyPart(renderContext, Boolean.parseBoolean(StringUtils.substringBefore(split[1],"/")), nodePath, true);
                        cacheKey = keyGenerator.replaceField(cacheKey, "acls", acls);
                        if (renderContext.getRequest().getParameter("ec") != null &&
                            renderContext.getRequest().getParameter("ec").equals(keyAttrbs.get("resourceID"))) {
                            cacheKey = keyGenerator.replaceField(cacheKey, "queryString",
                                    renderContext.getRequest().getQueryString());
                        }
                        if (renderContext.isLoggedIn() && renderContext.getRequest().getParameter("v") != null) {
                            cacheKey = keyGenerator.replaceField(cacheKey, "queryString", UUID.randomUUID().toString());
                        }
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                    } catch (PathNotFoundException e) {
                        try {
                            cacheKey = keyGenerator.replaceField(cacheKey, "acls", "invalid");
                        } catch (ParseException e1) {
                            logger.error(e1.getMessage(), e1);
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                String mrCacheKey = replacePlaceholdersInCacheKey(renderContext, cacheKey);
                cacheKey = DefaultCacheKeyGenerator.perUserPattern.matcher(cacheKey).replaceAll(renderContext.getUser().getUsername());

                if (logger.isDebugEnabled()) {
                    logger.debug("Check if {} is in cache", mrCacheKey);
                }
                if (cache.isKeyInCache(mrCacheKey)) {
                    final Element element = cache.get(mrCacheKey);
                    if (element != null && element.getValue() != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("{} has been found in cache", mrCacheKey);
                        }
                        @SuppressWarnings("unchecked")
                        final CacheEntry<String> cacheEntry = (CacheEntry<String>) element.getValue();
                        String content = cacheEntry.getObject();
                        /*if (logger.isDebugEnabled()) {
                            logger.debug("Document replace from : " + segment.getStartTagType() + " to " +
                                         segment.getElement().getEndTag().getEndTagType() + " with " + content);
                        }*/

                        if (cacheKeyStack.contains(cacheKey)) {
                            continue;
                        }
                        cacheKeyStack.push(cacheKey);

                        if (!cachedContent.equals(content)) {
                            String aggregatedContent = aggregateContent(cache, content, renderContext,(Map<String, Serializable>) cacheEntry.getProperty("moduleParams"),(String) cacheEntry.getProperty("areaResource"), cacheKeyStack);
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    aggregatedContent);
                        } else {
                            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                                    content);
                        }
                        setResources(renderContext, cacheEntry);

                        cacheKeyStack.pop();
                    } else {
                        cache.put(new Element(mrCacheKey, null));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Missing content: {}", mrCacheKey);
                        }
                        generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams,areaIdentifier);
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Missing content: {}", mrCacheKey);
                    }
                    generateContent(renderContext, outputDocument, segment, cacheKey, moduleParams,areaIdentifier);
                }
            }
            return outputDocument.toString();
        }
        return cachedContent;
    }

    @SuppressWarnings("unchecked")
    private void setResources(RenderContext renderContext, CacheEntry<?> cacheEntry) {
        Object property = cacheEntry.getProperty(FORM_TOKEN);
        if (property != null) {
            Map<String, Map<String, String>> forms = (Map<String, Map<String, String>>) renderContext.getRequest().getAttribute(
                    "form-parameter");
            if (forms == null) {
                forms = new HashMap<String, Map<String, String>>();
                renderContext.getRequest().setAttribute("form-parameter", forms);
            }
            forms.putAll((Map<? extends String, ? extends Map<String, String>>) property);
        }
    }

    private void generateContent(RenderContext renderContext, OutputDocument outputDocument, StartTag segment,
                                 String cacheKey, Map<String, Serializable> moduleParams, String areaIdentifier) {
        // if missing data call RenderService after creating the right resource
        final CacheKeyGenerator cacheKeyGenerator = cacheProvider.getKeyGenerator();
        try {
            Map<String, String> keyAttrbs = cacheKeyGenerator.parse(cacheKey);
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession(keyAttrbs.get(
                    "workspace"), LanguageCodeConverters.languageCodeToLocale(keyAttrbs.get("language")),
                    renderContext.getFallbackLocale());
            JCRNodeWrapper node = null;
            try {
                node = currentUserSession.getNode(keyAttrbs.get("path"));
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Node {} is no longer available." + " Replacing output with empty content.",
                            keyAttrbs.get("path"));
                }
                outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(),
                        StringUtils.EMPTY);
                return;
            }
            if(logger.isDebugEnabled()) {
                logger.debug("Calling render service for generating content for key " + cacheKey + " with attributes : " +
                             new ToStringBuilder(keyAttrbs,ToStringStyle.MULTI_LINE_STYLE)+ "\nmodule params : " +
                             ToStringBuilder.reflectionToString(moduleParams,ToStringStyle.MULTI_LINE_STYLE)+
                             " areaIdentifier "+ areaIdentifier);
            }
            renderContext.getRequest().removeAttribute(
                    "areaNodeTypesRestriction" + renderContext.getRequest().getAttribute("org.jahia.modules.level"));
            Template oldOne = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            String context = keyAttrbs.get("context");
            if(!context.equals("page")) {
                renderContext.getRequest().setAttribute("templateSet", Boolean.TRUE);
            }
            if (!StringUtils.isEmpty(keyAttrbs.get("templateNodes"))) {
                Template templateNodes = new Template(keyAttrbs.get("templateNodes"));
                renderContext.getRequest().setAttribute("previousTemplate", templateNodes);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }

            renderContext.getRequest().setAttribute("skipWrapper", Boolean.TRUE);
            Object oldInArea = (Object) renderContext.getRequest().getAttribute("inArea");
            String inArea = keyAttrbs.get("inArea");
            if (inArea == null || "".equals(inArea)) {
                renderContext.getRequest().removeAttribute("inArea");
            } else {
                renderContext.getRequest().setAttribute("inArea", Boolean.valueOf(inArea));
            }
            if(areaIdentifier!=null) {
                renderContext.getRequest().setAttribute("areaListResource",currentUserSession.getNodeByIdentifier(areaIdentifier));
            }
            Resource resource = new Resource(node, keyAttrbs.get("templateType"), keyAttrbs.get("template"), context);
            if (moduleParams != null) {
                for (Map.Entry<String, Serializable> entry : moduleParams.entrySet()) {
                    resource.getModuleParams().put(entry.getKey(), entry.getValue());
                }
            }
            String content = RenderService.getInstance().render(resource, renderContext);
            if(content==null || "".equals(content.trim())) {
                logger.error("Empty generated content for key " + cacheKey + " with attributes : " +
                             new ToStringBuilder(keyAttrbs,ToStringStyle.MULTI_LINE_STYLE)+ "\nmodule params : " +
                             ToStringBuilder.reflectionToString(moduleParams,ToStringStyle.MULTI_LINE_STYLE)+
                             " areaIdentifier "+ areaIdentifier);
            }
            outputDocument.replace(segment.getBegin(), segment.getElement().getEndTag().getEnd(), content);
            if(oldInArea!=null) {
                renderContext.getRequest().setAttribute("inArea", oldInArea);
            } else {
                renderContext.getRequest().removeAttribute("inArea");
            }
            if (oldOne!=null) {
                renderContext.getRequest().setAttribute("previousTemplate", oldOne);
            } else {
                renderContext.getRequest().removeAttribute("previousTemplate");
            }
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

    public void setGeneratorQueue(ModuleGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    private String appendDebugInformation(RenderContext renderContext, String key, String renderContent,
                                          Element cachedElement) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div class=\"cacheDebugInfo\">");
        stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Key: </span><span>");
        stringBuilder.append(key);
        stringBuilder.append("</span><br/>");
        /*if (cachedElement != null && cachedElement.getValue() != null) {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment has been created at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getCreationTime())));
            stringBuilder.append("</span><br/>");
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment will expire at: </span><span>");
            stringBuilder.append(SimpleDateFormat.getDateTimeInstance().format(new Date(
                    cachedElement.getExpirationTime())));
            stringBuilder.append("</span>");
            stringBuilder.append("<form action=\"").append(renderContext.getURLGenerator().getContext()).append(
                    "/tools/ehcache/flushKey.jsp\" method=\"post\"");
            stringBuilder.append("<input type=\"hidden\" name=\"keyToFlush\" value=\"").append(key).append("\"");
            stringBuilder.append("<button type=\"submit\"title=\"Flush it\">Flush It</button>");
            stringBuilder.append("</form>");
        } else {
            stringBuilder.append("<span class=\"cacheDebugInfoLabel\">Fragment Not Cacheable</span><br/>");
        }*/
        stringBuilder.append("</div>");
        stringBuilder.append(renderContent);
        return stringBuilder.toString();
    }

    private static OutputDocument emptyEsiIncludeTagContainer(Iterable<StartTag> segments, Source source) {
        OutputDocument outputDocument = new OutputDocument(source);
        for (StartTag segment : segments) {
            outputDocument.replace(segment.getElement().getContent(), "");
        }
        return outputDocument;
    }

    public static String removeEsiTags(String content) {
        if (StringUtils.isNotEmpty(content)) {
            String s = CLEANUP_REGEXP.matcher(content).replaceAll("");
            return s;
        } else {
            return content;
        }
    }

    @Override
    public void handleError(RenderContext renderContext, Resource resource, RenderChain chain, Exception e) {
        super.handleError(renderContext, resource, chain, e);
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null) {
            String perUserKey = userKeysLinkedList.get(0);

            final Cache cache = cacheProvider.getCache();
            cache.put(new Element(perUserKey, null));
        }
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain chain) {
        LinkedList<String> userKeysLinkedList = userKeys.get();
        if (userKeysLinkedList != null && userKeysLinkedList.size()>0) {

            String perUserKey = userKeysLinkedList.remove(0);
            if (perUserKey.equals(acquiredSemaphore.get())) {
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }

            Set<CountDownLatch> latches = processingLatches.get();
            Map<String, CountDownLatch> countDownLatchMap = generatorQueue.getGeneratingModules();
            CountDownLatch latch = countDownLatchMap.get(perUserKey);
            if (latches != null && latches.contains(latch)) {
                latch.countDown();
                synchronized (countDownLatchMap) {
                    latches.remove(countDownLatchMap.remove(perUserKey));
                }
            }
        }
    }

    private CountDownLatch avoidParallelProcessingOfSameModule(String key, String contextConfiguration,
                                                               HttpServletRequest request) throws Exception {
        CountDownLatch latch = null;
        boolean mustWait = true;
        boolean semaphoreAcquired = false;
        Map<String, CountDownLatch> generatingModules = generatorQueue.getGeneratingModules();
        if (generatingModules.get(key) == null && acquiredSemaphore.get() == null) {
            if (!generatorQueue.getAvailableProcessings().tryAcquire(generatorQueue.getModuleGenerationWaitTime(),
                    TimeUnit.MILLISECONDS)) {
                manageThreadDump();
                throw new Exception("Module generation takes too long due to maximum parallel processings reached (" +
                                    generatorQueue.getMaxModulesToGenerateInParallel() + ") - " + key + " - " +
                                    request.getRequestURI());
            } else {
                acquiredSemaphore.set(key);
                semaphoreAcquired = true;
            }
        }
        if (!generatorQueue.isUseLatchOnlyForPages() || "page".equals(contextConfiguration)) {
            synchronized (generatingModules) {
                latch = (CountDownLatch) generatingModules.get(key);
                if (latch == null) {
                    latch = new CountDownLatch(1);
                    generatingModules.put(key, latch);
                    mustWait = false;
                }
            }
        } else {
            mustWait = false;
        }
        if (mustWait) {
            if (semaphoreAcquired) {
                // another thread wanted the same module and got the latch first, so release the semaphore immediately as we must wait 
                generatorQueue.getAvailableProcessings().release();
                acquiredSemaphore.set(null);
            }
            try {
                if (!latch.await(generatorQueue.getModuleGenerationWaitTime(), TimeUnit.MILLISECONDS)) {
                    manageThreadDump();
                    throw new Exception("Module generation takes too long due to module not generated fast enough (>" +
                                        generatorQueue.getModuleGenerationWaitTime() + " ms)- " + key + " - " +
                                        request.getRequestURI());
                }
                latch = null;
            } catch (InterruptedException ie) {
                if (logger.isDebugEnabled()) {
                    logger.debug("The waiting thread has been interrupted :", ie);
                }
                throw new Exception(ie);
            }
        }
        return latch;
    }

    private void manageThreadDump() {
        boolean createDump = false;
        long minInterval = generatorQueue.getMinimumIntervalAfterLastAutoThreadDump();
        if (minInterval > -1 && (generatorQueue.isThreadDumpToSystemOut() || generatorQueue.isThreadDumpToFile())) {
            long now = System.currentTimeMillis();
            synchronized (threadDumpCheckLock) {
                if (now > (lastThreadDumpTime + minInterval)) {
                    createDump = true;
                    lastThreadDumpTime = now;
                }
            }
        }
        if (createDump) {
            ThreadMonitor tm = ThreadMonitor.getInstance();
            tm.dumpThreadInfo(generatorQueue.isThreadDumpToSystemOut(), generatorQueue.isThreadDumpToFile());
            tm = null;
        }
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent) {
            notCacheableFragment.clear();
        }
    }

    public void setModuleParamsProperties(Map<String,String> moduleParamsProperties) {
        this.moduleParamsProperties = moduleParamsProperties;
    }


    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        Config.LoggerProvider = LoggerProvider.DISABLED;
    }
}
