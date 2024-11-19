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
package org.jahia.ajax.gwt.client.widget.toolbar.action;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.form.FormDeployPortletDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 *
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:35 PM
*
*/
public class DeployPortletDefinitionActionItem extends BaseActionItem {
    public void onComponentSelection() {
        GWT.runAsync(new RunAsyncCallback() {

            @Override
            public void onFailure(Throwable reason) {
            }

            @Override
            public void onSuccess() {
                LinkerSelectionContext lh = linker.getSelectionContext();
                GWTJahiaNode parent = lh.getSingleSelection();
                if (parent != null) {
                    final com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
                    w.addStyleName("deploy-portlet-definition-modal");
                    w.setHeadingHtml(Messages.get("label.deployNewPortlet", "New portlets"));
                    w.setModal(true);
                    w.setResizable(false);
                    w.setBodyBorder(false);
                    w.setLayout(new FillLayout());
                    w.setWidth(600);
                    w.add(new FormDeployPortletDefinition() {
                        @Override
                        public void closeParent() {
                            w.hide();
                        }
                        @Override
                        public void refreshParent() {
                            Map<String, Object> data = new HashMap<String, Object>();
                            data.put(Linker.REFRESH_ALL, true);
                            linker.refresh(data);
                        }
                    });
                    w.setScrollMode(Style.Scroll.AUTO);
                    w.layout();
                    w.show();
                }
            }
        });

    }

    @Override
    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && hasPermission(lh.getSelectionPermissions()) && PermissionsUtils.isPermitted("jcr:write", lh.getSelectionPermissions()));
    }
}
