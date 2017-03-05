/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.data.definition;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

import java.io.Serializable;
import java.util.*;

/**
 * 
 * User: toto
 * Date: Aug 26, 2008
 * Time: 7:36:01 PM
 * 
 */
public class GWTJahiaNodeType extends BaseTreeModel implements Serializable {

    private List<GWTJahiaItemDefinition> inheritedItems;
    private List<GWTJahiaItemDefinition> items;
    private List<String> superTypes;

    public GWTJahiaNodeType() {
        super();
    }

    public GWTJahiaNodeType(String name) {
        super();
        setName(name);
    }

    public GWTJahiaNodeType(String name, String label) {
        this();
        setName(name);
        setLabel(label);
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

    public boolean isAbstract() {
        return (Boolean)get("isAbstract");
    }

    public void setAbstract(boolean isAbstract) {
        set("isAbstract",new Boolean(isAbstract));
    }

    public String getIcon() {
        return get("icon");
    }

    public void setIcon(String ext) {
        set("icon", ext);
    }

    public List<String> getSuperTypes() {
        return superTypes;
    }

    public void setSuperTypes(List<String> superTypes) {
        this.superTypes = superTypes;
    }

    public String getDescription() {
        return get("description");
    }

    public void setDescription(String desc) {
        set("description", desc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaNodeType that = (GWTJahiaNodeType) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getLabel() + "(" + getName() + ")";
    }
}
