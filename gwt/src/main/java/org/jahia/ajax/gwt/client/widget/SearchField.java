/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;

/**
 * User: rfelden
 * Date: 22 sept. 2008 - 16:10:56
 */
public abstract class SearchField extends ToolBar {

    private TextField<String> field;

    public SearchField(String name, boolean saveSearchbutton) {
        super();
        addStyleName("search-container");

        LabelToolItem label = new LabelToolItem(name);
        field = new TextField<String>();
        field.setWidth(150);
        field.setFieldLabel(name);
        field.addKeyListener(new KeyListener() {
            public void componentKeyPress(ComponentEvent event) {
                if (event.getKeyCode() == 13) { // this is the 'enter' code
                    onFieldValidation(field.getRawValue());
                }
            }
        });

        add(label);
        add(field);

        Button ok = new Button();
        ok.setIcon(StandardIconsProvider.STANDARD_ICONS.savedSearch());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent e) {
               onFieldValidation(field.getRawValue());
            }
        });
        add(ok);
        add(new FillToolItem());
        if (saveSearchbutton) {
            Button save = new Button(Messages.get("saveSearch.label"));
            save.addStyleName("button-savesearch");
            save.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent event) {
                   onSaveButtonClicked(field.getRawValue());
                }
            });
            add(save);
        }

    }

    public String getText() {
        return field.getRawValue();
    }

    public void clear() {
        field.setRawValue("");
    }

    /*public void setWidth(int width) {
        //field.setWidth(width);
    } */

    public abstract void onFieldValidation(String value);

    public abstract void onSaveButtonClicked(String value);

}
