/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.process;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobStat;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

/**
 * User: jahia
 * Date: 28 juil. 2008
 * Time: 10:46:41
 */
public class ProcessJobTopBar extends TopBar {
    private GWTJahiaProcessJob selectedGWTJahiaProcessJob;
    private ToolBar m_compent;
    private Button infoItem;
    private Button deleteItem;

    public ProcessJobTopBar() {
    }


    public void createUI() {
        m_compent = new ToolBar();
        m_compent.setHeight(21);
        // refresh button
        Button refreshItem = new Button();
        refreshItem.setIconStyle("gwt-pdisplay-icons-refresh");
        refreshItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                getLinker().refreshTable();
            }
        });

        // refresh button
        deleteItem = new Button(Messages.getNotEmptyResource("pd_button_deletewaitingjob","Delete waiting job"));
        deleteItem.setEnabled(false);
        deleteItem.setIconStyle("gwt-pdisplay-icons-delete");
        deleteItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                ProcessDisplayService.App.getInstance().deleteJob(selectedGWTJahiaProcessJob, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to delete waiting job");
                    }

                    public void onSuccess(Object o) {

                    }
                });
            }
        });
        // preference
        Button prefItem = new Button();
        prefItem.setText(Messages.getNotEmptyResource("pd_button_preferences","Preferences"));
        prefItem.setIconStyle("gwt-pdisplay-icons-preferences");
        prefItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                final Window preferenceWindow = new Window();
                preferenceWindow.setHeading(Messages.getNotEmptyResource("pd_button_preferences","Preferences"));
                preferenceWindow.setBodyBorder(false);
                preferenceWindow.setInsetBorder(false);
                preferenceWindow.setWidth(550);
                preferenceWindow.add(createFromPanelPreference(preferenceWindow));
                preferenceWindow.show();
            }
        });

        //info
        infoItem = new Button();
        infoItem.setIconStyle("gwt-pdisplay-icons-info");
        infoItem.setText("...");

        // init refresh time
        initTimer();

        m_compent.add(refreshItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(infoItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(deleteItem);
        m_compent.add(new SeparatorToolItem());
        m_compent.add(prefItem);

    }

    /**
     * Get the UI component used by the subclass since it is not directly a subclass of a widget
     * (multiple inheritance is not supported in Java, damn).
     *
     * @return the ui component
     */
    public Component getComponent() {
        return m_compent;
    }

    public GWTJahiaProcessJob getSelectedGWTJahiaProcessJob() {
        return selectedGWTJahiaProcessJob;
    }

    public void setSelectedGWTJahiaProcessJob(GWTJahiaProcessJob selectedGWTJahiaProcessJob) {
        this.selectedGWTJahiaProcessJob = selectedGWTJahiaProcessJob;
        if (selectedGWTJahiaProcessJob.getJobType().equalsIgnoreCase("waiting")) {
            deleteItem.setEnabled(true);
        } else {
            deleteItem.setEnabled(false);
        }
    }

    /**
     * Handle new selection
     *
     * @param leftTreeSelection
     * @param topTableSelection
     */
    public void handleNewSelection(Object leftTreeSelection, Object topTableSelection) {
        GWTJahiaProcessJob jahiaProcessJob = (GWTJahiaProcessJob) topTableSelection;
        if (jahiaProcessJob != null && jahiaProcessJob.getJobType().equalsIgnoreCase("waiting")) {
            deleteItem.setEnabled(true);
        } else {
            deleteItem.setEnabled(false);
        }
    }

    /**
     * Get Process Job Preference
     *
     * @return
     */
    private GWTJahiaProcessJobPreference getGWTJahiaProcessJobPreference() {
        return getPdisplayBrowserLinker().getGwtJahiaProcessJobPreference();
    }


    /**
     * Get PdisplayBrowserLinker
     *
     * @return
     */
    private ProcessdisplayManagerLinker getPdisplayBrowserLinker() {
        return ((ProcessdisplayManagerLinker) getLinker());
    }

    /**
     * init timer
     */
    private void initTimer() {
        Timer timer = new Timer() {
            public void run() {
                ProcessDisplayService.App.getInstance().getGWTProcessJobStat(GWTJahiaProcessJobStat.TIMER_MODE, new AsyncCallback<GWTJahiaProcessJobStat>() {
                    public void onFailure(Throwable throwable) {
                        infoItem.setIconStyle("gwt-pdisplay-icons-error");
                    }

                    public void onSuccess(GWTJahiaProcessJobStat gwtJahiaProcessJobStat) {
                        if (gwtJahiaProcessJobStat != null) {
                            // handle refresh
                            Log.debug("Need Refresh --> " + gwtJahiaProcessJobStat.isNeedRefresh());
                            if (gwtJahiaProcessJobStat.isNeedRefresh()) {
                                if (getGWTJahiaProcessJobPreference().isAutoRefresh()) {
                                    getLinker().refreshTable();
                                } else {
                                    infoItem.setIconStyle("gwt-pdisplay-icons-warning");
                                    infoItem.setToolTip(Messages.getNotEmptyResource("pd_tooltip_needrefresh","Need refresh"));
                                    infoItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
                                        public void componentSelected(ButtonEvent event) {
                                            getLinker().refreshTable();
                                        }
                                    });
                                }
                            } else {
                                infoItem.setIconStyle("gwt-pdisplay-icons-info");
                            }
                            infoItem.setText(gwtJahiaProcessJobStat.getLastJobCompletedTime());
                        }


                    }
                });
            }
        };

        // Schedule the timer to run each "timerRefresh/1000" seconds.
        timer.scheduleRepeating(5000);
    }

    /**
     * Create a Form Panel
     *
     * @return
     */
    private ContentPanel createFromPanelPreference(final Window window) {
        FormPanel panel = new FormPanel();
        panel.setBodyBorder(false);
        panel.setBorders(false);
        panel.setFrame(false);
        panel.setHeaderVisible(false);
        panel.setButtonAlign(Style.HorizontalAlignment.CENTER);
        panel.setStyleAttribute("padding", "4px 4px");
        panel.setLabelWidth(170);
        panel.setFieldWidth(250);
        panel.setWidth(500);

        // refesh
        final CheckBox autoRefreshField = new CheckBox();
        autoRefreshField.setFieldLabel(Messages.getNotEmptyResource("pd_prefs_autorefresh","Auto refresh"));
        autoRefreshField.setValue(getGWTJahiaProcessJobPreference().isAutoRefresh());
        panel.add(autoRefreshField);

        // max job
        final NumberField maxJobNumberField = new NumberField();
        maxJobNumberField.setFieldLabel(Messages.getNotEmptyResource("pd_prefs_maxjobs","Max. jobs"));
        maxJobNumberField.setValue(getGWTJahiaProcessJobPreference().getMaxJobs());
        maxJobNumberField.setAllowBlank(false);
        maxJobNumberField.setAllowNegative(false);
        panel.add(maxJobNumberField);

        // jobs per page
        final NumberField jobPerPageNumber = new NumberField();
        jobPerPageNumber.setFieldLabel(Messages.getNotEmptyResource("pd_prefs_jobsperpage","Jobs per page"));
        jobPerPageNumber.setValue(getGWTJahiaProcessJobPreference().getJobsPerPage());
        jobPerPageNumber.setAllowBlank(false);
        jobPerPageNumber.setAllowNegative(false);
        panel.add(jobPerPageNumber);


        final Button saveButton = new Button(Messages.getNotEmptyResource("save", "Save"));
        saveButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent event) {
                Log.debug(" save pdisplay pref.");

                boolean autoRefresh = autoRefreshField.getValue().booleanValue();
                Log.debug(" auto refresh value: " + autoRefresh);
                int maxJobs = maxJobNumberField.getValue().intValue();
                Log.debug(" max job value: " + maxJobs);
                int jobPerPage = jobPerPageNumber.getValue().intValue();
                Log.debug(" job Per Page value: " + jobPerPage);

                // creat pref. bean and save it
                final GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences = new GWTJahiaProcessJobPreference();
                gwtJahiaProcessJobPreferences.setDataType(GWTJahiaProcessJobPreference.PREF_GENERAL);
                gwtJahiaProcessJobPreferences.setAutoRefresh(autoRefresh);
                gwtJahiaProcessJobPreferences.setMaxJobs(maxJobs);
                gwtJahiaProcessJobPreferences.setJobsPerPage(jobPerPage);
                gwtJahiaProcessJobPreferences.setRefreshAtEndOfAnyPageWorkflow(true);

                // make an ajax call to save preferences
                ProcessDisplayService.App.getInstance().savePreferences(gwtJahiaProcessJobPreferences, new AsyncCallback() {
                    public void onFailure(Throwable throwable) {
                        window.hide();
                    }

                    public void onSuccess(Object o) {
                        getPdisplayBrowserLinker().refreshPreferenceAndTable();
                        window.hide();
                    }
                });
            }
        });
        panel.addButton(saveButton);
        return panel;

    }


}
