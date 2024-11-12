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
package org.jahia.taglibs.search;

import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.Hit;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteriaFactory;
import org.jahia.services.search.SearchResponse;
import org.jahia.settings.SettingsBean;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;

/**
 * Performs the content search and exposes search results for being displayed.
 *
 * @author Sergiy Shyrkov
 */
public class ResultsTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 2848686280888802590L;

    private String countVar;

    private String approxCountVar;

    private SearchResponse searchResponse;
    private List<Hit<?>> hits;

    private String searchCriteriaBeanName;

    private String searchResponseVar;

    private String searchCriteriaVar;

    private String termVar;

    private String var;

    private String basePagePath = SettingsBean.getInstance().getPropertiesFile().getProperty("search.basePathPath", "/sites/");;

    private boolean allowEmptySearchTerm = false;

    private final int MAX_LIMIT = SettingsBean.getInstance().getMaxSearchLimit();

    private int limit = MAX_LIMIT;
    private long offset = 0;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(getVar(), PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(getCountVar(), PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(getApproxCountVar(), PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(getSearchCriteriaVar(), PageContext.PAGE_SCOPE);
        pageContext.removeAttribute(getTermVar(), PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {

        RenderContext renderContext = getRenderContext();
        SearchCriteria criteria = getSearchCriteria(renderContext);

        if (null == criteria) {
            return SKIP_BODY;
        }

        final int result = searchAndSetAttributes(criteria, renderContext);

        return result < 0 ? SKIP_BODY : EVAL_BODY_INCLUDE;
    }

    protected int searchAndSetAttributes(SearchCriteria criteria, RenderContext renderContext) {
        criteria.setLimit(limit);
        criteria.setOffset(offset);

        if (!allowEmptySearchTerm && criteria.isEmpty()) {
            // if we don't have criteria and no searches, no need to go further since we don't have any hits, no need to further process
            // and run into an NPE with a null SearchResponse after...
            return -1;
        }

        searchResponse = ServicesRegistry.getInstance().getSearchService().search(criteria, renderContext);
        hits = searchResponse.getResults();
        pageContext.setAttribute(getVar(), hits);

        if (StringUtils.isNotBlank(getSearchResponseVar())) {
            pageContext.setAttribute(getSearchResponseVar(), searchResponse);
        }

        int count = hits.size();
        if (searchResponse!= null && searchResponse.hasMore()) {
            pageContext.setAttribute(getCountVar(), Integer.MAX_VALUE);
        } else {
            pageContext.setAttribute(getCountVar(), count);
        }
        pageContext.setAttribute(getApproxCountVar(), searchResponse!=null ? searchResponse.getApproxCount() : 0);
        pageContext.setAttribute(getSearchCriteriaVar(), criteria);
        if (!criteria.getTerms().isEmpty() && !criteria.getTerms().get(0).isEmpty()) {
            pageContext.setAttribute(getTermVar(), criteria.getTerms().get(0).getTerm());
        }
        return count;
    }

    /**
     * Returns the default name of the <code>countVar</code> variable if not
     * provided.
     *
     * @return the default name of the <code>countVar</code> variable if not
     *         provided
     */
    protected String getCountVar() {
        return countVar != null ? countVar : getDefaultCountVarName();
    }

    /**
     * Returns the default name of the <code>approxCountVar</code> variable if not
     * provided.
     *
     * @return the default name of the <code>approxCountVar</code> variable if not
     *         provided
     */
    private String getApproxCountVar() {
        return approxCountVar != null ? approxCountVar : getDefaultApproxCountVarName();
    }

    protected String getDefaultCountVarName() {
        return "count";
    }

    protected String getDefaultApproxCountVarName() {
        return "approxCount";
    }

    protected String getDefaultSearchCriteriaVarName() {
        return "searchCriteria";
    }

    protected String getDefaultTermVarName() {
        return "term";
    }

    /**
     * Returns the default name of the <code>var</code> variable if not
     * provided.
     *
     * @return the default name of the <code>var</code> variable if not provided
     */
    protected String getDefaultVarName() {
        return "hits";
    }

    /**
     * Returns a list of {@link Hit} objects that are results of the query.
     *
     * @return a list of {@link Hit} objects that are results of the query
     */
    public List<Hit<?>> getHits() {
        return hits;
    }

    /**
     * Obtains the {@link SearchCriteria} bean to execute the search with.
     *
     * @param ctx current rendering context
     * @return the {@link SearchCriteria} bean to execute the search with
     */
    protected SearchCriteria getSearchCriteria(RenderContext ctx) {
        SearchCriteria criteria = searchCriteriaBeanName != null ? (SearchCriteria) pageContext.getAttribute(searchCriteriaBeanName)
                : SearchCriteriaFactory.getInstance(ctx);

        String[] values = (criteria != null && criteria.getPagePath() != null) ? criteria.getPagePath().getValues() : null;
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                if (StringUtils.isNotEmpty(value) && !value.startsWith(basePagePath)) {
                    values[i] = basePagePath;
                }
            }
        }

        return criteria;
    }

    protected String getSearchCriteriaVar() {
    	return searchCriteriaVar != null ? searchCriteriaVar : getDefaultSearchCriteriaVarName();
    }

    private String getTermVar() {
    	return termVar != null ? termVar : getDefaultTermVarName();
    }

    private String getVar() {
        return var != null ? var : getDefaultVarName();
    }

    @Override
    protected void resetState() {
        var = null;
        countVar = null;
        searchResponse = null;
        hits = null;
        searchCriteriaBeanName = null;
        searchCriteriaVar = null;
        termVar = null;
        allowEmptySearchTerm = false;
        limit = MAX_LIMIT;
        offset = 0;
        basePagePath = SettingsBean.getInstance().getPropertiesFile().getProperty("search.basePathPath", "/sites/");
        super.resetState();
    }

    public void setCountVar(String countVar) {
        this.countVar = countVar;
    }

    public void setApproxCountVar(String approxCountVar) {
        this.approxCountVar = approxCountVar;
    }

    public void setSearchCriteriaBeanName(String searchCriteriaBeanName) {
        this.searchCriteriaBeanName = searchCriteriaBeanName;
    }

    public void setSearchCriteriaVar(String searchCriteriaVar) {
    	this.searchCriteriaVar = searchCriteriaVar;
    }

    public void setTermVar(String termVar) {
    	this.termVar = termVar;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setAllowEmptySearchTerm(boolean allowEmptySearchTerm) {
        this.allowEmptySearchTerm = allowEmptySearchTerm;
    }

    protected String getSearchResponseVar() {
        return searchResponseVar;
    }

    public void setSearchResponseVar(String searchResponseVar) {
        this.searchResponseVar = searchResponseVar;
    }

    public void setBasePagePath(String basePagePath) {
        this.basePagePath = basePagePath;
    }
}
