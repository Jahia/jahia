package org.jahia.services.search.jcr;

import static org.jahia.services.content.JCRContentUtils.stringToJCRSearchExp;
import static org.jahia.services.content.JCRContentUtils.stringToQueryLiteral;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.search.AbstractHit;
import org.jahia.services.search.FileHit;
import org.jahia.services.search.Hit;
import org.jahia.services.search.PageHit;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchProvider;
import org.jahia.services.search.SearchResponse;
import org.jahia.services.search.SearchCriteria.DateValue;
import org.jahia.services.search.SearchCriteria.DocumentProperty;
import org.jahia.services.search.SearchCriteria.SearchMode;
import org.jahia.services.search.SearchCriteria.Term;
import org.jahia.services.search.SearchCriteria.Term.MatchType;
import org.jahia.services.search.SearchCriteria.Term.SearchFields;
import org.jahia.utils.DateUtils;
import org.jahia.utils.JahiaTools;

public class JahiaJCRSearchProvider implements SearchProvider {

    private static Logger logger = Logger
            .getLogger(JahiaJCRSearchProvider.class);

    public SearchResponse search(SearchCriteria criteria) {
        String xpathQuery = buildXpathQuery(criteria);

        SearchResponse response = new SearchResponse();

        if (!StringUtils.isEmpty(xpathQuery)) {
            List<Hit> results = new ArrayList<Hit>();
            try {
                JCRStoreService jcrService = ServicesRegistry.getInstance()
                        .getJCRStoreService();
                JCRSessionWrapper session = jcrService.getSessionFactory()
                        .getCurrentUserSession();
                QueryManager qm = session.getWorkspace().getQueryManager();
                Query query = qm.createQuery(xpathQuery, Query.XPATH);

                QueryResult queryResult = query.execute();
                RowIterator it = queryResult.getRows();

                while (it.hasNext()) {
                    try {
                        Row row = it.nextRow();
                        String path = row.getValue(JcrConstants.JCR_PATH)
                                .getString();
                        JCRNodeWrapper node = session.getNode(path);
                        if (node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                            node = node.getParent();
                        }
                        AbstractHit searchHit = buildHit(row, node);

                        results.add(searchHit);
                    } catch (Exception e) {
                        logger.warn("Error resolving search hit", e);
                    }
                }
            } catch (Exception e) {
                logger.error("Error while trying to perform a search", e);
            }
            response.setResults(results);
        }

        return response;
    }
    
    private AbstractHit buildHit(Row row, JCRNodeWrapper node) throws RepositoryException {
        AbstractHit searchHit = null;
        if (node.isFile()) {
            FileHit fileHit = new FileHit(node);
            searchHit = fileHit;
        } else {
            PageHit pageHit = new PageHit(node);
            JCRNodeWrapper pageNode = node;
            while (pageNode != null
                    && !pageNode
                            .isNodeType(Constants.JAHIANT_PAGE)) {
                pageNode = pageNode.getParent();
            }
            if (pageNode != null) {
                pageHit.setPage(pageNode);
            }
            searchHit = pageHit;
        }

        searchHit.setScore((float) (row.getValue(
                JcrConstants.JCR_SCORE).getDouble() / 1000.));

        // this is Jackrabbit specific, so if other implementations
        // throw exceptions, we have to do a check here
        Value excerpt = row
                .getValue("rep:excerpt(.)");
        if (excerpt != null) {
            searchHit.setExcerpt(excerpt.getString());
        }
        return searchHit;
    }

    private String buildXpathQuery(SearchCriteria params) {
        String xpathQuery = null;

        StringBuilder query = new StringBuilder(256);
        String path = null;
        if (params.getFileLocation() != null
                && !params.getFileLocation().isEmpty()) {
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
                    getNodeType(params)).append(")");
        } else {
            query.append("//element(*,").append(
                    getNodeType(params)).append(")");
        }

        query = appendConstraints(params, query);
        xpathQuery = query.toString();

        logger.info("XPath query built: " + xpathQuery);

        return xpathQuery;
    }

    private String getNodeType(SearchCriteria params) {
        return StringUtils.isEmpty(params.getDocumentType()) ? (params
                .getMode().equals(SearchMode.FILES) ? "nt:hierarchyNode"
                : "nt:base") : params.getDocumentType();
    }

    private StringBuilder appendConstraints(SearchCriteria params,
            StringBuilder query) {
        StringBuilder constraints = new StringBuilder(64);

        addTermConstraints(params, constraints);

        addDateAndAuthorConstraints(params, constraints);

        addFileTypeConstraints(params, constraints);

        addLanguageConstraints(params, constraints);

        List<DocumentProperty> props = params.getPropertiesAll();
        if (!props.isEmpty()) {
            addPropertyConstraints(constraints, props);
        }

        if (constraints.length() > 0) {
            query.append("[").append(constraints).append("]");
        }

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
            StringBuilder constraints) {

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
            addDateConstraint(constraints, params.getCreated(), "@jcr:created");
        }

        if (!params.getLastModified().isEmpty()
                && DateValue.Type.ANYTIME != params.getLastModified().getType()) {
            addDateConstraint(constraints, params.getLastModified(),
                    "@jcr:lastModified");
        }

    }

    private void addDateConstraint(StringBuilder constraints,
            SearchCriteria.DateValue dateValue, String paramName) {
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
                addConstraint(constraints, "and", paramName
                        + " >= xs:dateTime('"
                        + ISO8601.format(DateUtils.dayStart(greaterThanDate))
                        + "')");
            }
            if (smallerThanDate != null) {
                addConstraint(constraints, "and", paramName
                        + " <= xs:dateTime('"
                        + ISO8601.format(DateUtils.dayEnd(smallerThanDate))
                        + "')");
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
            boolean includeChildren) {
        try {
            String categoryPath = Category.getCategoryPath(value);
            addConstraint(categoryConstraints, "or", "@" + name + "="
                    + stringToJCRSearchExp(categoryPath));
            if (includeChildren) {
                addConstraint(categoryConstraints, "or", "jcr:like(@"
                        + name
                        + ","
                        + stringToJCRSearchExp(categoryPath
                                + Category.PATH_DELIMITER + "%") + ")");
            }
        } catch (JahiaException e) {
            logger.warn("Category: " + value + " could not be retrieved", e);
        }
    }

    private void addPropertyConstraints(StringBuilder constraints,
            List<DocumentProperty> properties) {
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
                            + property.getName());
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
                if (params.getMode().equals(SearchMode.FILES)) {
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
                } else {
                    addConstraint(textSearchConstraints, "or",
                            "jcr:contains(., " + searchExpression + ")");
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

    private String getMimeTypeConstraint(String mimeType) {

        return mimeType.contains("*") ? "jcr:like(jcr:content/@jcr:mimeType,"
                + stringToQueryLiteral(StringUtils.replaceChars(mimeType, '*',
                        '%')) + ")" : "jcr:content/@jcr:mimeType="
                + stringToQueryLiteral(mimeType);
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

    private String cleanMultipleWhiteSpaces(String term) {
        return term.replaceAll("\\s{2,}", " ");
    }
}
