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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jahia.
 * User: ktlili
 * Date: 13 juil. 2007
 * Time: 15:13:53
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaContainerList implements Serializable {
    private int containerListId;
    private List<GWTJahiaContainer> containers = new ArrayList<GWTJahiaContainer>();
    private String addContainerLauncher;

    public GWTJahiaContainerList() {
    }

    public GWTJahiaContainerList(int id) {
        super();
        this.containerListId = id;
    }

    public int getContainerListId() {
        return containerListId;
    }

    public void setContainerListId(int containerListId) {
        this.containerListId = containerListId;
    }


    public List<GWTJahiaContainer> getContainers() {
        return containers;
    }


    public void setContainers(List<GWTJahiaContainer> containers) {
        this.containers = containers;
    }

    public void addContainer(GWTJahiaContainer container) {
        containers.add(container);
    }


    public String getAddContainerLauncher() {
        return addContainerLauncher;
    }

    public void setAddContainerLauncher(String addContainerLauncher) {
        this.addContainerLauncher = addContainerLauncher;
    }
}
