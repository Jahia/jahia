/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.filemanagement.client.ui.portlet;

import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.jahia.ajax.gwt.filemanagement.client.model.GWTJahiaPortletDefinition;

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
        Button ok = new Button("OK", new SelectionListener<ComponentEvent>() {
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
