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
package org.jahia.ajax.gwt.client.data.definition;

import com.extjs.gxt.ui.client.data.BaseModelData;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 26, 2008
 * Time: 7:36:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaNodeType extends BaseModelData implements Serializable {

    private List<GWTJahiaItemDefinition> inheritedItems;
    private List<GWTJahiaItemDefinition> items;

    public GWTJahiaNodeType() {
    }


    public List<GWTJahiaItemDefinition> getInheritedItems() {
        return inheritedItems;
    }

    public void setInheritedItems(List<GWTJahiaItemDefinition> inheritedItems) {
        this.inheritedItems = inheritedItems;
    }

    public List<GWTJahiaItemDefinition> getItems() {
        return items;
    }

    public void setItems(List<GWTJahiaItemDefinition> items) {
        this.items = items;
    }

    public String getName() {
        return get("name");
    }

    public void setName(String name) {
        set("name", name);
    }

    public String getLabel() {
        return get("label");
    }

    public void setLabel(String label) {
        set("label", label);
    }

    public boolean isMixin() {
        return (Boolean)get("isMixin");
    }

    public void setMixin(boolean mixin) {
        set("isMixin",new Boolean(mixin));
    }

    public String getPrimaryItemName() {
        return get("primaryItemName");
    }

    public void setPrimaryItemName(String primaryItemName) {
        set("primaryItemName",primaryItemName);
    }

    public String getValidator() {
        return get("validator");
    }

    public void setValidator(String validator) {
        set("validator",validator);
    }
}
