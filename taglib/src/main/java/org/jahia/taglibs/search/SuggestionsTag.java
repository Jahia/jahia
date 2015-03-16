/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.taglibs.search;

import java.util.List;

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
    
    private boolean runQuery = true;
    
    private int maxTermsToSuggest = 1;

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

    @Override
    public int doStartTag() throws JspException {
        int retVal = super.doStartTag();
        
        if (retVal == SKIP_BODY && !runQuery) {
            retVal = EVAL_BODY_INCLUDE;
        } else if (retVal == EVAL_BODY_INCLUDE) {
            final Object countVarValue = pageContext.getAttribute(getCountVar());
            int count = countVarValue == null ? 0 : (Integer) countVarValue;
            List<String> allSuggestions = suggestion.getAllSuggestions();
            int iterationCount = 1;
            while (count == 0 && iterationCount < allSuggestions.size()) {
                SearchCriteria criteria = (SearchCriteria)pageContext.getAttribute(getSearchCriteriaVar());
                SearchCriteria suggestedCriteria = (SearchCriteria) SerializationUtils.clone(criteria);
                suggestedCriteria.getTerms().get(0).setTerm(allSuggestions.get(iterationCount));
                
                count = searchAndSetAttributes(suggestedCriteria, getRenderContext());
                if (count > 0) {
                    suggestion.setSuggestedQuery(allSuggestions.get(iterationCount));
                }
                
                iterationCount++;
            }
        }
        
        return retVal;
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
        runQuery = true;
        maxTermsToSuggest = 1;
        super.resetState();
    }

    public void setSuggestionVar(String suggestionVar) {
        this.suggestionVar = suggestionVar;
    }

    private SearchCriteria suggest(SearchCriteria criteria) {
        SearchCriteria suggestedCriteria = null;
        if (!criteria.getTerms().isEmpty() && !criteria.getTerms().get(0).isEmpty()) {
            suggestion = ServicesRegistry.getInstance().getSearchService().suggest(
                    criteria.getTerms().get(0).getTerm(), getRenderContext(), maxTermsToSuggest);
            if (logger.isDebugEnabled()) {
                logger.debug("Suggestion for search query '" + criteria.getTerms().get(0).getTerm() + "' site '"
                        + getRenderContext().getSite().getSiteKey() + "' and locale "
                        + getRenderContext().getMainResourceLocale() + ": " + suggestion);
            }
            if (suggestion != null) {
                if (suggestionVar != null) {
                    pageContext.setAttribute(suggestionVar, suggestion);
                }
                if (runQuery) {
                    // we've found a suggestion
                    suggestedCriteria = (SearchCriteria) SerializationUtils.clone(criteria);
                    suggestedCriteria.getTerms().get(0).setTerm(suggestion.getSuggestedQuery());
                }    
            }
        }

        return suggestedCriteria;
    }

    public void setRunQuery(boolean runQuery) {
        this.runQuery = runQuery;
    }

    public void setMaxTermsToSuggest(int maxTermsToSuggest) {
        this.maxTermsToSuggest = maxTermsToSuggest;
    }
}
