/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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

package org.jahia.operations.valves;

import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.StartTag;
import org.jahia.content.ContentObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.operations.PageGeneratorQueue;
import org.jahia.operations.PageState;
import org.jahia.params.AdvCompareModeSettings;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.CacheKeyGeneratorService;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.services.cache.SkeletonCache;
import org.jahia.services.cache.SkeletonCacheEntry;
import org.jahia.services.theme.ThemeService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.ajax.gwt.utils.GWTInitializer;
import org.springframework.util.StopWatch;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Title: </p> <p>Description: </p> <p>Copyright: Copyright (c) 2004</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class SkeletonAggregatorValve implements Valve {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SkeletonAggregatorValve.class);
    private static PageGeneratorQueue generatorQueue;
    public static final String ESI_VARIABLE_USERNAME = "username";
    public static final String ESI_VARIABLE_USER = "user";
    public static final String THEME_VARIABLE = "theme";
    public static final String GWT_VARIABLE = "gwtInit";
    private static ContainerHTMLCache containerHTMLCache = null;
    private static CacheKeyGeneratorService service;

    public void setGeneratorQueue(PageGeneratorQueue generatorQueue) {
        this.generatorQueue = generatorQueue;
    }

    public SkeletonAggregatorValve() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        ProcessingContext processingContext = (ProcessingContext) context;
        boolean exit = checkCache(processingContext);
        if (exit) return;
        valveContext.invokeNext(context);
    }

    public static boolean checkCache(ProcessingContext processingContext) throws PipelineException {

        // force generation of page if this one is not cacheable
        boolean iscacheable = false;
        // first order of business is to do all cache processing, in order to
        // minimize what we have to do.
        boolean exit = false;

        processingContext.getSessionState().setAttribute(ProcessingContext.SESSION_LAST_REQUESTED_PAGE_ID,processingContext.getPageID());

        if (processingContext.settings().isOutputContainerCacheActivated()) {
            /** we only do HTML content caching if it is a core engine call
             *  and if the HTTP method is GET. We might extend on this later
             *  on but for the sake of simplicity of implementation we
             *  restrict it for now. We might be interested in doing more
             *  work here in order to cache more often.
             */
            SkeletonCache skeletonCache;
            try {
                // Get the HTML cache instance
                skeletonCache = ServicesRegistry.getInstance().getCacheService().getSkeletonCacheInstance();
            } catch (JahiaInitializationException ex) {
                throw new PipelineException(ex);
            }
            // Get the language code
            String curLanguageCode = processingContext.getLocale().toString();
            GroupCacheKey entryKey = null;
            StopWatch watch = new StopWatch("Skeleton aggregator valve watch");
            if ("core".equals(processingContext.getEngine()) &&
                !"debug".equals(processingContext.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER)) &&
                (processingContext.getHttpMethod() == ProcessingContext.GET_METHOD) &&
                (!processingContext.settings().isContainerCacheLiveModeOnly() ||
                        ProcessingContext.NORMAL.equals(processingContext.getOperationMode()))) {
                // logger.debug("Requested page is : " + jParams.getObjectKey() );
                // This page is cacheable
                if (processingContext.getCacheExpirationDelay() == -1 ||
                    processingContext.getCacheExpirationDelay() > 0) iscacheable = true;
                if (service == null) service = ServicesRegistry.getInstance().getCacheKeyGeneratorService();
                watch.start("computeSkeletonEntryKeyWithGroups");
                String queryString = processingContext.getQueryString();
                String aesMode = (String) processingContext.getSessionState().getAttribute("aesMode") ;
                StringBuffer advPreviewMode = new StringBuffer();
                if (AdvPreviewSettings.isInUserAliasingMode() || AdvPreviewSettings.isPreviewingAtDefinedDateMode()){
                    advPreviewMode.append(AdvPreviewSettings.getThreadLocaleInstance());
                }
                StringBuffer advCompareMode = new StringBuffer();
                if (AdvCompareModeSettings.isComparingUsingArchivedRevision()){
                    advCompareMode.append(AdvCompareModeSettings.getThreadLocaleInstance());
                }
                if (aesMode == null) {
                    aesMode = "" ;
                }
                if (queryString == null) {
                    queryString = "" ;
                }

                // this key is to create a skeleton for each display option (query string + advanced edit settings)
                final StringBuilder builder = new StringBuilder(queryString);
                if(!ProcessingContext.NORMAL.equals(processingContext.getOperationMode()) &&
                        !ProcessingContext.EDIT.equals(processingContext.getOperationMode())) {
                    builder.append(processingContext.getPathInfo());
                }
                builder.append(aesMode);
                builder.append(advPreviewMode.toString());
                builder.append(advCompareMode.toString());
                String userAgentViewKey = (String) processingContext.getAttribute(UserAgentViewSwitcherValve.VIEW_SWITCHING_VALVE_KEY_REQUEST_ATTRIBUTE_NAME);
                if (userAgentViewKey != null) {
                    builder.append(userAgentViewKey);
                }
                String additionalKey = builder.toString() ;

                final JahiaUser jahiaUser = processingContext.getUser();
                entryKey = service
                        .computeSkeletonEntryKeyWithGroups(processingContext.getPage(),
                                                           additionalKey,
                                                           jahiaUser,
                                                           curLanguageCode,
                                                           processingContext.getOperationMode(),
                                                           processingContext.getScheme(),
                                                           new HashSet<ContentObjectKey>());
                if (logger.isDebugEnabled()) {
                    logger.debug("Page is : " + processingContext.getPage().getTitle()) ;
                    logger.debug("Additional key is : " + additionalKey) ;
                    logger.debug("User is : " + jahiaUser.getUserKey()) ;
                    logger.debug("Language code is : " + curLanguageCode) ;
                    logger.debug("Operation mode is : " + processingContext.getOperationMode()) ;
                    logger.debug("Scheme is : " + processingContext.getScheme()) ;
                    logger.debug("Skeleton key is : " + entryKey.toString()) ;
                }
                watch.stop();
                if (generatorQueue.getNotCacheablePage().containsKey(entryKey)) {
                    ValveContext.valveResources.set(null);
                    return exit;
                }
                if (skeletonCache.getConcurrentHashMap().containsKey(entryKey)) {
                    skeletonCache.getConcurrentHashMap().remove(entryKey);
                    ValveContext.valveResources.set(new PageState(false, entryKey));
                    return exit;
                }
                watch.start("getCacheEntry");
                CacheEntry cacheEntry = skeletonCache.getCacheEntry(entryKey);
                watch.stop();
                if (cacheEntry != null) {
                    //                            logger.debug ("Found HTML page in cache!!!!!!!!!!!!!!!!!!");
                    watch.start("cacheEntry.getContentBody()");
                    SkeletonCacheEntry htmlEntry = (SkeletonCacheEntry) cacheEntry.getObject();
                    Source htmlContent = htmlEntry.getSource();
                    if(htmlContent == null) {
                        logger.debug("Getting HTML content from skeleton entry instead of source") ;
                        htmlContent = new Source(htmlEntry.getContentBody());
                        htmlEntry.setSource(htmlContent);
                    }
                    watch.stop();

                    if (!cacheEntry.getOperationMode().equals(processingContext.getOperationMode())) {
                        if (logger.isDebugEnabled()) logger.debug(
                                "Cache entry mode is NOT equal to current mode, flushing page entry and generating page...");
                        skeletonCache.remove(entryKey);

                    }
                    else {
                        if (htmlContent != null) {
                            //Before writing we will try to aggregate content
                            watch.start("create new output document");
                            OutputDocument outputDocument = new OutputDocument(htmlContent);
                            watch.stop();
                            watch.start("find tags");
                            List esiIncludeTags = htmlEntry.getIncludeTag();
                            if(esiIncludeTags == null) {
                                esiIncludeTags = htmlContent.findAllStartTags("esi:include");
                                htmlEntry.setIncludeTag(esiIncludeTags);
                            }
                            List esiVarsTags = htmlEntry.getVarsTag();
                            if(esiVarsTags == null) {
                                esiVarsTags = htmlContent.findAllStartTags("esi:vars");
                                htmlEntry.setVarsTag(esiVarsTags);
                            }
                            watch.stop();
                            if (containerHTMLCache == null) {
                                try {
                                    containerHTMLCache = ServicesRegistry.getInstance()
                                            .getCacheService()
                                            .getContainerHTMLCacheInstance();
                                } catch (JahiaInitializationException e) {
                                    throw new PipelineException(e);
                                }
                            }
                            if (containerHTMLCache != null) {
                                watch.start("Iterate through include tags (" + esiIncludeTags.size() + ")");
                                for (Iterator i = esiIncludeTags.iterator(); i.hasNext();) {
                                    StartTag segment = (StartTag) i.next();
                                    String[] attrs = segment.getAttributeValue("src").split("&");
                                    String cacheKey = "";
                                    try {
                                        cacheKey = attrs[1].split("=")[1];
                                    } catch (ArrayIndexOutOfBoundsException e) {
                                        // No cachekey
                                    }
                                    int ctnid = Integer.parseInt(attrs[0].split("=")[1]);
                                    GroupCacheKey containerKey =
                                            service.computeContainerEntryKey(ctnid,
                                                                             cacheKey,
                                                                             jahiaUser,
                                                                             curLanguageCode,
                                                                             processingContext.getOperationMode(),
                                                                             processingContext.getScheme(),
                                                                             processingContext.getSiteID());
                                    ContainerHTMLCacheEntry containerHTMLCacheEntry =
                                            (ContainerHTMLCacheEntry) containerHTMLCache.get(containerKey);
                                    if (containerHTMLCacheEntry != null)
                                        outputDocument.replace(segment.getBegin(), segment.getElement()
                                                .getEndTag().getEnd(), containerHTMLCacheEntry.getBodyContent());
                                    else {
                                        ValveContext.valveResources.set(new PageState(false, entryKey));
                                        return exit;
                                    }
                                }
                                watch.stop();
                                watch.start("Iterate through vars tags (" + esiVarsTags.size() + ")");
                                for (Iterator i = esiVarsTags.iterator(); i.hasNext();) {
                                    StartTag segment = (StartTag) i.next();
                                    String variableName = segment.getAttributeValue("var");
                                    if (variableName.equals(ESI_VARIABLE_USERNAME)) {
                                        JahiaUser user = AdvPreviewSettings.getAliasedUser(jahiaUser);
                                        String s = jahiaUser.getUsername();
                                        if (!s.equals(user.getUsername())) {
                                            s = s + " ( " + user.getUsername() + " )";
                                        }
                                        outputDocument.replace(segment.getBegin(),
                                                               segment.getElement().getEndTag().getEnd(),
                                                               s);
                                    }
                                    else if (variableName.startsWith(ESI_VARIABLE_USER)) {
                                        String propertyName = variableName.split("\\.")[1];
                                        final String s = jahiaUser.getProperty(propertyName);
                                        outputDocument.replace(segment.getBegin(),
                                                segment.getElement().getEndTag().getEnd(),
                                                s != null ? s : "");
                                    } else if (variableName.equals(THEME_VARIABLE)) {
                                        final String jahiaThemeCurrent = (String) processingContext.getAttribute(ThemeValve.THEME_ATTRIBUTE_NAME + "_" + processingContext.getSite().getID());
                                        if (jahiaThemeCurrent != null) {
                                            logger.debug("Try to apply theme : "+jahiaThemeCurrent);
                                            outputDocument.replace(segment.getBegin(),
                                                    segment.getElement().getEndTag().getEnd(),
                                                    ThemeService.getInstance().getCssLinks(processingContext, processingContext.getSite(), jahiaThemeCurrent));
                                        }
                                    } else if (variableName.equals(GWT_VARIABLE)) {
                                        outputDocument.replace(segment.getBegin(),
                                                segment.getElement().getEndTag().getEnd(),
                                                GWTInitializer.getInitString((ParamBean) processingContext));
                                    }
                                }
                                watch.stop();
                            }
                            watch.start("Send to client");
                            if (logger.isDebugEnabled())
                                logger.debug("Found content in cache, writing directly bypassing processing...");
                            HttpServletResponse realResp = ((ParamBean) processingContext).
                                    getRealResponse();
                            String contentType = htmlEntry.getContentType();
                            if (contentType != null) {
                                realResp.setContentType(contentType);
                                if (logger.isDebugEnabled())
                                    logger.debug("Sending content type : [" + contentType + "]");
                            }
                            try {
                                ServletOutputStream outputStream = realResp.getOutputStream();
                                long length = outputDocument.getEstimatedMaximumOutputLength();
                                if(length== -1L) length = 4096;
                                Writer streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream), (int) length);
                                if (contentType != null) {
                                    int charsetPos = contentType.toLowerCase().
                                            indexOf("charset=");
                                    if (charsetPos != -1) {
                                        String encoding = contentType.substring(charsetPos + "charset=".length()).
                                                toUpperCase();
                                        if (logger.isDebugEnabled())
                                            logger.debug("Using streamWriter with encoding : " + encoding);
                                        streamWriter = new BufferedWriter(new OutputStreamWriter(outputStream, encoding),(int) length);
                                    }
                                }
                                outputDocument.writeTo(streamWriter);
                            } catch (java.io.IOException ioe) {
                                logger.error("Error writing cache output, IOException generated error", ioe);
                                JahiaException outputException =
                                        new JahiaException("OperationsManager.handleOperations",
                                                           "Error writing cache content to writer",
                                                           JahiaException.SECURITY_ERROR,
                                                           JahiaException.ERROR_SEVERITY,
                                                           ioe);
                                throw new PipelineException(outputException);
                            }
                            watch.stop();
                            if(logger.isDebugEnabled())
                                logger.debug(watch.prettyPrint());
                            exit = true;// exit handling here !
                        }
                    }
                }
                else {
                    if (logger.isDebugEnabled())
                        logger.debug("!!!!!! Could not find HTML page in cache!!!!!!!!!!!!!!!!!!");
                }
            }
            ValveContext.valveResources.set(new PageState(iscacheable, entryKey));
        }
        else {
            if (logger.isDebugEnabled()) logger.debug("Output cache not activated.");
            ValveContext.valveResources.set(new PageState(false, null));
        }
        return exit;
    }

    public void initialize() {
    }
}