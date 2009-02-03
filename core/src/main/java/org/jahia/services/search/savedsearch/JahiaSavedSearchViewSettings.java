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
