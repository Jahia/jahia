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
package org.jahia.ajax.gwt.client.widget.node.portlet;

import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 11, 2008
 * Time: 5:27:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortletDefinitionField extends TriggerField<String> {
    @Override
    protected void onTriggerClick(ComponentEvent componentEvent) {
        super.onTriggerClick(componentEvent);
        if (disabled || isReadOnly()) {
            return;
        }
        final Window w = new Window();
        w.setLayout(new FitLayout());
        final PortletDefinitionCard card = new PortletDefinitionCard();
        w.setModal(true);
        w.setSize(600, 400);
        ButtonBar bar = new ButtonBar();
        Button ok = new Button(Messages.getNotEmptyResource("mw_ok","OK"), new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                GWTJahiaPortletDefinition def = card.getSelectedPortletDefinition();
                setRawValue(def.getDefinitionName());
                w.hide();
            }
        });
        bar.add(ok);
        w.setButtonBar(bar);
        w.add(card);
        w.show();        
    }
}
