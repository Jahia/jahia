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

import com.extjs.gxt.ui.client.data.RpcMap;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.content.util.ContentHelper;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import java.util.*;

/**
 * Button Item for update - used for edit content.
 */
public class UpdateButtonItem extends SaveButtonItem {

    @Override
    protected void save(AbstractContentEngine engine, boolean closeAfterSave, boolean skipValidation) {
        // Maintain WIP properties
        // Has to be done because the engine does not support WIP anymore, previous WIP functionality was partially implemented
        // directly in the "manage work in progress button". WIP status must be set in order to add wip properties to
        // changed props list which is sent to save() method of JahiaContentManagementServiceImpl.
        if (engine.getNode().getWorkInProgressStatus() != null) {
            engine.setWipStatus(GWTJahiaNode.WipStatus.valueOf(engine.getNode().getWorkInProgressStatus()));
        }

        super.save(engine, closeAfterSave, skipValidation);
    }

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave) {
        // node
        final Set<String> addedTypes = new HashSet<String>();
        final Set<String> removedTypes = new HashSet<String>();

        for (TabItem tab : engine.getTabs().getItems()) {
            EditEngineTabItem item = tab.getData("item");
            // case of contentTabItem
            if (item instanceof ContentTabItem) {
                if (((ContentTabItem) item).isNodeNameFieldDisplayed()) {
                    Field<String> name = ((ContentTabItem) item).getName();
                    if (!name.isValid()) {
                        com.google.gwt.user.client.Window.alert(name.getErrorMessage());
                        engine.unmask();
                        engine.setButtonsEnabled(true);
                        return;
                    }
                    engine.setNodeName(name.getValue());
                    engine.getNode().setName(engine.getNodeName());
                }
                final List<CheckBox> validLanguagesChecked = ((ContentTabItem) item).getCheckedLanguagesCheckBox();
                if (validLanguagesChecked != null) {
                    // Checkboxes are not null so they are displayed, if list is empty this means that this
                    // content is not visible in any language
                    final List<GWTJahiaLanguage> siteLanguages = JahiaGWTParameters.getSiteLanguages();
                    List<String> invalidLanguages = engine.getNode().getInvalidLanguages();
                    List<String> newInvalidLanguages = new ArrayList<String>();
                    for (GWTJahiaLanguage language : siteLanguages) {
                        boolean found = false;
                        for (CheckBox validLang : validLanguagesChecked) {
                            if (language.getLanguage().equals(validLang.getValueAttribute())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newInvalidLanguages.add(language.getLanguage());
                        }
                    }
                    boolean hasChanged = newInvalidLanguages.size() != invalidLanguages.size();
                    if (!hasChanged) {
                        for (String lang : newInvalidLanguages) {
                            if (!invalidLanguages.contains(lang)) {
                                hasChanged = true;
                                break;
                            }
                        }
                    }
                    if (hasChanged) {
                        List<String> strings = new ArrayList<String>(siteLanguages.size());
                        for (GWTJahiaLanguage siteLanguage : siteLanguages) {
                            strings.add(siteLanguage.getLanguage());
                        }
                        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                        gwtJahiaNodeProperty.setName("j:invalidLanguages");
                        gwtJahiaNodeProperty.setMultiple(true);
                        for (CheckBox value : validLanguagesChecked) {
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
                        } else {
                            gwtJahiaNodeProperty.setValues(new ArrayList<GWTJahiaNodePropertyValue>());
                            engine.getChangedProperties().add(gwtJahiaNodeProperty);
                        }
                    }
                }
            }

            // case of right tab
            item.doSave(engine.getNode(), engine.getChangedProperties(), engine.getChangedI18NProperties(), addedTypes,
                    removedTypes, null, engine.getAcl());
        }

        engine.getNode().getNodeTypes().removeAll(removedTypes);
        engine.getNode().getNodeTypes().addAll(addedTypes);

        engine.removeUneditedLanguages();

        JahiaContentManagementService.App.getInstance().saveNode(engine.getNode(), engine.getAcl(), engine.getChangedI18NProperties(), engine.getChangedProperties(), removedTypes, new BaseAsyncCallback<RpcMap>() {

            @Override
            public void onApplicationFailure(Throwable throwable) {
                failSave(engine, throwable);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void onSuccess(RpcMap o) {
                Info.display(Messages.get("label.information", "Information"), Messages.get("saved_prop", "Properties saved\n\n"));
                Map<String, Object> data = new HashMap<String, Object>();
                data.put(Linker.REFRESH_MAIN, true);
                data.put("forceImageRefresh", true);
                EditLinker editLinker = null;
                if (engine.getLinker() instanceof SidePanelTabItem.SidePanelLinker) {
                    editLinker = ((SidePanelTabItem.SidePanelLinker) engine.getLinker()).getEditLinker();
                } else if (engine.getLinker() instanceof EditLinker) {
                    editLinker = (EditLinker) engine.getLinker();
                }
                GWTJahiaNode node = engine.getNode();
                if (editLinker != null && node.equals(editLinker.getMainModule().getNode()) && !node.getName().equals(editLinker.getMainModule().getNode().getName())) {
                    editLinker.getMainModule().handleNewMainSelection(node.getPath().substring(0, node.getPath().lastIndexOf("/") + 1) + node.getName(), editLinker.getMainModule().getTemplate());
                }
                data.put("node", node);
                ((EditContentEngine) engine).closeEngine();
                if (o != null && o.containsKey(GWTJahiaNode.SITE_LANGUAGES)) {
                    JahiaGWTParameters.getSiteNode().set(GWTJahiaNode.SITE_LANGUAGES, o.get(GWTJahiaNode.SITE_LANGUAGES));
                    if (o.containsKey(GWTJahiaNode.PERMISSIONS)) {
                        PermissionsUtils.loadPermissions((List<String>) o.get(GWTJahiaNode.PERMISSIONS));
                    }
                }
                if (!engine.skipRefreshOnSave()) {
                    engine.getLinker().refresh(data);
                }
                ContentHelper.sendContentModificationEvent(node.getUUID(), node.getPath(), engine.getNodeName(), "update", null);
                refresh();
            }
        });
    }

    public native void refresh() /*-{
       if ($wnd.top.authoringApi && $wnd.top.authoringApi.refreshContent) {
           $wnd.top.authoringApi.refreshContent();
       }
    }-*/;
}
