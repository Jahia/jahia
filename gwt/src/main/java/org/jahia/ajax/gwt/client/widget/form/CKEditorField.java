package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.menu.ColorMenu;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.util.Size;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.core.El;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.impl.RichTextAreaImpl;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.core.client.GWT;

import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig; /**
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
 **/

/**
 * User: ktlili
 * Date: Nov 25, 2009
 * Time: 12:51:25 PM
 *
 * Code inspired from AdapterField. Update this class if GXT version is updated
 *
 */
public class CKEditorField extends Field<String> {

    /**
     * The wrapped widget.
     */
    protected CKEditor ckeditor;

    private boolean resizeWidget;

    /**
     * Creates a new adapter field.
     */
    public CKEditorField() {
        ckeditor = new CKEditor(null);
    }

    public CKEditorField(CKEditorConfig config) {
        ckeditor = new CKEditor(config);
    }

    @Override
    public Element getElement() {
        // we need this because of lazy rendering
        return ckeditor.getElement();
    }

    /**
     * Returns the wrapped widget.
     *
     * @return the widget
     */
    public CKEditor getCKEditor() {
        return ckeditor;
    }

    @Override
    public boolean isAttached() {
        if (ckeditor != null) {
            return ckeditor.isAttached();
        }
        return false;
    }

    /**
     * Returns true if the wrapped widget is being resized.
     *
     * @return true is resizing is enabled
     */
    public boolean isResizeWidget() {
        return resizeWidget;
    }

    @Override
    public boolean isValid(boolean silent) {
        return true;
    }

    @Override
    public void onBrowserEvent(Event event) {
        // Fire any handler added to the CKEditorField itself.
        super.onBrowserEvent(event);

        // Delegate events to the widget.
        ckeditor.onBrowserEvent(event);
    }

    /**
     * True to resize the wrapped widget when the field is resized (defaults to
     * false).
     *
     * @param resizeWidget true to resize the wrapped widget
     */
    public void setResizeWidget(boolean resizeWidget) {
        this.resizeWidget = resizeWidget;
    }

    @Override
    public boolean validate(boolean preventMark) {
        return true;
    }

    @Override
    protected void onAttach() {
        ComponentHelper.doAttach(ckeditor);
        DOM.setEventListener(getElement(), this);
        onLoad();
    }

    @Override
    protected void onBlur(ComponentEvent ce) {

    }

    @Override
    protected void onDetach() {
        try {
            onUnload();
        } finally {
            ComponentHelper.doDetach(ckeditor);
        }
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        ckeditor.disable();
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        ckeditor.enable();
    }

    @Override
    protected void onFocus(ComponentEvent ce) {

    }

    @Override
    protected void onRender(Element target, int index) {
        if (!ckeditor.isRendered()) {
            ckeditor.render(target, index);
        }
        setElement(ckeditor.getElement(), target, index);    
    }


    @Override
    protected boolean validateValue(String value) {
        return true;
    }

    @Override
    public void clear() {
        ckeditor.clear();
        super.clear();
    }

    @Override
    public boolean isDirty() {
        return ckeditor.isDirty() && super.isDirty();
    }

    @Override
    public String getValue() {
        return ckeditor.getData();
    }

    @Override
    public void setValue(String html) {
        ckeditor.setData(html);
        super.setValue(html);
    }
}

