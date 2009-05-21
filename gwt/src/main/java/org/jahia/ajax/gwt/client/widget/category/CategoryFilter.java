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
