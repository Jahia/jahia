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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.JahiaTemplatesRBLoader;

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
        return Collections.unmodifiableMap(selectorOptions);
    }

    public void setSelectorOptions(Map<String,String> selectorOptions) {
        this.selectorOptions = selectorOptions;
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

    protected String getResourceBundleId() {
        return ((ExtendedNodeType)getDeclaringNodeType()).getResourceBundleId();
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
            JahiaTemplatesPackage aPackage = getDeclaringNodeType().getTemplatePackage();
            label = new JahiaResourceBundle(getResourceBundleId(), locale, aPackage!=null ? aPackage.getName(): null, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), null)).getString(
                    getResourceBundleKey(), JCRContentUtils.replaceColon(getName()));
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
        if(label==null) {
            JahiaTemplatesPackage aPackage = nodeType.getTemplatePackage();
            label = new JahiaResourceBundle(nodeType.getResourceBundleId(), locale, aPackage!=null ? aPackage.getName(): null, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), null)).getString(
                    getResourceBundleKey(nodeType), getLabel(locale));
            labelNodeType.put(nodeType.getName(), label);
        }
        return label;
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
        if(label==null) {
            JahiaTemplatesPackage aPackage = nodeType.getTemplatePackage();
            label = new JahiaResourceBundle(nodeType.getResourceBundleId(), locale, aPackage!=null ? aPackage.getName(): null, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), null)).getString(
                    getResourceBundleKey(nodeType)+".ui.tooltip", "");
            labelNodeType.put(nodeType.getName(), label);
        }
        return label;
    }

    public String getItemType() {
        if (itemType == null) {
            itemType = getDeclaringNodeType().getItemsType();
            if (itemType == null) {
                itemType = "content";
            }
        }
        return itemType;
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
    
    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final ExtendedItemDefinition castOther = (ExtendedItemDefinition) obj;
            return new EqualsBuilder()
                .append(this.getName(), castOther.getName())
                .append(this.getDeclaringNodeType().getName(), castOther.getDeclaringNodeType().getName())                
                .isEquals();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getDeclaringNodeType().getName())                
                .toHashCode();
    }    


    public void clearLabels() {
        labels.clear();
        tooltipsByNodeType.clear();
    }
}
