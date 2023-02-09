/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.content.nodetypes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRContentUtils;

/**
 * Jahia specific {@link ItemDefinition} implementation.
 * User: toto
 * Date: 15 janv. 2008
 * Time: 17:43:58
 */
public class ExtendedItemDefinition implements ItemDefinition {

    private ExtendedNodeType declaringNodeType;
    private Name name;
    private boolean isProtected = false;
    private boolean autoCreated = false;
    private boolean mandatory = false;
    private boolean hidden;
    private String itemType;
    private int onParentVersion = OnParentVersionAction.VERSION;
    private int onConflict = OnConflictAction.USE_LATEST;
    protected int selector = 0;
    private Map<String,String> selectorOptions = new ConcurrentHashMap<String,String>();
    private Map<Locale, String> labels = new ConcurrentHashMap<Locale, String>(1);
    private Map<Locale, Map<String,String>> labelsByNodeType = new ConcurrentHashMap<Locale, Map<String, String>>(1);
    private Map<Locale, Map<String,String>> tooltipsByNodeType = new ConcurrentHashMap<Locale, Map<String, String>>(1);
    private boolean override = false;

    public ExtendedNodeType getDeclaringNodeType() {
        return declaringNodeType;
    }

    public void setDeclaringNodeType(ExtendedNodeType declaringNodeType) {
        this.declaringNodeType = declaringNodeType;
    }

    public String getName() {
        return name.toString();
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getLocalName() {
        return name.getLocalName();
    }

    public Name getNameObject() {
        return name;
    }

    public String getPrefix() {
        return name.getPrefix();
    }

    public boolean isProtected() {
        return isProtected;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public boolean isAutoCreated() {
        return autoCreated;
    }

    public void setAutoCreated(boolean autoCreated) {
        this.autoCreated = autoCreated;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getOnParentVersion() {
        return onParentVersion;
    }

    public void setOnParentVersion(int onParentVersion) {
        this.onParentVersion = onParentVersion;
    }

    public int getOnConflict() {
        return onConflict;
    }

    public void setOnConflict(int onConflict) {
        this.onConflict = onConflict;
    }

    public int getSelector() {
        return selector;
    }

    public void setSelector(int selector) {
        this.selector = selector;
    }

    public Map<String,String> getSelectorOptions() {
        return selectorOptions;
    }

    public void setSelectorOptions(Map<String,String> selectorOptions) {
        this.selectorOptions = Collections.unmodifiableMap(selectorOptions);
    }

    public boolean isNode() {
        return false;
    }

    public boolean isOverride() {
        return override;
    }

    public void setOverride(boolean override) {
        this.override = override;
    }

    public String getResourceBundleKey() {
        return getResourceBundleKey(getDeclaringNodeType());
    }

    public String getResourceBundleKey(ExtendedNodeType nodeType) {
        if(nodeType==null)
            return JCRContentUtils.replaceColon((getDeclaringNodeType().getName() + "." + getName()));
        else
            return JCRContentUtils.replaceColon((nodeType.getName() + "." + getName()));
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            label = getDeclaringNodeType().lookupLabel(getResourceBundleKey(), locale, JCRContentUtils.replaceColon(getName()));
            labels.put(locale, label);
        }
        return label;
    }

    public String getLabel(Locale locale,ExtendedNodeType nodeType) {
        if(nodeType==null) {
            return getLabel(locale);
        }
        Map<String, String> labelNodeType = labelsByNodeType.get(locale);
        if (labelNodeType == null) {
            labelNodeType = new ConcurrentHashMap<String, String>();
            labelsByNodeType.put(locale,labelNodeType);
        }
        String label = labelNodeType.get(nodeType.getName());
        if (label == null) {
            label = nodeType.lookupLabel(getResourceBundleKey(nodeType), locale, null);
            if (label == null) {
                label = getLabel(locale);
            }
            labelNodeType.put(nodeType.getName(), label);
        }
        return label;
    }

    public void addLabel(Locale locale, String label){
        labels.put(locale, label);
    }

    public String getTooltip(Locale locale,ExtendedNodeType extendedNodeType) {
        ExtendedNodeType nodeType = extendedNodeType;
        if(nodeType==null) {
            nodeType = getDeclaringNodeType();
        }
        Map<String, String> labelNodeType = tooltipsByNodeType.get(locale);
        if (labelNodeType == null) {
            labelNodeType = new ConcurrentHashMap<String, String>();
            tooltipsByNodeType.put(locale,labelNodeType);
        }
        String label = labelNodeType.get(nodeType.getName());
        if (label == null) {
            label = nodeType.lookupLabel(getResourceBundleKey(nodeType)+".ui.tooltip", locale, StringUtils.EMPTY);
            labelNodeType.put(nodeType.getName(), label);
        }
        return label;
    }

    public void addTooltip(Locale locale,ExtendedNodeType extendedNodeType,String label){
        Map<String, String> labelByNodeType = tooltipsByNodeType.get(locale);
        if (labelByNodeType == null) {
            labelByNodeType = new ConcurrentHashMap<>();
        }
        labelByNodeType.put(extendedNodeType.getName(), label);
        tooltipsByNodeType.put(locale, labelByNodeType);
    }

    public String getItemType() {
        if (itemType == null) {
            String inheritedItemType = getDeclaringNodeType().getItemsType();
            if (inheritedItemType == null) {
                inheritedItemType = "content";
            }
            return inheritedItemType;
        }
        return itemType;
    }

    public String getLocalItemType() {
        return itemType;
    }

    public boolean isUnstructured() {
        return "*".equals(getName());
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public boolean isContentItem() {
        return !isHidden()&&"content".equals(getItemType());
//        declaringNodeType.isNodeType("jmix:droppableContent") || declaringNodeType.isNodeType("jnt:container")
//                 || declaringNodeType.isNodeType("jnt:content") || declaringNodeType.isNodeType("jmix:contentItem") || name.toString().equals("jcr:title") || name.toString().equals("jcr:language") || name.toString().equals("jcr:statement");
    }

    public ExtendedItemDefinition getOverridenDefinition() {
        ExtendedItemDefinition overridenItemDefintion = this;
        if (isOverride()) {
             for (ExtendedItemDefinition itemDef : declaringNodeType.getItems()) {
                 if (itemDef.getName().equals(this.getName()) && !itemDef.isOverride()) {
                     overridenItemDefintion = itemDef;
                     break;
                 }
             }
        }
        return overridenItemDefintion;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        final ExtendedItemDefinition other = (ExtendedItemDefinition) obj;

        return (getName() != null ? getName().equals(other.getName()) : other.getName() == null)
                && (getDeclaringNodeType().getName() != null ? getDeclaringNodeType().getName().equals(
                        other.getDeclaringNodeType().getName()) : other.getDeclaringNodeType().getName() == null);
    }

    @Override
    public int hashCode() {
        int hash = 17 * 37 + (getName() != null ? getName().hashCode() : 0);
        hash = 37 * hash + (getDeclaringNodeType().getName() != null ? getDeclaringNodeType().getName().hashCode() : 0);
        return hash;
    }

    public void clearLabels() {
        labels.clear();
        tooltipsByNodeType.clear();
        labelsByNodeType.clear();
    }
}
