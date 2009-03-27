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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.cache;

import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.apache.struts.taglib.TagUtils;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: rincevent Date: 12 juin 2008 Time: 09:01:00 To change this template use File |
 * Settings | File Templates.
 */
public class CacheTag extends AbstractJahiaTag {
    private static transient Category logger = Logger.getLogger(CacheTag.class);
    private String cacheKey;
    private String cacheKeyName;
    private String cacheKeyProperty;
    private String cacheKeyScope;
    private JahiaData jData;
    private Set<ContentObjectKey> dependencies;
    private boolean forceExecutionInEditMode;
    private long expiration= SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
    private boolean debug = false;

    public int doStartTag() throws JspException {
        int returned = EVAL_BODY_BUFFERED;
        try {
            if (cacheKeyName != null) {
                cacheKey = TagUtils.getInstance()
                        .lookup(pageContext, cacheKeyName, cacheKeyProperty, cacheKeyScope)
                        .toString();
            }
            if(cacheKey== null || "".equals(cacheKey.trim())) throw new JspException("None of cacheKey or cacheKeyName, cacheKeyProperty, cacheKeyScope is filled");
            ServletRequest request = pageContext.getRequest();
            jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ContainerHTMLCache cacheInstance2 =
                    ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            final ProcessingContext context2 = jData.getProcessingContext();
            // Let's build the cache key
            String cacheKey2 = buildCacheKey(context2);
            // Try to find the entry in cache
            final CacheEntry htmlCacheEntry2 =
                    cacheInstance2.getCacheEntryFromContainerCache(null, context2, cacheKey2, false, 0, null, null);
            // This will containes the html to output in the skeleton (HTML Page)
            String htmlOutput2;
            boolean cacheParam2 = (!ProcessingContext.NORMAL.equals(context2.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                || (context2.getEntryLoadRequest() != null && context2.getEntryLoadRequest().isVersioned()) ;
            final boolean outputContainerCacheActivated = org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
            if (!(forceExecutionInEditMode && context2.isInEditMode()) &&
                    outputContainerCacheActivated &&
                    htmlCacheEntry2 != null &&
                    !cacheParam2) {
                Date expireDate = htmlCacheEntry2.getExpirationDate();
                // entry is found and should be displayed
                if (debug) {
                    pageContext.getOut().print("<fieldset><legend align=\"right\">getting from cache (will expire at " +
                            dateFormat.format(expireDate) + ")</legend>");
                }
                htmlOutput2 = ((ContainerHTMLCacheEntry) htmlCacheEntry2.getObject()).getBodyContent();
                writeHtmlToStream(context2, cacheKey2, htmlOutput2);
                if (debug) {
                    pageContext.getOut().println("</fieldset>");
                }
                returned = SKIP_BODY;
            }
            else {
                if (dependencies == null) dependencies = new HashSet<ContentObjectKey>();
                if (debug) {
                    pageContext.getOut().print("<fieldset><legend align=\"right\">written to cache (at " +
                            dateFormat.format(new Date()) + ")</legend>");
                }
                if (outputContainerCacheActivated)
                    pageContext.getOut().print("<!-- cache:include src=\"" +
                               context2.getSiteURLForCurrentPageAndCurrentSite(false, true, true) +
                               "?ctnid=0&cacheKey=" +
                               cacheKey2 +
                               "\" -->");
            }
        } catch (JahiaInitializationException e) {
            logger.error("Jahia is not correctly initialized", e);
        } catch (IOException e) {
            logger.error("Error during execution of this content:cache tag", e);
        }
        return returned;
    }

    private String buildCacheKey(ProcessingContext context2) {
        String cacheKey2 =
                ContainerHTMLCache.appendAESMode(context2, new StringBuffer("_ctnListName_").append(cacheKey)
                        .append("_siteKey_")
                        .append(context2.getSiteKey()).toString());
        debug = "debug".equals(context2.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
        if (debug && !(cacheKey2.indexOf("___debug___")>0)) cacheKey2 = cacheKey2 + "___debug___";
        return cacheKey2;
    }

    public int doAfterBody() throws JspException {
        try {
            ServletRequest request = pageContext.getRequest();
            jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ContainerHTMLCache cacheInstance2 =
                    ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
            final ProcessingContext context2 = jData.getProcessingContext();
            // Let's build the cache key
            String cacheKey2 = buildCacheKey(context2);
            if (debug && !(cacheKey2.indexOf("___debug___")>0)) cacheKey2 = cacheKey2 + "___debug___";
            String content = getBodyContent().getString();
            final boolean outputContainerCacheActivated = org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
            boolean cacheParam2 = (!ProcessingContext.NORMAL.equals(context2.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                || (context2.getEntryLoadRequest() != null && context2.getEntryLoadRequest().isVersioned());
            if (!(forceExecutionInEditMode && context2.getOperationMode().equals(ProcessingContext.EDIT)) &&
                outputContainerCacheActivated &&
                content != null && !cacheParam2 &&
                content.length() > 0) {
                cacheInstance2.writeToContainerCache(null,
                                                     context2, 
                                                     content,
                                                     cacheKey2,
                                                     dependencies,
                                                     expiration);
            }
            getPreviousOut().print(content);
            if (outputContainerCacheActivated)
                getPreviousOut().print("<!-- /cache:include -->\n");
            if (debug) {
                getPreviousOut().println("</fieldset>");
            }
        } catch (JahiaInitializationException e) {
            logger.error("Jahia is not correctly initialized", e);
        } catch (IOException e) {
            logger.error("Error during execution of this content:cache tag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        super.doEndTag();
        pageContext.setAttribute(cacheKey + "Dependencies", dependencies);
        cacheKey = null;
        cacheKeyName = null;
        cacheKeyProperty = null;
        cacheKeyScope = null;
        dependencies = null;
        jData = null;
        pageContext.removeAttribute("cacheDependencies");
        expiration = SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
        return SKIP_BODY;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setCacheKeyName(String cacheKeyName) {
        this.cacheKeyName = cacheKeyName;
    }

    public void setCacheKeyProperty(String cacheKeyProperty) {
        this.cacheKeyProperty = cacheKeyProperty;
    }

    public void setCacheKeyScope(String cacheKeyScope) {
        this.cacheKeyScope = cacheKeyScope;
    }

    public void setDependencies(Set<ContentObjectKey> dependencies) {
        this.dependencies = dependencies;
    }

    public void setForceExecutionInEditMode(boolean forceExecutionInEditMode) {
        this.forceExecutionInEditMode = forceExecutionInEditMode;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    private void writeHtmlToStream(ProcessingContext context, String cacheKey, String htmlOutput) throws IOException {
        JspWriter out = pageContext.getOut();
        final SettingsBean bean = org.jahia.settings.SettingsBean.getInstance();
        final boolean showEsiTagsForSkeleton = bean.isOutputContainerCacheActivated();
        if (showEsiTagsForSkeleton) out.print("<!-- cache:include src=\"" +
                                              context.getSiteURLForCurrentPageAndCurrentSite(false, true, true) +
                                              "?ctnid=0&cacheKey=" +
                                              cacheKey +
                                              "\" -->");
        out.print(htmlOutput);
        if (showEsiTagsForSkeleton) out.print("<!-- /cache:include -->\n");
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        logger = Logger.getLogger(CacheTag.class);
    }

    public Set<ContentObjectKey> getDependencies() {
        return dependencies;
    }
}