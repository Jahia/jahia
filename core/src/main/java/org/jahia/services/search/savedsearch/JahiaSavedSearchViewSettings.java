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
package org.jahia.services.search.savedsearch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Saved search view settings bean.
 * 
 * @author Khue Nguyen
 */
public class JahiaSavedSearchViewSettings implements Cloneable {

    /**
     * Saved search view field bean, used for save search view customization.
     * 
     * @author Khue Nguyen
     */
    public static class ViewField implements Cloneable {

        private String label;

        private String name;

        private String resourceKey;

        private boolean selected;

        @Override
        public ViewField clone() {
            try {
                return (ViewField) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalArgumentException(e);
            }
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public String getResourceKey() {
            return resourceKey;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setResourceKey(String resourceKey) {
            this.resourceKey = resourceKey;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

    private boolean ascending;

    private transient Map<String, ViewField> fieldMap = new HashMap<String, ViewField>();

    private List<ViewField> fields = new LinkedList<ViewField>();

    private String sortBy;

    @Override
    public JahiaSavedSearchViewSettings clone() {
        JahiaSavedSearchViewSettings settings;
        try {
            settings = (JahiaSavedSearchViewSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
        List<ViewField> clonedFields = new LinkedList<ViewField>();
        for (ViewField fld : fields) {
            clonedFields.add(fld.clone());
        }
        settings.setFields(clonedFields);

        return settings;
    }

    public ViewField getField(String name) {
        ViewField foundField = getFieldMap().get(name);
        if (null == foundField) {
            throw new IllegalArgumentException("Field with the name '" + name
                    + "' cannot be found");
        }
        return foundField;
    }

    public Map<String, ViewField> getFieldMap() {
        if (fieldMap == null) {
            initFieldMap();
        }
        return fieldMap;
    }

    public List<ViewField> getFields() {
        return fields;
    }

    public String getFieldsOrder() {
        List<String> orderedFields = new LinkedList<String>();
        for (ViewField fld : fields) {
            orderedFields.add(fld.getName());
        }
        return StringUtils.join(orderedFields.iterator(), ',');
    }

    public String getSelectedFieldsOrder() {
        List<String> orderedFields = new LinkedList<String>();
        for (ViewField fld : fields) {
            if (fld.isSelected()) {
                orderedFields.add(fld.getName());
            }
        }
        return StringUtils.join(orderedFields.iterator(), ',');
    }

    public String getSortBy() {
        return sortBy;
    }

    private void initFieldMap() {
        fieldMap = new HashMap<String, ViewField>(fields.size());
        for (ViewField viewField : fields) {
            fieldMap.put(viewField.getName(), viewField);
        }
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setFields(List<ViewField> fields) {
        this.fields = fields;
        initFieldMap();
    }

    public void setFieldsOrder(String order) {
        String[] fieldNames = StringUtils.split(order, ',');
        List<ViewField> orderedFields = new LinkedList<ViewField>();
        for (String name : fieldNames) {
            orderedFields.add(getField(name));
        }
        setFields(orderedFields);
    }

    public void setSelectedForAll(boolean selected) {
        for (ViewField fld : fields) {
            fld.setSelected(false);
        }
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

}
