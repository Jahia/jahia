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
package org.jahia.ajax.gwt.client.widget.layoutmanager.listener;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortlet;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;

/**
 * User: ktlili
 * Date: 17 nov. 2008
 * Time: 21:35:05
 */
public class OnPortletRemoved extends SelectionListener<ComponentEvent> {
    private JahiaPortlet jahiaPortlet;

    public OnPortletRemoved(JahiaPortlet jahiaPortlet) {
        this.jahiaPortlet = jahiaPortlet;
    }

    public void componentSelected(ComponentEvent event) {
        final MessageBox box = new MessageBox();
        final Listener confirmBoxListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Dialog dialog = (Dialog) ce.component;
                Button btn = dialog.getButtonPressed();
                Log.debug("Clicked button: "+btn.getText());
                if (btn.getText().equalsIgnoreCase(MessageBox.OK)) {
                    close();
                }
            }
        };


        box.setButtons(MessageBox.OKCANCEL);
        box.setIcon(MessageBox.QUESTION);
        box.setTitle(jahiaPortlet.getHeading());
        box.addCallback(confirmBoxListener);
        box.setMessage("Do you really want to remove: <br/> " + jahiaPortlet.getHeading() + "?");
        box.show();

    }

    public void close() {
        // make a call ajax
        LayoutmanagerService.App.getInstance().removeLayoutItem(JahiaPageEntryPoint.getJahiaGWTPage(), jahiaPortlet.getPorletConfig(), new AsyncCallback() {
            public void onSuccess(Object o) {
                Log.debug("Portlet removed: " + jahiaPortlet.getPorletConfig().getPortlet());
                JahiaPortalManager.getInstance().removeJahiaPortlet(jahiaPortlet);
            }

            public void onFailure(Throwable t) {
                // handle failure
                Window.alert(t.getLocalizedMessage());
            }
        });
    }

}
