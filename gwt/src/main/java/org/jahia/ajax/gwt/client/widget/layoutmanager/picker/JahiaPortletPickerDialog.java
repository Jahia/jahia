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
