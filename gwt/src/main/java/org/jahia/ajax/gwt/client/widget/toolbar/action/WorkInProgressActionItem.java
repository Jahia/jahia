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
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 30, 2010
 * Time: 8:16:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkInProgressActionItem extends BaseActionItem {

    private static WorkInProgressActionItem instance;

    private List<String> statuses = new ArrayList<String>();
    private List<GWTJahiaJobDetail> processes = new ArrayList<GWTJahiaJobDetail>();

    private transient Timer timer;


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
                            processes = result;
                            refreshStatus();
                        }
                        scheduleRepeating(1000);
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
        instance.statuses.remove(status);
        instance.refreshStatus();
    }

    public static void setStatus(String status) {
        instance.statuses.add(status);
        instance.refreshStatus();
    }

    private void refreshStatus() {
        Button b = (Button) getTextToolItem();
        if (statuses.isEmpty() && processes.isEmpty()) {
            b.setText("View completed jobs");
            b.setIconStyle(null);
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
        JobListWindow.showJobListWindow(linker);
    }

}
