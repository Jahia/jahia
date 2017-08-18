/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.ajax.gwt.client.widget.content.portlet;

import com.extjs.gxt.ui.client.widget.form.TriggerField;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaPortletDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * 
 * User: toto
 * Date: Dec 11, 2008
 * Time: 5:27:32 PM
 * 
 */
public class PortletDefinitionField extends TriggerField<String> {
    @Override
    protected void onTriggerClick(ComponentEvent componentEvent) {
        super.onTriggerClick(componentEvent);
        if (disabled || isReadOnly()) {
            return;
        }
        final Window w = new Window();
        w.addStyleName("portlet-definition-window");
        w.setLayout(new FitLayout());
        final PortletDefinitionCard card = new PortletDefinitionCard();
        w.setModal(true);
        w.setSize(600, 400);
        ButtonBar bar = new ButtonBar();
        Button ok = new Button(Messages.get("org.jahia.engines.PortletsManager.wizard.ok.label","OK"), new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                GWTJahiaPortletDefinition def = card.getSelectedPortletDefinition();
                setRawValue(def.getDefinitionName());
                w.hide();
            }
        });
        bar.add(ok);
        w.setTopComponent(bar);
        w.add(card);
        w.show();        
    }
}
