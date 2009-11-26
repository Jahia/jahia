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
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;
import java.util.ArrayList; /**
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
public class PickedPageView extends BottomRightComponent implements PickedContent {
    private LayoutContainer m_component;
    private RadioGroup pageTypeGroup = new RadioGroup("pageType");
    private String defaultLinkType;
    private TextField<String> link = new TextField<String>();
    private TextField<String> alt = new TextField<String>();
    private TextField<String> url = new TextField<String>();
    private ComboBox<TargetModelData> target = new ComboBox<TargetModelData>();

    private final JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private final JahiaContentDefinitionServiceAsync cDefService = JahiaContentDefinitionService.App.getInstance();
    private GWTJahiaNode selectedContent = null;
    private static final boolean INTERNAL_VALUE = false;
    private boolean isInternal = true;


    public PickedPageView(String pickerType, boolean externalAllowed, boolean internalAllowed, List<GWTJahiaNode> selectedNodes, boolean multiple, final ManagerConfiguration config) {

        m_component = new LayoutContainer(new FitLayout());
        //m_component.setBodyBorder(false);
        //m_component.setHeaderVisible(false);
        m_component.setBorders(false);

        if (externalAllowed) {
            defaultLinkType = "external";
        }

        if (internalAllowed) {
            defaultLinkType = "internal";
        }


        // form panel
        FormPanel pageLinkForm = new FormPanel();
        pageLinkForm.setPadding(5);
        pageLinkForm.setCollapsible(false);
        pageLinkForm.setFrame(false);
        pageLinkForm.setAnimCollapse(false);
        pageLinkForm.setBorders(false);
        pageLinkForm.setBodyBorder(false);
        pageLinkForm.setHeaderVisible(false);
        pageLinkForm.setScrollMode(Style.Scroll.AUTO);
        pageLinkForm.setButtonAlign(Style.HorizontalAlignment.CENTER);

        // filed set


        Radio external = new Radio();
        Radio internal = new Radio();

        if (externalAllowed && internalAllowed) {
            external.setName("external");
            external.setBoxLabel(Messages.get("cm_external_link", "external link"));
            external.addListener(Events.Change, new Listener<FieldEvent>() {
                public void handleEvent(FieldEvent fieldEvent) {
                    isInternal = !isInternal;
                }
            });

            internal.setName("internal");
            internal.setBoxLabel(Messages.get("cm_internal_link", "internal link"));
            internal.setValue(INTERNAL_VALUE);

            pageTypeGroup.setFieldLabel(Messages.get("cm_page_type", "Page type"));
            pageTypeGroup.add(external);
            pageTypeGroup.add(internal);

            pageTypeGroup.setValue(internal);
        }


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
        modelData.setTarget(Messages.get("cm_new_window", "New window"));
        modelData.setValue(Messages.get("cm_new_window", "_blank"));
        store.add(modelData);

        modelData = new TargetModelData();
        modelData.setTarget(Messages.get("cm_same_window", "Same window"));
        modelData.setValue(Messages.get("cm_new_window", "_self"));
        store.add(modelData);

        target = new ComboBox<TargetModelData>();
        target.setFieldLabel(Messages.get("cm_target", "Target"));
        target.setDisplayField("target");
        target.setStore(store);
        target.select(0);

        if (pageTypeGroup != null) {
            pageLinkForm.add(pageTypeGroup);
        }

        FieldSet fieldSet = new FieldSet();
        fieldSet.setStyleAttribute("padding", "0");
        fieldSet.setBorders(false);
        //fieldSet.setHeading(Messages.get("cm_link_properties", "Properties"));
        fieldSet.setCollapsible(false);

        FormPanel sub = new FormPanel();
        sub.setBodyBorder(false);
        sub.setBorders(false);
        sub.setFieldWidth(500);
        sub.setLabelWidth(140);
        sub.setHeaderVisible(false);
        sub.setFrame(false);
        sub.add(link);
        sub.add(alt);
        sub.add(url);
        sub.add(target);
        fieldSet.add(sub);
        pageLinkForm.add(fieldSet);

        if (selectedNodes != null && selectedNodes.size() > 0) {
            selectedContent = selectedNodes.get(0);
            GWTJahiaNode node = (GWTJahiaNode) selectedContent.get("j:node");
            if (node != null) {
                link.setValue(node.getName());
                url.setValue(node.getUrl());
                pageTypeGroup.setValue(internal);
            } else {
                link.setValue((String) selectedContent.get("jcr:title"));
                url.setValue((String) selectedContent.get("j:url"));
                pageTypeGroup.setValue(external);
            }
            alt.setValue((String) selectedContent.get("j:alt"));

        }
        m_component.add(pageLinkForm);
        LayoutContainer right = new LayoutContainer();
        FormLayout layout = new FormLayout();
        layout.setLabelAlign(FormPanel.LabelAlign.TOP);
        right.setLayout(layout);
        right.add(pickContentButton);

    }

    /**
     * Clear
     */
    public void clear() {
        link.setValue("");
        alt.setValue("");
        url.setValue("");
        selectedContent.set("clear", "clear");
    }

    /**
     * Fill data
     *
     * @param root
     */
    public void fillData(Object root) {
        if (root == null) {
            clear();
        }


        List<GWTJahiaNode> list = (List<GWTJahiaNode>) root;
        if (list != null && list.size() > 0) {
            GWTJahiaNode node = list.get(0);
            link.setValue(node.getName());
            alt.setValue(node.getName());
            url.setValue(node.getUrl());

            selectedContent = new GWTJahiaNode();
            selectedContent.set("j:node", node);
            selectedContent.set("linkType", "internal");
        }


        // add extra parameter
        if (selectedContent != null) {
            selectedContent.set("jcr:title", link.getValue());
            selectedContent.set("j:url", url.getValue());
            selectedContent.set("j:alt", alt.getValue());
            selectedContent.remove("clear");
        }
    }


    public Component getComponent() {
        return m_component;
    }

    /**
     * Model for new target attribute of the link
     */
    private class TargetModelData extends BaseModelData {
        public String getTarget() {
            return get("target");
        }

        public void setTarget(String target) {
            set("target", target);
        }

        public String getValue() {
            return get("value");
        }

        public void setValue(String target) {
            set("value", target);
        }
    }

    public List<GWTJahiaNode> getSelectedContent() {
        // case of external link
        if (isExternalLink()) {
            selectedContent = new GWTJahiaNode();
            selectedContent.set("linkType", "external");
            selectedContent.set("jcr:title", link.getValue());
            selectedContent.set("j:url", url.getValue());
            selectedContent.set("j:alt", alt.getValue());
            selectedContent.remove("clear");
        }
        // case of internal link
        else if (selectedContent == null) {
            return null;
        }
        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
        l.add(selectedContent);
        return l;
    }

    /**
     * Return tru if it's an internal link
     *
     * @return
     */
    private boolean isExternalLink() {
        return !isInternal;
    }
}
