/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.publication;

import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Window;
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
    }

    protected void doPublish(List<GWTJahiaNodeProperty> nodeProperties, final WorkflowActionDialog dialog) {
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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnpublicationWorkflow)) {
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
