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
package org.jahia.services.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jahia.utils.DateUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Bean for holding all search parameters.
 *
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("unchecked")
public class SearchCriteria implements Serializable {

    /**
     * Base class for all facet definitions; encapsulates any common facet definition attributes like sort order of aggregated results.
     */
    abstract public static class BaseFacetDefinition implements Serializable {

        private static final long serialVersionUID = 7275734915870152689L;

        private String id;
        private int maxGroups;

        /**
         * Create a facet definition instance.
         *
         * @param id an unique identifier for this facet definition. This helps differentiate facet definitions that
         *           are of the same type but are operating on different data objects
         * @param maxGroups The max number of result groups the facet should return
         */
        protected BaseFacetDefinition(String id, int maxGroups) {
            if (id == null || id.length() == 0) {
                throw new IllegalArgumentException("Facet definition ID should not be null or empty");
            }
            this.id = id;
            this.maxGroups = maxGroups;
        }

        /**
         * @return the unique identifier for this facet definition
         */
        public String getId() {
            return id;
        }

        /**
         * @return The max number of result groups the facet should return
         */
        public int getMaxGroups() {
            return maxGroups;
        }
    }

    /**
     * Supports comma separated multiple values.
     *
     * @author Sergiy Shyrkov
     */
    public static class CommaSeparatedMultipleValue extends MultipleValue {

        private static final char MULTIPLE_VALUE_SEPARATOR = ',';

        private static final long serialVersionUID = 2324041504396269857L;

