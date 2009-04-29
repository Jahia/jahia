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
package org.jahia.ajax.gwt.client.data.layoutmanager;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 21 ao�t 2008
 * Time: 11:09:43
 */
public class GWTJahiaLayoutManagerConfig implements Serializable {
    private String id;
    private int nbColumns;
    private boolean liveDraggable;
    private boolean liveQuickbarVisible;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNbColumns() {
        return nbColumns;
    }

    public void setNbColumns(int nbColumns) {
        this.nbColumns = nbColumns;
    }

    public boolean isLiveDraggable() {
        return liveDraggable;
    }

    public void setLiveDraggable(boolean liveDraggable) {
        this.liveDraggable = liveDraggable;
    }

    public boolean isLiveQuickbarVisible() {
        return liveQuickbarVisible;
    }

    public void setLiveQuickbarVisible(boolean liveQuickbarVisible) {
        this.liveQuickbarVisible = liveQuickbarVisible;
    }
}
