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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.Arrays;
import java.util.List;

public class PublicationStatusActionItem extends BaseActionItem {

    private transient HorizontalPanel panel;

    @Override
    public void handleNewMainNodeLoaded(final GWTJahiaNode node) {
        final GWTJahiaPublicationInfo info = node.getAggregatedPublicationInfo();
        if (info != null) {
            displayInfo(info);
        } else {
            JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList(node.getPath()), Arrays.asList(GWTJahiaNode.PUBLICATION_INFO), new BaseAsyncCallback<List<GWTJahiaNode>>() {
                public void onSuccess(List<GWTJahiaNode> result) {
                    final GWTJahiaPublicationInfo info = result.get(0).getAggregatedPublicationInfo();
                    if (info != null) {
                        displayInfo(info);
                    }
                }
            });
        }
    }

    private void displayInfo(GWTJahiaPublicationInfo info) {
        Image res = GWTJahiaPublicationInfo.renderPublicationStatusImage(info);
        panel.removeAll();
        if (getGwtToolbarItem().getTitle() != null) {
            panel.add(new Html(getGwtToolbarItem().getTitle() + "&nbsp;:&nbsp;"));
        }
        panel.add(res);
        panel.layout();
    }

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        panel = new HorizontalPanel();
        panel.addStyleName(getGwtToolbarItem().getClassName());
        panel.addStyleName("action-bar-menu-item");
    }

    @Override
    public Component getCustomItem() {
        return panel;
    }

}
