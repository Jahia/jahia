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

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:52:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class LayoutTabItem extends PropertiesTabItem {
    private LayoutContainer htmlPreview;

    public LayoutTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.layout", "Layout"), engine, GWTJahiaItemDefinition.LAYOUT);
    }

    @Override
    public void attachPropertiesEditor() {
        if (engine.getNode() != null) {
            final ComboBox<GWTJahiaValueDisplayBean> templateField = (ComboBox<GWTJahiaValueDisplayBean>) propertiesEditor.getFieldsMap().get("j:template");
            final ComboBox<GWTJahiaValueDisplayBean> skinField = (ComboBox<GWTJahiaValueDisplayBean>) propertiesEditor.getFieldsMap().get("j:skin");
            final ComboBox<GWTJahiaValueDisplayBean> subNodesTemplateField = (ComboBox<GWTJahiaValueDisplayBean>) propertiesEditor.getFieldsMap().get("j:subNodesTemplate");
            final SelectionChangedListener<GWTJahiaValueDisplayBean> listener = new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                    Map<String, String> contextParams = new HashMap<String, String>();
                    if (skinField != null && skinField.getValue() != null) {
                        contextParams.put("forcedSkin", skinField.getValue().getValue());
                    }
                    if (subNodesTemplateField != null && subNodesTemplateField.getValue() != null) {
                        contextParams.put("forcedSubNodesTemplate", subNodesTemplateField.getValue().getValue());
                    }
                    updatePreview((templateField != null && templateField.getValue() != null) ? templateField.getValue().getValue() : null, contextParams);
                }
            };
            if (templateField != null) {
                templateField.addSelectionChangedListener(listener);
            }
            if (skinField != null) {
                skinField.addSelectionChangedListener(listener);
            }
            if (subNodesTemplateField != null) {
                subNodesTemplateField.addSelectionChangedListener(listener);
            }
            listener.selectionChanged(null);

            setLayout(new RowLayout());
            add(propertiesEditor);

            htmlPreview = new LayoutContainer(new FitLayout());
            htmlPreview.setId("bodywrapper");
            htmlPreview.setStyleAttribute("background-color", "white");
            htmlPreview.addStyleName("x-panel");
            htmlPreview.setScrollMode(Style.Scroll.AUTO);
            add(htmlPreview);

        }
    }

    /**
     * Update preview
     *
     * @param template
     * @param contextParams
     */
    private void updatePreview(String template, Map<String, String> contextParams) {
        if (engine.getNode() != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(engine.getNode().getPath(), null, null, template, "preview", contextParams, false, null, new BaseAsyncCallback<GWTRenderResult>() {
                public void onSuccess(GWTRenderResult result) {
                    HTML html = new HTML(result.getResult());

                    setHTML(html);
                    layout();
                }
            });
        } else {
            setHTML(null);
        }
    }

    /**
     * set preview HTML
     *
     * @param html
     */
    public void setHTML(HTML html) {
        htmlPreview.removeAll();
        if (html != null) {
            htmlPreview.add(html);
        }
        htmlPreview.layout();
    }

}
