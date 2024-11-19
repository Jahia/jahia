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

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTHooks;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEngineConfiguration;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Hover;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem.SidePanelLinker;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine loader component.
 * User: toto
 * Date: Jul 2, 2010
 * Time: 7:36:04 PM
 */
public class EngineLoader {
    public static final int CREATE = 1;
    public static final int EDIT = 2;

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node, GWTEngineConfiguration engineConfiguration) {
        showEngine(EDIT, linker, node, null, null, null, false, false, engineConfiguration, false, null, false);
    }

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node, GWTEngineConfiguration engineConfiguration, boolean skipRefreshOnSave, EditEngineJSConfig jsConfig) {
        showEngine(EDIT, linker, node, null, null, null, false, false, engineConfiguration, skipRefreshOnSave, jsConfig, false);
    }

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node, boolean forceEngineWindow, GWTEngineConfiguration engineConfiguration) {
        showEngine(EDIT, linker, node, null, null, null, false, forceEngineWindow, engineConfiguration, false, null, false);
    }

    public static void showCreateEngine(final Linker linker, final GWTJahiaNode node, final GWTJahiaNodeType type,
                                        final Map<String, GWTJahiaNodeProperty> props, final String targetName,
                                        final boolean createInParentAndMoveBefore, GWTEngineConfiguration engineConfiguration) {
        showCreateEngine(linker, node, type, props, targetName, createInParentAndMoveBefore, engineConfiguration, false, false);
    }

    public static void showCreateEngine(final Linker linker, final GWTJahiaNode node, final GWTJahiaNodeType type,
                                        final Map<String, GWTJahiaNodeProperty> props, final String targetName,
                                        final boolean createInParentAndMoveBefore, GWTEngineConfiguration engineConfiguration, boolean skipRefreshOnSave, boolean systemNameReadOnly) {
        showEngine(CREATE, linker, node, type, props, targetName, createInParentAndMoveBefore, false, engineConfiguration, skipRefreshOnSave, null, systemNameReadOnly);
    }

    private static void showEngine(final int t, final Linker linker, final GWTJahiaNode node,
                                   final GWTJahiaNodeType type, final Map<String, GWTJahiaNodeProperty> props,
                                   final String targetName, final boolean createInParentAndMoveBefore,
                                   final boolean forceEngineWindow, final GWTEngineConfiguration engineConfiguration,
                                   final boolean skipRefreshOnSave, final EditEngineJSConfig jsConfig,
                                   final boolean systemNameReadOnly) {
        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onFailure(Throwable reason) {

            }

            @Override
            public void onSuccess() {
                String operation = t == CREATE ? "create" : "edit";
                if (jsConfig == null && JahiaGWTHooks.hasHook(operation)) {
                    Map<String, Object> params = new HashMap<>();
                    // Provide the path
                    params.put("path", node.getPath());
                    params.put("uuid", node.getUUID());
                    if (type != null) {
                        params.put("contentTypes", Collections.singleton(type.getName()));
                        params.put("includeSubTypes", false);
                    }
                    JahiaGWTHooks.callHook(operation, params);
                    return;
                }
                EngineContainer container = createContainer(linker, forceEngineWindow);

                if (t == CREATE) {
                    new CreateContentEngine(engineConfiguration == null ? linker.getConfig().getEngineConfiguration(type) : engineConfiguration, linker, node, type, props, targetName, createInParentAndMoveBefore,
                            container, skipRefreshOnSave, systemNameReadOnly);
                } else if (t == EDIT) {
                    new EditContentEngine(engineConfiguration == null ? linker.getConfig().getEngineConfiguration(node) : engineConfiguration, node, linker, container, skipRefreshOnSave, jsConfig);
                }
                container.showEngine();
                Hover.getInstance().removeAll();
            }
        });
    }

    private static EngineContainer createContainer(Linker linker, boolean forceEngineWindow) {
        EngineContainer container;
        if (!forceEngineWindow && (!(GXT.isIE) && (linker instanceof EditLinker || linker instanceof SidePanelLinker))) {
            container = new EnginePanel();
        } else {
            container = new EngineWindow();
        }
        return container;
    }

    public static EngineContainer createContainer(Linker linker) {
        return createContainer(linker, false);
    }
}
