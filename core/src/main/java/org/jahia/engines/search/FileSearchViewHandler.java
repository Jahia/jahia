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
package org.jahia.engines.search;

import static org.jahia.services.content.JCRContentUtils.stringToJCRSearchExp;
import static org.jahia.services.content.JCRContentUtils.stringToXPathLiteral;

import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.engines.search.SearchCriteria.DateValue;
import org.jahia.engines.search.SearchCriteria.DocumentProperty;
import org.jahia.engines.search.SearchCriteria.Term;
import org.jahia.engines.search.SearchCriteria.Term.MatchType;
import org.jahia.engines.search.SearchCriteria.Term.SearchFields;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.utils.DateUtils;
import org.jahia.utils.JahiaTools;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;

/**
 * Search handler implementation for DMS search.
 * 
 * @author Benjamin Papez
 * @author Sergiy Shyrkov
 */
public class FileSearchViewHandler extends AbstractSearchViewHandler {

    private static Logger logger = Logger
            .getLogger(FileSearchViewHandler.class);

    private static final XStream SERIALIZER = new XStream(new XppDriver() {
        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new CompactWriter(out, xmlFriendlyReplacer());
        }
    });

    public static JahiaSavedSearch toJahiaSavedSearch(String title,
            String description, boolean isPublic, SearchCriteria params,
            ProcessingContext ctx) throws JahiaException {

        JahiaBaseACL acl = new JahiaBaseACL();
        acl.create(0);

        JahiaSavedSearch savedSearch = new JahiaSavedSearch(
                Integer.valueOf(-1), title, description, SearchCriteriaFactory
                        .serialize(params),
                new Long(System.currentTimeMillis()), ctx.getUser()
                        .getUserKey(), FileSearchViewHandler.class.getName(),
                ctx.getSiteID(), acl);
        savedSearch.allowGuest(isPublic);

        return savedSearch;
    }

    private StringBuilder addConstraint(StringBuilder constraints,
            String operand, String constraint) {
        if (constraints.length() > 0) {
            constraints.append(" ").append(operand).append(" ");
        }
        return constraints.append(constraint);
    }

    private void addDateAndAuthorConstraints(SearchCriteria params,
            StringBuilder constraints, ProcessingContext ctx) {

        if (params.getCreatedBy() != null && params.getCreatedBy().length() > 0) {
            addConstraint(constraints, "and", "jcr:contains(@jcr:createdBy, "
                    + stringToJCRSearchExp(params.getCreatedBy().trim()) + ")");
        }

        if (params.getLastModifiedBy() != null
                && params.getLastModifiedBy().length() > 0) {
            addConstraint(constraints, "and",
                    "jcr:contains(@jcr:lastModifiedBy, "
                            + stringToJCRSearchExp(params.getLastModifiedBy()
                                    .trim()) + ")");
        }

        if (!params.getCreated().isEmpty()
                && DateValue.Type.ANYTIME != params.getCreated().getType()) {
            addDateConstraint(constraints, params.getCreated(), "@jcr:created",
                    ctx);
        }

        if (!params.getLastModified().isEmpty()
                && DateValue.Type.ANYTIME != params.getLastModified().getType()) {
            addDateConstraint(constraints, params.getLastModified(),
                    "@jcr:lastModified", ctx);
        }

    }

    private void addDateConstraint(StringBuilder constraints,
            SearchCriteria.DateValue dateValue, String paramName,
            ProcessingContext ctx) {
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
            if (dateValue.getFromAsDate() != null) {
                greaterThanDate.setTime(dateValue.getFromAsDate());
                smallerThanDate = null;
            }
            if (dateValue.getToAsDate() != null) {
                greaterThanDate = null;
                smallerThanDate = Calendar.getInstance();
                smallerThanDate.setTime(dateValue.getToAsDate());
            }
        } else {
            throw new IllegalArgumentException("Unknown date value type '"
                    + dateValue.getType() + "'");
        }

        try {
            if (greaterThanDate != null) {
                addConstraint(constraints, "and", paramName
                        + " >= xs:dateTime('"
                        + ISO8601.format(DateUtils.dayStart(greaterThanDate)) + "')");
            }
            if (smallerThanDate != null) {
                addConstraint(constraints, "and", paramName
                        + " <= xs:dateTime('"
                        + ISO8601.format(DateUtils.dayEnd(smallerThanDate)) + "')");
            }
        } catch (IllegalStateException e) {
            logger.warn(e);
        }
    }

    private void addFileTypeConstraints(SearchCriteria params,
            StringBuilder constraints) {

        if (StringUtils.isNotEmpty(params.getFileType())) {
            List<String> mimeTypes = JCRContentUtils.getInstance()
                    .getMimeTypes().get(params.getFileType());
            if (mimeTypes != null && !mimeTypes.isEmpty()) {
                if (mimeTypes.size() > 1) {
                    StringBuilder fileTypeConstraints = new StringBuilder(128);
                    for (String mimeType : mimeTypes) {
                        addConstraint(fileTypeConstraints, "or",
                                getMimeTypeConstraint(mimeType));
                    }
                    addConstraint(constraints, "and", fileTypeConstraints
                            .insert(0, "(").append(")").toString());
                } else {
                    addConstraint(constraints, "and",
                            getMimeTypeConstraint(mimeTypes.get(0)));
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
            boolean includeChildren) throws JahiaException {
        String categoryPath = Category.getCategoryPath(value);
        addConstraint(categoryConstraints, "or", "@" + name + "="
                + stringToJCRSearchExp(categoryPath));
        if (includeChildren) {
            addConstraint(categoryConstraints, "or", "jcr:like(@" + name + ","
                    + stringToJCRSearchExp(categoryPath + Category.PATH_DELIMITER + "%") + ")");
        }
    }

    private void addPropertyConstraints(StringBuilder constraints,
            List<DocumentProperty> properties, ProcessingContext ctx)
            throws JahiaException {
        for (DocumentProperty property : properties) {
            if (!property.isEmpty()) {
                if (DocumentProperty.Type.CATEGORY == property.getType()) {
                    StringBuilder categoryConstraints = new StringBuilder(64);
                    for (String value : property.getCategoryValue().getValues()) {
                        addPropertyConstraintCategory(categoryConstraints,
                                property.getName(), value, property
                                        .getCategoryValue().isIncludeChildren());
                    }
                    addConstraint(constraints, "and", categoryConstraints
                            .insert(0, "(").append(")").toString());
                } else if (DocumentProperty.Type.DATE == property.getType()) {
                    addDateConstraint(constraints, property.getDateValue(), "@"
                            + property.getName(), ctx);
                } else if (DocumentProperty.Type.TEXT == property.getType()) {
                    StringBuilder propertyConstraints = new StringBuilder(64);
                    for (String value : property.getValues()) {
                        if (property.isConstrained()) {
                            addConstraint(propertyConstraints, "or", "@"
                                    + property.getName() + "="
                                    + stringToJCRSearchExp(value));
                        } else {
                            addConstraint(propertyConstraints, "or",
                                    "jcr:contains(@"
                                            + property.getName()
                                            + ","
                                            + getSearchExpressionForMatchType(
                                                    value, property.getMatch())
                                            + ")");
                        }
                    }
                    if (propertyConstraints.length() > 0) {
                        if (property.getValues().length == 1) {
                            addConstraint(constraints, "and",
                                    propertyConstraints.toString());
                        } else {
                            addConstraint(constraints, "and", "("
                                    + propertyConstraints.toString() + ")");
                        }
                    }
                } else if (DocumentProperty.Type.BOOLEAN == property.getType()) {
                    // only handle 'true' case
                    if (Boolean.parseBoolean(property.getValue())) {
                        addConstraint(constraints, "and", "@"
                                + property.getName() + "='true'");
                    }
                } else {
                    throw new IllegalArgumentException(
                            "Unknown document property type '"
                                    + property.getType() + "'");
                }
            }
        }
    }

    private void addTermConstraints(SearchCriteria params,
            StringBuilder constraints) {

        for (Term textSearch : params.getTerms()) {

            if (!textSearch.isEmpty()) {
                String searchExpression = getSearchExpressionForMatchType(
                        textSearch.getTerm(), textSearch.getMatch());

                SearchFields searchFields = textSearch.getFields();
                StringBuilder textSearchConstraints = new StringBuilder(256);

                if (searchFields.isContent()) {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(jcr:content, " + searchExpression
                                    + ")");
                }
                if (searchFields.isDescription()) {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(@jcr:description, "
                                    + searchExpression + ")");
                }
                if (searchFields.isDocumentTitle()) {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(@jcr:title, " + searchExpression
                                    + ")");
                }
                if (searchFields.isKeywords()) {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(@j:keywords, " + searchExpression
                                    + ")");
                }
                if (searchFields.isFilename()) {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(@j:filename, " + searchExpression
                                    + ")");
                }
                if (textSearchConstraints.length() > 0) {
                    addConstraint(constraints, "and", "("
                            + textSearchConstraints.toString() + ")");
                }
            }
        }
    }

    private void addLanguageConstraints(SearchCriteria params,
            StringBuilder constraints) {

        if (!params.getLanguages().isEmpty()) {
            for (String languageCode : params.getLanguages().getValues()) {
                if (languageCode != null && languageCode.length() != 0) {
                    // TODO impement language code constraint
                }
            }
        }
    }

    private StringBuilder appendConstraints(SearchCriteria params,
            StringBuilder query, ProcessingContext ctx) throws JahiaException {
        StringBuilder constraints = new StringBuilder(64);

        addTermConstraints(params, constraints);

        addDateAndAuthorConstraints(params, constraints, ctx);

        addFileTypeConstraints(params, constraints);

        addLanguageConstraints(params, constraints);

        List<DocumentProperty> props = params.getPropertiesAll();
        if (!props.isEmpty()) {
            addPropertyConstraints(constraints, props, ctx);
        }

        if (constraints.length() > 0) {
            query.append("[").append(constraints).append("]");
        }
        
        return query;
    }

    private String buildQuery(SearchCriteria params, ProcessingContext ctx)
            throws JahiaException {

        String xpathQuery = null;

        StringBuilder query = new StringBuilder(256);
        String path = null;
        if (params.getFileLocation() != null && !params.getFileLocation().isEmpty()) {
            path = params.getFileLocation().getValue().trim();
        }
        if (path != null) {
            String[] pathTokens = JahiaTools.getTokens(StringEscapeUtils
                    .unescapeHtml(path), "/");
            String lastFolder = null;
            StringBuilder jcrPath = new StringBuilder(64);
            jcrPath.append("/jcr:root/");
            for (String folder : pathTokens) {
                if (!params.getFileLocation().isIncludeChildren()) {
                    if (lastFolder != null) {
                        jcrPath.append(lastFolder).append("/");
                    }
                    lastFolder = folder;
                } else {
                    jcrPath.append(folder).append("/");
                }
            }
            if (params.getFileLocation().isIncludeChildren()) {
                jcrPath.append("/");
                lastFolder = "*";
            }
            query.append(jcrPath.toString()).append("element(").append(
                    lastFolder).append(",").append(
                    getNodeType(params.getDocumentType())).append(")");
        } else {
            query.append("//element(*,").append(
                    getNodeType(params.getDocumentType())).append(")");
        }

        query = appendConstraints(params, query, ctx);
        xpathQuery = query.toString();

        this.query = xpathQuery;

        logger.info("XPath query built: " + xpathQuery);

        return xpathQuery;
    }

    private String cleanMultipleWhiteSpaces(String term) {
        return term.replaceAll("\\s{2,}", " ");
    }

    private String getMimeTypeConstraint(String mimeType) {

        return mimeType.contains("*") ? "jcr:like(jcr:content/@jcr:mimeType,"
                + stringToXPathLiteral(StringUtils.replaceChars(mimeType, '*',
                        '%')) + ")" : "jcr:content/@jcr:mimeType="
                + stringToXPathLiteral(mimeType);
    }

    private String getNodeType(String docType) {
        return docType != null && docType.length() > 0 ? docType
                : "nt:hierarchyNode";
    }

    @Override
    public List<JahiaSavedSearch> getSavedSearches() throws JahiaException {
        List<JahiaSavedSearch> allSavedSearches = super.getSavedSearches();
        List<JahiaSavedSearch> fileSearches = new ArrayList<JahiaSavedSearch>();
        for (JahiaSavedSearch savedSearch : allSavedSearches) {
            if (this.getClass().getName().equals(
                    savedSearch.getSearchViewHandlerClass())) {
                fileSearches.add((JahiaSavedSearch)SERIALIZER.fromXML(new StringReader(
                        savedSearch.getSearch())));
            }
        }
        return fileSearches;
    }

    @Override
    public String getSaveSearchDoc(ProcessingContext ctx) throws JahiaException {
        return SearchCriteriaFactory.serialize(SearchCriteriaFactory
                .getInstance(ctx));
    }

    private String getSearchExpressionForMatchType(String term,
            MatchType matchType) {
        if (MatchType.ANY_WORD == matchType) {
            term = StringUtils.replace(cleanMultipleWhiteSpaces(term), " ",
                    " OR ");
        } else if (MatchType.EXACT_PHRASE == matchType) {
            term = "\"" + term.trim() + "\"";
        } else if (MatchType.WITHOUT_WORDS == matchType) {
            // because of the underlying Lucene limitations a star '*' (means
            // 'match all') is added to the query string
            term = "* -"
                    + StringUtils.replace(cleanMultipleWhiteSpaces(term), " ",
                            " -");
        }

        return stringToJCRSearchExp(term);
    }

    public JahiaSearchResult search(SearchCriteria searchCriteria,
            ProcessingContext ctx) throws JahiaException {
        JahiaSearchResult srcResult = ServicesRegistry.getInstance()
                .getJahiaSearchService().fileSearch(
                        buildQuery(searchCriteria, ctx), ctx.getUser());

        return srcResult;
    }

    @Override
    public JahiaSearchResult search(ProcessingContext ctx)
            throws JahiaException {
        return search(SearchCriteriaFactory.getInstance(ctx), ctx);
    }

    @Override
    public void update(ProcessingContext jParams, Map<String, Object> engineMap)
            throws JahiaException {
        // just temporary implementation - to test saved searches
        SearchOptionsHandler optionsHandler = new SearchOptionsHandler(this);
        optionsHandler.handleActions(jParams, engineMap);
    }
}
