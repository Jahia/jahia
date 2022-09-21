/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.Arrays;
import java.util.List;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* 
*/
public class PublishSiteActionItem extends PublishActionItem {

    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        allSubTree = true;
        updateTitle(gwtToolbarItem.getTitle() + " " + JahiaGWTParameters.getSiteKey() + " - " + JahiaGWTParameters.getLanguageDisplayName());
        super.init(gwtToolbarItem, linker);
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext ctx = linker.getSelectionContext();
        if (hasPermission(ctx.getSelectionPermissions()) && isNodeTypeAllowed(ctx.getMultipleSelection())) {
            boolean hasOnlyOneLanguage = JahiaGWTParameters.getSiteLanguages().size() == 1;
            setEnabled(true);
            if (allLanguages) {
                if(hasOnlyOneLanguage) {
                    setEnabled(false);
                    return;
                }
                updateTitle(getGwtToolbarItem().getTitle() + " " + JahiaGWTParameters.getSiteKey() + " - " + Messages.get("label.publish.all.languages", "all languages"));
            } else {
                updateTitle(getGwtToolbarItem().getTitle() + " " + JahiaGWTParameters.getSiteKey() + " - " + JahiaGWTParameters.getLanguageDisplayName());
            }
        } else {
            setEnabled(false);
        }
    }

    @Override public void onComponentSelection() {
        linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
        JahiaContentManagementService.App.getInstance().getNodes(Arrays.asList("/sites/"+JahiaGWTParameters.getSiteKey()),
                Arrays.asList(GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.WORKFLOW_INFO),
                new BaseAsyncCallback<List<GWTJahiaNode>>() {
                    public void onSuccess(List<GWTJahiaNode> result) {
                        linker.loaded();
                        final LinkerSelectionContext selectionContext = linker.getSelectionContext();
                        final List<GWTJahiaNode> multipleSelection = selectionContext.getMultipleSelection();
                        selectionContext.setSelectedNodes(result);
                        selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);
                        PublishSiteActionItem.super.onComponentSelection();
                        selectionContext.setSelectedNodes(multipleSelection);
                        selectionContext.refresh(LinkerSelectionContext.SELECTED_NODE_ONLY);
                    }
                });
    }
}