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
package org.jahia.ajax.gwt.client.data.toolbar;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

/**
 * User: jahia
 * Date: 4 mars 2008
 * Time: 15:38:45
 */
public class GWTJahiaToolbarSet implements Serializable {
    private List<GWTJahiaToolbar> toolbarList;

    public List<GWTJahiaToolbar> getToolbarList() {
        return toolbarList;
    }

    public void setToolbarList(List<GWTJahiaToolbar> toolbarList) {
        this.toolbarList = toolbarList;
    }

    public void addGWTToolbar(GWTJahiaToolbar gwtToolbar) {
        if (toolbarList == null) {
            toolbarList = new ArrayList<GWTJahiaToolbar>();
        }
        toolbarList.add(gwtToolbar);
    }
}
