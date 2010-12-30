/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.data;


import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;

import java.util.LinkedList;
import java.util.List;

/**
 * GWT bean that represents single permission.
 * User: ktlili
 * Date: Feb 2, 2010
 * Time: 11:43:21 AM
 */
public class GWTJahiaPermission extends GWTJahiaRolePermissionBase implements TreeModel {

    private List<GWTJahiaPermission> childs;
    private List<GWTJahiaPermission> dependencies;
    private GWTJahiaPermission parent;

    public GWTJahiaPermission() {
        super();
        childs = new LinkedList<GWTJahiaPermission>();
    }

    public GWTJahiaPermission(String name) {
        super(name);
        childs = new LinkedList<GWTJahiaPermission>();
        dependencies = new LinkedList<GWTJahiaPermission>();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GWTJahiaPermission that = (GWTJahiaPermission) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    public List<GWTJahiaPermission> getChilds() {
        return childs;
    }

    public void addChild(GWTJahiaPermission permission) {
        childs.add(permission);
    }

    /**
     * Adds a child to the model.
     *
     * @param child the model to add
     */
    public void add(ModelData child) {
        addChild((GWTJahiaPermission) child);
    }

    /**
     * Returns the child at the given index.
     *
     * @param index the index
     * @return the child
     */
    public ModelData getChild(int index) {
        return childs.get(index);
    }

    /**
     * Returns the child count.
     *
     * @return the child count
     */
    public int getChildCount() {
        return childs.size();
    }

    /**
     * Returns the model's children.
     *
     * @return the children
     */
    public List<ModelData> getChildren() {
        return new LinkedList<ModelData>(childs);
    }

    /**
     * Returns the model's parent.
     *
     * @return the parent
     */
    public TreeModel getParent() {
        return parent;
    }

    /**
     * Returns the index of the child.
     *
     * @param child the child
     * @return the index
     */
    public int indexOf(ModelData child) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Inserts a child.
     *
     * @param child the child to add
     * @param index the insert location
     */
    public void insert(ModelData child, int index) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns true if the model is a leaf and has children. The method provides
     * the ability to mark a model as having children before the children have
     * been added.
     *
     * @return true for leaf
     */
    public boolean isLeaf() {
        return childs.size()==0;
    }

    /**
     * Removes a child.
     *
     * @param child the child to remove
     */
    public void remove(ModelData child) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Removes all the children.
     */
    public void removeAll() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Sets the model's parent.
     *
     * @param parent the new parent
     */
    public void setParent(TreeModel parent) {
        this.parent = (GWTJahiaPermission) parent;
    }

    public void addDependency(GWTJahiaPermission permission) {
        dependencies.add(permission);
    }

    public List<GWTJahiaPermission> getDependencies() {
        return dependencies;
    }
}
