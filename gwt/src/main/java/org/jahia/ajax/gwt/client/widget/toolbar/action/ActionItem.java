/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.Linker;


import java.io.Serializable;

/**
 * Defines an action item that used in edit mode and content manager UIs.
 *
 * @author rfelden
 */
public interface ActionItem extends Serializable {

    void setEnabled(boolean enabled) ;

    void setVisible(boolean visible) ;

    /**
     * If implemented and doesn't return null, the actionItem component will be always the one returned by this method
     * @return
     */
    Component getCustomItem() ;

    Component getTextToolItem() ;

    Item getMenuItem() ;

    Item getContextMenuItem() ;

    void setTextToolitem(Component textToolitem);

    void setMenuItem(MenuItem menuItem);

    void setContextMenuItem(MenuItem contextMenuItem);

    GWTJahiaToolbarItem getGwtToolbarItem();

    void onComponentSelection();

    void handleNewLinkerSelection();

    void handleNewMainNodeLoaded(GWTJahiaNode node);

    void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker);


}
