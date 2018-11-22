/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        engine.prepare();

        doSave((CreateContentEngine)engine, nodeName, engine.getChangedProperties(), engine.getChangedI18NProperties(),
                new ArrayList<String>(engine.getAddedTypes()), engine.getChildren(), engine.getNewNodeACL(),
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
        if (mixin.contains("jmix:createdFromPageModel")) {
            String sourcePath = null;
            for (GWTJahiaNodeProperty p : engine.getChangedProperties()) {
                if (p.getName().equals("j:templateName")) {
                    sourcePath = p.getValues().get(0).getString();
                    engine.getChangedProperties().remove(p);
                    break;
                }
            }
            JahiaContentManagementService.App.getInstance().createPageFromPageModel(sourcePath, engine.getTargetNode().getPath(), nodeName, engine.getType()
                    .getName(), mixin, newNodeACL, props, langCodeProperties, callback);
        } else if (engine.isCreateInParentAndMoveBefore()) {
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
