/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes;

import java.util.*;

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.jahia.bin.Jahia;
import org.jahia.services.sites.JahiaSite;
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
    private int onParentVersion = OnParentVersionAction.COPY;
    private int onConflict = OnConflictAction.USE_LATEST;
    protected int selector = 0;
    private Map<String,String> selectorOptions = new HashMap<String,String>();
    private Map<Locale, String> labels = new HashMap<Locale, String>(1);

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


    protected String getResourceBundleId() {
        return ((ExtendedNodeType)getDeclaringNodeType()).getResourceBundleId();
    }

    public String getResourceBundleKey() {
        return (getDeclaringNodeType().getName() + "." + getName()).replace(':', '_');
    }

    public String getLabel(Locale locale) {
        String label = labels.get(locale);
        if (label == null) {
            JahiaSite site = Jahia.getThreadParamBean().getSite();
            final String packageName = site != null ? site.getTemplatePackageName() : null;
            label = new JahiaResourceBundle(getResourceBundleId(), locale, packageName, JahiaTemplatesRBLoader
                    .getInstance(Thread.currentThread().getContextClassLoader(), packageName)).getString(
                    getResourceBundleKey(), getName().replace(':', '_'));
            labels.put(locale, label);
        }
        return label;
    }

    public boolean isSystemItem() {
        return !isMetadataItem() && !isLayoutItem() && !name.toString().equals("jcr:title") && !name.toString().equals("jcr:language") &&
                (declaringNodeType.getName().startsWith("mix:") ||
                        declaringNodeType.getName().startsWith("nt:") ||
                        declaringNodeType.getName().equals("jmix:i18n"));
    }

    public boolean isMetadataItem() {
        return !name.toString().equals("jcr:title")  &&
                (declaringNodeType.isMixin() && declaringNodeType.isNodeType("jmix:metadata") ||
                        declaringNodeType.getName().equals("jmix:hierarchyNode") ||
                        declaringNodeType.getName().equals("mix:created") ||
                        declaringNodeType.getName().equals("mix:lastModified") ||
                        declaringNodeType.getName().equals("mix:title"));
    }

    public boolean isLayoutItem() {
        return declaringNodeType.isMixin() && declaringNodeType.isNodeType("jmix:layout");
    }

    public boolean isOptionItem() {
        return declaringNodeType.isMixin() && declaringNodeType.isNodeType("jmix:option");
    }

    public boolean isContentItem() {
        return declaringNodeType.isNodeType("jmix:content") || declaringNodeType.isNodeType("jnt:container")
                 || declaringNodeType.isNodeType("jnt:content")|| name.toString().equals("jcr:title") || name.toString().equals("jcr:language") || name.toString().equals("jcr:statement");
    }

    public boolean isPublicationItem() {
        return declaringNodeType.isMixin() && declaringNodeType.isNodeType("jmix:publication");
    }

    public boolean isCacheItem() {
        return declaringNodeType.isMixin() && declaringNodeType.isNodeType("jmix:cache");
    }

    public boolean isJahiaContentItem() {
        return !isSystemItem() && !isMetadataItem();
    }


}
