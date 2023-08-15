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
package org.jahia.ajax.gwt.client.widget.publication;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkInProgressActionItem;
import org.jahia.ajax.gwt.client.widget.workflow.WorkflowActionDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Sep 10, 2010
 * Time: 3:32:00 PM
 */
public class UnpublicationWorkflow extends PublicationWorkflow {
    private static final long serialVersionUID = -4916142720074054130L;

    public UnpublicationWorkflow() {
    }

    public UnpublicationWorkflow(List<GWTJahiaPublicationInfo> publicationInfos) {
        super(publicationInfos);
    }

    protected void initDialog(WorkflowActionDialog dialog) {
        TabItem tab = new TabItem("Unpublication infos");
        tab.setLayout(new FitLayout());

        PublicationStatusGrid g = new PublicationStatusGrid(publicationInfos, true, dialog.getLinker(), dialog.getContainer());
        tab.add(g);

        dialog.getTabPanel().add(tab);

        TabItem p = dialog.getTabPanel().getItem(0);
        LayoutContainer layoutContainer = new LayoutContainer(new RowLayout());
        layoutContainer.setStyleAttribute("margin", "5px");

        int results = 0;
        int pageResults = 0;
        for (GWTJahiaPublicationInfo info : publicationInfos) {
            results++;
            if (Boolean.TRUE.equals(info.get("isPage"))) {
                pageResults++;
            }
        }

        HorizontalPanel h = new HorizontalPanel();
        h.add(GWTJahiaPublicationInfo.renderPublicationStatusImage(GWTJahiaPublicationInfo.UNPUBLISHED));
        String text = "&nbsp;" + Messages.get("label.publication.unpublished.task", "Unpublishing content") + " : ";
        if (results > 10) {
            text += " <span style=\"color:red\">" + results + " " + Messages.get("label.items", "Items") + "</span>";
        } else if (results > 1) {
            text += results + " " + Messages.get("label.items", "Items");
        } else {
            text += results + " " + Messages.get("label.item", "Item");
        }
        if (pageResults > 0) {
            if (pageResults > 1) {
                text += " " + Messages.get("label.including", "including") + " <span style=\"color:red\">" + pageResults + " " + Messages.get("label.pages", "pages") + "</span>";
            } else {
                text += " " + Messages.get("label.including", "including") + " " + pageResults + " " + Messages.get("label.page", "page");
            }
        }
        Html w = new Html(text);
        h.add(w);

        layoutContainer.add(h);
        p.add(layoutContainer, new BorderLayoutData(Style.LayoutRegion.NORTH, 25));
    }

    protected void doPublish(List<GWTJahiaNodeProperty> nodeProperties, final WorkflowActionDialog dialog, String language) {
        final String status = Messages.get("label.publication.unpublished.task", "Unpublishing content");
        Info.display(status, status);
        WorkInProgressActionItem.setStatus(status);
        final List<String> allUuids = getAllUuids();
        BaseAsyncCallback callback = new BaseAsyncCallback() {
            public void onApplicationFailure(Throwable caught) {
                WorkInProgressActionItem.removeStatus(status);
                Info.display("Cannot unpublish", "Cannot unpublish");
                Window.alert("Cannot unpublish " + caught.getMessage());
            }

            public void onSuccess(Object result) {
                WorkInProgressActionItem.removeStatus(status);
                if (allUuids.size() < 20) {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(Linker.REFRESH_MAIN, true);
                    data.put("event", "unpublicationSuccess");
                    dialog.getLinker().refresh(data);
                }
            }
        };
        JahiaContentManagementService.App.getInstance().unpublish(allUuids, callback);
    }

    @Override
    public List<String> getAllUuids() {
        return getAllUuids(publicationInfos, false, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }

        UnpublicationWorkflow that = (UnpublicationWorkflow) o;

        if (publicationInfos != null ? !publicationInfos.equals(that.publicationInfos) :
            that.publicationInfos != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = publicationInfos != null ? publicationInfos.hashCode() : 0;
        return result;
    }
}
