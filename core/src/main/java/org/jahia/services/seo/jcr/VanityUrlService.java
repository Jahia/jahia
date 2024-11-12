/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.seo.jcr;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolationException;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.render.URLResolverListener;
import org.jahia.services.seo.VanityUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to manage vanity urls in Jahia
 *
 * @author Benjamin Papez
 */
public class VanityUrlService {

    private static final Logger logger = LoggerFactory.getLogger(VanityUrlService.class);

    private VanityUrlManager vanityUrlManager;
    private Cache<String, List<VanityUrl>> cacheByUrl;

    public static final String CACHE_BY_URL = "vanityUrlByUrlCache";
    private static final String KEY_SEPARATOR = "___";

    /**
     * Gets a node's default vanity URL for the given locale and workspace. If none is default then take the first mapping for the locale.
     *
     * @param contentNode
     *            the content node for which to return a mapping
     * @param workspace
     *            the workspace to look for mappings
     * @param locale
     *            the locale to which the mapping should apply
     * @param siteKey
     *            the key of the current site; if the content node's site does not match the specified one in <code>siteKey</code>,
     *            <code>null</code> is returned by this method
     * @return the VanityUrl bean
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public VanityUrl getVanityUrlForWorkspaceAndLocale(final JCRNodeWrapper contentNode,
                                                       String workspace, Locale locale, final String siteKey) throws RepositoryException {
        if (siteKey == null) {
            return null;
        }
        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, locale,
                new JCRCallback<VanityUrl>() {

                    @Override
                    public VanityUrl doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        return vanityUrlManager.getVanityUrlForCurrentLocale(contentNode, siteKey, session);
                    }
                });
    }

    /**
     * Gets a node's default vanity URL for the given locale and workspace. If none is default then take the first mapping for the locale.
     *
     * @param contentNodePath
     *            the content node path for which to return a mapping
     * @param workspace
     *            the workspace to look for mappings
     * @param locale
     *            the locale to which the mapping should apply
     * @param siteKey
     *            the key of the current site; if the content node's site does not match the specified one in <code>siteKey</code>,
     *            <code>null</code> is returned by this method
     * @return the VanityUrl bean
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public VanityUrl getVanityUrlForWorkspaceAndLocale(final String contentNodePath,
                                                       String workspace, Locale locale, final String siteKey) throws RepositoryException {
        if (siteKey == null) {
            return null;
        }
        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, locale,
                session -> {
                    if (session.nodeExists(contentNodePath)){
                        return vanityUrlManager.getVanityUrlForCurrentLocale(session.getNode(contentNodePath), siteKey, session);
                    } else {
                        logger.debug("Node {} does not exist in workspace {} for locale {}", contentNodePath, session.getWorkspace().getName(), locale != null ? locale.toString(): "null");
                        return null;
                    }
                });
    }

    /**
     * Gets all node's vanity URLs for the current locale in the session
     *
     * @param contentNode
     *            the content node for which to return the mappings
     * @param session
     *            the JCR session holding the information about the workspace, locale and user
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> getVanityUrlsForCurrentLocale(JCRNodeWrapper contentNode,
            JCRSessionWrapper session) throws RepositoryException {
        return getVanityUrls(contentNode, session.getLocale().toString(), session);
    }

    /**
     * Gets all node's vanity URLs for the given locale
     *
     * @param contentNode
     *            the content node for which to return the mappings
     * @param languageCode
     *            the language code for which to return the mappings
     * @param session
     *            the JCR session holding the information about the workspace and user
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> getVanityUrls(JCRNodeWrapper contentNode, String languageCode,
            JCRSessionWrapper session) throws RepositoryException {
        return vanityUrlManager.getVanityUrls(contentNode, languageCode, session);
    }

    /**
     * Completely delete a mapped vanity URL. If the deleted vanity URL is the default one for the current locale, then check whether there
     * are other active mappings for the same locale and set the first found one as the new default.
     *
     * @param contentNode
     *            the content node for which to remove the given mapping
     * @param vanityUrl
     *            the VanityUrl bean representing the URL to be removed
     * @return true if the vanity URL was removed or false if it was not removed
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean removeVanityUrlMapping(final JCRNodeWrapper contentNode,
            final VanityUrl vanityUrl) throws RepositoryException {
        cacheByUrl.flush();
        return vanityUrlManager.removeVanityUrlMapping(contentNode, vanityUrl, contentNode.getSession());
    }

    /**
     * Completely delete all mapped vanity URL for a locale.
     *
     * @param contentNode
     *            the content node for which to remove the mappings
     * @param languageCode
     *            the language code for which the mappings should be removed
     * @return true if the vanity URL was removed or false if it was not removed
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean removeVanityUrlMappings(final JCRNodeWrapper contentNode,
            final String languageCode) throws RepositoryException {
        cacheByUrl.flush();
        return vanityUrlManager.removeVanityUrlMappings(contentNode, languageCode, contentNode.getSession());
    }

    /**
     * Add or update a vanity URL mapping belonging to a specific content node and identified either by the vanity URL node UUID or the
     * vanity URL value set in the VanityUrl bean.
     * <p>
     * If the URL mapping has already been saved before, we check whether the default, active, language or vanity URL value in the bean
     * is different from the saved one, so we do an update, otherwise no operation is done.
     * <p>
     * If the new or updated mapping is now the default one for the language, then we also check
     * if there already is another default URL for this language and set its default flag to false.
     * <p>
     * We also check whether the same URL is already existing for a different node or language
     * in the current site and throw a ConstraintViolationException if this is the case.
     *
     * @param contentNode the content node for which to add or update the given mapping
     * @param vanityUrl   the VanityUrl bean representing the URL to be added or updated
     * @return true if the vanity URL was added or false if it was not added
     * @throws ConstraintViolationException if the vanity URL mapping already exists for a different content node or language within the site
     * @throws RepositoryException          if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMapping(final JCRNodeWrapper contentNode, final VanityUrl vanityUrl)
            throws RepositoryException {
        return saveVanityUrlMapping(contentNode, vanityUrl, true);
    }

    /**
     * Add or update a vanity URL mapping belonging to a specific content node and identified either by the vanity URL node UUID or the
     * vanity URL value set in the VanityUrl bean.
     * <p>
     * If the URL mapping has already been saved before, we check whether the default, active, language or vanity URL value in the bean
     * is different from the saved one, so we do an update, otherwise no operation is done.
     * <p>
     * If the new or updated mapping is now the default one for the language, then we also check
     * if there already is another default URL for this language and set its default flag to false.
     * <p>
     * We also check whether the same URL is already existing for a different node or language
     * in the current site and throw a ConstraintViolationException if this is the case.
     *
     * @param contentNode the content node for which to add or update the given mapping
     * @param vanityUrl   the VanityUrl bean representing the URL to be added or updated
     * @param save        should the session be saved at the end
     * @return true if the vanity URL was added or false if it was not added
     * @throws ConstraintViolationException if the vanity URL mapping already exists for a different content node or language within the site
     * @throws RepositoryException          if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMapping(final JCRNodeWrapper contentNode, final VanityUrl vanityUrl, boolean save)
            throws RepositoryException {
        cacheByUrl.flush();
        return vanityUrlManager.saveVanityUrlMapping(contentNode, vanityUrl, contentNode.getSession(), save);
    }


    /**
     * Add, update or delete all vanity URL mappings for a specific content node and the language codes set in the list of VanityUrl beans.
     * First we load all existing mappings for all the languages set in the updatedLocales collection. Then we compare the existing with the
     * list of URL mappings given in the vanityUrls collection to know which nodes need to be added, updated or deleted.
     *
     * If the default mapping for a language is set for a new or updated mapping, then we check if there already is another default URL for
     * this language and set its default flag to false.
     *
     * We also check whether the same added URL is already existing for a different node or language in the current site (and which is not
     * being deleted in the same operation) and throw a ConstraintViolationException if this is the case. We also throw a
     * ConstraintViolationException if two URL mappings for the same language in the given vanityUrls collection have the default flag set
     * to true.
     *
     * @param contentNode
     *            the content node for which to add the given mapping
     * @param vanityUrls
     *            the list of VanityUrls bean representing the URLs to be added or updated
     * @param updatedLocales
     *            a set with all locales, which have been edited (e.g. if all mappings for a language need to be deleted, add the language
     *            to this set, while in the vanityUrls list there will be no mappings for that language)
     * @return true if any vanity URL was added, updated or deleted or false if no change was done
     * @throws ConstraintViolationException
     *             if the vanity URL mapping already exists for a different content node or language in the site
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public boolean saveVanityUrlMappings(final JCRNodeWrapper contentNode,
            final List<VanityUrl> vanityUrls, final Set<String> updatedLocales)
            throws RepositoryException {
        cacheByUrl.flush();
        return vanityUrlManager.saveVanityUrlMappings(contentNode, vanityUrls,
                updatedLocales, contentNode.getSession());
    }

    /**
     * Find any mappings for the given vanity URL. If a site is specified the query will be done only for the specified site, otherwise all
     * sites in the workspace will be searched through and a list of VanityURL beans will be returned. The method searches mappings in any
     * language.
     *
     * @param url
     *            URL path to check whether there is a content mapping for it (URL must start with /)
     * @param site
     *            key of the site to search for the mapping or all sites if the string is null or empty
     * @param workspace
     *            the workspace to look for mappings
     * @return the list of VanityUrl beans
     * @throws RepositoryException
     *             if there was an unexpected exception accessing the repository
     */
    public List<VanityUrl> findExistingVanityUrls(String url, final String site, final String workspace) throws RepositoryException {

        final String finalUrl = (url.endsWith("/") ? url.substring(0, url.length() - 1) : url);

        final String cacheKey = getCacheByUrlKey(finalUrl, site, workspace);
        List<VanityUrl> result = (List<VanityUrl>) cacheByUrl.get(cacheKey);
        if (result != null) {
            return result;
        }

        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null,
                new JCRCallback<List<VanityUrl>>() {

                    @Override
                    public List<VanityUrl> doInJCR(JCRSessionWrapper session)
                            throws RepositoryException {
                        List<VanityUrl> vanityUrls = vanityUrlManager.findExistingVanityUrls(finalUrl, site, session);
                        cacheByUrl.put(cacheKey, vanityUrls);
                        return vanityUrls;
                    }
                });
    }

    /**
     * Injects the dependency to {@link VanityUrlManager}.
     *
     * @param vanityUrlManager
     *            the dependency to {@link VanityUrlManager}
     */
    public void setVanityUrlManager(VanityUrlManager vanityUrlManager) {
        this.vanityUrlManager = vanityUrlManager;
    }

    public void setCacheService(CacheService cacheService) {
        try {
            cacheByUrl = cacheService.getCache(CACHE_BY_URL, true);
        } catch (JahiaInitializationException e) {
            logger.error("Error while creating cache: " + CACHE_BY_URL);
        }
    }

    public String getCacheByUrlKey(final String url, final String site, final String workspace) {
        StringBuilder builder = new StringBuilder(url);
        builder.append(KEY_SEPARATOR);
        builder.append(site);
        builder.append(KEY_SEPARATOR);
        builder.append(workspace);
        return builder.toString();
    }

    public void setUrlResolverListener(URLResolverListener urlResolverListener) {
        urlResolverListener.setVanityUrlService(this); // we wire this manually to avoid loops.
    }

    public void flushCaches() {
        cacheByUrl.flush();
    }

    public void flushCacheEntry(String key) {
        cacheByUrl.remove(key);
    }
}
