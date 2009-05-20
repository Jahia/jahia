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
package org.jahia.ajax.gwt.client.util.tree;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.DataProxy;

import java.util.List;

/**
 * User: rfelden
 * Date: 19 nov. 2008 - 14:44:06
 */
public abstract class CustomTreeLoader<M extends BaseTreeModel> extends BaseTreeLoader<M> {

    private boolean openPreviousPaths = true ;

    /**
     * Constructor with automatic restore of previous state
     *
     * @param proxy the data proxy
     */
    public CustomTreeLoader(DataProxy<M, List<M>> proxy) {
        super(proxy) ;
    }

    /**
     * Alt constructor
     *
     * @param proxy the data proxy
     * @param openPreviousPaths false to deactivate last state restore
     */
    public CustomTreeLoader(DataProxy<M, List<M>> proxy, boolean openPreviousPaths) {
        this(proxy) ;
        this.openPreviousPaths = openPreviousPaths ;
    }

    @Override
    protected void onLoadSuccess(M parent, List<M> children) {
        super.onLoadSuccess(parent, children);
        if (openPreviousPaths) {
            openPreviousPaths = false ;
            expandPreviousPaths();
        }
    }

    protected abstract void expandPreviousPaths() ;

}
