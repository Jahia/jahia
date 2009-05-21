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
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.GWTJahiaField;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 9 juil. 2007
 * Time: 14:37:04
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaContainer implements Serializable {
    private int containerId;
    private int column = 0;
    private int row = 0;
    private Map<String, GWTJahiaField> fields = new HashMap<String, GWTJahiaField>();
    private Map<String, GWTJahiaContainerList> containerLists = new HashMap<String, GWTJahiaContainerList>();
    private String editContainerLauncher;
    private String deleteContainerLauncher;

    public GWTJahiaContainer() {
    }

    public GWTJahiaContainer(int containerId) {
        super();
        this.containerId = containerId;
    }

    public int getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }


    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }


    public Map<String, GWTJahiaField> getFields() {
        return fields;
    }

    public void setFields(Map<String, GWTJahiaField> fields) {
        this.fields = fields;
    }

    public void addField(String name, String value) {
        GWTJahiaField field = new GWTJahiaField();
        field.setName(name);
        field.setValue(value);
        getFields().put(name, field);
    }

    public void addContainerList(String name, GWTJahiaContainerList containerList) {
        containerLists.put(name, containerList);
    }

    public GWTJahiaContainerList getContainerList(String name) {
        return containerLists.get(name);
    }


    public Map<String, GWTJahiaContainerList> getContainerLists() {
        return containerLists;
    }

    public void setContainerLists(Map<String, GWTJahiaContainerList> containerLists) {
        this.containerLists = containerLists;
    }


    public String getEditContainerLauncher() {
        return editContainerLauncher;
    }

    public void setEditContainerLauncher(String editContainerLauncher) {
        this.editContainerLauncher = editContainerLauncher;
    }

    public String getDeleteContainerLauncher() {
        return deleteContainerLauncher;
    }

    public void setDeleteContainerLauncher(String deleteContainerLauncher) {
        this.deleteContainerLauncher = deleteContainerLauncher;
    }
}
