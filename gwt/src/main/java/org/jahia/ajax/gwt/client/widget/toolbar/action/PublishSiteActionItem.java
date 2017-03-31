/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
        if (isNodeTypeAllowed(ctx.getMultipleSelection())) {
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