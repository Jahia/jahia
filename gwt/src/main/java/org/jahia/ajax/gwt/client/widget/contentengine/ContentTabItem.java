/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.contentengine;


import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

import java.util.Arrays;

/**
 * User: toto
 * Date: Jan 6, 2010
 * Time: 8:10:21 PM
 */
public class ContentTabItem extends PropertiesTabItem {
    private transient boolean isNodeNameFieldDisplayed = false;
    private transient TextField<String> name;

    public TextField<String> getName() {
        return name;
    }

    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        setMultiLang(true);
        if (dataType == null) {
        dataType = Arrays.asList(GWTJahiaItemDefinition.CONTENT);
        }
        return super.create(engineTab, engine);
    }

    @Override
    public void attachPropertiesEditor(NodeHolder engine, AsyncTabItem tab) {
        // handle jcr:title property
        if (!propertiesEditor.getFieldsMap().containsKey("jcr:title") && !engine.isMultipleSelection()) {
                tab.setLayout(new RowLayout());
            final FormLayout fl = new FormLayout();
            fl.setLabelWidth(0);
            FieldSet fSet = new FieldSet();
            createNamePanel(engine, tab);
            FormData fd = new FormData("98%");
            fd.setMargins(new Margins(0));
            fSet.setHeading(name.getName());
            fSet.setLayout(fl);
            fSet.add(name,fd);
            isNodeNameFieldDisplayed = true;
            propertiesEditor.insert(fSet,0);
        } else {
        	isNodeNameFieldDisplayed = false;
        }

        // attach properties node
            super.attachPropertiesEditor(engine, tab);
        }


    /**
     * Get Form panel that contains the name of the nodes
     *
     * @return  @param engine
     * @param tab
     */
    private void createNamePanel(NodeHolder engine, AsyncTabItem tab) {

        name = new TextField<String>();
        name.setWidth("98%");
        name.setStyleAttribute("padding-left", "0");
        name.setFieldLabel("Name");
        name.setName(Messages.get("label.systemName","System name"));
        if (engine.isExistingNode()) {
            name.setValue(engine.getNode().getName());
            tab.setData("NodeName", engine.getNode().getName());
            name.setReadOnly(true);
        } else {
            name.setValue(Messages.get("label.nodeAutoName", "Automatically Created (you can type your name here if you want)"));
        }
    }

    /**
     * Return true if nodeNameField is displayed
     *
     * @return
     */
    public boolean isNodeNameFieldDisplayed() {
        return isNodeNameFieldDisplayed;
    }

}
