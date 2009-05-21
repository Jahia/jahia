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
package org.jahia.ajax.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.PageSearchResultBuilderImpl;

/**
 * @author Xavier Lawrence
 */
public class SearchAutoFullFillAction extends AjaxAction {
    private static final String SEARCH_STRING = "searchString";
    private static final String WORD_LIMIT = "wordLimit";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SearchAutoFullFillAction.class);

    private static final JahiaSearchService searchService = servicesRegistry.getJahiaSearchService();

    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);
            final String searchString = getParameter(request, SEARCH_STRING);
            int wordLimit = 5;
            try {
                wordLimit = Integer.parseInt(getParameter(request, WORD_LIMIT, "5"));
            } catch (Exception e) {
                // Ignore
            }
            final Iterator<String> terms = searchService.getTerms(jParams.getSiteID(), searchString);
            final Set<String> result = new HashSet<String>();
            final StringBuffer buff = new StringBuffer();
            int count = 0;
            while (terms.hasNext()) {
                String term = cleanUpTerm((String) terms.next());
                final StringBuffer query = new StringBuffer("(").append(term);
                if (!searchString.endsWith("*")) {
                    query.append("*");
                }
                query.append(")");
                final PageSearchResultBuilderImpl resultBuilder = new PageSearchResultBuilderImpl(true);
                final List<String> languageCodes = new ArrayList<String>();
                languageCodes.add(jParams.getLocale().toString());
                final JahiaSearchResult searchResults = searchService.search(jParams.getSiteID(),
                        StringEscapeUtils.unescapeHtml(query.toString()), jParams, languageCodes, resultBuilder);
                if (searchResults.getHitCount() > 0) {
                    if (!result.contains(term)) {
                        result.add(term);
                        buff.append(term);
                        count++;
                    }
                    if (count == (wordLimit - 1)) {
                        break;
                    } else {
                        buff.append(",");
                    }
                }
            }
            sendResponse(buff.toString(), response);

        } catch (Exception e) {
            logger.error(e, e);
        }

        return null;
    }

    protected String cleanUpTerm(final String term) {
        String result = term;
        if (term.indexOf("@") > -1) {
            result = term.substring(0, term.indexOf("@"));
        }
        if (result.indexOf(".") > -1) {
            result = result.substring(0, result.indexOf("."));
        }

        if (result.indexOf(",") > -1) {
            result = result.substring(0, result.indexOf(","));
        }

        if (result.indexOf(";") > -1) {
            result = result.substring(0, result.indexOf(";"));
        }
        return result;
    }
}
