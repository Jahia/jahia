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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Info;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.poller.Poller;
import org.jahia.ajax.gwt.client.widget.poller.TaskEvent;

/**
 *
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:51:18 PM
 */
@SuppressWarnings("serial")
public class NumberOfTasksWorkflowMenuActionItem extends BaseActionItem implements Poller.PollListener<TaskEvent> {

    private Integer numberOfTasks = null;
    private boolean displayWorkflowCounter;

    public void init(final GWTJahiaToolbarItem gwtToolbarItem, final Linker linker) {
        super.init(gwtToolbarItem, linker);
        if (displayWorkflowCounter) {
            JahiaContentManagementService.App.getInstance().getNumberOfTasksForUser(new BaseAsyncCallback<Integer>() {
                public void onSuccess(Integer result) {
                    numberOfTasks = result;
                    updateLabel(result);
                }
            });
            Poller.getInstance().registerListener(this, TaskEvent.class);
        }
    }

    public void handlePollingResult(TaskEvent result) {
        if (result.getNewTask() != null) {
            Info.display(Messages.get("label.tasks.new", "You have a new task to do"), result.getNewTask());
            if (numberOfTasks != null) {
                numberOfTasks++;
                updateLabel(numberOfTasks);
            }
        }
        if (result.getEndedTask() != null) {
            if (numberOfTasks != null && numberOfTasks > 0) {
                numberOfTasks--;
                updateLabel(numberOfTasks);
            }
        }
        if (result.getEndedWorkflow() != null) {
            Info.display(Messages.get("label.workflow.ended", "A workflow has ended"), result.getEndedWorkflow());
        }
    }

    private void updateLabel(Integer nb) {
        numberOfTasks = nb;
        if (nb == 0) {
            updateTitle(Messages.get("label.numberoftasksforuser.notasks", "No Waiting tasks"));
        } else {
            updateTitle(getGwtToolbarItem().getTitle() + " (" + nb + ")");
        }
    }

    /**
     * Called when there is a new liker selection. Override this method to provide custom behaviour
     */
    @Override
    public void handleNewLinkerSelection() {
        setEnabled(false);
    }

    public boolean isDisplayWorkflowCounter() {
        return displayWorkflowCounter;
    }

    public void setDisplayWorkflowCounter(boolean displayWorkflowCounter) {
        this.displayWorkflowCounter = displayWorkflowCounter;
    }
}
