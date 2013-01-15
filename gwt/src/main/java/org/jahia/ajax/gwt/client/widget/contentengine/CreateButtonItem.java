/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.*;

public class CreateButtonItem extends SaveButtonItem {

    private boolean useNamePopup = false;

    public void setUseNamePopup(boolean useNamePopup) {
        this.useNamePopup = useNamePopup;
    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave) {
        if (useNamePopup) {
            showNamePopup(engine,closeAfterSave);
        } else {
            continuePrepareAndSave(engine, closeAfterSave, ((CreateContentEngine) engine).getTargetName());
        }
    }

    protected void continuePrepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave, String nodeName) {
        GWTJahiaNodeACL newNodeACL = new GWTJahiaNodeACL();
        List<GWTJahiaNode> children = new ArrayList<GWTJahiaNode>();
        newNodeACL.setAce(new ArrayList<GWTJahiaNodeACE>());

        final Set<String> addedTypes = new HashSet<String>();
        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    nodeName = ((ContentTabItem) item).getName().getValue();
                }
                final List<CheckBox> values = ((ContentTabItem) item).getInvalidLanguagesCheckBoxes();
                if (!values.isEmpty()) {
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    if (values.size() != siteLanguages.size()) {
                        List<String> strings = new ArrayList<String>(siteLanguages.size());
                        for (GWTJahiaLanguage siteLanguage : siteLanguages) {
                            strings.add(siteLanguage.getLanguage());
                        }
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName("j:invalidLanguages");
                        gwtJahiaNodeProperty.setMultiple(true);
                        for (CheckBox value : values) {
                            if (value.getValue()) {
                                strings.remove(value.getValueAttribute());
                            }
                        }
                        if(strings.size()>0) {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            for (String string : strings) {
                                gwtJahiaNodeProperty.getValues().add(new GWTJahiaNodePropertyValue(string));
                            }
                        }
                        final List<GWTJahiaNodePropertyValue> gwtJahiaNodePropertyValues = gwtJahiaNodeProperty.getValues();
                        if (gwtJahiaNodePropertyValues!=null && gwtJahiaNodePropertyValues.size() > 0) {
                            engine.getChangedProperties().add(gwtJahiaNodeProperty);
                            addedTypes.add("jmix:i18n");
                        }
                    }
                }
            }
            item.doSave(engine.getNode(), engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes, new HashSet<String>(), children, newNodeACL);
        }

        doSave((CreateContentEngine)engine, nodeName, engine.getChangedProperties(), engine.getChangedI18NProperties(), new ArrayList<String>(addedTypes), children, newNodeACL,
                closeAfterSave);
    }

    protected void doSave(final CreateContentEngine engine, String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, List<GWTJahiaNode> children, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        final AsyncCallback<GWTJahiaNode> callback = new BaseAsyncCallback<GWTJahiaNode>() {
            public void onApplicationFailure(Throwable throwable) {
                failSave(engine, throwable);
            }

            public void onSuccess(GWTJahiaNode node) {
                if (closeAfterSave) {
                    Info.display(
                            Messages.get("label.information", "Information"),
                            Messages.get(
                                    "org.jahia.engines.contentmanager.addContentWizard.formCard.success.save",
                                    "Content node created successfully:")
                                    + " " + node.getName());
                    engine.close();

                    engine.getLinker().setSelectPathAfterDataUpdate(Arrays.asList(node.getPath()));
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    data.put("node", node);
                    engine.getLinker().refresh(data);
                } else {
                    engine.getTabs().removeAll();
                    engine.initTabs();
                    engine.getChangedI18NProperties().clear();
                    engine.getChangedProperties().clear();
                    engine.getTabs().setSelection(engine.getTabs().getItem(0));
                    engine.layout(true);
                    engine.unmask();
                    engine.setButtonsEnabled(true);
                }
            }
        };
        if (engine.isCreateInParentAndMoveBefore()) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(engine.getTargetNode().getPath(), nodeName, engine.getType().getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else {
            JahiaContentManagementService.App.getInstance().createNode(engine.getParentPath(), nodeName, engine.getType().getName(), mixin, newNodeACL, props, langCodeProperties, children, null, true, callback);
        }
    }

    protected void showNamePopup(final AbstractContentEngine engine, final boolean closeAfterSave) {
        final Window popup = new Window();
        popup.setHeading(Messages.get("label.saveAs", "Save as ..."));
        popup.setHeight(120);
        popup.setWidth(350);
        popup.setModal(true);
        FormPanel f = new FormPanel();
        f.setHeaderVisible(false);
        f.setBorders(false);
        final TextField<String> name = new TextField<String>();
        name.setFieldLabel(Messages.get("label.name", "Name"));
        name.setMinLength(1);
        f.add(name);

        Button b = new Button(Messages.get("label.submit", "submit"));
        f.addButton(b);
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                continuePrepareAndSave(engine, closeAfterSave, name.getValue());
                popup.hide();
            }
        });

        Button c = new Button(Messages.get("label.cancel", "Cancel"));
        c.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                popup.hide();
            }
        });
        f.addButton(c);
        f.setButtonAlign(Style.HorizontalAlignment.CENTER);

        FormButtonBinding binding = new FormButtonBinding(f);
        binding.addButton(b);
        popup.add(f);
        popup.show();
    }


}
