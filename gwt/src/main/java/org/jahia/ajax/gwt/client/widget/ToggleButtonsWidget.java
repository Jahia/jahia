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
package org.jahia.ajax.gwt.client.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is a multiple ToggleButtons widget but with only one toggled button at a time.
 * The others are automatically toggled off.
 *
 * User: Administrator
 * Date: 14 juil. 2008
 * Time: 21:52:19
 * To change this template use File | Settings | File Templates.
 */
public class ToggleButtonsWidget extends Composite implements ClickListener {

    private HorizontalPanel buttonsPanel;
    private List<IndexedToggleButton> buttons;

    public ToggleButtonsWidget(){
        buttons = new ArrayList<IndexedToggleButton>();
        buttonsPanel = new HorizontalPanel();
        buttonsPanel.setStyleName("jahia-ToggleButtonsWidget");
        initWidget(buttonsPanel);
    }

    public IndexedToggleButton addButton(String text){
        IndexedToggleButton button = new IndexedToggleButton(text,buttons.size());
        button.addClickListener(this);
        buttons.add(button);
        buttonsPanel.add(button);
        return button;
    }

    public void onClick(Widget sender){
        Iterator<IndexedToggleButton> iterator = buttons.iterator();
        IndexedToggleButton button;
        while(iterator.hasNext()){
            button = iterator.next();
            if (button != sender && button.isDown()){
                button.setDown(false);
            }
        }
        IndexedToggleButton senderButton = (IndexedToggleButton)sender;
        senderButton.setDown(true);
    }

    public void addClickListener(ClickListener listener){
        Iterator<IndexedToggleButton> iterator = buttons.iterator();
        IndexedToggleButton button = null;
        while(iterator.hasNext()){
            button = iterator.next();
            button.addClickListener(listener);
        }
    }
}
