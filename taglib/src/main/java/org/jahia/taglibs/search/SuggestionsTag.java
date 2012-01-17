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

package org.jahia.taglibs.search;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.Suggestion;

/**
 * Performs the content search and exposes search results for being displayed.
 * 
 * @author Sergiy Shyrkov
 */
public class SuggestionsTag extends ResultsTag {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SuggestionsTag.class);

    private static final long serialVersionUID = -4991766714209759529L;

    private Suggestion suggestion;

    private String suggestionVar = "suggestion";

    @Override
    public int doEndTag() throws JspException {
        if (suggestionVar != null) {
            pageContext.removeAttribute(suggestionVar, PageContext.PAGE_SCOPE);
        }
        return super.doEndTag();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.taglibs.search.ResultsTag#getDefaultCountVarName()
     */
    @Override
    protected String getDefaultCountVarName() {
        return "suggestedCount";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.taglibs.search.ResultsTag#getDefaultVarName()
     */
    @Override
    protected String getDefaultVarName() {
        return "suggestedHits";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jahia.taglibs.search.ResultsTag#getSearchCriteria(org.jahia.services
     * .render.RenderContext)
     */
    @Override
    protected SearchCriteria getSearchCriteria(RenderContext ctx) {
        SearchCriteria criteria = super.getSearchCriteria(ctx);
        return criteria != null ? suggest(criteria) : null;
    }

    /**
     * @return the suggestion
     */
    public Suggestion getSuggestion() {
        return suggestion;
    }

    @Override
    protected void resetState() {
        suggestionVar = "suggestion";
        suggestion = null;
        super.resetState();
    }

    public void setSuggestionVar(String suggestionVar) {
        this.suggestionVar = suggestionVar;
    }

    private SearchCriteria suggest(SearchCriteria criteria) {
        SearchCriteria suggestedCriteria = null;
        if (!criteria.getTerms().isEmpty() && !criteria.getTerms().get(0).isEmpty()) {
            suggestion = ServicesRegistry.getInstance().getSearchService().suggest(
                    criteria.getTerms().get(0).getTerm(), getRenderContext().getSite().getSiteKey(),
                    getRenderContext().getMainResourceLocale());
            if (logger.isDebugEnabled()) {
                logger.debug("Suggestion for search query '" + criteria.getTerms().get(0).getTerm() + "' site '"
                        + getRenderContext().getSite().getSiteKey() + "' and locale "
                        + getRenderContext().getMainResourceLocale() + ": " + suggestion);
            }
            if (suggestion != null) {
                if (suggestionVar != null) {
                    pageContext.setAttribute(suggestionVar, suggestion);
                }
                // we've found a suggestion
                suggestedCriteria = (SearchCriteria) SerializationUtils.clone(criteria);
                suggestedCriteria.getTerms().get(0).setTerm(suggestion.getSuggestedQuery());
            }
        }

        return suggestedCriteria;
    }
}
