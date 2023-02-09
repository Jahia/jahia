/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.Suggestion;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.*;
import java.util.List;

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
                SearchCriteria suggestedCriteria = cloneCriteria(criteria);
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
                    criteria, getRenderContext(), maxTermsToSuggest);
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
                    suggestedCriteria = cloneCriteria(criteria);
                    suggestedCriteria.getTerms().get(0).setTerm(suggestion.getSuggestedQuery());
                }
            }
        }

        return suggestedCriteria;
    }

    private static SearchCriteria cloneCriteria(SearchCriteria criteria) {
        SearchCriteria clone;

        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream(512);
            out = new ObjectOutputStream(bos);
            out.writeObject(criteria);
            in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
            clone = (SearchCriteria) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new JahiaRuntimeException("Cannot clone criteria object", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return clone;
    }

    public void setRunQuery(boolean runQuery) {
        this.runQuery = runQuery;
    }

    public void setMaxTermsToSuggest(int maxTermsToSuggest) {
        this.maxTermsToSuggest = maxTermsToSuggest;
    }
}
