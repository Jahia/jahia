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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;


/**
 * User: ktlili
 * Date: 17 nov. 2008
 * Time: 22:13:13
 */
public class JahiaPortletPickerDialog extends Dialog {
    private JahiaPortletPicker  portletPicker = new JahiaPortletPicker();

    public JahiaPortletPickerDialog() {
        init();
    }

    private void init() {
        setHeading("Contents picker");
        setLayout(new FitLayout());
        setSize(500, 600);
        setModal(false);
        //portletPicker.loadContent();
        add(portletPicker);
        setButtons(Dialog.OKCANCEL);
        setHideOnButtonClick(true);
        addListener(Events.Hide, new Listener<WindowEvent>() {
            public void handleEvent(WindowEvent be) {
               // List<GWTJahiaNode> gwtJahiaNodes = portletPicker.getSelections();
                if (be.buttonClicked == getButtonById("ok")) {
                  /*  for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
                        GWTJahiaLayoutItem gwtJahiaDraggableWidget = new GWTJahiaLayoutItem();
                        gwtJahiaDraggableWidget.setColumn(0);
                        gwtJahiaDraggableWidget.setRow(0);
                        gwtJahiaDraggableWidget.setStatus(JahiaPropertyHelper.getStatusNormaleValue());
                        gwtJahiaDraggableWidget.setGwtJahiaNode(gwtJahiaNode);
                        JahiaPortalManager.getInstance().addJahiaPortlet(gwtJahiaDraggableWidget);
                    } */
                }
            }
        });
    }

    @Override
    public void show() {
       // portletPicker.loadContent();
        super.show();
    }


}
