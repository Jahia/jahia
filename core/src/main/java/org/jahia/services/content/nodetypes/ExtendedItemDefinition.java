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

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.jcr.nodetype.ItemDefinition;
import javax.jcr.version.OnParentVersionAction;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.i18n.ResourceBundleMarker;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 15 janv. 2008
 * Time: 17:43:58
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedItemDefinition implements ItemDefinition {
    
    private static final transient Logger logger = Logger
            .getLogger(ExtendedItemDefinition.class);
    
    private ExtendedNodeType declaringNodeType;
    private Name name;
    private boolean isProtected = false;
    private boolean autoCreated = false;
    private boolean mandatory = false;
    private boolean hidden;
    private int onParentVersion = OnParentVersionAction.COPY;
    protected int selector = 0;
    private Map<String,String> selectorOptions = new HashMap<String,String>();

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


    public String getResourceBundleId() {
        return ((ExtendedNodeType)getDeclaringNodeType()).getResourceBundleId();
    }

    public String getResourceBundleMarker() {
        return ResourceBundleMarker.drawMarker(getResourceBundleId(), getResourceBundleKey(), getName().replace(':','_'));
    }

    public String getResourceBundleKey() {
        return (getDeclaringNodeType().getName() + "." + getName()).replace(':', '_');
    }

    public String getLabel(Locale locale) {
        try {
            return ResourceBundleMarker.parseMarkerValue(getResourceBundleMarker()).getValue(locale);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return getName().replace(':','_');
    }

}
