/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.engines.search;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import org.jahia.engines.search.SearchCriteria.Term.MatchType;
import org.jahia.engines.calendar.CalendarHandler;

/**
 * Bean for holding all search parameters.
 * 
 * @author Sergiy Shyrkov
 */
public class SearchCriteria implements Serializable {

    private static final long serialVersionUID = 4633533116047727827L;

    /**
     * Supports comma separated multiple values.
     * 
     * @author Sergiy Shyrkov
     */
    public static class CommaSeparatedMultipleValue extends MultipleValue {

        private static final long serialVersionUID = 2324041504396269857L;
        
        private static final char MULTIPLE_VALUE_SEPARATOR = ',';

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

        private static final long serialVersionUID = -1637520083714465344L;

        public enum Type {
            ANYTIME, LAST_MONTH, LAST_SIX_MONTHS, LAST_THREE_MONTHS, LAST_WEEK, RANGE, TODAY;
        }

        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
                CalendarHandler.DEFAULT_DATEONLY_FORMAT);

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
                try {
                    fromAsDate = DATE_FORMAT.parse(dateFromAsString);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                from = null;
                fromAsDate = null;
            }
        }

        public void setFromAsDate(Date dateFrom) {
            fromAsDate = dateFrom;
            from = dateFrom != null ? DATE_FORMAT.format(dateFrom) : null;
        }

        public void setTo(String dateToAsString) {
            if (dateToAsString != null && dateToAsString.length() > 0) {
                to = dateToAsString;
                try {
                    toAsDate = DATE_FORMAT.parse(dateToAsString);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                to = null;
                toAsDate = null;
            }
        }

        public void setToAsDate(Date dateTo) {
            toAsDate = dateTo;
            to = dateTo != null ? DATE_FORMAT.format(dateTo) : null;
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
     * Single search criterion on the node property.
     * 
     * @author Sergiy Shyrkov
     */
    public static class DocumentProperty extends MultipleValue {

        private static final long serialVersionUID = 1356495981201889467L;

        public enum Type {
            BOOLEAN, CATEGORY, DATE, TEXT;
        }

        private HierarchicalValue categoryValue = new HierarchicalValue();

        private boolean constrained;

        private DateValue dateValue = new DateValue();

        private String documentType;

        private MatchType match = MatchType.AS_IS;

        private boolean multiple;

        private String name;

        private Type type = Type.TEXT;

        public HierarchicalValue getCategoryValue() {
            return categoryValue;
        }

        public DateValue getDateValue() {
            return dateValue;
        }

        public String getDocumentType() {
            return documentType;
        }

        public MatchType getMatch() {
            return match;
        }

        public String getName() {
            return name;
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
                        "Unknown document property value type '" + type + "'");
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

        public void setDocumentType(String nodeType) {
            this.documentType = nodeType;
        }

        public void setMatch(MatchType matchType) {
            this.match = matchType;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
        }

        public void setName(String name) {
            this.name = name;
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
     * Contains description of the document (node) property.
     * 
     * @author Sergiy Shyrkov
     */
    public static class DocumentPropertyDescriptor {

        private String[] allowedValues;

        private boolean constrained;

        private String defaultValue;

        private String label;

        private boolean multiple;

        private String name;

        private Map<String,String> selectorOptions;

        private DocumentProperty.Type type = DocumentProperty.Type.TEXT;

        /**
         * Initializes an instance of this class.
         * 
         * @param name
         *            document type name
         * @param label
         *            display label
         * @param type
         *            the property type
         */
        public DocumentPropertyDescriptor(String name, String label,
                DocumentProperty.Type type) {
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

        public Map<String,String> getSelectorOptions() {
            return selectorOptions;
        }

        public DocumentProperty.Type getType() {
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

        public void setSelectorOptions(Map<String,String> selectorOptions) {
            this.selectorOptions = new HashMap<String,String>(selectorOptions);
        }

        public void setType(DocumentProperty.Type type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.reflectionToString(this);
        }
    }

    /**
     * Represents a selactable value (file location, category etc.) that is a
     * part of the hierachical structure.
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
     * Represents a selactable value (file location, category etc.) that is a
     * part of the hierachical structure.
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

    public enum SearchMode {
        AUTODETECT, FILES, PAGES;
    }

    /**
     * Single text serch criterion with a search text and match type.
     * 
     * @author Sergiy Shyrkov
     */
    public static class Term implements Serializable {

        private static final long serialVersionUID = -3881090179063748926L;

        public enum MatchType {
            ALL_WORDS, ANY_WORD, AS_IS, EXACT_PHRASE, WITHOUT_WORDS;
        }

        /**
         * Represents a set of fields to consider during search.
         * 
         * @author Sergiy Shyrkov
         */
        public static class SearchFields implements Serializable {

            private static final long serialVersionUID = 6583369520862461173L;

            private boolean content;

            private boolean description;

            private boolean documentTitle;

            private boolean filename;

            private boolean keywords;

            private boolean metadata;

            public boolean areAllSelected(SearchMode mode) {
                boolean allSelected = false;
                if (SearchMode.FILES.equals(mode)) {
                    allSelected = isContent() && isDescription()
                            && isDocumentTitle() && isFilename()
                            && isKeywords();
                } else if (SearchMode.PAGES.equals(mode)) {
                    allSelected = isContent() && isMetadata();
                } else {
                    throw new IllegalArgumentException("Search mode '" + mode
                            + "' is not supported");
                }

                return allSelected;
            }

            public boolean isContent() {
                return content
                        || (!description && !documentTitle && !filename
                                && !keywords && !metadata);
            }

            public boolean isDescription() {
                return description;
            }

            public boolean isDocumentTitle() {
                return documentTitle;
            }

            public boolean isFilename() {
                return filename;
            }

            public boolean isKeywords() {
                return keywords;
            }

            public boolean isMetadata() {
                return metadata;
            }

            public void setAll(boolean all) {
                setContent(all);
                setDescription(all);
                setDocumentTitle(all);
                setFilename(all);
                setKeywords(all);
                setMetadata(all);
            }

            public void setCustom(String custom) {
                if (custom != null) {
                    if (custom.contains("all")) {
                        setAll(true);
                    } else {
                        if (custom.contains("content")) {
                            setContent(true);
                        }
                        if (custom.contains("description")) {
                            setDescription(true);
                        }
                        if (custom.contains("documentTitle")) {
                            setDocumentTitle(true);
                        }
                        if (custom.contains("filename")) {
                            setFilename(true);
                        }
                        if (custom.contains("keywords")) {
                            setKeywords(true);
                        }
                        if (custom.contains("metadata")) {
                            setMetadata(true);
                        }
                    }
                }
            }

            public void setContent(boolean content) {
                this.content = content;
            }

            public void setDescription(boolean description) {
                this.description = description;
            }

            public void setDocumentTitle(boolean documentTitle) {
                this.documentTitle = documentTitle;
            }

            public void setFilename(boolean filename) {
                this.filename = filename;
            }

            public void setKeywords(boolean keywords) {
                this.keywords = keywords;
            }

            public void setMetadata(boolean metadata) {
                this.metadata = metadata;
            }

            @Override
            public String toString() {
                return ReflectionToStringBuilder.reflectionToString(this,
                        TO_STRING_STYLE);
            }

        }

        private SearchFields fields = new SearchFields();

        private MatchType match = MatchType.AS_IS;

        private String term;

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

    }

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

            } else if (obj instanceof DocumentProperty) {
                toStringItems.add(ReflectionToStringBuilder.reflectionToString(
                        obj, TO_STRING_STYLE));
            } else {
                toStringItems.add(ReflectionToStringBuilder.reflectionToString(
                        obj, TO_STRING_STYLE));
            }
        }
        return toStringItems;
    }

    private DateValue created = new DateValue();

    private String createdBy;

    private String documentType;

    private HierarchicalValue fileLocation = new HierarchicalValue();

    private String fileType;

    private int itemsPerPage;

    private CommaSeparatedMultipleValue languages = new CommaSeparatedMultipleValue();

    private DateValue lastModified = new DateValue();

    private String lastModifiedBy;

    private SearchMode mode = SearchMode.AUTODETECT;

    private HierarchicalValue pagePath = new HierarchicalValue();

    private Map<String /* documentType */, Map<String /* propertyName */, DocumentProperty>> properties = LazyMap
            .decorate(new HashMap<String, Map<String, DocumentProperty>>(),
                    new Factory() {
                        public Object create() {
                            return LazyMap.decorate(
                                    new HashMap<String, DocumentProperty>(),
                                    new Factory() {
                                        public Object create() {
                                            return new DocumentProperty();
                                        }
                                    });
                        }
                    });

    private String rawQuery;

    private String site;

    private List<Term> terms = LazyList.decorate(new LinkedList<Term>(),
            new Factory() {
                public Object create() {
                    return new Term();
                }
            });

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

    public String getDocumentType() {
        return documentType;
    }

    public HierarchicalValue getFileLocation() {
        return fileLocation;
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

    public SearchMode getMode() {
        return mode;
    }

    public SearchMode getModeAutodetect() {

        SearchMode resolvedMode = getMode();

        if (getMode() == SearchMode.AUTODETECT) {
            boolean fileSearch = StringUtils.isNotEmpty(getDocumentType())
                    || StringUtils.isNotEmpty(getFileType())
                    || !getFileLocation().isEmpty();
            if (!fileSearch && !getProperties().isEmpty()) {
                for (DocumentProperty property : getPropertiesAll()) {
                    if (!property.isEmpty()) {
                        fileSearch = true;
                        break;
                    }
                }
            }
            resolvedMode = fileSearch ? SearchMode.FILES : SearchMode.PAGES;
        }

        return resolvedMode;
    }

    public HierarchicalValue getPagePath() {
        return pagePath;
    }

    public Map<String /* documentType */, Map<String /* propertyName */, DocumentProperty>> getProperties() {
        return properties;
    }

    public List<DocumentProperty> getPropertiesAll() {
        List<DocumentProperty> props = new LinkedList<DocumentProperty>();

        for (Map<String, DocumentProperty> docTypeEntry : getProperties()
                .values()) {
            for (DocumentProperty prop : docTypeEntry.values()) {
                props.add(prop);
            }
        }

        return props;
    }

    public String getRawQuery() {
        return rawQuery;
    }

    public String getSite() {
        return site;
    }

    public List<Term> getTerms() {
        return terms;
    }

    /**
     * Returns <code>true</code> if none of required search parameters was
     * specified; otherwise returns <code>false</code>.
     * 
     * @return <code>true</code> if none of required search parameters was
     *         specified; otherwise returns <code>false</code>
     */
    public boolean isEmpty() {
        boolean empty = isValueEmpty(getRawQuery())
                && isValueEmpty(getDocumentType())
                && isValueEmpty(getFileType()) && isValueEmpty(getCreatedBy())
                && getCreated().isEmpty() && isValueEmpty(getLastModifiedBy())
                && getLastModified().isEmpty() && getPagePath().isEmpty()
                && getFileLocation().isEmpty();

        if (empty) {
            for (Term term : getTerms()) {
                if (!term.isEmpty()) {
                    empty = false;
                    break;
                }
            }
        }

        if (empty) {
            for (Map<String, DocumentProperty> docProperties : getProperties()
                    .values()) {
                for (DocumentProperty prop : docProperties.values()) {
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

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setFileLocation(HierarchicalValue fileLocation) {
        this.fileLocation = fileLocation;
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

    public void setMode(SearchMode mode) {
        this.mode = mode;
    }

    public void setPagePath(HierarchicalValue pagePath) {
        this.pagePath = pagePath;
    }

    public void setProperties(
            Map<String, Map<String, DocumentProperty>> properties) {
        this.properties = properties;
    }

    public void setRawQuery(String rawQuery) {
        this.rawQuery = rawQuery;
    }

    public void setSite(String site) {
        this.site = site;
    }

    /**
     * Simplified method for setting the search term. This method resets all
     * text searches, creates a new one with the providede termm, default match
     * type and all search fields.<br>
     * 
     * @param term
     *            the search term string
     */
    public void setTerm(String term) {
        getTerms().clear();
        Term search = getTerms().get(0);
        search.setTerm(term);
        search.getFields().setAll(true);
    }

    public void setTerms(List<Term> textSearches) {
        this.terms = textSearches;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, TO_STRING_STYLE).append("mode",
                this.getMode()).append("rawQuery", this.getRawQuery()).append(
                "createdby", this.getCreatedBy()).append("created",
                this.getCreated()).append("lastModifiedBy",
                this.getLastModifiedBy()).append("lastModified",
                this.getLastModified()).append("pagePath", this.getPagePath())
                .append("fileType", this.getFileType()).append("documentType",
                        this.getDocumentType()).append("fileLocation",
                        this.getFileLocation()).append("properties",
                        listToString(this.getPropertiesAll())).append("terms",
                        listToString(this.getTerms())).append("itemsPerPage",
                        this.getItemsPerPage()).append("site", this.getSite())
                .append("languages", this.getLanguages()).toString();
    }

}
