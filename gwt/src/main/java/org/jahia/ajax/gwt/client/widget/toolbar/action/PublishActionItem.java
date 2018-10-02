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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowDefinition;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.publication.PublicationWorkflow;

import java.util.*;

/**
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:58:56 PM
 */
@SuppressWarnings("serial")
public class PublishActionItem extends NodeTypeAwareBaseActionItem {

    /**
     * Returns <code>true</code> if the selected item (single-selection case) is locked by the marked for deletion operation on its parent.
     * In a multi-selection mode, the condition should be fulfilled for all selected items. If at least one of the selected items is not in
     * marked for deletion state this method returns false.
     *
     * @param selection the current selection context
     * @return <code>true</code> if the selected item (single-selection case) is locked by the marked for deletion operation on its parent.
     * In a multi-selection mode, the condition should be fulfilled for all selected items. If at least one of the selected items is
     * not in marked for deletion state this method returns false.
     */
    public static boolean isChildOfMarkedForDeletion(LinkerSelectionContext selection) {
        if (selection.getMultipleSelection().size() == 0) {
            return false;
        }

        boolean markedForDeletion = false;
        for (GWTJahiaNode node : selection.getMultipleSelection()) {
            markedForDeletion = node.isMarkedForDeletion()
                    && !node.isMarkedForDeletionRoot();
            if (!markedForDeletion) {
                break;
            }
        }

        return markedForDeletion;
    }

    protected transient String workflowType = "publish";
    protected transient boolean checkForUnpublication = false;
    protected boolean allSubTree = false;
    protected boolean allLanguages = false;

    @Override
    public void handleNewLinkerSelection() {
        setEnabled(false);
        this.getGwtToolbarItem().setHideWhenDisabled(true);
        LinkerSelectionContext ctx = linker.getSelectionContext();
        boolean hasOnlyOneLanguage = JahiaGWTParameters.getSiteLanguages().size() == 1;
        if (allLanguages) {
            if (hasOnlyOneLanguage) {
                setEnabled(false);
                return;
            }
            if (ctx.getMultipleSelection() != null && ctx.getMultipleSelection().size() > 1) {
                if (!isChildOfMarkedForDeletion(ctx) && hasPermission(ctx.getSelectionPermissions())
                        && supportPublication(ctx.getMultipleSelection())
                        && isNodeTypeAllowed(ctx.getMultipleSelection())) {
                    setEnabled(true);
                    if (allSubTree) {
                        updateTitle(Messages.get("label.publish.all.selected.items.all.languages", "Publish all under selected items in all languages"));
                    } else {
                        updateTitle(Messages.get("label.publish.selected.items.all.languages", "Publish all selected items in all languages"));
                    }
                }
            } else {
                GWTJahiaNode gwtJahiaNode = ctx.getSingleSelection();
                if (isWorkInProgress(gwtJahiaNode)) {
                    setEnabled(false);
                } else if (gwtJahiaNode != null) {
                    String title;
                    String displayName = gwtJahiaNode.getDisplayName().length() > 40 ? gwtJahiaNode.getDisplayName().substring(0, 40)+"..." : gwtJahiaNode.getDisplayName();
                    if (allSubTree) {
                        title = Messages.getWithArgs("label.publishall.all.languages", "Publish all under <i>{0}</i> in all languages", new String[] {displayName});
                    } else {
                        title = Messages.getWithArgs("label.publish.languages", "Publish <i>{0}</i> in all languages",
                                new String[] {displayName});
                    }
                    updateItem(ctx, gwtJahiaNode, title);
                }
            }
        } else if (allSubTree) {
            if (ctx.getMultipleSelection() != null
                    && ctx.getMultipleSelection().size() > 1) {
                if (!isChildOfMarkedForDeletion(ctx) && hasPermission(ctx.getSelectionPermissions())
                        && supportPublication(ctx.getMultipleSelection())
                        && isNodeTypeAllowed(ctx.getMultipleSelection())) {
                    setEnabled(true);
                    updateTitle(Messages.get("label.publish.all.selected.items"));
                }
            } else {
                GWTJahiaNode gwtJahiaNode = ctx.getSingleSelection();
                if (isWorkInProgress(gwtJahiaNode)) {
                    setEnabled(false);
                } else if (gwtJahiaNode != null) {
                    String title = Messages.get("label.publishall") + " <i>" + gwtJahiaNode.getDisplayName() +
                            "</i> - " + JahiaGWTParameters.getLanguageDisplayName();
                    updateItem(ctx, gwtJahiaNode, title);
                }
            }
        } else {
            if (ctx.getMultipleSelection() != null
                    && ctx.getMultipleSelection().size() > 1) {
                if (!isChildOfMarkedForDeletion(ctx) && hasPermission(ctx.getSelectionPermissions())
                        && supportPublication(ctx.getMultipleSelection())
                        && isNodeTypeAllowed(ctx.getMultipleSelection())) {
                    setEnabled(true);
                    updateTitle(getGwtToolbarItem().getTitle());
                }
            } else {
                GWTJahiaNode gwtJahiaNode = ctx.getSingleSelection();
                if (isWorkInProgress(gwtJahiaNode)) {
                    setEnabled(false);
                } else if (gwtJahiaNode != null && !isChildOfMarkedForDeletion(ctx)
                        && Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication"))
                        && hasPermission(gwtJahiaNode) && isNodeTypeAllowed(gwtJahiaNode)) {
                    setEnabled(true);

                    if (checkForUnpublication) {
                        GWTJahiaPublicationInfo publicationInfo = gwtJahiaNode.getAggregatedPublicationInfo() != null ? gwtJahiaNode
                                .getAggregatedPublicationInfo() : gwtJahiaNode.getQuickPublicationInfo();
                        if (publicationInfo != null && !publicationInfo.isUnpublishable()) {
                            setEnabled(false);
                        }
                    } else if (gwtJahiaNode.getAggregatedPublicationInfo() != null) {
                        GWTJahiaPublicationInfo info = gwtJahiaNode.getAggregatedPublicationInfo();
                        GWTJahiaWorkflowDefinition def = null;
                        if (gwtJahiaNode.getWorkflowInfo() != null) {
                            def = gwtJahiaNode.getWorkflowInfo().getPossibleWorkflows().get(new GWTJahiaWorkflowType(workflowType));
                        }

                        setEnabled(info.isPublishable() && (def != null || info.isAllowedToPublishWithoutWorkflow()));
                    }
                    String displayName = gwtJahiaNode.getDisplayName().length() > 40 ? gwtJahiaNode.getDisplayName().substring(0, 40)+"..." : gwtJahiaNode.getDisplayName();
                    if (gwtJahiaNode.isFile() || gwtJahiaNode.isNodeType("nt:folder")) {
                        updateTitle(getGwtToolbarItem().getTitle() + " <i>" + displayName + "</i>");
                    } else {
                        updateTitle(getGwtToolbarItem().getTitle() + " <i>" + displayName +
                                "</i> - " + JahiaGWTParameters.getLanguageDisplayName());
                    }
                }
            }
        }
    }