        @Override
        public void setValue(String value) {
            if (StringUtils.isNotEmpty(value)
                    && value.indexOf(MULTIPLE_VALUE_SEPARATOR) != -1) {
                super.setValues(StringUtils.split(value,
                        MULTIPLE_VALUE_SEPARATOR));
            } else {
                super.setValue(value);
            }
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this,
                    TO_STRING_STYLE);
        }

    }

    /**
     * Holder for the criterion of date type.
     *
     * @author Sergiy Shyrkov
     */
    public static class DateValue implements Serializable {

        public enum Type {
            ANYTIME, LAST_MONTH, LAST_SIX_MONTHS, LAST_THREE_MONTHS, LAST_WEEK, RANGE, TODAY;
        }

        public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern(DateUtils.DEFAULT_DATE_FORMAT);

        private static final long serialVersionUID = -1637520083714465344L;

        private String from;

        private Date fromAsDate;

        private String to;

        private Date toAsDate;

        private Type type = Type.ANYTIME;

        public String getFrom() {
            return from;
        }

        public Date getFromAsDate() {
            return fromAsDate;
        }

        public String getTo() {
            return to;
        }

        public Date getToAsDate() {
            return toAsDate;
        }

        public Type getType() {
            return type;
        }

        public boolean isEmpty() {
            return Type.ANYTIME.equals(type) || Type.RANGE.equals(type)
                    && null == fromAsDate && null == toAsDate;
        }

        public void setFrom(String dateFromAsString) {
            if (dateFromAsString != null && dateFromAsString.length() > 0) {
                from = dateFromAsString;
                fromAsDate = DATE_FORMAT.parseDateTime(dateFromAsString).toDate();
            } else {
                from = null;
                fromAsDate = null;
            }
        }

        public void setFromAsDate(Date dateFrom) {
            fromAsDate = dateFrom;
            from = dateFrom != null ? DATE_FORMAT.print(dateFrom.getTime()) : null;
        }

        public void setTo(String dateToAsString) {
            if (dateToAsString != null && dateToAsString.length() > 0) {
                to = dateToAsString;
                toAsDate = DATE_FORMAT.parseDateTime(dateToAsString).toDate();
             } else {
                to = null;
                toAsDate = null;
            }
        }

        public void setToAsDate(Date dateTo) {
            toAsDate = dateTo;
            to = dateTo != null ? DATE_FORMAT.print(dateTo.getTime()) : null;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this,
                    TO_STRING_STYLE);
        }

    }

    /**
     * Represents a selactable value (file location, category etc.) that is a
     * part of the hierarchical structure.
     *
     * @author Sergiy Shyrkov
     */
    public static class HierarchicalValue extends CommaSeparatedMultipleValue {

        private static final long serialVersionUID = -2708875840446947769L;
        private boolean includeChildren;

        public boolean isIncludeChildren() {
            return includeChildren;
        }

        public void setIncludeChildren(boolean includeChildren) {
            this.includeChildren = includeChildren;
        }

    }

    /**
     * Represents a multiple value holder.
     *
     * @author Sergiy Shyrkov
     */
    public static abstract class MultipleValue implements Serializable {

        private static final long serialVersionUID = 1797359207235144293L;

        private String[] values;

        public String getValue() {
            return values != null && values.length > 0 ? values[0] : null;
        }

        public String[] getValues() {
            return values;
        }

        public boolean isEmpty() {
            boolean empty = true;
            if (values != null && values.length > 0) {
                for (String val : values) {
                    empty = empty && isValueEmpty(val);
                    if (!empty) {
                        break;
                    }
                }
            }
            return empty;
        }

        public void setValue(String value) {
            this.values = new String[] { value };
        }

        public void setValues(String[] values) {
            this.values = values;
        }
    }

    /**
     * Single search criterion on the node property.
     *
     * @author Sergiy Shyrkov
     */
    public static class NodeProperty extends MultipleValue {

        public enum Type {
            BOOLEAN, CATEGORY, DATE, TEXT;
        }

        private static final long serialVersionUID = 1356495981201889467L;

        private HierarchicalValue categoryValue = new HierarchicalValue();

        private boolean constrained;

        private DateValue dateValue = new DateValue();

        private Term.MatchType match = Term.MatchType.AS_IS;

        private boolean multiple;

        private String name;

        private String nodeType;

        private Type type = Type.TEXT;

        public HierarchicalValue getCategoryValue() {
            return categoryValue;
        }

        public DateValue getDateValue() {
            return dateValue;
        }

        public Term.MatchType getMatch() {
            return match;
        }

        public String getName() {
            return name;
        }

        public String getNodeType() {
            return nodeType;
        }

        public Type getType() {
            return type;
        }

        public boolean isAllEmpty() {
            return super.isEmpty() && categoryValue.isEmpty()
                    && dateValue.isEmpty();
        }

        public boolean isConstrained() {
            return constrained;
        }

        @Override
        public boolean isEmpty() {
            boolean empty = false;
            if (Type.CATEGORY == type) {
                empty = categoryValue.isEmpty();
            } else if (Type.DATE == type) {
                empty = dateValue.isEmpty();
            } else if (Type.TEXT == type || Type.BOOLEAN == type) {
                empty = super.isEmpty();
            } else {
                throw new IllegalArgumentException(
                        "Unknown node property value type '" + type + "'");
            }
            return empty;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public void setCategoryValue(HierarchicalValue categoryValue) {
            this.categoryValue = categoryValue;
        }

        public void setConstrained(boolean constrained) {
            this.constrained = constrained;
        }

        public void setDateValue(DateValue dateValue) {
            this.dateValue = dateValue;
        }

        public void setMatch(Term.MatchType matchType) {
            this.match = matchType;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public void setType(Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this,
                    TO_STRING_STYLE);
        }

    }

    /**
     * Contains description of the node property.
     *
     * @author Sergiy Shyrkov
     */
    public static class NodePropertyDescriptor implements Serializable {

        private static final long serialVersionUID = 857471721394958140L;

        private String[] allowedValues;

        private boolean constrained;

        private String defaultValue;

        private String label;

        private boolean multiple;

        private String name;

        private Map<String, String> selectorOptions;

        private NodeProperty.Type type = NodeProperty.Type.TEXT;

        /**
         * Initializes an instance of this class.
         *
         * @param name
         *            node type name
         * @param label
         *            display label
         * @param type
         *            the property type
         */
        public NodePropertyDescriptor(String name, String label,
                NodeProperty.Type type) {
            super();
            this.name = name;
            this.label = label;
            this.type = type;
        }

        public String[] getAllowedValues() {
            return allowedValues;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getSelectorOptions() {
            return selectorOptions;
        }

        public NodeProperty.Type getType() {
            return type;
        }

        public boolean isConstrained() {
            return constrained;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public void setAllowedValues(String[] allowedValues) {
            this.allowedValues = allowedValues;
        }

        public void setConstrained(boolean constrained) {
            this.constrained = constrained;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setSelectorOptions(Map<String, String> selectorOptions) {
            this.selectorOptions = new HashMap<String, String>(selectorOptions);
        }

        public void setType(NodeProperty.Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this);
        }
    }

    protected static class NodePropertyFactory implements Factory, Serializable {
        private static final long serialVersionUID = 3303613294641347422L;

        @Override
        public Object create() {
            return new NodeProperty();
        }
    }

    protected static class NodePropertyMapFactory implements Factory, Serializable {
        private static final long serialVersionUID = 5271166314214230283L;

        @Override
        public Object create() {
            return LazyMap.decorate(
                    new HashMap<String, NodeProperty>(),
                    new NodePropertyFactory());
        }
    }

    private static class OrderingFactory implements Factory, Serializable {
        private static final long serialVersionUID = -2291640852801927345L;

        @Override
        public Object create() {
            return new Ordering();
        }
    }

    private static class TermFactory implements Factory, Serializable {

        private static final long serialVersionUID = -7196425250357122068L;

        @Override
        public Object create() {
            return new Term();
        }
    }

    /**
     * Single text search criterion with a search text and match type.
     *
     * @author Sergiy Shyrkov
     */
    public static class Term implements Serializable {

        public enum MatchType {
            ALL_WORDS,
            ANY_WORD,
            AS_IS,
            EXACT_PHRASE,
            WITHOUT_WORDS,
            EXACT_PROPERTY_VALUE,
            NO_EXACT_PROPERTY_VALUE;
        }

        /**
         * Represents a set of fields to consider during search.
         *
         * @author Sergiy Shyrkov
         */
        public static class SearchFields implements Serializable {

            private static final long serialVersionUID = 6583369520862461173L;

            private boolean description;

            private boolean fileContent;

            private boolean filename;

            private boolean keywords;

            private boolean siteContent;

            private boolean tags;

            private boolean title;

            public boolean isDescription() {
                return description;
            }

            public boolean isFileContent() {
                return fileContent;
            }

            public boolean isFilename() {
                return filename;
            }

            public boolean isKeywords() {
                return keywords;
            }

            public boolean isSiteContent() {
                return siteContent;
            }

            public boolean isTags() {
                return tags;
            }

            public boolean isTitle() {
                return title;
            }

            public void setCustom(String[] customFields) {
                for (String custom : customFields) {
                    if (custom != null) {
                        if (custom.contains("siteContent")) {
                            setSiteContent(true);
                        }
                        if (custom.contains("fileContent")) {
                            setFileContent(true);
                        }
                        if (custom.contains("description")) {
                            setDescription(true);
                        }
                        if (custom.contains("title")) {
                            setTitle(true);
                        }
                        if (custom.contains("filename")) {
                            setFilename(true);
                        }
                        if (custom.contains("keywords")) {
                            setKeywords(true);
                        }
                        if (custom.contains("tags")) {
                            setTags(true);
                        }
                        if (custom.contains("files")) {
                            setDescription(true);
                            setFileContent(true);
                            setFilename(true);
                            setKeywords(true);
                            setTitle(true);
                        }
                    }
                }
            }

            public void setDescription(boolean description) {
                this.description = description;
            }

            public void setFileContent(boolean everywhere) {
                this.fileContent = everywhere;
            }

            public void setFilename(boolean filename) {
                this.filename = filename;
            }

            public void setKeywords(boolean keywords) {
                this.keywords = keywords;
            }

            public void setSiteContent(boolean content) {
                this.siteContent = content;
                this.tags = content;
            }

            public void setFiles(boolean files) {
                setDescription(files);
                setFileContent(files);
                setFilename(files);
                setKeywords(files);
                setTitle(files);
            }

            public void setTags(boolean tags) {
                this.tags = tags;
            }

            public void setTitle(boolean title) {
                this.title = title;
            }

            @Override
            public String toString() {
                return ReflectionToStringBuilder.reflectionToString(this,
                        TO_STRING_STYLE);
            }

        }

        private static final long serialVersionUID = -3881090179063748926L;

        private SearchFields fields = new SearchFields();

        private MatchType match = MatchType.AS_IS;

        private String term;

        private boolean applyFilter;

        public SearchFields getFields() {
            return fields;
        }

        public MatchType getMatch() {
            return match;
        }

        public String getTerm() {
            return term;
        }

        public boolean isEmpty() {
            return isValueEmpty(term);
        }

        public void setFields(SearchFields searchFields) {
            this.fields = searchFields;
        }

        public void setMatch(MatchType matchType) {
            this.match = matchType;
        }

        public void setTerm(String term) {
            this.term = term;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this,
                    TO_STRING_STYLE);
        }

        public boolean isApplyFilter() {
            return applyFilter;
        }

        public void setApplyFilter(boolean applyFilter) {
            this.applyFilter = applyFilter;
        }
    }

    /**
     * Represents an ordering definition
     *
     * @author Benjamin Papez
     */
    public static class Ordering implements Serializable {

        private static final long serialVersionUID = -8242956239071973316L;
        public enum Order {
            ASCENDING, DESCENDING;
        }

        public enum CaseConversion {
            LOWER, UPPER;
        }

        public enum Operand {
            SCORE, PROPERTY;
        }

        private Order order = Order.DESCENDING;
        private CaseConversion caseConversion;
        private Operand operand = Operand.SCORE;
        private boolean normalize;
        private String propertyName;

        public Order getOrder() {
            return order;
        }
        public void setOrder(Order order) {
            this.order = order;
        }
        public CaseConversion getCaseConversion() {
            return caseConversion;
        }
        public void setCaseConversion(CaseConversion caseConversion) {
            this.caseConversion = caseConversion;
        }
        public boolean isNormalize() {
            return normalize;
        }
        public void setNormalize(boolean normalize) {
            this.normalize = normalize;
        }
        public String getPropertyName() {
            return propertyName;
        }
        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }
        public Operand getOperand() {
            return operand;
        }
        public void setOperand(Operand operand) {
            this.operand = operand;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this,
                    TO_STRING_STYLE);
        }
    }

    private static final long serialVersionUID = 4633533116047727827L;

    private static final ToStringStyle TO_STRING_STYLE = ToStringStyle.MULTI_LINE_STYLE;

    private static boolean isValueEmpty(String value) {
        return StringUtils.isBlank(value);
    }

    private static List<String> listToString(List<?> items) {
        List<String> toStringItems = new LinkedList<String>();
        for (Object obj : items) {
            if (obj instanceof Term) {
                Term term = (Term) obj;
                toStringItems.add(new ToStringBuilder(obj, TO_STRING_STYLE)
                        .append("term", term.getTerm()).append("match",
                                term.getMatch()).append(
                                "fields",
                                ReflectionToStringBuilder.reflectionToString(
                                        term.getFields(), TO_STRING_STYLE))
                        .toString());

            } else {
                toStringItems.add(ReflectionToStringBuilder.reflectionToString(
                        obj, TO_STRING_STYLE));
            }
        }
        return toStringItems;
    }

    private DateValue created = new DateValue();

    private String createdBy;

    private Collection<BaseFacetDefinition> facetDefinitions;

    private HierarchicalValue filePath = new HierarchicalValue();

    private String fileType;

    private int itemsPerPage;

    private CommaSeparatedMultipleValue languages = new CommaSeparatedMultipleValue();

    private DateValue lastModified = new DateValue();

    private String lastModifiedBy;

    private long limit;

    private String nodeType;

    private long offset;

    private String originSiteKey;

    private HierarchicalValue pagePath = new HierarchicalValue();

    private Map<String /* nodeType */, Map<String /* propertyName */, NodeProperty>> properties = LazyMap
            .decorate(new HashMap<String, Map<String, NodeProperty>>(),
                    new NodePropertyMapFactory());

    /**
     * @deprecated Not implemented
     */
    @Deprecated
    private String rawQuery;

    private CommaSeparatedMultipleValue sites = new CommaSeparatedMultipleValue();
    private CommaSeparatedMultipleValue sitesForReferences = new CommaSeparatedMultipleValue();

    private List<Term> terms = LazyList.decorate(new LinkedList<Term>(), new TermFactory());

    private List<Ordering> orderings = LazyList.decorate(new LinkedList<Ordering>(), new OrderingFactory());

    /**
     * Initializes an instance of this class.
     */
    public SearchCriteria() {
        super();
    }

    public DateValue getCreated() {
        return created;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Collection<BaseFacetDefinition> getFacetDefinitions() {
        return facetDefinitions;
    }

    public HierarchicalValue getFilePath() {
        return filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public CommaSeparatedMultipleValue getLanguages() {
        return languages;
    }

    public DateValue getLastModified() {
        return lastModified;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * Returns the maximum hit count to be returned. If the limit was not set, returns 0.
     * @return the maximum hit count to be returned. If the limit was not set, returns 0
     */
    public long getLimit() {
        return limit;
    }

    public String getNodeType() {
        return nodeType;
    }

    /**
     * Returns the start offset of the search hit list. If the offset was not set, returns 0.
     * @return the start offset of the search hit list. If the offset was not set, returns 0
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the origin site key, i.e. the key of the "current" site, where
     * the query was executed from. This value is used for example to resolve
     * the tag by its name as tags are site-specific.
     *
     * @return the originSiteKey the origin site key, i.e. the key of the
     *         "current" site, where the query was executed from. This value is
     *         used for example to resolve the tag by its name as tags are
     *         site-specific.
     */
    public String getOriginSiteKey() {
        return originSiteKey;
    }

    public HierarchicalValue getPagePath() {
        return pagePath;
    }

    public Map<String /* nodeType */, Map<String /* propertyName */, NodeProperty>> getProperties() {
        return properties;
    }

    public List<NodeProperty> getPropertiesAll() {
        List<NodeProperty> props = new LinkedList<NodeProperty>();

        for (Map<String, NodeProperty> docTypeEntry : getProperties()
                .values()) {
            for (NodeProperty prop : docTypeEntry.values()) {
                props.add(prop);
            }
        }

        return props;
    }

    /**
     * @deprecated Not implemented
     */
    @Deprecated
    public String getRawQuery() {
        return rawQuery;
    }

    public CommaSeparatedMultipleValue getSites() {
        return sites;
    }

    public CommaSeparatedMultipleValue getSitesForReferences() {
        return sitesForReferences;
    }

    public List<Term> getTerms() {
        return terms;
    }

    public List<Ordering> getOrderings() {
        return orderings;
    }

    /**
     * Returns <code>true</code> if none of required search parameters was
     * specified; otherwise returns <code>false</code>.
     *
     * @return <code>true</code> if none of required search parameters was
     *         specified; otherwise returns <code>false</code>
     */
    public boolean isEmpty() {
        boolean empty = isValueEmpty(getNodeType())
                && isValueEmpty(getFileType()) && isValueEmpty(getCreatedBy())
                && getCreated().isEmpty() && isValueEmpty(getLastModifiedBy())
                && getLastModified().isEmpty() && getPagePath().isEmpty()
                && getFilePath().isEmpty();

        if (empty) {
            for (Term term : getTerms()) {
                if (!term.isEmpty()) {
                    empty = false;
                    break;
                }
            }
        }

        if (empty) {
            for (Map<String, NodeProperty> docProperties : getProperties()
                    .values()) {
                for (NodeProperty prop : docProperties.values()) {
                    if (!prop.isEmpty()) {
                        empty = false;
                        break;
                    }
                    if (!empty) {
                        break;
                    }
                }
            }
        }

        return empty;
    }

    public void setCreated(DateValue creationDate) {
        this.created = creationDate;
    }

    public void setCreatedBy(String author) {
        this.createdBy = author;
    }

    public void setFacetDefinitions(Collection<BaseFacetDefinition> facetDefinitions) {
        this.facetDefinitions = facetDefinitions != null ? Collections.unmodifiableCollection(facetDefinitions) : null;
    }

    public void setFilePath(HierarchicalValue fileLocation) {
        this.filePath = fileLocation;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public void setLanguages(CommaSeparatedMultipleValue languages) {
        this.languages = languages;
    }

    public void setLastModified(DateValue lastModificationDate) {
        this.lastModified = lastModificationDate;
    }

    public void setLastModifiedBy(String lastEditor) {
        this.lastModifiedBy = lastEditor;
    }

    /**
     * Sets the maximum size of the result set to <code>limit</code>.
     * @param limit the maximum hot count to be returned
     */
    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * Sets the start offset of the result set to <code>offset</code>.
     * @param offset the start offset of the search hit list
     */
    public void setOffset(long offset) {
        this.offset = offset;
    }

    /**
     * Sets the origin site key, i.e. the key of the "current" site, where the
     * query was executed from. This value is used for example to resolve the
     * tag by its name as tags are site-specific.
     *
     * @param originSiteKey the origin site key, i.e. the key of the "current"
     *            site, where the query was executed from. This value is used
     *            for example to resolve the tag by its name as tags are
     *            site-specific.
     */
    public void setOriginSiteKey(String originSiteKey) {
        this.originSiteKey = originSiteKey;
    }

    public void setPagePath(HierarchicalValue pagePath) {
        this.pagePath = pagePath;
    }

    public void setProperties(
            Map<String, Map<String, NodeProperty>> properties) {
        this.properties = properties;
    }

    /**
     * @deprecated Not implemented
     */
    @Deprecated
    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public void setSites(CommaSeparatedMultipleValue sites) {
        this.sites = sites;
    }

    public void setSitesForReferences(CommaSeparatedMultipleValue sitesForReferences) {
        this.sitesForReferences = sitesForReferences;
    }

    /**
     * Simplified method for setting the search term. This method resets all
     * text searches, creates a new one with the provided term, default match
     * type and all search fields.
     *
     * @param term
     *            the search term string
     */
    public void setTerm(String term) {
        getTerms().clear();
        Term search = getTerms().get(0);
        search.setTerm(term);
        search.getFields().setFileContent(true);
    }

    public void setTerms(List<Term> textSearches) {
        this.terms = textSearches;
    }

    public void setOrderings(List<Ordering> orderings) {
        this.orderings = orderings;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, TO_STRING_STYLE).append("createdBy", this.getCreatedBy())
                .append("created", this.getCreated()).append("lastModifiedBy", this.getLastModifiedBy())
                .append("lastModified", this.getLastModified()).append("pagePath", this.getPagePath())
                .append("fileType", this.getFileType()).append("nodeType", this.getNodeType()).append("filePath", this.getFilePath())
                .append("properties", listToString(this.getPropertiesAll())).append("terms", listToString(this.getTerms()))
                .append("orderings", listToString(this.getOrderings()))
                .append("itemsPerPage", this.getItemsPerPage()).append("sites", this.getSites())
                .append("sitesForReferences", this.getSitesForReferences()).append("languages", this.getLanguages())
                .append("limit", this.getLimit()).append("offset", this.getOffset()).append("originSiteKey", this.getOriginSiteKey())
                .toString();
    }

    /**
     * Checks if this search criteria represents a file search.
     *
     * @return <code>true</code> if this search criteria represents file search; <code>false</code> otherwise
     */
    public boolean isFileSearch() {
        for (Term term : getTerms()) {
            if (term.getFields() != null
                    && (term.getFields().isSiteContent() || (!term.getFields().isDescription()
                            && !term.getFields().isFileContent() && !term.getFields().isFilename()
                            && !term.getFields().isKeywords() && !term.getFields().isTitle()))
                    && !(term.getFields().isDescription() && term.getFields().isFileContent()
                            && term.getFields().isFilename() && term.getFields().isKeywords()
                            && term.getFields().isTitle())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this search criteria represents a site content search.
     *
     * @return <code>true</code> if this search criteria represents site search; <code>false</code> otherwise
     */
    public boolean isSiteSearch() {
        for (Term term : getTerms()) {
            if (term.getFields() != null && term.getFields().isSiteContent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a hash code for this object, calculate based on all fields.
     * 
     * @return a hash code for this object, calculate based on all fields
     */
    public int toHashCode() {
        return new HashCodeBuilder().append(created).append(createdBy).append(facetDefinitions).append(filePath)
                .append(fileType).append(itemsPerPage).append(languages).append(lastModified).append(lastModifiedBy)
                .append(limit).append(nodeType).append(offset).append(orderings).append(originSiteKey).append(pagePath)
                .append(properties).append(rawQuery).append(sites).append(sitesForReferences).append(terms)
                .toHashCode();
    }
}
