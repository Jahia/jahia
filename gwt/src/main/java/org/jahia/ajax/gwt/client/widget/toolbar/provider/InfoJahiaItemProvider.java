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
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Map;

/**
 * User: jahia
 * Date: 18 avr. 2008
 * Time: 10:52:59
 */
public class InfoJahiaItemProvider extends AbstractJahiaToolItemProvider {
    public <T extends ComponentEvent> SelectionListener<T> getSelectListener(GWTJahiaToolbarItem gwtToolbarItem) {
        return null;
    }

    public Component createNewToolItem(GWTJahiaToolbarItem gwtToolbarItem) {
        // TO DO: find a better implementation of this item. The current one is ....
        JahiaButton item = new JahiaButton();
        item.setIconStyle(gwtToolbarItem.getMinIconStyle());
        Map preferences = gwtToolbarItem.getProperties();
        if (preferences != null) {
            final GWTJahiaProperty info = (GWTJahiaProperty) preferences.get(ToolbarConstants.INFO);
            if (info != null) {
                item.setText(info.getValue());
            }
        }
        return item;
    }
    

    private class JahiaButton extends Button {
        JahiaTextButton button;

        private JahiaButton() {
            button = new JahiaTextButton();
        }

        @Override
        public void removeStyleName(String s) {
            button.removeStyleName(s);
        }

        @Override
        public void removeStyleDependentName(String s) {
            button.removeStyleDependentName(s);
        }

        @Override
        public void setEnabled(boolean b) {
            button.setEnabled(b);
        }

        public void setText(String text) {
            button.setText(text);
        }

        public void setIconStyle(String iconStyle) {
            button.setIconStyle(iconStyle);
        }

        @Override
        protected void onRender(Element target, int index) {
            button.render(target, index);
            setElement(button.getElement());
        }

        @Override
        protected void doAttachChildren() {
            super.doAttachChildren();
            ComponentHelper.doAttach(button);
        }

        @Override
        protected void doDetachChildren() {
            super.doDetachChildren();
            ComponentHelper.doDetach(button);
        }
        
    }

    private class JahiaTextButton extends Button {

        @Override
        protected void onClick(ComponentEvent componentEvent) {
//            super.onClick(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onFocus(ComponentEvent componentEvent) {
//            super.onFocus(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseDown(ComponentEvent componentEvent) {
//            super.onMouseDown(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMenuHide(ComponentEvent componentEvent) {
//            super.onMenuHide(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMenuShow(ComponentEvent componentEvent) {
//            super.onMenuShow(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }


        @Override
        protected void onMouseOut(ComponentEvent componentEvent) {
//            super.onMouseOut(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseOver(ComponentEvent componentEvent) {
//            super.onMouseOver(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        protected void onMouseUp(ComponentEvent componentEvent) {
//            super.onMouseUp(componentEvent);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
