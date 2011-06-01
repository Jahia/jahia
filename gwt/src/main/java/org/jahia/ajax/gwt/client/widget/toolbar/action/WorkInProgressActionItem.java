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

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.job.JobListWindow;

import java.util.ArrayList;
import java.util.List;

/**
 * User: toto
 * Date: Aug 30, 2010
 * Time: 8:16:07 PM
 */
public class WorkInProgressActionItem extends BaseActionItem {

    private static WorkInProgressActionItem instance;

    private List<String> statuses = new ArrayList<String>();
    private List<GWTJahiaJobDetail> processes = new ArrayList<GWTJahiaJobDetail>();

    private transient Timer timer;
    
    private boolean adminMode = true;

    @Override
    public void init(GWTJahiaToolbarItem gwtToolbarItem, Linker linker) {
        super.init(gwtToolbarItem, linker);
        instance = this;
        refreshStatus();

        timer = new Timer() {
            public void run() {
                JahiaContentManagementService.App.getInstance().getActiveJobs(new AsyncCallback<List<GWTJahiaJobDetail>>() {
                    public void onSuccess(List<GWTJahiaJobDetail> result) {
                        if (!processes.equals(result)) {
                            final ArrayList<GWTJahiaJobDetail> deleted = new ArrayList<GWTJahiaJobDetail>(processes);
                            deleted.removeAll(result);
                            processes = result;
                            refreshStatus(deleted);
                        }
                        schedule(5000);
                    }

                    public void onFailure(Throwable caught) {
                        Log.error("Cannot get jobs", caught);
                    }
                });
            }
        };
        timer.run();
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

    private void refreshStatus(List<GWTJahiaJobDetail> oldJobs) {
        refreshStatus();
        int refresh = 0;
        if (oldJobs != null) {
            for (GWTJahiaJobDetail oldJob : oldJobs) {
                if (oldJob.getGroup().equals("PublicationJob")) {
                    refresh |= Linker.REFRESH_PAGES;
                    refresh |= Linker.REFRESH_MAIN;
                }
            }
        }
        if (refresh > 0) {
            linker.refresh(refresh);
        }
    }

    private void refreshStatus() {
        Button b = (Button) getTextToolItem();
        if (statuses.isEmpty() && processes.isEmpty()) {
            b.setText(getMenuItem().getText());
            b.setIcon(getMenuItem().getIcon());
            b.setEnabled(true);
        } else if (statuses.size() == 1) {
            b.setIconStyle("x-status-busy");
            b.setText(statuses.get(0) + " ...");
            b.setEnabled(true);
        } else if (processes.size() == 1) {
            b.setIconStyle("x-status-busy");
            b.setText(processes.get(0).getLabel() + " ...");
            b.setEnabled(true);
        } else {
            b.setIconStyle("x-status-busy");
            b.setText((statuses.size() + processes.size()) + " tasks running ...");
            b.setEnabled(true);
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
