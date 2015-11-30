/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.poller.Poller;
import org.jahia.ajax.gwt.client.widget.job.JobListWindow;
import org.jahia.ajax.gwt.client.widget.poller.ProcessPollingEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: toto
 * Date: Aug 30, 2010
 * Time: 8:16:07 PM
 */
@SuppressWarnings("serial")
public class WorkInProgressActionItem extends BaseActionItem implements Poller.PollListener<ProcessPollingEvent> {

    private static WorkInProgressActionItem instance;

    private List<String> statuses = new ArrayList<String>();
    private List<GWTJahiaJobDetail> lastStartedJobs = new ArrayList<GWTJahiaJobDetail>();
    private List<GWTJahiaJobDetail> lastEndedJobs = new ArrayList<GWTJahiaJobDetail>();
    private int processesCount = 0;

    private boolean adminMode = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        instance = this;
        refreshStatus();

        Poller.getInstance().registerListener(this, ProcessPollingEvent.class);
    }

    public void handlePollingResult(ProcessPollingEvent result) {
        lastStartedJobs = result.getStartedJob();
        lastEndedJobs = result.getEndedJob();

        if (!lastStartedJobs.isEmpty() || !lastEndedJobs.isEmpty() || processesCount != result.getTotalCount()) {
            processesCount = result.getTotalCount();

            refreshStatus();
        }
    }

    public static void removeStatus(String status) {
        if(instance!=null) {
            instance.statuses.remove(status);
            instance.refreshStatus();
        }
    }

    public static void setStatus(String status) {
        if(instance!=null) {
            instance.statuses.add(status);
            instance.refreshStatus();
        }
    }

    private void refreshStatus() {
        Button b = (Button) getTextToolItem();
        if (statuses.isEmpty() && processesCount == 0) {
            b.setText(getMenuItem().getHtml());
            b.setIcon(getMenuItem().getIcon());
            b.setEnabled(true);
        } else if (statuses.size() == 1) {
            b.setIconStyle("x-status-busy");
            b.setText(statuses.get(0) + " ...");
            b.setEnabled(true);
        } else if (processesCount == 1 && lastStartedJobs.size() == 1) {
            b.setIconStyle("x-status-busy");
            b.setText(Messages.get(lastStartedJobs.get(0).getLabelKey()) + " ...");
            b.setEnabled(true);
        } else {
            b.setIconStyle("x-status-busy");
            b.setText((statuses.size() + processesCount) + " " + Messages.get("label.tasksExecuting", "tasks running") + " ...");
            b.setEnabled(true);
        }

        Map<String, Object> refreshData = new HashMap<String, Object>();

        if (!lastEndedJobs.isEmpty()) {
            for (GWTJahiaJobDetail endedJob : lastEndedJobs) {
                if (endedJob.getSite() == null || endedJob.getSite().equals(JahiaGWTParameters.getSiteKey())) {
                    if (endedJob.getGroup().equals("PublicationJob")) {
                        if (endedJob.getUser().equals(JahiaGWTParameters.getCurrentUser())) {
                            refreshData.put("publishedNodes", endedJob.getTargetPaths());
                        }
                        refreshData.put("event", "publicationSuccess");
                    }
                }
            }
        }
        if (!refreshData.isEmpty()) {
            linker.refresh(refreshData);
        }
    }

    @Override
    public Component createNewToolItem() {
        Button b = new Button();
        b.setEnabled(false);
        return b;
    }

    public void onComponentSelection() {
        new JobListWindow(linker, adminMode).show();
    }

	public void setAdminMode(boolean adminMode) {
    	this.adminMode = adminMode;
    }

}
