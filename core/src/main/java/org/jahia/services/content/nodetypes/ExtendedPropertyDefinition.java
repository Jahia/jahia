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
package org.jahia.services.content.nodetypes;

import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:02:49
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedPropertyDefinition extends ExtendedItemDefinition implements PropertyDefinition {
    private NodeTypeRegistry registry;

    private int requiredType = 0;

    private boolean internationalized = false;

    private Value[] valueConstraints = new Value[0];
    private Value[] defaultValues = new Value[0];

    private boolean multiple;

    public static final int INDEXED_NO = 0;
    public static final int INDEXED_TOKENIZED = 1;
    public static final int INDEXED_UNTOKENIZED = 2;
    public static final int STORE_YES = 0;
    public static final int STORE_NO = 1;
    public static final int STORE_COMPRESS = 2;

    private int index = INDEXED_TOKENIZED;
    private double scoreboost = 1.;
    private String analyzer;
    
    private boolean sortable = false;
    private boolean facetable = false;
    private Boolean fulltextSearchable = null;

    public ExtendedPropertyDefinition(NodeTypeRegistry registry) {
        this.registry = registry;
    }

    public void setDeclaringNodeType(ExtendedNodeType declaringNodeType) {
        super.setDeclaringNodeType(declaringNodeType);
        declaringNodeType.setPropertyDefinition(getName(), this);
    }

    public int getRequiredType() {
        return requiredType;
    }

    public void setRequiredType(int requiredType) {
        this.requiredType = requiredType;
        if (selector == 0 && SelectorType.defaultSelectors.get(requiredType) != null) {
            setSelector(SelectorType.defaultSelectors.get(requiredType));
        }
    }

    public Value[] getValueConstraintsAsUnexpandedValue() {
        return valueConstraints;
    }

    public Value[] getValueConstraintsAsValue() {
        List<Value> res = new ArrayList<Value>();
        for (int i = 0; i < valueConstraints.length; i++) {
            if (valueConstraints[i] instanceof DynamicValueImpl) {
                Value[] v = ((DynamicValueImpl)valueConstraints[i]).expand();
                for (Value value : v) {
                    res.add(value);
                }
            } else {
                res.add(valueConstraints[i]);
            }
        }
        return res.toArray(new Value[res.size()]);
    }

    public String[] getValueConstraints() {
        Value[] value = getValueConstraintsAsValue();
        String[] res = new String[value.length];
        for (int i = 0; i < value.length; i++) {
            try {
                res[i] = value[i].getString();
            } catch (RepositoryException e) {
            }
        }
        return res;
    }

    public void setValueConstraints(Value[] valueConstraints) {
        if (requiredType != PropertyType.BOOLEAN) {
            this.valueConstraints = valueConstraints;
        }
    }

    public Value[] getDefaultValues() {
        List<Value> res = new ArrayList<Value>();
        for (int i = 0; i < defaultValues.length; i++) {
            if (defaultValues[i] instanceof DynamicValueImpl) {
                Value[] v = ((DynamicValueImpl)defaultValues[i]).expand();
                for (Value value : v) {
                    res.add(value);
                }
            } else {
                res.add(defaultValues[i]);
            }
        }
        return res.toArray(new Value[res.size()]);
    }

    public void setDefaultValues(Value[] defaultValues) {
        this.defaultValues = defaultValues;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isInternationalized() {
        return internationalized;
    }

    public void setInternationalized(boolean internationalized) {
        this.internationalized = internationalized;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public double getScoreboost() {
        return scoreboost;
    }

    public void setScoreboost(double scoreboost) {
        this.scoreboost = scoreboost;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public void setFacetable(boolean facetable) {
        this.facetable = facetable;
    }

    public Boolean getFulltextSearchable() {
        return fulltextSearchable;
    }

    public void setFulltextSearchable(Boolean fulltextSearchable) {
        this.fulltextSearchable = fulltextSearchable;
    }
}
