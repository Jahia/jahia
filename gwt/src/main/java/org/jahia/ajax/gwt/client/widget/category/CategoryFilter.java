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
package org.jahia.ajax.gwt.client.widget.category;

import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.StoreFilterField;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.data.ModelData;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 6, 2009
 * Time: 6:56:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class CategoryFilter extends TopBar {
    ToolBar m_component;
    private StoreFilterField filter;

    public CategoryFilter() {
        m_component = new ToolBar() ;
        m_component.add(new LabelToolItem("Filter")) ;

        filter = new StoreFilterField() {
            @Override
            protected boolean doSelect (Store store, ModelData parent, ModelData record, String property, String filter) {
                if (record instanceof GWTJahiaCategoryNode) {
                    GWTJahiaCategoryNode node = (GWTJahiaCategoryNode) record;
                    String s = node.getName().toLowerCase();
                    if(s.startsWith("(") && s.endsWith(")")) s = s.substring(1);
                    if(s.startsWith(filter.toLowerCase())) return true;
                }
                return false;
            }
        };

        m_component.add(new AdapterToolItem(filter)) ;
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {

    }

    public Component getComponent() {
        return m_component;
    }

    public StoreFilterField getFilter() {
        return filter;
    }
}
