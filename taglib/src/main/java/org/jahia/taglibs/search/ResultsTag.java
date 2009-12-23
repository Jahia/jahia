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
package org.jahia.taglibs.search;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.Hit;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteriaFactory;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.utility.Utils;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Performs the content search and exposes search results for being displayed.
 *
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class ResultsTag extends AbstractJahiaTag {

    private static final String DEF_COUNT_VAR = "count";

    private static final String DEF_VAR = "hits";

    private int count;

    private String countVar = DEF_COUNT_VAR;

    private List<Hit<?>> hits;

    private String storedQuery;

    private String var = DEF_VAR;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(countVar, PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {

        SearchCriteria criteria = null;
        ProcessingContext ctx = Utils.getProcessingContext(pageContext);
        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        try {
            criteria = // TODO implement new: storedQuery != null ? getStoredQueryCriteria() : 
                    getSearchCriteria(ctx);
        } catch (JahiaException e) {
            throw new JspTagException(e);
        }

        if (null == criteria) {
            return SKIP_BODY;
        }
        hits = ServicesRegistry.getInstance().getSearchService().search(criteria, renderContext).getResults();
        count = hits.size();

        pageContext.setAttribute(var, hits);

        pageContext.setAttribute(countVar, Integer.valueOf(count));

        return EVAL_BODY_INCLUDE;
    }

    public int getCount() {
        return count;
    }

    public String getCountVar() {
        return countVar;
    }

    public List<Hit<?>> getHits() {
        return hits;
    }

    private SearchCriteria getSearchCriteria(ProcessingContext ctx)
            throws JahiaException {
        return SearchCriteriaFactory.getInstance(ctx);
    }


    public String getVar() {
        return var;
    }

    @Override
    protected void resetState() {
        var = DEF_VAR;
        countVar = DEF_COUNT_VAR;
        count = 0;
        hits = null;
        storedQuery = null;
        super.resetState();
    }

    public void setCountVar(String countVar) {
        this.countVar = countVar;
    }

    public void setStoredQuery(String storedQuery) {
        this.storedQuery = storedQuery;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
