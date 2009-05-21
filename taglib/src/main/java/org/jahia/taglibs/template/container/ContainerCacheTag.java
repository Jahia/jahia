/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.template.container;

import org.apache.log4j.Category;
import org.apache.struts.taglib.TagUtils;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentContainerListKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.ContentPageKey;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.CacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.cache.GroupCacheKey;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;
import org.jahia.engines.calendar.CalendarHandler;

import javax.servlet.ServletRequest;
import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 29 mars 2007
 * Time: 09:19:13
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ContainerCacheTag extends AbstractJahiaTag implements ContainerCache {
    private static transient final Category logger =
            org.apache.log4j.Logger.getLogger(ContainerTag.class);

    private JahiaContainer container = null;
    private JahiaContainerList containerList = null;
    private String containerContent = null;
    private int counter = 1;
    private transient JahiaData jData = null;
    private String cache = Boolean.toString(org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated());
    private boolean currentCache;
    private boolean debug = false;
    private String cacheKey = null;
    private String cacheKeyName = null;
    private String cacheKeyProperty = null;
    private String cacheKeyScope = null;
    private Set<ContentObjectKey> dependencies = null;
    private ContainerCache oldCacheTag;
    private boolean display = true;
    private Date expirDate;
    private String expiration;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public void setCacheKey(Object cacheKey) {
        this.cacheKey = cacheKey.toString();
    }

    public String getCacheKeyName() {
        return cacheKeyName;
    }

    public void setCacheKeyName(String cacheKeyName) {
        this.cacheKeyName = cacheKeyName;
    }

    public String getCacheKeyProperty() {
        return cacheKeyProperty;
    }

    public void setCacheKeyProperty(String cacheKeyProperty) {
        this.cacheKeyProperty = cacheKeyProperty;
    }

    public String getCacheKeyScope() {
        return cacheKeyScope;
    }

    public void setCacheKeyScope(String cacheKeyScope) {
        this.cacheKeyScope = cacheKeyScope;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    /**
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="id attribute for this tag.
     * <p><attriInfo>Inherited from javax.servlet.jsp.tagext.TagSupport
     * </attriInfo>"
     */

    public boolean displayBody() {
        return display;
    }

    public JahiaContainer getContainer() {
        return this.container;
    }

    public ContainerBean getContainerBean() {
        if (this.container == null) {
            return null;
        }
        return new ContainerBean(this.container, jData.getProcessingContext());
    }

    public int getCounter() {
        return this.counter;
    }

    public int doStartTag() throws JspException {
        pushTag();
        if (cacheKeyName != null) {
            cacheKey = TagUtils.getInstance().lookup(pageContext, cacheKeyName, cacheKeyProperty, cacheKeyScope).toString();
        }

        oldCacheTag = (ContainerCache) pageContext.getAttribute(ContainerTag.CACHETAG);
        pageContext.setAttribute(ContainerTag.CACHETAG, this);
        ServletRequest request = pageContext.getRequest();
        jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

         // format the cacheKey to include AES settings
        final ProcessingContext context = jData.getProcessingContext();
        cacheKey = ContainerHTMLCache.appendAESMode(context, cacheKey) ;
        ContainerListTag cListTag = (ContainerListTag) findAncestorWithClass(this, ContainerListTag.class, pageContext.getRequest());
        ContainerTag containerTag = (ContainerTag) findAncestorWithClass(this, ContainerTag.class, pageContext.getRequest());
        containerTag.setCache("false");
        container = containerTag.getContainer();
        containerList = cListTag.getContainerList();
        if ((containerList == null) || (container == null)) {
            return SKIP_BODY;
        }
        dependencies = new HashSet<ContentObjectKey>();

        boolean cacheOff = (!ProcessingContext.NORMAL.equals(context.getOperationMode()) && Jahia.getSettings().isContainerCacheLiveModeOnly())
                || (context.getEntryLoadRequest() != null && context.getEntryLoadRequest().isVersioned()) ;
        
        currentCache = "true".equals(cache) && !cacheOff;
        
        debug = "debug".equals(context.getParameter(ProcessingContext.CONTAINERCACHE_MODE_PARAMETER));
        if(debug) cacheKey = cacheKey+"___debug___";
        if (currentCache) {
            try {
                containerContent = getFromContainerCache(container, jData);
                if (containerContent != null) {
                    return SKIP_BODY;
                }
            } catch (JahiaInitializationException jie) {
                logger.error("Error initializing container rendering", jie);
                throw new JspTagException();
            }
        }
        return EVAL_BODY_BUFFERED;
    }


    // loops through the next elements
    public int doAfterBody() throws JspException {
        if (this.display) {
            try {
                String content = getBodyContent().getString();
                ProcessingContext context = jData.getProcessingContext();
                if (content != null && content.length() > 0) {
                    if (currentCache) {
                        writeToContainerCache(container, jData, content);
                    }
                    ContainerCacheTag.writeOut(getPreviousOut(), getBodyContent(), currentCache, debug, container,
                                               cacheKey, context.getSiteURL(context.getSite(), context.getPageID(),false,true,true),
                                               pageContext);
                }
                getBodyContent().clear();
                this.counter++;
            } catch (IOException ioe) {
                logger.error("Error displaying container output", ioe);
                throw new JspTagException();
            } catch (JahiaInitializationException jie) {
                logger.error("Error displaying container output", jie);
                throw new JspTagException();
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        if ((this.display) && (containerContent != null && containerContent.length() > 0)) {
            try {
                ProcessingContext context = jData.getProcessingContext();
                writeOutFromCache(pageContext.getOut(), containerContent, debug,container, cacheKey,
                                  context.getSiteURL(context.getSite(), context.getPageID(),false,true,true),
                                  pageContext, expirDate);
            } catch (IOException ioe) {
                logger.error("Error displaying container output", ioe);
                throw new JspTagException(ioe.getMessage());
            }
        }
        super.doEndTag();
        container = null;
        containerList = null;
        containerContent = null;
        counter = 1;
        cacheKey = null;
        jData = null;
        dependencies = null;
        display = true;
        cache = Boolean.toString(org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated());
        pageContext.setAttribute(ContainerTag.CACHETAG, oldCacheTag);
        oldCacheTag = null;

        cacheKeyName = null;
        cacheKeyProperty = null;
        cacheKeyScope = null;
        expirDate = null;
        super.release();
        popTag();
        return EVAL_PAGE;
    }

    public void addContainerListDependency(int listId) {
        dependencies.add(new ContentContainerListKey(listId));
        ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null)
            ancestor.addContainerListDependency(listId);
    }

    public void addContainerDependency(int containerId) {
        dependencies.add(new ContentContainerKey(containerId));
        ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null)
            ancestor.addContainerDependency(containerId);
    }

    public void addPageDependency(int pageId) {
        dependencies.add(new ContentPageKey(pageId));
        ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null)
            ancestor.addPageDependency(pageId);
    }

    public void disableCache() {
        currentCache = false;

        ContainerCache ancestor = (ContainerCache) findAncestorWithClass(this, ContainerCache.class, pageContext.getRequest());
        if (ancestor != null)
            ancestor.disableCache();
    }

    private void writeToContainerCache(JahiaContainer jahiaContainer, JahiaData jahiaData,
                                       String bodyContent) throws JahiaInitializationException {
        if(bodyContent.contains("<!-- cache:include src="))return;
        ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> containerHTMLCache = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        ProcessingContext processingContext = jahiaData.getProcessingContext();
        String mode = jahiaData.getProcessingContext().getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = ServicesRegistry.getInstance().getCacheKeyGeneratorService().computeContainerEntryKeyWithGroups(
                jahiaContainer, cacheKey, processingContext.getUser(),
                curLanguageCode,
                mode,
                processingContext.getScheme(), dependencies);
        long expirI;
        if (expiration != null && !"".equals(expiration.trim())) {
            try {
                expirI = Long.parseLong(expiration);
            } catch (NumberFormatException e) {
                expirI = org.jahia.settings.SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
            }
        } else {
            expirI = org.jahia.settings.SettingsBean.getInstance().getContainerCacheDefaultExpirationDelay();
        }
        if (expirI > 0l) {
            containerHTMLCache.put(containerKey, new ContainerHTMLCacheEntry(bodyContent));
            try {
                containerHTMLCache.getCacheEntry(containerKey).setExpirationDate(new Date(System.currentTimeMillis() + (expirI * 1000)));
            } catch (NumberFormatException e) {
                logger.error("The argument expiration of your tag is not a number", e);
            }
        }
    }

    private String getFromContainerCache(JahiaContainer jahiaContainer, JahiaData jahiaData) throws JahiaInitializationException {
        ContainerHTMLCache<GroupCacheKey, ContainerHTMLCacheEntry> containerHTMLCache = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        ProcessingContext processingContext = jahiaData.getProcessingContext();
        if (processingContext.getEntryLoadRequest() != null && processingContext.getEntryLoadRequest().isVersioned()){
            // we don't cache versioned content
            return null;
        }
        String mode = jahiaData.getProcessingContext().getOperationMode();
        // Get the language code
        String curLanguageCode = processingContext.getLocale().toString();
        GroupCacheKey containerKey = ServicesRegistry.getInstance().getCacheKeyGeneratorService().computeContainerEntryKey(
                jahiaContainer, cacheKey, processingContext.getUser(),
                curLanguageCode,
                mode,
                processingContext.getScheme());
        CacheEntry<ContainerHTMLCacheEntry> cacheEntry = containerHTMLCache.getCacheEntry(containerKey);
        if (cacheEntry == null)
            return null;
        expirDate = cacheEntry.getExpirationDate();
        ContainerHTMLCacheEntry entry = (ContainerHTMLCacheEntry) cacheEntry.getObject();
        return entry.getBodyContent();
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);

    public static void writeOut(JspWriter writer, BodyContent content, boolean currentCache, boolean debug,
                                JahiaContainer container, String cacheKey, String pageURL, PageContext pageContext) throws IOException {
        writeOut(writer, content.getString(), currentCache, debug, container, cacheKey, pageURL, pageContext);
    }

    public static void writeOut(JspWriter writer, String content, boolean currentCache, boolean debug,
                                JahiaContainer container, String cacheKey, String pageURL, PageContext pageContext) throws IOException {
        if (debug) {
            if (currentCache) {
                writer.println("<fieldset><legend align=\"right\">written to cache (at " + dateFormat.format(new Date()) + ")</legend>");
            } else {
                writer.println("<fieldset><legend align=\"right\">not cached (now " + dateFormat.format(new Date()) + ")</legend>");
            }
        }
        boolean b = currentCache && !content.contains("<!-- cache:include src=") && org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
        if (b) {
            writer.println("<!-- cache:include src=\"" + pageURL + "?ctnid=" + (container!=null?container.getID():"0") + "&cacheKey=" + cacheKey + "\" -->");
        }
        writer.println(content);        
        if (b) {
            writer.println("<!-- /cache:include -->");
        }
        if (debug) {
            writer.println("</fieldset>");
        }
    }

    public static void writeOutFromCache(JspWriter writer, String content, boolean debug, JahiaContainer container,
                                         String cacheKey, String pageURL, PageContext pageContext, Date expireDate) throws IOException {
        if (debug) {
            writer.print("<fieldset><legend align=\"right\">getting from cache (will expire at "+dateFormat.format(expireDate)+")</legend>");
        }
        final boolean b = content.contains("<!-- cache:include src=") && org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated();
        if(!b && org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated()) {
            writer.print("<!-- cache:include src=\""+pageURL+"?ctnid="+container.getID()+"&cacheKey="+cacheKey+"\" -->");
        }
        writer.print(content);
        if(!b && org.jahia.settings.SettingsBean.getInstance().isOutputContainerCacheActivated()) {
            writer.println("<!-- /cache:include -->");
        }
        if (debug) {
            writer.println("</fieldset>");
        }
    }

}
