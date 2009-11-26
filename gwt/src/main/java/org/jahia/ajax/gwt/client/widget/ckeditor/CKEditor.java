package org.jahia.ajax.gwt.client.widget.ckeditor;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.core.client.JavaScriptObject;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
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
 **/

/**
 * User: ktlili
 * Date: Nov 25, 2009
 * Time: 2:11:47 PM
 */
public class CKEditor extends Component {
    private String instanceId;
    protected JavaScriptObject editorInstance;
    private CKEditorConfig config;
    private boolean isLoaded;

    public CKEditor(CKEditorConfig config) {
        if(config == null){
             config = new CKEditorConfig();
        }
        this.config = config;
    }

    @Override
    protected void onRender(Element target, int index) {
        Element ele = DOM.createDiv();
        DOM.setElementAttribute(ele, "width", config.getWidth());
        DOM.setElementAttribute(ele, "height", config.getHeight());
        setElement(ele, target, index);
        DeferredCommand.addCommand(new Command() {
            public void execute() {
                instanceId = getElement().getId();
                editorInstance = initEditor();
            }
        });
      /*  addLoadListener(new CKEditorLoadListener() {
            public void onLoad() {
                isLoaded = true;
            }
        });   */
        super.onRender(target, index);
    }

    /**
     * Set html content
     *
     * @param html
     */
    public void setData(final String html) {
        if (!isLoaded) {
            addLoadListener(new CKEditorLoadListener() {
                public void onLoad() {
                    _setCKData(html);
                }
            });
        } else {
            _setCKData(html);
        }
    }

    /**
     * Get html content
     * @return
     */
    public String getData() {
        if (!isLoaded) {
            return getCKData();
        }
        return null;
    }


    /**
     * Nadive method that set the html of the CKEditor
     *
     * @param html
     */
    private native void _setCKData(String html)/*-{
        var oEditor = this.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor::editorInstance ;
        oEditor.setData(html);
      }-*/;

    /**
     * Native methode to get html of the CKEditor
     * @return
     */
    public native String getCKData()/*-{
        var oEditor = this.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor::editorInstance ;
        return oEditor.getData();
      }-*/;


    /**
     * init editior
     * @return
     */
    private native JavaScriptObject initEditor()/*-{
        var oCKeditor = new $wnd.CKEDITOR.replace(this.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor::instanceId);
        var config = this.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditor::config;

        oCKeditor.Width = config.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig::getWidth()();
        oCKeditor.Height = config.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorConfig::getHeight()();

        return oCKeditor;
      }-*/;

    /**
     * Add a load listener
     * @param listener
     */
    public native void addLoadListener(CKEditorLoadListener listener)/*-{
        $wnd.__FCK_addLoadListener(function() {
          listener.@org.jahia.ajax.gwt.client.widget.ckeditor.CKEditorLoadListener::onLoad()();
        });
      }-*/;

}
