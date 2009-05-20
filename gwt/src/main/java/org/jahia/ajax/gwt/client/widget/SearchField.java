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
