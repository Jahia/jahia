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
        IndexedToggleButton button = null;
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
