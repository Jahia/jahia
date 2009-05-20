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
package org.jahia.ajax.gwt.client.widget.menu;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 27 ao�t 2008
 * Time: 11:45:54
 * To change this template use File | Settings | File Templates.
 */
public abstract class VersionMenuItem extends Item {

    protected Component component;

    public VersionMenuItem(Component component) {
        this.component = component;
        component.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                parentMenu.fireEvent(Events.Select, ce);
                parentMenu.hide();
            }
        });
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        component.render(target, index);
        setElement(component.getElement());
    }
    
    @Override
    protected void handleClick(ComponentEvent be) {
        component.onComponentEvent(be);
    }

    public Component getComponent() {
        return component;
    }

}
