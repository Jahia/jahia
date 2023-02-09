/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.GWTRenderResult;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineTab;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:52:00 PM
 *
 */
public class LayoutTabItem extends PropertiesTabItem {
    private transient LayoutContainer ctn;
    private transient LayoutContainer htmlPreview;
    private transient SelectionChangedListener<GWTJahiaValueDisplayBean> listener;
    private String cssWrapper;

    @Override public AsyncTabItem create(GWTEngineTab engineTab, NodeHolder engine) {
        if (dataType == null) {
            dataType = Arrays.asList(GWTJahiaItemDefinition.LAYOUT);
        }
        return super.create(engineTab,engine);
    }

    public void init(final NodeHolder engine, final AsyncTabItem tab, String language) {
        super.init(engine, tab, language);

        // refresh view
        if (listener != null) {
            listener.selectionChanged(null);
        }
    }

    @Override
    public void attachPropertiesEditor(final NodeHolder engine, final AsyncTabItem tab) {
        if (engine.getNode() != null && engine.getLinker() instanceof EditLinker) {
            final PropertiesEditor.PropertyAdapterField templateField =  propertiesEditor.getFieldsMap().get("j:view");
            final PropertiesEditor.PropertyAdapterField skinField =  propertiesEditor.getFieldsMap().get("j:skin");
            final PropertiesEditor.PropertyAdapterField subNodesViewField = propertiesEditor.getFieldsMap().get("j:subNodesView");
            listener = new SelectionChangedListener<GWTJahiaValueDisplayBean>() {
                public void selectionChanged(SelectionChangedEvent<GWTJahiaValueDisplayBean> se) {
                    Map<String, List<String>> contextParams = new HashMap<String, List<String>>();
                    if (skinField != null && skinField.getValue() != null) {
                        contextParams.put("forcedSkin", Arrays.asList(((ComboBox<GWTJahiaValueDisplayBean>)skinField.getField()).getValue().getValue()));
                    }
                    if (subNodesViewField != null && subNodesViewField.getValue() != null) {
                        contextParams.put("forcedSubNodesTemplate", Arrays.asList(((ComboBox<GWTJahiaValueDisplayBean>)subNodesViewField.getField()).getValue().getValue()));
                    }
                    String template = (templateField != null && templateField.getValue() != null) ? ((ComboBox<GWTJahiaValueDisplayBean>)templateField.getField()).getValue().getValue() : null;
                    if (engine.getNode() != null) {
                        JahiaContentManagementService
                                .App.getInstance().getRenderedContent(engine.getNode().getPath(), null, LayoutTabItem.this.language,
                                template, "preview", contextParams, false, null, null, null, new BaseAsyncCallback<GWTRenderResult>() {
                            public void onSuccess(GWTRenderResult result) {
                                HTML html = new HTML(result.getResult());

                                setHTML(html);
                                tab.layout();
                            }
                        });
                    } else {
                        setHTML(null);
                    }
                }
            };
            if (templateField != null) {
                ((ComboBox<GWTJahiaValueDisplayBean>)templateField.getField()).addSelectionChangedListener(listener);
            }
            if (skinField != null) {
                ((ComboBox<GWTJahiaValueDisplayBean>)skinField.getField()).addSelectionChangedListener(listener);
            }
            if (subNodesViewField != null) {
                ((ComboBox<GWTJahiaValueDisplayBean>)subNodesViewField.getField()).addSelectionChangedListener(listener);
            }

            tab.setLayout(new FillLayout());

            if (ctn == null) {
                ctn = new LayoutContainer(new FitLayout());
                tab.add(ctn);
                htmlPreview = new LayoutContainer();
                htmlPreview.addStyleName(cssWrapper);
                htmlPreview.setStyleAttribute("background-color", "white");
                FieldSet f = new FieldSet();
                f.addStyleName("x-panel");
                f.setHeadingHtml(Messages.get("label.preview", "Preview"));
                f.setScrollMode(Style.Scroll.AUTO);
                f.add(htmlPreview);
                tab.add(f);
            }

            ctn.add(propertiesEditor);
        } else {
            super.attachPropertiesEditor(engine, tab);
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

    @Override public void setProcessed(boolean processed) {
        if (!processed && langPropertiesEditorMap != null) {
            ctn = null;
            htmlPreview = null;
            listener = null;
        }
        super.setProcessed(processed);
    }

    public void setCssWrapper(String cssWrapper) {
        this.cssWrapper = cssWrapper;
    }
}
