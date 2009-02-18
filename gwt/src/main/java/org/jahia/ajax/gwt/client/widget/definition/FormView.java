/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.util.definition.FormFieldCreator;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaPropertyDefinition;

/**
 * 
 *
 * User: rfelden
 * Date: 16 sept. 2008 - 09:46:42
 */
public class FormView extends TopRightComponent {

    private ContentPanel m_component ;

    private FormPanel theForm;


    public FormView() {
        Log.debug("form view<init>");
        m_component = new ContentPanel(new FitLayout()) ;
        m_component.setHeaderVisible(false);
        m_component.setBorders(true);
    }


    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
    }

    public void initContextMenu() {
    }

    public void setContent(Object root) {
        Log.debug("setContent : "+root);
        m_component.removeAll();

        theForm = new FormPanel();

        theForm.setFieldWidth(300);
        theForm.setLabelWidth(200);
        theForm.setScrollMode(Style.Scroll.AUTO);
        theForm.setBorders(false);
        theForm.setBodyBorder(false);
        theForm.setHeaderVisible(false);
        theForm.setFrame(false);
        theForm.setButtonAlign(Style.HorizontalAlignment.CENTER);

        GWTJahiaNodeType nodeType = (GWTJahiaNodeType) root;
        Log.debug("adding...");
        for (GWTJahiaItemDefinition definition : nodeType.getItems()) {
            if (!definition.isNode()) {
                Log.debug("prop..."+definition);
                final Field field = FormFieldCreator.createField((GWTJahiaPropertyDefinition) definition, null) ;
                if (field != null) {
                    theForm.add(field) ;
                }
            } else {
                Log.debug("node..."+definition);
            }
        }
        m_component.add(theForm);
        m_component.layout() ;        
    }

    public void setProcessedContent(Object content) {
    }

    public void clearTable() {
    }

    public Object getSelection() {
        return null ;
    }

    public void refresh() {
    }

    public Component getComponent() {
        return m_component ;
    }
}
