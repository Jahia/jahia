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

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 16 mars 2006
 * Time: 10:18:38
 * To change this template use File | Settings | File Templates.
 */
public class CriteriaBean {

    private String name;
    private Object value;
    private String comparator;
    private int order;
    private boolean enabled = true;
    private boolean isDate = false;

    public CriteriaBean(String name,
                        Object value,
                        String comparator,
                        int order,
                        boolean enabled,
                        boolean isDate){
        this.name = name;
        this.value = value;
        this.comparator = comparator;
        this.order = order;
        this.enabled = enabled;
        this.isDate = isDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getComparator() {
        return comparator;
    }

    public void setComparator(String comparator) {
        this.comparator = comparator;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean date) {
        isDate = date;
    }

}
