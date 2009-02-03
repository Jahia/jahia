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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;
import org.jahia.content.ContentContainerKey;
import org.jahia.data.beans.ContainerBean;
import org.jahia.engines.search.FileSearchViewHandler;
import org.jahia.engines.search.SearchViewHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.services.search.savedsearch.JahiaSavedSearchView;
import org.jahia.services.search.savedsearch.JahiaSavedSearchViewSettings;
import org.jahia.taglibs.utility.Utils;
import org.jahia.taglibs.template.container.ContainerTag;

/**
 * Uses view settings for the saved search results display, i.e. displayed table
 * columns, default sorting.
 * <p>
 * View settings are based on the current saved search container ID, saved
 * search query ID and the user key. If no settings were provided for the
 * current saved search box yet, uses default view settings, configured in the
 * applicationcontext-savesearch.xml file. This tag should be nested into the
 * 'results' tag and the 'content:container' tag.
 * </p>
 * <p>
 * The retrieved view settings will be applied to child 'resultTable' tag or can
 * be exposed into the page scope under the name, provided by 'var' attribute,
 * and can be passed as an attribute 'viewSettings' into the 'resultTable' tag
 * to control the view parameters.
 * </p>
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class ResultTableSettingsTag extends TagSupport {

    private static final String DEF_ICON = "images/columns.gif";

    private static final String DEF_TITLE = "boxContainer.savedSearchBox.customizeView";

    private static final String DEF_VAR = "vewiSettings";

    private static final transient Logger logger = Logger
            .getLogger(ResultTableSettingsTag.class);

    private boolean allowChanges = true;

    private String contextId;

    private String icon = DEF_ICON;

    private String title = DEF_TITLE;

    private String var = DEF_VAR;

    private JahiaSavedSearchView view;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {

        JahiaSavedSearch query = getStoredQuery();
        try {
            view = ServicesRegistry.getInstance().getJahiaSearchService()
                    .getSavedSearchView(
                            SearchViewHandler.SEARCH_MODE_JCR,
                            query != null ? query.getId() : 0,
                            getContextId(),
                            query != null ? query.getSearchViewHandlerClass()
                                    : FileSearchViewHandler.class.getName(),
                            Utils.getProcessingContext(pageContext));
        } catch (JahiaException e) {
            logger.error("Unable to retrieve the saved search view object", e);
            throw new JspTagException(e);
        }

        if (allowChanges && ((ResultsTag) findAncestorWithClass(this,
                ResultsTag.class)).getCount() > 0) {
            renderCustomizeViewButton();
        }

        return EVAL_BODY_INCLUDE;
    }

    private String getContextId() throws JspTagException {
        if (null == contextId) {
            ContainerTag containerTag = (ContainerTag) findAncestorWithClass(
                    this, ContainerTag.class);
            if (containerTag != null) {
                contextId = ContentContainerKey
                        .toObjectKeyString(((ContainerBean) pageContext
                                .getAttribute(containerTag.getId())).getID());

            } else {
                throw new JspTagException(
                        "Parent tag not found. "
                                + "This tag must be enclosed into the '<content:container>' tag.");
            }
        }

        return contextId;
    }

    private String getIconUrl() {
        String path = icon.startsWith("/") ? icon : Utils.getJahiaBean(
                pageContext).getIncludes().getWebPath().lookup(icon);

        return ((HttpServletRequest) pageContext.getRequest()).getContextPath()
                + (path.startsWith("/") ? path : "/" + path);
    }

    private JahiaSavedSearch getStoredQuery() throws JspTagException {
        ResultsTag resultsTag = (ResultsTag) findAncestorWithClass(this,
                ResultsTag.class);
        if (resultsTag == null) {
            throw new JspTagException("Parent tag not found. "
                    + "This tag must be enclosed into the 'results' tag");
        }
        return resultsTag.getStoredQuery();
    }

    private String getTitle() {
        return title.equals(DEF_TITLE) ? Utils.getJahiaBean(pageContext)
                .getI18n().get(DEF_TITLE) : title;
    }

    public String getVar() {
        return var;
    }

    public JahiaSavedSearchViewSettings getViewSettings() {
        return view.getSettings();
    }

    @Override
    public void release() {
        resetState();
        super.release();
    }

    private void renderCustomizeViewButton() throws JspTagException {
        Map<String, Object> params = new HashMap<String, Object>(4);
        params.put("searchMode", view.getSearchMode());
        params.put("saveSearchId", view.getSavedSearchId());
        params.put("contextId", view.getContextId());
        params.put("viewConfigName", view.getName());
        String customizeViewURL = null;
        try {
            customizeViewURL = EnginesRegistry.getInstance().getEngineByBeanName("customizeSaveSearchViewEngine")
                    .renderLink(Utils.getProcessingContext(pageContext),
                            params);
        } catch (JahiaException e) {
            throw new IllegalArgumentException(e);
        }
        XhtmlOutput out = new XhtmlOutput(pageContext.getOut());

        try {
            out.emptyElem("input").attr("type", "image").attr("title",
                    getTitle()).attr("src", getIconUrl());
            out
                    .attr(
                            "onclick",
                            "var myWin=window.open('"
                                    + customizeViewURL
                                    + "','customizeSaveSearchView',"
                                    + "'width=500,height=650,"
                                    + "left=10,top=10,resizable=yes,scrollbars=no,status=no');"
                                    + " myWin.focus();").end();
        } catch (IOException e) {
            throw new JspTagException(e);
        }
    }

    private void resetState() {
        var = DEF_VAR;
        icon = DEF_ICON;
        title = DEF_TITLE;
        contextId = null;
        allowChanges = true;
        view = null;
    }

    public void setAllowChanges(boolean allowChanges) {
        this.allowChanges = allowChanges;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
