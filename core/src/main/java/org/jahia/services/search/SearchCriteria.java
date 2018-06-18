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
import java.util.Arrays;
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
     * 
     * Note, please, if you are creating a sub-class for this class and adding fields, be sure to override {@link #hashCode()} and
     * {@link #equals(Object)} methods.
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + maxGroups;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            BaseFacetDefinition other = (BaseFacetDefinition) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            if (maxGroups != other.maxGroups)
                return false;
            return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((from == null) ? 0 : from.hashCode());
            result = prime * result + ((fromAsDate == null) ? 0 : fromAsDate.hashCode());
            result = prime * result + ((to == null) ? 0 : to.hashCode());
            result = prime * result + ((toAsDate == null) ? 0 : toAsDate.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            DateValue other = (DateValue) obj;
            if (from == null) {
                if (other.from != null)
                    return false;
            } else if (!from.equals(other.from))
                return false;
            if (fromAsDate == null) {
                if (other.fromAsDate != null)
                    return false;
            } else if (!fromAsDate.equals(other.fromAsDate))
                return false;
            if (to == null) {
                if (other.to != null)
                    return false;
            } else if (!to.equals(other.to))
                return false;
            if (toAsDate == null) {
                if (other.toAsDate != null)
                    return false;
            } else if (!toAsDate.equals(other.toAsDate))
                return false;
            if (type != other.type)
                return false;
            return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + (includeChildren ? 1231 : 1237);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            HierarchicalValue other = (HierarchicalValue) obj;
            if (includeChildren != other.includeChildren)
                return false;
            return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(values);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MultipleValue other = (MultipleValue) obj;
            if (!Arrays.equals(values, other.values))
                return false;
            return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((categoryValue == null) ? 0 : categoryValue.hashCode());
            result = prime * result + (constrained ? 1231 : 1237);
            result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
            result = prime * result + ((match == null) ? 0 : match.hashCode());
            result = prime * result + (multiple ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!super.equals(obj))
                return false;
            if (getClass() != obj.getClass())
                return false;
            NodeProperty other = (NodeProperty) obj;
            if (categoryValue == null) {
                if (other.categoryValue != null)
                    return false;
            } else if (!categoryValue.equals(other.categoryValue))
                return false;
            if (constrained != other.constrained)
                return false;
            if (dateValue == null) {
                if (other.dateValue != null)
                    return false;
            } else if (!dateValue.equals(other.dateValue))
                return false;
            if (match != other.match)
                return false;
            if (multiple != other.multiple)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (nodeType == null) {
                if (other.nodeType != null)
                    return false;
            } else if (!nodeType.equals(other.nodeType))
                return false;
            if (type != other.type)
                return false;
            return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(allowedValues);
            result = prime * result + (constrained ? 1231 : 1237);
            result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            result = prime * result + (multiple ? 1231 : 1237);
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((selectorOptions == null) ? 0 : selectorOptions.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            NodePropertyDescriptor other = (NodePropertyDescriptor) obj;
            if (!Arrays.equals(allowedValues, other.allowedValues))
                return false;
            if (constrained != other.constrained)
                return false;
            if (defaultValue == null) {
                if (other.defaultValue != null)
                    return false;
            } else if (!defaultValue.equals(other.defaultValue))
                return false;
            if (label == null) {
                if (other.label != null)
                    return false;
            } else if (!label.equals(other.label))
                return false;
            if (multiple != other.multiple)
                return false;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (selectorOptions == null) {
                if (other.selectorOptions != null)
                    return false;
            } else if (!selectorOptions.equals(other.selectorOptions))
                return false;
            if (type != other.type)
                return false;
            return true;
        }
    }

    private static class FacetMapFactory implements Factory, Serializable {

        private static final long serialVersionUID = 7123060746726311423L;

        @Override
        public Object create() {
            return new HashMap<String, String>();
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

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + (description ? 1231 : 1237);
                result = prime * result + (fileContent ? 1231 : 1237);
                result = prime * result + (filename ? 1231 : 1237);
                result = prime * result + (keywords ? 1231 : 1237);
                result = prime * result + (siteContent ? 1231 : 1237);
                result = prime * result + (tags ? 1231 : 1237);
                result = prime * result + (title ? 1231 : 1237);
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (obj == null)
                    return false;
                if (getClass() != obj.getClass())
                    return false;
                SearchFields other = (SearchFields) obj;
                if (description != other.description)
                    return false;
                if (fileContent != other.fileContent)
                    return false;
                if (filename != other.filename)
                    return false;
                if (keywords != other.keywords)
                    return false;
                if (siteContent != other.siteContent)
                    return false;
                if (tags != other.tags)
                    return false;
                if (title != other.title)
                    return false;
                return true;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (applyFilter ? 1231 : 1237);
            result = prime * result + ((fields == null) ? 0 : fields.hashCode());
            result = prime * result + ((match == null) ? 0 : match.hashCode());
            result = prime * result + ((term == null) ? 0 : term.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Term other = (Term) obj;
            if (applyFilter != other.applyFilter)
                return false;
            if (fields == null) {
                if (other.fields != null)
                    return false;
            } else if (!fields.equals(other.fields))
                return false;
            if (match != other.match)
                return false;
            if (term == null) {
                if (other.term != null)
                    return false;
            } else if (!term.equals(other.term))
                return false;
            return true;
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
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((caseConversion == null) ? 0 : caseConversion.hashCode());
            result = prime * result + (normalize ? 1231 : 1237);
            result = prime * result + ((operand == null) ? 0 : operand.hashCode());
            result = prime * result + ((order == null) ? 0 : order.hashCode());
            result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Ordering other = (Ordering) obj;
            if (caseConversion != other.caseConversion)
                return false;
            if (normalize != other.normalize)
                return false;
            if (operand != other.operand)
                return false;
            if (order != other.order)
                return false;
            if (propertyName == null) {
                if (other.propertyName != null)
                    return false;
            } else if (!propertyName.equals(other.propertyName))
                return false;
            return true;
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

    /**
     * Active facets for the search.
     */
    private Map<String, Map<String, String>> facets = LazyMap.decorate(new HashMap<String, Map<String, String>>(),
            new FacetMapFactory());
    
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

    /**
     * Return active facet filters.
     * 
     * @return active facet filters
     */
    public Map<String, Map<String, String>> getFacets() {
        return facets;
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

    /**
     * Sets the active facet filters.
     * 
     * @param facets the active facet filters
     */
    public void setFacets(Map<String, Map<String, String>> facets) {
        this.facets = facets;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((created == null) ? 0 : created.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((facetDefinitions == null) ? 0 : facetDefinitions.hashCode());
        result = prime * result + ((facets == null) ? 0 : facets.hashCode());
        result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
        result = prime * result + ((fileType == null) ? 0 : fileType.hashCode());
        result = prime * result + itemsPerPage;
        result = prime * result + ((languages == null) ? 0 : languages.hashCode());
        result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
        result = prime * result + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
        result = prime * result + (int) (limit ^ (limit >>> 32));
        result = prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
        result = prime * result + (int) (offset ^ (offset >>> 32));
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
        result = prime * result + ((originSiteKey == null) ? 0 : originSiteKey.hashCode());
        result = prime * result + ((pagePath == null) ? 0 : pagePath.hashCode());
        result = prime * result + ((properties == null) ? 0 : properties.hashCode());
        result = prime * result + ((rawQuery == null) ? 0 : rawQuery.hashCode());
        result = prime * result + ((sites == null) ? 0 : sites.hashCode());
        result = prime * result + ((sitesForReferences == null) ? 0 : sitesForReferences.hashCode());
        result = prime * result + ((terms == null) ? 0 : terms.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchCriteria other = (SearchCriteria) obj;
        if (created == null) {
            if (other.created != null)
                return false;
        } else if (!created.equals(other.created))
            return false;
        if (createdBy == null) {
            if (other.createdBy != null)
                return false;
        } else if (!createdBy.equals(other.createdBy))
            return false;
        if (facetDefinitions == null) {
            if (other.facetDefinitions != null)
                return false;
        } else if (!facetDefinitions.equals(other.facetDefinitions))
            return false;
        if (facets == null) {
            if (other.facets != null)
                return false;
        } else if (!facets.equals(other.facets))
            return false;
        if (filePath == null) {
            if (other.filePath != null)
                return false;
        } else if (!filePath.equals(other.filePath))
            return false;
        if (fileType == null) {
            if (other.fileType != null)
                return false;
        } else if (!fileType.equals(other.fileType))
            return false;
        if (itemsPerPage != other.itemsPerPage)
            return false;
        if (languages == null) {
            if (other.languages != null)
                return false;
        } else if (!languages.equals(other.languages))
            return false;
        if (lastModified == null) {
            if (other.lastModified != null)
                return false;
        } else if (!lastModified.equals(other.lastModified))
            return false;
        if (lastModifiedBy == null) {
            if (other.lastModifiedBy != null)
                return false;
        } else if (!lastModifiedBy.equals(other.lastModifiedBy))
            return false;
        if (limit != other.limit)
            return false;
        if (nodeType == null) {
            if (other.nodeType != null)
                return false;
        } else if (!nodeType.equals(other.nodeType))
            return false;
        if (offset != other.offset)
            return false;
        if (orderings == null) {
            if (other.orderings != null)
                return false;
        } else if (!orderings.equals(other.orderings))
            return false;
        if (originSiteKey == null) {
            if (other.originSiteKey != null)
                return false;
        } else if (!originSiteKey.equals(other.originSiteKey))
            return false;
        if (pagePath == null) {
            if (other.pagePath != null)
                return false;
        } else if (!pagePath.equals(other.pagePath))
            return false;
        if (properties == null) {
            if (other.properties != null)
                return false;
        } else if (!properties.equals(other.properties))
            return false;
        if (rawQuery == null) {
            if (other.rawQuery != null)
                return false;
        } else if (!rawQuery.equals(other.rawQuery))
            return false;
        if (sites == null) {
            if (other.sites != null)
                return false;
        } else if (!sites.equals(other.sites))
            return false;
        if (sitesForReferences == null) {
            if (other.sitesForReferences != null)
                return false;
        } else if (!sitesForReferences.equals(other.sitesForReferences))
            return false;
        if (terms == null) {
            if (other.terms != null)
                return false;
        } else if (!terms.equals(other.terms))
            return false;
        return true;
    }
}