    protected boolean isWorkInProgress(GWTJahiaNode gwtJahiaNode) {
        return gwtJahiaNode != null && !gwtJahiaNode.isMarkedForDeletion()
                && gwtJahiaNode.isInWorkInProgress(JahiaGWTParameters.getLanguage());
    }

    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        this.getGwtToolbarItem().setHideWhenDisabled(true);
        setEnabled(false);
    }

    @Override
    public void onComponentSelection() {
        LinkerSelectionContext ctx = linker.getSelectionContext();
        if (!ctx.getMultipleSelection().isEmpty()) {
            final List<String> uuids = new ArrayList<String>();
            List<GWTJahiaNode> jahiaNodes = ctx.getMultipleSelection();
            if (jahiaNodes.size() > 1) {
                for (GWTJahiaNode jahiaNode : jahiaNodes) {
                    uuids.add(jahiaNode.getUUID());
                }
            } else {
                uuids.add(ctx.getSingleSelection().getUUID());
            }
            linker.loading(Messages.get("label.gettingPublicationInfo", "Getting publication information"));
            PublicationWorkflow.openPublicationWorkflow(uuids, linker, allSubTree, allLanguages, checkForUnpublication);
        }
    }



    /**
     *
     * @param allSubTree true if this item should publish all subtree of selected elements
     */
    public void setAllSubTree(boolean allSubTree) {
        this.allSubTree = allSubTree;
    }

    /**
     *
     * @param allLanguages true if this item should publish selected items in all languages
     */
    public void setAllLanguages(boolean allLanguages) {
        this.allLanguages = allLanguages;
    }

    private void updateItem(LinkerSelectionContext ctx, GWTJahiaNode gwtJahiaNode, String title) {
        if (!isChildOfMarkedForDeletion(ctx) && Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication")) && hasPermission(gwtJahiaNode) && isNodeTypeAllowed(gwtJahiaNode)) {
            setEnabled(true);
            updateTitle(title);
        }
    }

    private boolean supportPublication(List<GWTJahiaNode> multipleSelection) {
        for (GWTJahiaNode gwtJahiaNode : multipleSelection) {
            if (!Boolean.TRUE.equals(gwtJahiaNode.get("supportsPublication"))) {
                return false;
            }
        }
        return true;
    }
}
