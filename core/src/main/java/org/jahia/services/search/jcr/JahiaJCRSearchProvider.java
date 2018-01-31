/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.search.jcr;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.util.ISO9075;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.*;
import org.jahia.services.search.SearchCriteria.DateValue;
import org.jahia.services.search.SearchCriteria.NodeProperty;
import org.jahia.services.search.SearchCriteria.Ordering;
import org.jahia.services.search.SearchCriteria.Ordering.CaseConversion;
import org.jahia.services.search.SearchCriteria.Ordering.Order;
import org.jahia.services.search.SearchCriteria.Term;
import org.jahia.services.search.SearchCriteria.Term.MatchType;
import org.jahia.services.search.SearchCriteria.Term.SearchFields;
import org.jahia.services.search.spell.CompositeSpellChecker;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.tags.TaggingService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.DateUtils;
import org.jahia.utils.Patterns;
import org.jahia.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.query.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jahia.services.content.JCRContentUtils.stringToJCRSearchExp;
import static org.jahia.services.content.JCRContentUtils.stringToQueryLiteral;

/**
 * This is the default search provider used by Jahia and used the index created by Jahia's main
 * repository, which is based on Apache Jackrabbit. The search request is also done on mounted
 * external repositories.
 * <p/>
 * For now the search criteria is converted to XPATH queries, which is despite of the deprecation
 * still the most stable and performance means to use search in Jackrabbit.
 * <p/>
 * For future versions we may change to either SQL-2 or directly the QueryObejctModel specified
 * in JSR-283.
 *
 * @author Benjamin Papez
 */
public class JahiaJCRSearchProvider implements SearchProvider, SearchProvider.SupportsSuggestion {

    private static final Pattern AND_PATTERN = Pattern.compile(" AND ");

    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s{2,}");

    private static final Pattern QUOTED_OR_PLAIN_TERMS_WITH_OPTIONAL_NEGATION_PATTERN = Pattern.compile("-*\"([^\"]*)\"|(\\S+)");

    private static final Pattern NOT_PATTERN = Pattern.compile(" NOT ");

    private static final Pattern OR_PATTERN = Pattern.compile(" OR ");
    private static final String AND = "and";
    private static final String OR = "or";

    private static final Logger logger = LoggerFactory.getLogger(JahiaJCRSearchProvider.class);

    private TaggingService taggingService = null;

    private String name;

    private Set<String> typesToHideFromSearchResults;

