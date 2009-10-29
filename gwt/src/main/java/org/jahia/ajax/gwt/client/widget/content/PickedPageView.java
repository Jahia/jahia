package org.jahia.ajax.gwt.client.widget.content;

import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.messages.Messages;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.data.BaseModelData;

import java.util.List; /**
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
 * Date: Oct 29, 2009
 * Time: 7:11:56 PM
 */
public class PickedPageView extends BottomRightComponent {
    private ContentPanel m_component;
    private RadioGroup pageTypeGroup = new RadioGroup("pageType");
    private TextField<String> link = new TextField<String>();
    private TextField<String> alt = new TextField<String>();
    private TextField<String> url = new TextField<String>();
    private ComboBox<TargetModelData> target = new ComboBox<TargetModelData>();

    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private final JahiaContentDefinitionServiceAsync cDefService = JahiaContentDefinitionService.App.getInstance();


    public PickedPageView(String pickerType, List<GWTJahiaNode> selectedNodes, boolean multiple, final ManagerConfiguration config) {
        m_component = new ContentPanel(new ColumnLayout());
        m_component.setBodyBorder(false);
        m_component.setHeaderVisible(false);
        m_component.setBorders(false);

        // form panel
        FormPanel pageLinkForm = new FormPanel();
        pageLinkForm.setCollapsible(false);
        pageLinkForm.setFrame(false);
        pageLinkForm.setAnimCollapse(false);
        pageLinkForm.setBorders(false);
        pageLinkForm.setBodyBorder(false);
        pageLinkForm.setHeaderVisible(false);
        pageLinkForm.setScrollMode(Style.Scroll.AUTO);
        pageLinkForm.setButtonAlign(Style.HorizontalAlignment.CENTER);


        Radio external = new Radio();
        external.setName("external");
        external.setBoxLabel("external link");
        external.setValue(true);

        Radio internal = new Radio();

        internal.setName("internal");
        internal.setBoxLabel("internal link");

        pageTypeGroup.setFieldLabel("Page type");
        pageTypeGroup.add(external);
        pageTypeGroup.add(internal);


        link = new TextField<String>();
        link.setFieldLabel("Link");
        link.setAllowBlank(false);


        alt = new TextField<String>();
        alt.setFieldLabel("Alt");

        url = new TextField<String>();
        url.setFieldLabel("Url");
        Button pickContentButton = new Button(Messages.getResource("fm_remove"));
        pickContentButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent buttonEvent) {
                getLinker().select(null);
                fillData(null);
            }
        });
        pickContentButton.setIcon(ContentModelIconProvider.getInstance().getMinusRound());

        ListStore<TargetModelData> store = new ListStore<TargetModelData>();
        TargetModelData modelData = new TargetModelData();
        modelData.setTarget("New window");
        store.add(modelData);

        modelData = new TargetModelData();
        modelData.setTarget("Same window");
        store.add(modelData);

        target = new ComboBox<TargetModelData>();
        target.setFieldLabel("Target");
        target.setDisplayField("target");
        target.setStore(store);
        target.select(0);

        pageLinkForm.add(pageTypeGroup);
        pageLinkForm.add(link);
        pageLinkForm.add(alt);
        pageLinkForm.add(url);
        pageLinkForm.add(target);


        fillData(selectedNodes);
        m_component.add(pageLinkForm);
        LayoutContainer right = new LayoutContainer();
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        right.setLayout(layout);
        right.add(pickContentButton);
       // m_component.add(right, new ColumnData(.5));

    }

    public void clear() {
        link.setValue("");
        alt.setValue("");
        url.setValue("");
    }

    public void fillData(Object root) {
        if (root == null) {
            clear();
        }

        if (pageTypeGroup.getRawValue().equalsIgnoreCase("external")) {
            return;
        }
        List<GWTJahiaNode> list = (List<GWTJahiaNode>) root;
        if (list != null && list.size() > 0) {
            GWTJahiaNode node = list.get(0);
            link.setValue(node.getName());
            alt.setValue(node.getName());
            url.setValue(node.getUrl());
        }
    }


    public Component getComponent() {
        return m_component;
    }

    private class TargetModelData extends BaseModelData {
        private String target;

        public String getTarget() {
            return get("target");
        }

        public void setTarget(String target) {
            set("target", target);
        }
    }
}
