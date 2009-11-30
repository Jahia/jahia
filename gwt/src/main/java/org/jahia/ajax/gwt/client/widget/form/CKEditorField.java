package org.jahia.ajax.gwt.client.widget.form;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.event.*;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;

import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor;
import org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig;
import org.jahia.ajax.gwt.client.util.URL; /**
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
 * <p/>
 * Code inspired from AdapterField. Update this class if GXT version is updated
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
        return ckeditor.isDirty();
    }

    @Override
    public String getRawValue() {
        return rewriteURL(ckeditor.getData());
    }

    @Override
    public void setRawValue(String html) {
        ckeditor.setData(html);
        super.setRawValue(html);
    }

    /**
     * Add place holder for richText
     *
     * @param data
     * @return
     */
    public String rewriteURL(String data) {
        Element ele = DOM.createDiv();
        ele.setInnerHTML(data);
        return processElement(ele);
    }

    /**
     * Add place holder for richText
     *
     * @param ele
     * @return
     */
    public String processElement(Element ele) {
        int nb = DOM.getChildCount(ele);
        for (int i = 0; i < nb; i++) {
            Element eleChild = DOM.getChild(ele, i);
            rewriteTagAttribute(eleChild, "src");
            rewriteTagAttribute(eleChild, "href");
            rewriteTagAttribute(eleChild, "value");

            // process subchildren
            processElement(eleChild);
        }
        return ele.getInnerHTML();
    }

    /**
     * Add place holder for richText
     *
     * @param n
     * @param attribute
     * @return
     */
    public boolean rewriteTagAttribute(Element n, String attribute) {
        String value = DOM.getElementAttribute(n, attribute);
        if (value != null && value.length() > 0) {
            DOM.setElementAttribute(n, attribute, URL.rewrite(value));
            return true;
        }
        return false;
    }
}

