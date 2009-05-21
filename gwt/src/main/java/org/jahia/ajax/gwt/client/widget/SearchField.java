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

import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 *
 *
 * User: rfelden
 * Date: 22 sept. 2008 - 16:10:56
 */
public abstract class SearchField extends ToolBar {

    private TextField field ;

    public SearchField(String name, boolean saveSearchbutton) {
        super() ;

        LabelToolItem label = new LabelToolItem(name) ;

        field = new TextField() ;
        field.setWidth(500) ;
        field.setFieldLabel(name) ;
        field.addKeyListener(new KeyListener() {
            public void componentKeyPress(ComponentEvent event) {
                if (event.getKeyCode() == 13) { // this is the 'enter' code
                    onFieldValidation(field.getRawValue());
                }
            }
        });

        add(label) ;
        add(new AdapterToolItem(field)) ;
        TextToolItem ok = new TextToolItem() ;
        ok.setIconStyle("fm-savedSearch");
        ok.addSelectionListener(new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent e) {
                onFieldValidation(field.getRawValue());
            }
        });
        add(ok) ;
        add(new FillToolItem()) ;
        if (saveSearchbutton) {
            TextToolItem save = new TextToolItem(Messages.getResource("fm_saveSearch")) ;
            save.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent event) {
                    onSaveButtonClicked(field.getRawValue());
                }
            });
            add(save) ;
        }
    }

    public String getText() {
        return field.getRawValue() ;
    }

    public void clear() {
        field.setRawValue("");
    }

    public void setWidth(int width) {
        field.setWidth(width);
    }

    public abstract void onFieldValidation(String value) ;

    public abstract void onSaveButtonClicked(String value) ;

}