    @Override
    public SearchResponse search(SearchCriteria criteria, RenderContext context) {
        SearchResponse response = new SearchResponse();
        List<Hit<?>> results = new ArrayList<Hit<?>>();
        response.setResults(results);

        try {
            JCRSessionWrapper session = ServicesRegistry
                    .getInstance()
                    .getJCRStoreService()
                    .getSessionFactory()
                    .getCurrentUserSession(context.getMainResource().getWorkspace(),
                            context.getMainResource().getLocale(), context.getFallbackLocale());
            Query query = buildQuery(criteria, session);
            final int offset = criteria.getOffset() < 0 ? 0 : (int) criteria.getOffset();
            final int limit = criteria.getLimit() <= 0 ? Integer.MAX_VALUE : (int) criteria.getLimit();
            response.setOffset(offset);
            response.setLimit(limit);
            int count = 0;
            if (query != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Executing search query [{}]", query.getStatement());
                }
                QueryResult queryResult = query.execute();
                RowIterator it = queryResult.getRows();
                Set<String> languages = new HashSet<String>();
                if (it.hasNext()) {
                    if (!criteria.getLanguages().isEmpty()) {
                        for (String languageCode : criteria.getLanguages().getValues()) {
                            if (!StringUtils.isEmpty(languageCode)) {
                                languages.add(languageCode);
                            }
                        }
                    } else {
                        if (session.getLocale() != null) {
                            languages.add(session.getLocale().toString());
                        }
                    }
                }

                Set<String> usageFilterSites = !criteria.getSites().isEmpty() && !criteria.getSites().getValue().equals("-all-")
                        && !criteria.getSitesForReferences().isEmpty() ? Sets.newHashSet(criteria.getSites().getValues()) : null;

                Set<String> addedNodes = new HashSet<String>();
                Map<String, JCRNodeHit> addedHits = new HashMap<String, JCRNodeHit>();
                List<JCRNodeHit> hitsToAdd = new ArrayList<JCRNodeHit>();
                final int requiredHits = limit + offset;

                boolean displayableNodeCompat = Boolean.valueOf(SettingsBean.getInstance().getPropertiesFile().getProperty("search.displayableNodeCompat"));

                while (it.hasNext()) {
                    count++;
                    Row row = it.nextRow();
                    try {
                        JCRNodeWrapper node = (JCRNodeWrapper) row.getNode();
                        if (node != null && (node.isNodeType(Constants.JAHIANT_TRANSLATION)
                                || Constants.JCR_CONTENT.equals(node.getName()))) {
                            node = node.getParent();
                        }
                        if (node != null && node.isNodeType("jnt:vanityUrl")) {
                            node = node.getParent().getParent();
                        }
                        if (node != null && addedNodes.add(node.getIdentifier())) {
                            boolean skipNode = isNodeToSkip(node, criteria, languages);

                            JCRNodeHit hit = !skipNode ? buildHit(row, node, context, usageFilterSites) : null;

                            if (!skipNode && !displayableNodeCompat) {
                                //check if node is invisible (or don't have a displayable parent or reference)
                                skipNode = hit.getDisplayableNode() == null;
                            }
                            if (!skipNode && usageFilterSites != null
                                    && !usageFilterSites.contains(node.getResolveSite().getName())) {
                                skipNode = hit.getUsages().isEmpty();
                            }
                            if (!skipNode) {
                                hitsToAdd.add(hit);

                                if (hitsToAdd.size() + addedHits.size() >= requiredHits) {
                                    SearchServiceImpl.executeURLModificationRules(hitsToAdd, context);
                                    addHitsToResults(hitsToAdd, results, addedHits, offset);
                                    hitsToAdd.clear();

                                    if (addedHits.size() >= requiredHits) {
                                        response.setHasMore(true);
                                        if (it.getSize() > 0) {
                                            int approxCount = ((int) it.getSize() * addedHits.size() / count);
                                            approxCount = (int) Math.ceil(MathUtils.round(approxCount,
                                                    approxCount < 1000 ? -1 : (approxCount < 10000 ? -2
                                                            : -3), BigDecimal.ROUND_UP));
                                            response.setApproxCount(approxCount);
                                        }
                                        return response;
                                    }
                                }
                            }
                        }
                    } catch (ItemNotFoundException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found node is not visible or published: " + row.getPath(), e);
                        }
                    } catch (PathNotFoundException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found node is not visible or published: " + row.getPath(), e);
                        }
                    } catch (Exception e) {
                        logger.warn("Error resolving search hit", e);
                    }
                }
                if (hitsToAdd.size() > 0) {
                    SearchServiceImpl.executeURLModificationRules(hitsToAdd, context);
                    addHitsToResults(hitsToAdd, results, addedHits, offset);
                }
            }
        } catch (RepositoryException e) {
            if (e.getMessage() != null && e.getMessage().contains(ParseException.class.getName())) {
                logger.warn(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            } else {
                logger.error("Error while trying to perform a search", e);
            }
        } catch (Exception e) {
            logger.error("Error while trying to perform a search", e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Search query has {} results", results.size());
        }
        return response;
    }

    private void addHitsToResults(List<JCRNodeHit> collectedHits,
            List<Hit<?>> results, Map<String, JCRNodeHit> addedHits,
            int offset) {
        for (JCRNodeHit hit : collectedHits) {
            if (!addedHits.containsKey(hit.getLink())) {
                addedHits.put(hit.getLink(), hit);
                if (addedHits.size() >= offset) {
                    results.add(hit);
                }
            } else {
                JCRNodeHit previousHit = addedHits.get(hit.getLink());
                for (Row row : hit.getRows()) {
                    previousHit.addRow(row);
                }
            }
        }
    }

    private boolean isNodeToSkip(JCRNodeWrapper node, SearchCriteria criteria, Set<String> languages) {
        boolean skipNode = false;
        try {
            if (typesToHideFromSearchResults.contains(node.getPrimaryNodeTypeName())) {
                return true;
            }
            if (!languages.isEmpty() && isSiteSearch(criteria)
                    && (node.isFile() || node.isNodeType(Constants.NT_FOLDER))) {
                // if just site-search and no file-search, then skip the node unless it is referred
                // by a node in the wanted language - unreferenced files are skipped
                skipNode = !isFileSearch(criteria) ? true : skipNode;
                for (PropertyIterator it = node.getWeakReferences(); it.hasNext(); ) {
                    // if site-search and file-search, then skip the node unless it is referred
                    // by a node in the wanted language - unreferenced files are not skipped
                    skipNode = isFileSearch(criteria) ? true : skipNode;

                    try {
                        JCRNodeWrapper refNode = (JCRNodeWrapper) it
                                .nextProperty().getParent();
                        if (languages.contains(refNode.getLanguage())) {
                            skipNode = false;
                            break;
                        }
                    } catch (Exception e) {
                        logger.debug(
                                "Error while trying to check for node language",
                                e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Error while trying to check for node language", e);
        }

        return skipNode;
    }

    private JCRNodeHit buildHit(Row row, JCRNodeWrapper node, RenderContext context, Set<String> usageFilterSites) throws RepositoryException {
        JCRNodeHit searchHit = null;
        if (node.isFile() || node.isNodeType(Constants.NT_FOLDER)) {
            searchHit = new FileHit(node, context);
        } else {
            searchHit = new JCRNodeHit(node, context);
        }

        try {
            searchHit.setUsageFilterSites(usageFilterSites);
            searchHit.setScore((float) (row.getScore() / 1000.));

            searchHit.addRow(row);
        } catch (Exception e) {
            logger.warn("Search details cannot be retrieved", e);
        }
        return searchHit;
    }

    private String buildSQLQuery(SearchCriteria params, JCRSessionWrapper session) {
        StringBuilder query = new StringBuilder("select * from ");
        query.append("[");
        query.append(getNodeType(params));
        query.append("] as n ");

        String path = null;
        boolean includeChildren = false;
        if (!params.getFilePath().isEmpty()) {
            path = params.getFilePath().getValue().trim();
            includeChildren = params.getFilePath().isIncludeChildren();
        } else if (!params.getPagePath().isEmpty()) {
            path = params.getPagePath().getValue().trim();
            includeChildren = params.getPagePath().isIncludeChildren();
        }
        if (path != null) {
            query.append("where (");
            if (includeChildren) {
                query.append("isdescendantnode(n,'");
            } else {
                query.append("ischildnode(n,'");
            }
            query.append(JCRContentUtils.sqlEncode(path)).append("')");
            query.append(")");
        } else if (!params.getSites().isEmpty()) {
            query.append("where (");
            if ("-all-".equals(params.getSites().getValue())) {
                query.append("isdescendantnode(n,'/sites/')"); // ticket PEU-468
            } else {
                Set<String> sites = new LinkedHashSet<String>();
                for (String site : params.getSites().getValues()) {
                    sites.add(site);
                }
                if (!params.getSitesForReferences().isEmpty()) {
                    for (String site : params.getSitesForReferences().getValues()) {
                        sites.add(site);
                    }
                }
                for (String site : sites) {
                    query.append("isdescendantnode(n,'/sites/").append(JCRContentUtils.sqlEncode(site)).append("') or ");
                }
                query.delete(query.length() - 4, query.length());
            }
            query.append(")");
        }

        query = appendConstraints(params, query, false);
        query = appendOrdering(params, query, false);

        return query.toString();
    }

    private String buildXpathQuery(SearchCriteria params, JCRSessionWrapper session) {
        String xpathQuery = null;

        StringBuilder query = new StringBuilder(256);
        String path = null;
        boolean includeChildren = false;
        if (!params.getFilePath().isEmpty()) {
            path = params.getFilePath().getValue().trim();
            includeChildren = params.getFilePath().isIncludeChildren();
        } else if (!params.getPagePath().isEmpty()) {
            path = params.getPagePath().getValue().trim();
            includeChildren = params.getPagePath().isIncludeChildren();
        }
        if (path != null) {
            String[] pathTokens = Patterns.SLASH.split(StringEscapeUtils.unescapeHtml(path));
            String lastFolder = null;
            StringBuilder jcrPath = new StringBuilder(64);
            jcrPath.append("/jcr:root/");
            for (String folder : pathTokens) {
                if (folder.length() == 0) {
                    continue;
                }
                folder = ISO9075.encode(folder);
                if (!includeChildren) {
                    if (lastFolder != null) {
                        jcrPath.append(lastFolder).append("/");
                    }
                    lastFolder = folder;
                } else {
                    jcrPath.append(folder).append("/");
                }
            }
            if (includeChildren) {
                jcrPath.append("/");
                lastFolder = "*";
            }
            query.append(jcrPath).append("element(").append(
                    lastFolder).append(",").append(
                    getNodeType(params)).append(")");
        } else if (!params.getSites().isEmpty()) {
            query.append("/jcr:root/sites/");
            if ("-all-".equals(params.getSites().getValue())) {
                query.append("*");
            } else {
                Set<String> sites = new LinkedHashSet<String>();
                for (String site : params.getSites().getValues()) {
                    sites.add(site);
                }
                if (!params.getSitesForReferences().isEmpty()) {
                    for (String site : params.getSitesForReferences().getValues()) {
                        sites.add(site);
                    }
                }
                if (sites.size() == 1) {
                    query.append(ISO9075.encode(sites.iterator().next()));
                } else {
                    query.append("*[");
                    int i = 0;
                    for (String site : sites) {
                        if (i > 0) {
                            query.append(" or ");
                        }
                        query.append("fn:name() = ");
                        query.append(stringToQueryLiteral(ISO9075.encode(site)));
                        i++;
                    }
                    query.append("]");
                }
            }

            if (isSiteSearch(params)) {
                query.append("/*[@j:isHomePage='true' or fn:name() = 'files' or fn:name() = 'contents']");
            }

            query.append("//element(*,").append(getNodeType(params))
                    .append(")");
        } else {
            query.append("//element(*,").append(
                    getNodeType(params)).append(")");
        }

        query = appendConstraints(params, query, true);
        query = appendOrdering(params, query, true);

        xpathQuery = query.toString();

        if (logger.isDebugEnabled()) {
            logger.debug("XPath query built: " + xpathQuery);
        }

        return xpathQuery;
    }

    private boolean isFileSearch(SearchCriteria params) {
        return params.isFileSearch();
    }
    
    private boolean isFieldSearch(SearchFields searchFields) {
        return searchFields.isTags() || searchFields.isFileContent() || searchFields.isDescription() || searchFields.isTitle()
                || searchFields.isKeywords() || searchFields.isFilename();
    }

    private boolean isSiteSearch(SearchCriteria params) {
        return params.isSiteSearch();
    }

    private String getNodeType(SearchCriteria params) {
        if (StringUtils.isNotEmpty(params.getNodeType())) {
            return params.getNodeType();
        }

        if (isFileSearch(params) && !isSiteSearch(params)) {
            return Constants.NT_HIERARCHYNODE;
//        } else if (!isFileSearch(params) && isSiteSearch(params)) {
//            return Constants.JAHIANT_CONTENT;
        }

        return Constants.JAHIAMIX_SEARCHABLE;
    }

    private StringBuilder appendConstraints(SearchCriteria params,
                                            StringBuilder query, boolean xpath) {
        StringBuilder constraints = new StringBuilder(64);

        addTermConstraints(params, constraints, xpath);

        addDateAndAuthorConstraints(params, constraints, xpath);

        addFileTypeConstraints(params, constraints, xpath);

        addLanguageConstraints(params, constraints, xpath);

        List<NodeProperty> props = params.getPropertiesAll();
        if (!props.isEmpty()) {
            addPropertyConstraints(constraints, props, xpath);
        }

        if (constraints.length() > 0) {
            if (xpath) {
                query.append("[").append(constraints).append("]");
            } else {
                if (query.indexOf("where") > -1) {
                    query.append(" and ");
                } else {
                    query.append(" where ");
                }
                query.append("(").append(constraints).append(")");
            }
        }

        return query;
    }

    private StringBuilder appendOrdering(SearchCriteria params, StringBuilder query, boolean xpath) {
        StringBuilder orderByClause = new StringBuilder();
        if (params.getOrderings().isEmpty()) {
            orderByClause.append(xpath ? " order by jcr:score() descending" : " order by score() desc");
        } else {
            for (Ordering ordering : params.getOrderings()) {
                StringBuilder orderingBuilder = new StringBuilder();
                switch (ordering.getOperand()) {
                    case SCORE:
                        orderingBuilder.append(xpath ? "jcr:score()" : "SCORE()");
                        break;
                    case PROPERTY:
                        orderingBuilder.append(xpath ? "@" : "[").append(ordering.getPropertyName()).append(xpath ? "" : "]");
                        break;
                }
                if (ordering.getCaseConversion() != null) {
                    orderingBuilder.insert(0, ordering.getCaseConversion() == CaseConversion.LOWER ? xpath ? "fn:lower-case(" : "LOWER("
                            : xpath ? "fn:upper-case(" : "UPPER(");
                    orderingBuilder.append(")");
                }
                // there is no normalize function for SQL-2 yet, so for now we use it only for xpath and ignore it for SQL-2
                if (ordering.isNormalize() && xpath) {
                    orderingBuilder.insert(0, "rep:normalize(");
                    orderingBuilder.append(")");
                }

                orderingBuilder
                        .append(ordering.getOrder() == Order.ASCENDING ? xpath ? " ascending" : " asc" : xpath ? " descending" : " desc");
                if (orderByClause.length() > 0) {
                    orderByClause.append(", ");
                }
                orderByClause.append(orderingBuilder);
            }
            orderByClause.insert(0, " order by ");
        }
        query.append(orderByClause);
        return query;
    }

    private StringBuilder addConstraint(StringBuilder constraints,
                                        String operand, String constraint) {
        if (constraints.length() > 0) {
            constraints.append(" ").append(operand).append(" ");
        }
        return constraints.append(constraint);
    }

    private void addDateAndAuthorConstraints(SearchCriteria params,
                                             StringBuilder constraints, boolean xpath) {

        if (params.getCreatedBy() != null && params.getCreatedBy().length() > 0) {
            addConstraint(constraints, AND, getContainsExpr(getPropertyName(Constants.JCR_CREATEDBY, xpath), stringToJCRSearchExp(params.getCreatedBy().trim()), xpath));
        }

        if (params.getLastModifiedBy() != null
                && params.getLastModifiedBy().length() > 0) {
            addConstraint(constraints, AND, getContainsExpr(getPropertyName(Constants.JCR_LASTMODIFIEDBY, xpath), stringToJCRSearchExp(params.getLastModifiedBy().trim()), xpath));
        }

        if (!params.getCreated().isEmpty()
                && DateValue.Type.ANYTIME != params.getCreated().getType()) {
            addDateConstraint(constraints, params.getCreated(), Constants.JCR_CREATED, xpath);
        }

        if (!params.getLastModified().isEmpty()
                && DateValue.Type.ANYTIME != params.getLastModified().getType()) {
            addDateConstraint(constraints, params.getLastModified(), Constants.JCR_LASTMODIFIED, xpath);
        }

    }

    private void addDateConstraint(StringBuilder constraints,
                                   SearchCriteria.DateValue dateValue, String paramName, boolean xpath) {
        Calendar greaterThanDate = Calendar.getInstance();
        Calendar smallerThanDate = null;

        if (DateValue.Type.TODAY == dateValue.getType()) {
            // no date adjustment needed
        } else if (DateValue.Type.LAST_WEEK == dateValue.getType()) {
            greaterThanDate.add(Calendar.DATE, -7);
        } else if (DateValue.Type.LAST_MONTH == dateValue.getType()) {
            greaterThanDate.add(Calendar.MONTH, -1);
        } else if (DateValue.Type.LAST_THREE_MONTHS == dateValue.getType()) {
            greaterThanDate.add(Calendar.MONTH, -3);
        } else if (DateValue.Type.LAST_SIX_MONTHS == dateValue.getType()) {
            greaterThanDate.add(Calendar.MONTH, -6);
        } else if (DateValue.Type.RANGE == dateValue.getType()) {
            greaterThanDate = null;
            smallerThanDate = null;
            if (dateValue.getFromAsDate() != null) {
                greaterThanDate = Calendar.getInstance();
                greaterThanDate.setTime(dateValue.getFromAsDate());
            }
            if (dateValue.getToAsDate() != null) {
                smallerThanDate = Calendar.getInstance();
                smallerThanDate.setTime(dateValue.getToAsDate());
            }
        } else {
            throw new IllegalArgumentException("Unknown date value type '"
                    + dateValue.getType() + "'");
        }

        try {
            if (greaterThanDate != null) {
                addConstraint(constraints, AND, getPropertyName(paramName, xpath)
                        + " >= " + getDateLiteral(DateUtils.dayStart(greaterThanDate), xpath));
            }
            if (smallerThanDate != null) {
                addConstraint(constraints, AND, getPropertyName(paramName, xpath)
                        + " <= " + getDateLiteral(DateUtils.dayEnd(smallerThanDate), xpath));
            }
        } catch (IllegalStateException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    private void addFileTypeConstraints(SearchCriteria params,
                                        StringBuilder constraints, boolean xpath) {

        if (StringUtils.isNotEmpty(params.getFileType())) {
            List<String> mimeTypes = JCRContentUtils.getInstance()
                    .getMimeTypes().get(params.getFileType());
            if (mimeTypes != null && !mimeTypes.isEmpty()) {
                if (mimeTypes.size() > 1) {
                    StringBuilder fileTypeConstraints = new StringBuilder(128);
                    for (String mimeType : mimeTypes) {
                        addConstraint(fileTypeConstraints, OR,
                                getMimeTypeConstraint(mimeType, xpath));
                    }
                    addConstraint(constraints, AND, fileTypeConstraints
                            .insert(0, "(").append(")").toString());
                } else {
                    addConstraint(constraints, AND,
                            getMimeTypeConstraint(mimeTypes.get(0), xpath));
                }
            } else {
                logger.warn("Unsupported file type '" + params.getFileType()
                        + "'. See applicationcontext-basejahiaconfig.xml file"
                        + " for configured file types.");
            }
        }
    }

    private void addPropertyConstraintCategory(
            StringBuilder categoryConstraints, String name, String value,
            boolean includeChildren, boolean xpath) {
        try {
            Category cat = Category.getCategoryByPath(value, JCRSessionFactory.getInstance().getCurrentUser());
            if (cat == null) {
                logger.warn("User " + JCRSessionFactory.getInstance().getCurrentUser().getUsername() + " has no right to read the category");
                return;
            }
            addConstraint(categoryConstraints, OR, getPropertyName(name,xpath) + "=" + stringToJCRSearchExp(cat.getID()));
            if (includeChildren) {
                addSubCategoriesConstraints(categoryConstraints, cat, name, xpath);
            }
        } catch (JahiaException e) {
            logger.warn("Category: " + value + " could not be retrieved", e);
        }
    }

    private void addSubCategoriesConstraints(
            StringBuilder categoryConstraints, Category category, String name, boolean xpath) throws JahiaException {

        List<Category> childs = category.getChildCategories();
        if (childs != null && childs.size() > 0) {
            for (Category cat : childs) {
                addConstraint(categoryConstraints, OR, getPropertyName(name,xpath) + "=" + stringToJCRSearchExp(cat.getID()));
                addSubCategoriesConstraints(categoryConstraints, cat, name, xpath);
            }
        }
    }


    private void addPropertyConstraints(StringBuilder constraints,
                                        List<NodeProperty> properties, boolean xpath) {
        for (NodeProperty property : properties) {
            if (!property.isEmpty()) {
                if (NodeProperty.Type.CATEGORY == property.getType()) {
                    StringBuilder categoryConstraints = new StringBuilder(64);
                    for (String value : property.getCategoryValue().getValues()) {
                        addPropertyConstraintCategory(categoryConstraints,
                                property.getName(), value, property
                                .getCategoryValue().isIncludeChildren(), xpath);
                    }
                    if (categoryConstraints.length() > 0) {
                        addConstraint(constraints, AND, categoryConstraints
                                .insert(0, "(").append(")").toString());
                    }
                } else if (NodeProperty.Type.DATE == property.getType()) {
                    addDateConstraint(constraints, property.getDateValue(), property.getName(), xpath);
                } else if (NodeProperty.Type.TEXT == property.getType()) {
                    StringBuilder propertyConstraints = new StringBuilder(64);
                    for (String value : property.getValues()) {
                        if (property.isConstrained()) {
                            String matchType = "=";
                            if (property.getMatch() == MatchType.WITHOUT_WORDS || property.getMatch() == MatchType.NO_EXACT_PROPERTY_VALUE) {
                                matchType = "!=";
                            }
                            addConstraint(propertyConstraints, OR, getPropertyName(property.getName(), xpath) + matchType + stringToJCRSearchExp(value));
                        } else {
                            addConstraint(propertyConstraints, OR, getSearchExpression(getPropertyName(property.getName(), xpath), value, property.getMatch(), false, xpath));
                        }
                    }
                    if (propertyConstraints.length() > 0) {
                        if (property.getValues().length == 1) {
                            addConstraint(constraints, AND,
                                    propertyConstraints.toString());
                        } else {
                            addConstraint(constraints, AND, "("
                                    + propertyConstraints.toString() + ")");
                        }
                    }
                } else if (NodeProperty.Type.BOOLEAN == property.getType()) {
                    // only handle 'true' case
                    if (Boolean.parseBoolean(property.getValue())) {
                        addConstraint(constraints, AND, getPropertyName(property.getName(), xpath) + "='true'");
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Unknown document property type '"
                                    + property.getType() + "'");
                }
            }
        }
    }

    private void addTermConstraints(SearchCriteria params, StringBuilder constraints, boolean xpath) {
        for (Term textSearch : params.getTerms()) {
            if (textSearch.isEmpty()) {
                continue;
            }

            StringBuilder textSearchConstraints = new StringBuilder(256);
            if (textSearch.getFields().isSiteContent() || !isFieldSearch(textSearch.getFields())) {
                addConstraint(textSearchConstraints, OR, getSearchExpression(xpath ? "." : "n", textSearch.getTerm(), textSearch.getMatch(),
                        textSearch.isApplyFilter(), xpath));
            }
            if (textSearch.getFields().isFileContent()) {
                addConstraint(textSearchConstraints, OR,
                        getSearchExpression(xpath ? Constants.JCR_CONTENT : getPropertyName(Constants.JCR_CONTENT, false),
                                textSearch.getTerm(), textSearch.getMatch(), textSearch.isApplyFilter(), xpath));
            }
            if (textSearchConstraints.length() == 0 || !isWithoutTermContraintInFulltextQuery(textSearch)) {
                addTermConstraintsOnFields(params, textSearch, textSearch.getFields(), textSearchConstraints, xpath);
            }
            if (textSearchConstraints.length() > 0) {
                addConstraint(constraints, AND, "(" + textSearchConstraints.toString() + ")");
            }
        }
    }
    private void addTermConstraintsOnFields(SearchCriteria params, Term textSearch, SearchFields searchFields, StringBuilder textSearchConstraints, boolean xpath) {
        if (searchFields.isDescription()) {
            addConstraint(textSearchConstraints, OR, getSearchExpression(getPropertyName(Constants.JCR_DESCRIPTION, xpath),
                    textSearch.getTerm(), textSearch.getMatch(), textSearch.isApplyFilter(), xpath));
        }
        if (searchFields.isTitle()) {
            addConstraint(textSearchConstraints, OR, getSearchExpression(getPropertyName(Constants.JCR_TITLE, xpath), textSearch.getTerm(),
                    textSearch.getMatch(), textSearch.isApplyFilter(), xpath));
        }
        if (searchFields.isKeywords()) {
            addConstraint(textSearchConstraints, OR, getSearchExpression(getPropertyName(Constants.KEYWORDS, xpath), textSearch.getTerm(),
                    textSearch.getMatch(), textSearch.isApplyFilter(), xpath));
        }

        String[] terms = null;
        String constraint = OR;
        if (searchFields.isFilename() || searchFields.isTags()) {
            if (textSearch.getMatch() == MatchType.ANY_WORD || textSearch.getMatch() == MatchType.ALL_WORDS
                    || textSearch.getMatch() == MatchType.WITHOUT_WORDS) {
                terms = Patterns.SPACE.split(cleanMultipleWhiteSpaces(textSearch.getTerm()));
                if (textSearch.getMatch() == MatchType.ALL_WORDS || textSearch.getMatch() == MatchType.WITHOUT_WORDS) {
                    constraint = AND;
                }
            } else {
                terms = new String[]{textSearch.getTerm()};
            }
        }
        if (searchFields.isFilename()) {
            String nameSearchConstraints = createFilenameConstraints(textSearch, terms, constraint, xpath);
            if (!nameSearchConstraints.isEmpty()) {
                addConstraint(textSearchConstraints, OR, "(" + nameSearchConstraints + ")");
            }
        }
        if (searchFields.isTags() && getTaggingService() != null
                && (params.getSites().getValue() != null || params.getOriginSiteKey() != null)
                && !StringUtils.containsAny(textSearch.getTerm(), "?*")) {
            for (String term : terms) {
                String tag = taggingService.getTagHandler().execute(term);
                if (!StringUtils.isEmpty(tag)) {
                    addConstraint(textSearchConstraints, OR, getPropertyName("j:tagList", xpath) + "=" + stringToJCRSearchExp(tag));
                }
            }
            if (terms.length > 1) {
                String tag = taggingService.getTagHandler().execute(textSearch.getTerm());
                if (!StringUtils.isEmpty(tag)) {
                    addConstraint(textSearchConstraints, OR, getPropertyName("j:tagList", xpath) + "=" + stringToJCRSearchExp(tag));
                }
            }
        }
    }


    private boolean isWithoutTermContraintInFulltextQuery(Term textSearch) {
        boolean withoutTermContraint = textSearch.getMatch() == MatchType.WITHOUT_WORDS;
        if (textSearch.getMatch() == MatchType.AS_IS) {
            Matcher matcher = QUOTED_OR_PLAIN_TERMS_WITH_OPTIONAL_NEGATION_PATTERN.matcher(textSearch.getTerm());
            while (!withoutTermContraint && matcher.find()) {
                if (matcher.group(1) != null ? matcher.group().startsWith("-") : matcher.group(2).startsWith("-")) {
                    withoutTermContraint = true;
                }
            }
        }
        return withoutTermContraint;
    }
    
    private String createFilenameConstraints(Term textSearch, String[] terms, String constraint, boolean xpath) {
        StringBuilder nameSearchConstraints = new StringBuilder(256);

        if (textSearch.getMatch() == MatchType.AS_IS) {
            // special handling for AS_IS match type on file name search
            Matcher matcher = QUOTED_OR_PLAIN_TERMS_WITH_OPTIONAL_NEGATION_PATTERN.matcher(textSearch.getTerm());
            String previousOperand = null;
            while (matcher.find()) {
                boolean negation = false;
                String asIsTerm;
                if (matcher.group(1) != null) {
                    asIsTerm = matcher.group(1);
                    if (matcher.group().startsWith("-")) {
                        negation = true;
                    }
                } else {
                    asIsTerm = matcher.group(2);
                    if (asIsTerm.startsWith("-")) {
                        asIsTerm = StringUtils.substring(asIsTerm, 1);
                        negation = true;
                    }
                }

                // is an operand && no previous operand && not the first term
                if ((OR.equalsIgnoreCase(asIsTerm) || AND.equalsIgnoreCase(asIsTerm)) && previousOperand == null
                        && nameSearchConstraints.length() != 0) {
                    previousOperand = asIsTerm.toLowerCase();
                } else {
                    if (!asIsTerm.isEmpty()) {
                        String termConstraint = createNodenameLikeTermConstraint(asIsTerm, xpath);
                        if (negation) {
                            termConstraint = "not(" + termConstraint + ")";
                        }
                        if (previousOperand != null) {
                            addConstraint(nameSearchConstraints, previousOperand, termConstraint);
                            previousOperand = null;
                        } else {
                            addConstraint(nameSearchConstraints, AND, termConstraint);
                        }
                    } else {
                        previousOperand = null;
                    }
                }
            }
            // if operand was the last term, then it should not be treated as operand
            if (previousOperand != null) {
                addConstraint(nameSearchConstraints, AND, previousOperand);
            }
        } else {
            for (String term : terms) {
                if (!term.isEmpty()) {
                    String termConstraint = createNodenameLikeTermConstraint(term, xpath);
                    if (textSearch.getMatch() == MatchType.WITHOUT_WORDS) {
                        termConstraint = "not(" + termConstraint + ")";
                    }
                    addConstraint(nameSearchConstraints, constraint, termConstraint);
                }
            }
        }
        return nameSearchConstraints.toString();
    }

    private String createNodenameLikeTermConstraint(String term, boolean xpath) {
        final String likeTerm = term.contains("*") ? stringToQueryLiteral(StringUtils.replaceChars(term, '*', '%'))
                : stringToQueryLiteral("%" + term + "%");
        String lowerCaseTerm = likeTerm.toLowerCase();
        return xpath ? ("jcr:like(fn:lower-case(fn:name()), " + lowerCaseTerm + ")")
                : ("LOWER(n.[j:nodename]) like " + lowerCaseTerm);
    }

    private void addLanguageConstraints(SearchCriteria params,
                                        StringBuilder constraints, boolean xpath) {
        StringBuilder languageSearchConstraints = new StringBuilder(256);
        if (!params.getLanguages().isEmpty()) {
            for (String languageCode : params.getLanguages().getValues()) {
                if (languageCode != null && languageCode.length() != 0) {
                    addConstraint(languageSearchConstraints, OR,
                            getPropertyName("jcr:language",xpath) + "=" + stringToJCRSearchExp(languageCode.trim()));
                }
            }
        } else {
            try {
                JCRStoreService jcrService = ServicesRegistry.getInstance()
                        .getJCRStoreService();
                JCRSessionWrapper session = jcrService.getSessionFactory()
                        .getCurrentUserSession();
                if (session.getLocale() != null) {
                    addConstraint(languageSearchConstraints, OR,
                            getPropertyName("jcr:language",xpath) + "=" + stringToJCRSearchExp(session.getLocale().toString()));
                }
            } catch (RepositoryException e) {
            }
        }
        if (languageSearchConstraints.length() > 0) {
            addConstraint(languageSearchConstraints, OR,
                    xpath ? "not(@jcr:language)" : "[jcr:language] is null");
            addConstraint(constraints, AND, "(" + languageSearchConstraints
                    .toString() + ")");
        }
    }

    private String getMimeTypeConstraint(String mimeType, boolean xpath) {
        if (xpath) {
            return mimeType.contains("*") ? "jcr:like(jcr:content/@jcr:mimeType,"
                    + stringToQueryLiteral(StringUtils.replaceChars(mimeType, '*',
                    '%')) + ")" : "jcr:content/@jcr:mimeType="
                    + stringToQueryLiteral(mimeType);
        } else {
            return "n.[jcr:mimetype]=" + stringToQueryLiteral(mimeType);
        }

    }

    private String getDateLiteral(Calendar date, boolean xpath) {
        return xpath ? ("xs:dateTime('"
                + ISO8601.format(date)
                + "')") : ("'" + ISO8601.format(date) + "'");
    }

    private String getPropertyName(String paramName, boolean xpath) {
        return xpath ? ("@" + paramName) : ("n.[" + paramName + "]");
    }

    private String getContainsExpr(String scope, String expr, boolean xpath) {
        if (xpath) {
            return "jcr:contains(" + scope + "," + expr + ")";
        } else {
            return "contains(" + scope + "," + expr + ")";
        }
    }

    private String getSearchExpression(String scope, String term, MatchType matchType, boolean applyFilter, boolean xpath) {

        // as Lucene does not analyze wildcard terms, check whether integrator requested Jahia
        // to apply this accent filter
        if (applyFilter && StringUtils.containsAny(term, "?*")) {
            term = TextUtils.removeAccents(term);
        }

        if (Term.MatchType.AS_IS != matchType && Term.MatchType.EXACT_PROPERTY_VALUE != matchType && Term.MatchType.NO_EXACT_PROPERTY_VALUE != matchType) {
            term = QueryParser.escape(NOT_PATTERN.matcher(OR_PATTERN.matcher(AND_PATTERN.matcher(term).replaceAll(" and ")).replaceAll(
                    " or ")).replaceAll(" not "));
        }

        if (MatchType.ALL_WORDS == matchType || MatchType.AS_IS == matchType) {
            return getContainsExpr(scope, stringToJCRSearchExp(term), xpath);
        } else if (MatchType.ANY_WORD == matchType) {
            term = StringUtils.replace(cleanMultipleWhiteSpaces(term), " ", " OR ");
            return getContainsExpr(scope, stringToJCRSearchExp(term), xpath);
        } else if (MatchType.EXACT_PHRASE == matchType) {
            term = "\"" + term.trim() + "\"";
            return getContainsExpr(scope, stringToJCRSearchExp(term), xpath);
        } else if (MatchType.WITHOUT_WORDS == matchType) {
            // because of the underlying Lucene limitations a star '*' (means
            // 'match all') is added to the query string
            term = "* -" + StringUtils.replace(cleanMultipleWhiteSpaces(term), " ", " -");
            return getContainsExpr(scope, stringToJCRSearchExp(term), xpath);
        } else if (MatchType.EXACT_PROPERTY_VALUE == matchType) {
            return (scope + "=" + stringToQueryLiteral(term));
        } else if (MatchType.NO_EXACT_PROPERTY_VALUE == matchType) {
            return (scope + "!=" + stringToQueryLiteral(term));
        } else {
            throw new IllegalArgumentException("Unsupported match type: " + matchType);
        }
    }


    private String cleanMultipleWhiteSpaces(String term) {
        return MULTIPLE_SPACES_PATTERN.matcher(term).replaceAll(" ");
    }

    /* (non-Javadoc)
        * @see org.jahia.services.search.SearchProvider#suggest(java.lang.String, java.lang.String, java.util.Locale)
        */
    @Override
    public Suggestion suggest(String originalQuery, RenderContext context, int maxTerms) {
        return suggest(originalQuery, new String[] { context.getSite().getSiteKey() }, context, maxTerms);
    }

    @Override
    public Suggestion suggest(SearchCriteria originalQuery, RenderContext context, int maxTermsToSuggest) {
        return suggest(originalQuery.getTerms().get(0).getTerm(), originalQuery.getSites().getValues(), context, maxTermsToSuggest);
    }

    public Suggestion suggest(String originalQuery, String[] sites, RenderContext context, int maxTermsToSuggest) {
        if (StringUtils.isBlank(originalQuery)) {
            return null;
        }

        if (sites.length == 1 && sites[0].equals("-all-")) {
            List<String> sitesNames = JahiaSitesService.getInstance().getSitesNames();
            sites = sitesNames.toArray(new String[sitesNames.size()]);
        }

        Locale locale = context.getMainResourceLocale();

        Suggestion suggestion = null;
        JCRSessionWrapper session;
        try {
            session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession(
                    context.getWorkspace(), locale);
            QueryManager qm = session.getWorkspace().getQueryManager();
            StringBuilder xpath = new StringBuilder(64);
            xpath.append("/jcr:root[rep:spellcheck(")
                    .append(stringToJCRSearchExp(originalQuery
                            + CompositeSpellChecker.SEPARATOR_IN_SUGGESTION
                            + CompositeSpellChecker.MAX_TERMS_PARAM + "="
                            + maxTermsToSuggest
                            + CompositeSpellChecker.SEPARATOR_IN_SUGGESTION
                            + CompositeSpellChecker.SITES_PARAM + "=" + StringUtils.join(sites, "*")
                    )).append(")");
            if (locale != null) {
                xpath.append(" or @jcr:language='").append(locale).append("'");
            }
            xpath.append("]");
            xpath.append("/(rep:spellcheck())");

            Query query = qm.createQuery(xpath.toString(), Query.XPATH);
            RowIterator rows = query.execute().getRows();
            if (rows.hasNext()) {
                Row r = rows.nextRow();
                Value v = r.getValue("rep:spellcheck()");
                if (v != null) {
                    String[] suggestions = StringUtils.splitByWholeSeparator(v.getString(), CompositeSpellChecker.SEPARATOR_IN_SUGGESTION);
                    suggestion = new Suggestion(originalQuery, suggestions[0], Arrays.asList(suggestions));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Making spell check suggestion for '" + originalQuery + "' site '"
                            + Arrays.asList(sites) + "' and locale '" + locale + "' using XPath query ["
                            + xpath.toString() + "]. Result suggestion: " + suggestion);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return suggestion;
    }

    /**
     * Creates the {@link Query} instance by converting the provided
     * {@link SearchCriteria} bean into XPath query.
     *
     * @param criteria the search criteria to use for the query
     * @param session  current JCR session
     * @return the {@link Query} instance created by converting the provided
     *         {@link SearchCriteria} bean into XPath query or <code>null</code>
     *         if the query cannot be created
     * @throws InvalidQueryException
     * @throws RepositoryException in case of JCR-related errors
     */
    public Query buildQuery(SearchCriteria criteria, JCRSessionWrapper session) throws InvalidQueryException,
            RepositoryException {
        Query query = null;
        String xpathQuery = buildXpathQuery(criteria, session);
        String sql = buildSQLQuery(criteria, session);
        if (!StringUtils.isEmpty(xpathQuery)) {
            QueryManagerWrapper qm = session.getWorkspace().getQueryManager();
            query = qm.createDualQuery(xpathQuery, Query.XPATH, sql);
        }

        return query;
    }

    public TaggingService getTaggingService() {
        return taggingService;
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }

    public void setTypesToHideFromSearchResults(Set<String> typesToHideFromSearchResults) {
        this.typesToHideFromSearchResults = typesToHideFromSearchResults;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
