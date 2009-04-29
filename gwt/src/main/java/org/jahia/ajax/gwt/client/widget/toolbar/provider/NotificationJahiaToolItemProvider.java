/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.monitor.GWTJahiaStateInfo;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.util.ToolbarConstants;

import java.util.Map;

/**
 * User: jahia
 * Date: 7 ao�t 2008
 * Time: 14:09:47
 */
public class NotificationJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    private GWTJahiaStateInfo gwtJahiaStateInfo;
    private final static int MAX_AJAX_CALL = 50;
    private int attempts = 0;


    public ToolItem createNewToolItem(final GWTJahiaToolbarItem gwtToolbarItem) {
        final TextToolItem toolbarItem = new TextToolItem();
        toolbarItem.setEnabled(false);
        final Map preferences = gwtToolbarItem.getProperties();

        // get refresh time
        final GWTJahiaProperty refreshTimeProp = (GWTJahiaProperty) preferences.get(ToolbarConstants.NOTIFICATION_REFRESH_TIME);
        int refreshTime = 5000;
        if (refreshTimeProp != null) {
            try {
                refreshTime = Integer.parseInt(refreshTimeProp.getValue());
            } catch (NumberFormatException e) {
                Log.error("Refresh time value[" + refreshTime + "] is not an integer.");
            }
        }

        // timer
        final Timer timer = createStateInfoTimer(toolbarItem);

        // Schedule the timer to run each "timerRefresh/1000" seconds.
        timer.scheduleRepeating(refreshTime);

        return toolbarItem;
    }

    private Timer createStateInfoTimer(final TextToolItem toolbarItem) {
        Log.debug("create notification Info timer");
        final Timer timer = new Timer() {
            public void run() {
                ToolbarService.App.getInstance().updateGWTJahiaStateInfo(getJahiaGWTPageContext(), gwtJahiaStateInfo, new AsyncCallback<GWTJahiaStateInfo>() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to update pdisplay info timer", throwable);
                        toolbarItem.setIconStyle("gwt-toolbar-ItemsGroup-icons-notification-error");
                        attempts++;
                        if (attempts > 5) {
                            cancel();
                            Log.debug("5 attempds without success --> notification timer stopped.", throwable);
                        }
                    }

                    public void onSuccess(final GWTJahiaStateInfo currentGWTJahiaStateInfo) {
                        if (currentGWTJahiaStateInfo != null) {
                            toolbarItem.setIconStyle(currentGWTJahiaStateInfo.getIconStyle());
                            toolbarItem.setToolTip(currentGWTJahiaStateInfo.getText());

                            // current user job ended
                            if (currentGWTJahiaStateInfo.isCurrentUserJobEnded()) {
                                String alertMessage = "<a href=\"#\" onclick=\"window.open('" + currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobReportUrl() + "','report','width=700,height=500')\">" + currentGWTJahiaStateInfo.getAlertMessage() + "</a>";
                                String title = currentGWTJahiaStateInfo.getGwtProcessJobInfo().getLastTitle() ;
                                if (title == null || title.length() == 0) {
                                    title = "" ;
                                } else {
                                    title = " (" + title + ")" ;
                                }
                                InfoConfig infoConfig = new InfoConfig(currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobType() + title, alertMessage);
                                infoConfig.display = currentGWTJahiaStateInfo.getDisplayTime();
                                infoConfig.height = 75;
                                infoConfig.listener = new Listener() {
                                    public void handleEvent(BaseEvent event) {
                                        Window.open(currentGWTJahiaStateInfo.getGwtProcessJobInfo().getJobReportUrl(), "report", "width=700,height=500");
                                    }
                                };
                                Info info = new Info();
                                if (GXT.isIE6) {
                                    info.setStyleAttribute("position", "absolute");
                                } else {
                                    info.setStyleAttribute("position", "fixed");
                                }
                                info.display(infoConfig);
                            }

                            // need refreah
                            if (currentGWTJahiaStateInfo.isNeedRefresh()) {
                                toolbarItem.setEnabled(true);
                                String message = "<a href=\"#\" onclick=\"location.reload() ;\">" + currentGWTJahiaStateInfo.getRefreshMessage() + "</a>";
                                InfoConfig infoConfig = new InfoConfig("", message);
                                infoConfig.display = 20000;
                                infoConfig.height = 75;
                                Info info = new Info();
                                if (GXT.isIE6) {
                                    info.setStyleAttribute("position", "absolute");
                                } else {
                                    info.setStyleAttribute("position", "fixed");
                                }
                                info.display(infoConfig);
                                // know that the user need to do a refresh
                                cancel();
                            } else {
                                toolbarItem.setEnabled(false);
                            }

                            gwtJahiaStateInfo = currentGWTJahiaStateInfo;
                            attempts = 0;

                        }


                    }
                });
            }
        };
        return timer;
    }

    /**
     * Executed when the item is clicked
     *
     * @param gwtToolbarItem
     * @return
     */
    public SelectionListener<ComponentEvent> getSelectListener(GWTJahiaToolbarItem gwtToolbarItem) {
        return new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                ToolbarService.App.getInstance().updateGWTJahiaStateInfo(getJahiaGWTPageContext(), gwtJahiaStateInfo, new AsyncCallback<GWTJahiaStateInfo>() {
                    public void onFailure(Throwable throwable) {
                        Log.error("Unable to update pdisplay info timer", throwable);
                    }

                    public void onSuccess(GWTJahiaStateInfo currentGWTJahiaStateInfo) {
                        if (currentGWTJahiaStateInfo != null) {
                            if (currentGWTJahiaStateInfo.isNeedRefresh()) {
                                Window.Location.reload();
                            }
                        }
                    }
                });
            }
        };
    }
}
