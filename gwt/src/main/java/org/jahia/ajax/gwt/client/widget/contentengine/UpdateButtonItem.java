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

import static org.jahia.ajax.gwt.client.util.content.actions.ContentActions.fillContentChanges;

/**
 * Button Item for update - used for edit content.
 */
public class UpdateButtonItem extends SaveButtonItem {

    @Override
    protected void prepareAndSave(final AbstractContentEngine engine, final boolean closeAfterSave) {

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
            }
        }

        Set<String> removedTypes = fillContentChanges(engine, true);

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
            }
        });
    }
}
