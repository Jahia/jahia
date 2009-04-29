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
