/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.MainModule;

import java.util.*;

/**
 * Button Item for create
 */
public class CreateButtonItem extends SaveButtonItem {

    private boolean forceCreation = true;

    private boolean useNamePopup = false;

    private boolean redirectToCreatedPage;

    public void setUseNamePopup(boolean useNamePopup) {
        this.useNamePopup = useNamePopup;
    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave) {
        if (useNamePopup) {
            showNamePopup(engine, closeAfterSave);
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
                final List<CheckBox> values = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (values != null) {
                    // Checkboxes are not null so they are displayed, if list is empty this means that this
                    // content is not visible in any language
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
                        if (strings.size() > 0) {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            for (String string : strings) {
                                gwtJahiaNodeProperty.getValues().add(new GWTJahiaNodePropertyValue(string));
                            }
                        }
                        final List<GWTJahiaNodePropertyValue> gwtJahiaNodePropertyValues = gwtJahiaNodeProperty.getValues();
                        if (gwtJahiaNodePropertyValues != null && gwtJahiaNodePropertyValues.size() > 0) {
                            engine.getChangedProperties().add(gwtJahiaNodeProperty);
                            addedTypes.add("jmix:i18n");
                        }
                    }
                }
            }
            item.doSave(engine.getNode(), engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes,
                    new HashSet<String>(), children, newNodeACL);
        }

        doSave((CreateContentEngine)engine, nodeName, engine.getChangedProperties(), engine.getChangedI18NProperties(), new ArrayList<String>(addedTypes), children, newNodeACL,
                closeAfterSave);
    }

    protected void doSave(final CreateContentEngine engine, String nodeName, List<GWTJahiaNodeProperty> props, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<String> mixin, List<GWTJahiaNode> children, GWTJahiaNodeACL newNodeACL, final boolean closeAfterSave) {
        final AsyncCallback<GWTJahiaNode> callback = new BaseAsyncCallback<GWTJahiaNode>() {

            @Override
            public void onApplicationFailure(Throwable throwable) {
                failSave(engine, throwable);
            }

            @Override
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
                    if (!engine.skipRefreshOnSave()) {
                        Map<String, Object> data = new HashMap<String, Object>();
                        data.put(Linker.REFRESH_MAIN, true);
                        data.put("node", node);
                        engine.getLinker().refresh(data);

                        MainModule mainModule = MainModule.getInstance();
                        if (redirectToCreatedPage && mainModule != null && mainModule.getEditLinker() != null && node.isPage()) {
                            // if redirection for a newly created page is activated, we refresh the left-side panel and navigate to the created page
                            MainModule.staticGoTo(node.getPath(), null);
                        }
                    }
                } else {
                    engine.getTabs().removeAll();
                    engine.getProperties().clear();
                    engine.initTabs();
                    engine.getChangedI18NProperties().clear();
                    engine.getChangedProperties().clear();
                    engine.getTabs().setSelection(engine.getTabs().getItem(0));
                    engine.layout(true);
                    engine.unmask();
                    engine.setButtonsEnabled(true);
                    engine.setChildCount(engine.getChildCount() + 1);
                }
                ContentHelper.sendContentModificationEvent(node.getUUID(), node.getPath(), node.getName(), "create", engine.getType().getName());
            }
        };

        engine.removeUneditedLanguages();
        if (engine.isCreateInParentAndMoveBefore()) {
            JahiaContentManagementService.App.getInstance().createNodeAndMoveBefore(engine.getTargetNode().getPath(), nodeName, engine.getType().getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else {
            JahiaContentManagementService.App.getInstance().createNode(engine.getParentPath(), nodeName, engine.getType().getName(), mixin, newNodeACL, props, langCodeProperties, children, null, forceCreation, callback);
        }
    }

    protected void showNamePopup(final AbstractContentEngine engine, final boolean closeAfterSave) {
        final Window popup = new Window();
        popup.addStyleName("set-name-modal");
        popup.setHeadingHtml(Messages.get("label.saveAs", "Save as ..."));
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
        b.addStyleName("button-submit");
        f.addButton(b);
        b.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                continuePrepareAndSave(engine, closeAfterSave, name.getValue());
                popup.hide();
            }
        });

        Button c = new Button(Messages.get("label.cancel", "Cancel"));
        c.addStyleName("button-cancel");
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
        popup.setFocusWidget(name);
        popup.show();
    }

    public void setForceCreation(boolean forceCreation) {
        this.forceCreation = forceCreation;
    }

    public void setRedirectToCreatedPage(boolean redirectToCreatedPage) {
        this.redirectToCreatedPage = redirectToCreatedPage;
    }
}
