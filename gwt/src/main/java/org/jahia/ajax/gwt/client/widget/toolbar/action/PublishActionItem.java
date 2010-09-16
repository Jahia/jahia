/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflow;
import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowType;
import org.jahia.ajax.gwt.client.widget.edit.EditActions;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.widget.edit.PublicationWorkflow;
import org.jahia.ajax.gwt.client.widget.edit.workflow.dialog.WorkflowActionDialog;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:58:56 PM
* To change this template use File | Settings | File Templates.
*/
public class PublishActionItem extends BaseActionItem {
    private transient GWTJahiaWorkflow wf;
    private transient GWTJahiaNode gwtJahiaNode;

    public void onComponentSelection() {
        if (wf == null) {
            EditActions.publish(linker, false);
        } else  {
            WorkflowActionDialog dialog = new WorkflowActionDialog(gwtJahiaNode, linker);
            dialog.setCustom(wf.getCustomWorkflowInfo());
            dialog.initExecuteActionDialog(wf.getAvailableTasks().get(0));
            dialog.show();
        }
    }

    /**
     * Init the action item.
     *
     * @param gwtToolbarItem
     * @param linker
     */
    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);    //To change body of overridden methods use File | Settings | File Templates.
        setEnabled(false);
    }

    public void handleNewLinkerSelection() {
        gwtJahiaNode = linker.getSelectedNode();
        if (gwtJahiaNode == null) {
            gwtJahiaNode = linker.getMainNode();
        }
        if (gwtJahiaNode != null) {
            wf = gwtJahiaNode.getWorkflowInfo().getActiveWorkflows().get(new GWTJahiaWorkflowType("publish"));
            if (wf != null && !wf.getAvailableTasks().isEmpty()) {
                wf.getAvailableTasks().get(0).getDisplayName();
                setEnabled(true);
                updateTitle(wf.getAvailableTasks().get(0).getDisplayName() + " : " + gwtJahiaNode.getName());
            } else {
                wf = null;
                GWTJahiaPublicationInfo info = gwtJahiaNode.getPublicationInfo();

                Set<Integer> status = new HashSet<Integer>(info.getSubnodesStatus());
                status.addAll(info.getReferencesStatus());
                status.add(info.getStatus());
                
                setEnabled(!gwtJahiaNode.isLanguageLocked(JahiaGWTParameters.getLanguage()) && info.isCanPublish() &&
                        (status.contains(GWTJahiaPublicationInfo.NOT_PUBLISHED) || status.contains(GWTJahiaPublicationInfo.MODIFIED) || status.contains(GWTJahiaPublicationInfo.UNPUBLISHED)));
                updateTitle(getGwtToolbarItem().getTitle() + " " + gwtJahiaNode.getName());
            }
        }
    }
}
