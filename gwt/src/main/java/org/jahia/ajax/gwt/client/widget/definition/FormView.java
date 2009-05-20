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
