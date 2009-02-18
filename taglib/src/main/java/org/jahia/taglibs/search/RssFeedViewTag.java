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

package org.jahia.taglibs.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.jahia.data.beans.JahiaBean;
import org.jahia.engines.search.Hit;
import org.jahia.engines.search.SearchCriteriaFactory;
import org.jahia.engines.search.SearchCriteria.SearchMode;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.AbstractJahiaTag;

import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.module.opensearch.OpenSearchModule;
import com.sun.syndication.feed.module.opensearch.entity.OSQuery;
import com.sun.syndication.feed.module.opensearch.impl.OpenSearchModuleImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Produces an RSS feed (different formats) view of the search results.
 * 
 * @author Sergiy Shyrkov
 */
public class RssFeedViewTag extends AbstractJahiaTag {

    private static final String DEF_TYPE = "rss_2.0";

    private static final String DEF_VAR = "feed";

    private static final transient Logger logger = Logger
            .getLogger(RssFeedViewTag.class);

    private String description;

    private boolean display = true;

    private String title;

    private String type = DEF_TYPE;

    private String var = DEF_VAR;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        ResultsTag parent = (ResultsTag) findAncestorWithClass(this,
                ResultsTag.class);
        if (null == parent) {
            throw new JspTagException("Parent tag not found. This tag ("
                    + this.getClass().getName()
                    + ") must be nested inside the "
                    + ResultsTag.class.getName());
        }

        List<Hit> results = parent.getHits();

        SyndFeed feed = getFeed(results);

        if (display) {
            SyndFeedOutput output = new SyndFeedOutput();
            try {
                output.output(feed, pageContext.getOut());
            } catch (Exception e) {
                throw new JspTagException(
                        "Error writing RSS feed content with search results.",
                        e);
            }

        }

        pageContext.setAttribute(var, feed);

        return display ? SKIP_BODY : EVAL_BODY_INCLUDE;
    }

    private Link getDescriptorLink() {
        Link link = new Link();
        link.setType("application/opensearchdescription+xml");
        link.setTitle(title);
        JahiaBean jahiaBean = getJahiaBean();
        try {
            link
                    .setHref(jahiaBean.getSite().getExternalUrl()
                            + "?template="
                            + URLEncoder
                                    .encode(
                                            jahiaBean
                                                    .getIncludes()
                                                    .getTemplatePath()
                                                    .lookup(
                                                            "/opensearch/descriptor-"
                                                                    + (isFileSearchMode() ? "files"
                                                                            : "pages")
                                                                    + "-rss.jsp"),
                                            pageContext.getResponse()
                                                    .getCharacterEncoding() != null ? pageContext
                                                    .getResponse()
                                                    .getCharacterEncoding()
                                                    : SettingsBean
                                                            .getInstance()
                                                            .getDefaultResponseBodyEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return link;
    }

    private SyndFeed getFeed(List<Hit> results) {
        SyndFeed feed = new SyndFeedImpl();

        OpenSearchModuleImpl osm = new OpenSearchModuleImpl();
        osm.setTotalResults(results.size());

        osm.setLink(getDescriptorLink());

        OSQuery query = new OSQuery();
        query.setRole("request");
        query.setSearchTerms(SearchCriteriaFactory.getInstance(
                getProcessingContext()).getTerms().get(0).getTerm());
        osm.addQuery(query);

        List<OpenSearchModule> modules = feed.getModules();
        modules.add(osm);
        feed.setModules(modules);

        feed.setFeedType(type);
        JahiaBean jBean = getJahiaBean();
        String feedTitle = title != null ? title : jBean.getSite().getTitle()
                + " - "
                + (isFileSearchMode() ? "document repository" : "content")
                + " (RSS)";
        feed.setTitle(feedTitle);
        feed.setDescription(description != null ? description : feedTitle);

        feed.setLink("Link");

        List<SyndEntry> entries = new ArrayList<SyndEntry>();

        JahiaSearchService searchService = ServicesRegistry.getInstance()
                .getJahiaSearchService();
        ParamBean ctx = (ParamBean) getProcessingContext();
        String serverUrl = getTargetUrl();
        for (Hit hit : results) {
            try {
                SyndEntry entry = searchService.getSyndEntry(hit, ctx,
                        serverUrl);
                if (entry != null) {
                    entries.add(entry);
                }
            } catch (Exception e) {
                logger.warn("Exception occured creating SyndEntry from hit "
                        + hit.getLink() + ". Cause: " + e.getMessage(), e);
            }
        }
        feed.setEntries(entries);

        return feed;
    }

    private String getTargetUrl() {
        String serverUrl = getJahiaBean().getSite().getServerName();
        int port = SettingsBean.getInstance().getSiteURLPortOverride();
        port = port > 0 ? port : pageContext.getRequest().getServerPort();
        return pageContext.getRequest().getScheme() + "://" + serverUrl
                + (port != 80 ? ":" + port : "");
    }

    private boolean isFileSearchMode() {
        return SearchMode.FILES == SearchCriteriaFactory.getInstance(
                getProcessingContext()).getMode();
    }

    @Override
    protected void resetState() {
        description = null;
        display = true;
        title = null;
        type = DEF_TYPE;
        var = DEF_VAR;
        super.resetState();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisplay(boolean display) {
        this.display = display;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVar(String var) {
        this.var = var;
    }

}
