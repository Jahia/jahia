/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.nodetypes;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.spi.commons.nodetype.InvalidConstraintException;
import org.apache.jackrabbit.spi.commons.nodetype.constraint.ValueConstraint;
import org.slf4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

import javax.jcr.Value;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * User: toto
 * Date: 4 janv. 2008
 * Time: 14:02:49
 * 
 */
public class ExtendedPropertyDefinition extends ExtendedItemDefinition implements PropertyDefinition {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ExtendedPropertyDefinition.class);

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
    
    private boolean queryOrderable = true;
    private boolean fulltextSearchable = true;
    private boolean facetable = false;
    private boolean hierarchical = false;
    private String[] availableQueryOperators = Lexer.ALL_OPERATORS;
    
    private Map<Locale, Map<String, String>> messageMaps = new ConcurrentHashMap<Locale, Map<String, String>>(1);

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
    
    public ValueConstraint[] getValueConstraintObjects() {
        ValueConstraint[] constraintObjs = null;
        try {
            String[] constraints = getValueConstraints();
            if (requiredType == PropertyType.REFERENCE
                    || requiredType == PropertyType.WEAKREFERENCE) {
                String[] expandedConstraints = new String[constraints.length];
                int i = 0;
                for (String constraint : constraints) {
                    try {
                        ExtendedNodeType nodeType = registry.getNodeType(constraint);
                        Name name = nodeType.getNameObject();
                        expandedConstraints[i++] = "{" + name.getUri() + "}" + name.getLocalName();
                    } catch (RepositoryException ex) {
                    }
                }
                constraints = expandedConstraints;
            }
            constraintObjs = ValueConstraint.create(getRequiredType(), constraints);
        } catch (InvalidConstraintException e) {
            logger.warn("Internal error during creation of constraint.", e);
        }
        return constraintObjs;
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

    public boolean isQueryOrderable() {
        return queryOrderable;
    }

    public void setQueryOrderable(boolean sortable) {
        this.queryOrderable = sortable;
    }

    public boolean isFacetable() {
        return facetable;
    }

    public void setFacetable(boolean facetable) {
        this.facetable = facetable;
    }

    public boolean isHierarchical() {
        return hierarchical;
    }

    public void setHierarchical(boolean hierarchical) {
        this.hierarchical = hierarchical;
    }

    public boolean isFullTextSearchable() {
        return fulltextSearchable;
    }

    public void setFullTextSearchable(boolean fulltextSearchable) {
        this.fulltextSearchable = fulltextSearchable;
    }

    public String[] getAvailableQueryOperators() {
        return availableQueryOperators;
    }

    public void setAvailableQueryOperators(String[] availableQueryOperators) {
        this.availableQueryOperators = availableQueryOperators;
    }
    
    public String getMessage(String msgKeySuffix, Locale locale) {
        Map<String, String> messageMap = messageMaps.get(locale);
        if (messageMap == null) {
            messageMap = new HashMap<String, String>();
            messageMaps.put(locale, messageMap);
        }
        String message = messageMap.get(msgKeySuffix);
        if (message == null) {
            JahiaTemplatesPackage aPackage = getDeclaringNodeType().getTemplatePackage();
            message = new JahiaResourceBundle(getResourceBundleId(), locale, aPackage!=null ? aPackage.getName(): null, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), null)).getString(
                    getResourceBundleKey() + (!StringUtils.isEmpty(msgKeySuffix) ? "." + msgKeySuffix : ""), "");
            messageMap.put(msgKeySuffix, message);
        }
        return message;
    }
}
