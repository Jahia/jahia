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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.engines.search;

import static org.jahia.services.search.JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD;
import static org.jahia.services.search.JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_CREATION_DATE;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_CREATOR;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_LAST_CONTRIBUTOR;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_LAST_MODIFICATION_DATE;
import static org.jahia.services.search.JahiaSearchConstant.METADATA_PAGE_PATH;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.engines.search.SearchCriteria.DateValue;
import org.jahia.engines.search.SearchCriteria.SearchMode;
import org.jahia.engines.search.SearchCriteria.Term;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.search.NumberPadding;
import org.jahia.services.search.PageSearcher;
import org.jahia.utils.DateUtils;

/**
 * Handler class for page search.
 * 
 * @author Sergiy Shyrkov
 */
public class PagesSearchViewHandler extends AbstractSearchViewHandler {

    private static Logger logger = Logger
            .getLogger(PagesSearchViewHandler.class);

    private void appendQueryClause(StringBuilder query, String clause) {
        if (clause != null) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(clause);
        }
    }

    private String buildQuery(SearchCriteria criteria, ProcessingContext ctx) {
        StringBuilder query = new StringBuilder(64);

        appendQueryClause(query, getRawQuery(criteria, ctx));
        appendQueryClause(query, getAuthorAndDateQuery(criteria, ctx));
        appendQueryClause(query, getTermQuery(criteria, ctx));
        appendQueryClause(query, getPagePathQuery(criteria, ctx));

        return query.toString();
    }

    private String getAuthorAndDateQuery(SearchCriteria criteria,
            ProcessingContext ctx) {

        StringBuilder query = new StringBuilder();

        // creator
        if (StringUtils.isNotBlank(criteria.getCreatedBy())) {
            query.append(METADATA_CREATOR + ":(").append(
                    criteria.getCreatedBy()).append(")");
        }

        // last editor
        if (StringUtils.isNotBlank(criteria.getLastModifiedBy())) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(METADATA_LAST_CONTRIBUTOR + ":(").append(
                    criteria.getLastModifiedBy()).append(")");
        }

        // creation date
        String dateQuery = getDateQuery(criteria.getCreated(),
                METADATA_CREATION_DATE);
        if (dateQuery != null) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(dateQuery);
        }

        // last modification date
        dateQuery = getDateQuery(criteria.getLastModified(),
                METADATA_LAST_MODIFICATION_DATE);
        if (dateQuery != null) {
            if (query.length() > 0) {
                query.append(" AND ");
            }
            query.append(dateQuery);
        }

        return query.length() > 0 ? query.toString() : null;
    }

    private String getDateQuery(DateValue date, String fieldName) {
        String query = null;

        if (!date.isEmpty() && DateValue.Type.ANYTIME != date.getType()) {
            Calendar from = Calendar.getInstance();
            Calendar to = null;

            if (DateValue.Type.TODAY == date.getType()) {
                // no date adjustment needed
            } else if (DateValue.Type.LAST_WEEK == date.getType()) {
                from.add(Calendar.DAY_OF_MONTH, -7);
            } else if (DateValue.Type.LAST_MONTH == date.getType()) {
                from.add(Calendar.MONTH, -1);
            } else if (DateValue.Type.LAST_THREE_MONTHS == date.getType()) {
                from.add(Calendar.MONTH, -3);
            } else if (DateValue.Type.LAST_SIX_MONTHS == date.getType()) {
                from.add(Calendar.MONTH, -6);
            } else if (DateValue.Type.RANGE == date.getType()) {
                if (date.getFromAsDate() != null) {
                    from.setTime(date.getFromAsDate());
                } else {
                    from = null;
                }
                if (date.getToAsDate() != null) {
                    to = Calendar.getInstance();
                    to.setTime(date.getToAsDate());
                } else {
                    to = null;
                }
            } else {
                throw new IllegalArgumentException("Unknown date value type '"
                        + date.getType() + "'");
            }

            query = new StringBuilder(64).append(fieldName).append(":[")
                    .append(
                            NumberPadding.pad(from != null ? DateUtils
                                    .dayStart(from).getTimeInMillis() : 1))
                    .append(" TO ").append(
                            to != null ? NumberPadding.pad(DateUtils.dayEnd(to)
                                    .getTimeInMillis()) : "999999999999999")
                    .append("]").toString();
        }

        return query;
    }

    private String getPagePathQuery(SearchCriteria criteria,
            ProcessingContext ctx) {
        String query = null;
        if (!criteria.getPagePath().isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Page path: " + criteria.getPagePath());
            }
            String path = null;
            int pageId = 0;
            try {
                pageId = Integer.parseInt(criteria.getPagePath().getValue());
            } catch (NumberFormatException e) {
                logger.warn("Illegal value for the page ID in page path '"
                        + criteria.getPagePath().getValue() + "'", e);
            }
            if (pageId > 0) {
                try {
                    ContentPage page = ContentPage.getPage(pageId, false);
                    if (page != null) {
                        path = page.getPagePathString(ctx);
                    }
                } catch (JahiaException e) {
                    logger.warn("Unable to retrieve page with ID '" + pageId
                            + "'", e);
                }
            }
            if (path != null) {
                query = new StringBuilder(64).append(METADATA_PAGE_PATH + ":").append(
                        path).append(
                        criteria.getPagePath().isIncludeChildren() ? "*" : "")
                        .toString();
            }
        }

        return query;
    }

    private PageSearcher getPageSearcher(ProcessingContext ctx) {
        SearchCriteria criteria = SearchCriteriaFactory.getInstance(ctx);
        List<String> languageCodes = new LinkedList<String>();
        if (!criteria.getLanguages().isEmpty()) {
            for (String lang : criteria.getLanguages().getValues()) {
                if (lang != null && lang.length() > 0) {
                    languageCodes.add(lang);
                }
            }
        }

        if (languageCodes.isEmpty()) {
            // if language codes are not specified, add current lanaguage
            languageCodes.add(ctx.getLocale().toString());
        }

        return new PageSearcher(new String[] { ServicesRegistry.getInstance()
                .getJahiaSearchService().getSearchHandler(ctx.getSiteID())
                .getName() }, languageCodes);
    }

    private String getRawQuery(SearchCriteria criteria, ProcessingContext ctx) {
        return StringUtils.isNotBlank(criteria.getRawQuery()) ? criteria
                .getRawQuery().trim() : null;
    }

    private String getTerm(Term term, ProcessingContext ctx) {

        // trim the whole text, replace special logical keywords and escape the
        // text
        String text = term.getTerm().trim();

        if (Term.MatchType.AS_IS != term.getMatch()) {
            text = QueryParser.escape(text.replaceAll(" AND ", " and ").replaceAll(
                    " OR ", " or ").replaceAll(" NOT ", " not "));
        }

        if (Term.MatchType.ANY_WORD == term.getMatch()) {
            // leave the term as it is
        } else if (Term.MatchType.ALL_WORDS == term.getMatch()) {
            // replace any group or a single whitespace with a whitespace and
            // '+' sign, indicating mandatory term
            text = "+" + text.replaceAll("\\s{1,}", " +");
        } else if (Term.MatchType.EXACT_PHRASE == term.getMatch()) {
            // enclose the whole phrase into quotes
            text = '"' + text + '"';
        } else if (Term.MatchType.WITHOUT_WORDS == term.getMatch()) {
            // replace any group or a single whitespace with a whitespace and
            // '-' sign, indicating term negation
            text = "-" + text.replaceAll("\\s{1,}", " -");
        } else if (Term.MatchType.AS_IS == term.getMatch()) {
            // we pass the value without any modification (escaping, rewriting
            // etc.)
        } else {
            throw new IllegalArgumentException("Unknown term match type '"
                    + term.getMatch() + "'");
        }

        return text;
    }

    private String getTermQuery(SearchCriteria criteria, ProcessingContext ctx) {

        StringBuilder query = new StringBuilder(64);
        boolean doSearchAllFields = true;
        SearchMode mode = criteria.getModeAutodetect();

        for (Term textSearch : criteria.getTerms()) {
            if (!textSearch.isEmpty()
                    && !textSearch.getFields().areAllSelected(mode)) {
                doSearchAllFields = false;
                break;
            }
        }

        if (criteria.getTerms().size() > 0) {
            for (Term textSearch : criteria.getTerms()) {
                if (!textSearch.isEmpty()) {
                    if (query.length() > 0) {
                        query.append(" ");
                    }
                    String term = getTerm(textSearch, ctx);
                    if (doSearchAllFields) {
                        query.append(term);
                    } else {
                        if (textSearch.getFields().isContent()) {
                            query.append(CONTENT_FULLTEXT_SEARCH_FIELD + ":(")
                                    .append(term).append(")");
                        }

                        if (textSearch.getFields().isMetadata()) {
                            if (textSearch.getFields().isContent()) {
                                query.append(" ");
                            }
                            query.append(METADATA_FULLTEXT_SEARCH_FIELD + ":(")
                                    .append(term).append(")");
                        }

                    }
                }
            }
        }
        if (query.length() > 0 && doSearchAllFields) {
            query.insert(0, ALL_FULLTEXT_SEARCH_FIELD + ":(").append(")");
        }

        return query.length() > 0 ? query.toString() : null;
    }

    @Override
    public JahiaSearchResult search(ProcessingContext ctx)
            throws JahiaException {

        SearchCriteria criteria = SearchCriteriaFactory.getInstance(ctx);
        if (null == criteria) {
            throw new IllegalArgumentException(
                    "Search criteria are not specified."
                            + " Unable to perform search.");
        }

        if (criteria.isEmpty()) {
            logger.info("Search parameters are empty. Skipping search");
            return null;
        }

        String query = buildQuery(criteria, ctx);
        if (logger.isDebugEnabled()) {
            logger.debug("Executing page search for query: " + query);
        }

        JahiaSearchResult searchResult = getPageSearcher(ctx)
                .search(query, ctx);

        if (logger.isDebugEnabled()) {
            logger.debug("Found: " + searchResult.getHitCount() + " hits");
        }

        return searchResult;
    }
}
