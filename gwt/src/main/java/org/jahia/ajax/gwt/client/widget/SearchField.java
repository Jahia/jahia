/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget;

import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
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
