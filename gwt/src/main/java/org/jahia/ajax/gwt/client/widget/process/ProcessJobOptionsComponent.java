/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.process;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.service.process.ProcessDisplayService;
import org.jahia.ajax.gwt.client.data.process.GWTJahiaProcessJobPreference;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.util.ResourceBundle;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.DataListEvent;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * User: jahia
 * Date: 24 juil. 2008
 * Time: 11:34:02
 */
public class ProcessJobOptionsComponent extends LeftComponent {
    private ContentPanel m_component;
    private DataList typeDataList = new DataList();
    private DataList statusDataList = new DataList();
    private DataList userDataList = new DataList();


    public ProcessJobOptionsComponent() {
    }


    /**
     * Create main UI
     */
    public void createUI() {

        // add form to main panel
        m_component = new ContentPanel(new AccordionLayout());

        // filter by type
        typeDataList = createOptionDataList(ProcessDisplayPanel.TYPE_OPTIONS_NAMES, ProcessDisplayPanel.TYPE_OPTIONS_VALUES, getGWTJahiaProcessJobPreference().getJobsTypeToIgnore());
        ContentPanel contentPanel = createFilteringContentPanel(ResourceBundle.getResource("pdisplay", "pdisplay", "pd_filter_type"), typeDataList);
        m_component.add(contentPanel);

        // filter by status
        statusDataList = createOptionDataList(ProcessDisplayPanel.STATUS_OPTIONS_NAMES, ProcessDisplayPanel.STATUS_OPTIONS_VALUES, getGWTJahiaProcessJobPreference().getJobsStatusToIgnore());
        contentPanel = createFilteringContentPanel(ResourceBundle.getResource("pdisplay", "pdisplay", "pd_filter_status"), statusDataList);
        m_component.add(contentPanel);

        // filter by user
        List<String> onlyCurrentUser = new ArrayList<String>();
        if (getGWTJahiaProcessJobPreference().isOnlyCurrentUser()) {
            onlyCurrentUser.add(ProcessDisplayPanel.OWNER_OPTIONS_VALUES[0]);
        } 
        /*userDataList = createOptionDataList(ProcessDisplayPanel.OWNER_OPTIONS_NAMES, ProcessDisplayPanel.OWNER_OPTIONS_VALUES, onlyCurrentUser);
        contentPanel = createFilteringContentPanel(ProcessDisplayEntryPoint.getResource("pd_filter_owner"), userDataList);
        m_component.add(contentPanel); */
    }

    /**
     * Create a filtering content panel
     *
     * @param type
     * @param dataList
     * @return
     */
    private ContentPanel createFilteringContentPanel(final String type, final DataList dataList) {
        final ContentPanel filterContentPanel = new ContentPanel(new FitLayout());
        filterContentPanel.setTitleCollapse(true);
        filterContentPanel.setHeading(type);
        filterContentPanel.add(dataList);
        return filterContentPanel;
    }


    public Object getSelectedItem() {
        // not implemented: useless
        return null;
    }

    public void refresh() {
        // To Do: get value from preferences
    }

    private GWTJahiaProcessJobPreference getGWTJahiaProcessJobPreference() {
        return ((ProcessdisplayBrowserLinker) getLinker()).getGwtJahiaProcessJobPreference();
    }

    /**
     * @param item
     */
    public void openAndSelectItem(Object item) {
        // not implemented: useless
    }

    /**
     * Get main componebt
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }

    /**
     * Create an option dataList
     *
     * @param name
     * @param values
     * @return
     */
    private DataList createOptionDataList(String[] name, String[] values, final List<String> selectedValues) {
        final DataList optionsDataList = new DataList() {
            @Override
            protected void onClick(DataListItem dataListItem, DataListEvent dataListEvent) {
                super.onClick(dataListItem, dataListEvent);
                savePreferences();
            }
        };
        optionsDataList.setSelectionMode(Style.SelectionMode.SINGLE);
        optionsDataList.setCheckable(true);
        optionsDataList.setFlatStyle(true);
        for (int i = 0; i < name.length; i++) {
            final DataListItem item = new DataListItem();
            item.setText(name[i]);
            item.setItemId(values[i]);
            if (selectedValues != null && selectedValues.contains(values[i])) {
                item.setChecked(false);
            } else {
                item.setChecked(true);
            }
            optionsDataList.add(item);
        }

        return optionsDataList;
    }

    /**
     * Save preferences
     */
    private void savePreferences() {
        // default preference value
        int maxJobs = 100;
        List<String> jobsTypeToIgnore = new ArrayList<String>();
        List<String> jobsStatusToIgnore = new ArrayList<String>();
        boolean onlyCurrentUser = false;

        // get selected values
        List<DataListItem> items = typeDataList.getItems();
        for (DataListItem item : items) {
            if (!item.isChecked()) {
                jobsTypeToIgnore.add(item.getItemId());

            }
        }
        // selected status
        items = statusDataList.getItems();
        for (DataListItem item : items) {
            if (!item.isChecked()) {
                jobsStatusToIgnore.add(item.getItemId());
            }
        }

        // selecte by owner
        items = userDataList.getSelectedItems();
        for (DataListItem item : items) {
            String realValue = item.getItemId();
            // case of type
            if (realValue.equalsIgnoreCase(ProcessDisplayPanel.OWNER_OPTIONS_VALUES[0])) {
                onlyCurrentUser = true;
            }
        }

        // creat pref. bean and save it
        final GWTJahiaProcessJobPreference gwtJahiaProcessJobPreferences = new GWTJahiaProcessJobPreference();
        gwtJahiaProcessJobPreferences.setDataType(GWTJahiaProcessJobPreference.PREF_FILTER);
        gwtJahiaProcessJobPreferences.setMaxJobs(maxJobs);
        gwtJahiaProcessJobPreferences.setJobsStatusToIgnore(jobsStatusToIgnore);
        gwtJahiaProcessJobPreferences.setJobsTypeToIgnore(jobsTypeToIgnore);
        gwtJahiaProcessJobPreferences.setOnlyCurrentUser(onlyCurrentUser);
        // make an ajax call to save preferences
        ProcessDisplayService.App.getInstance().savePreferences(gwtJahiaProcessJobPreferences, new AsyncCallback() {
            public void onFailure(Throwable throwable) {
                Log.error("Error when saving pref.");
            }

            public void onSuccess(Object o) {
                getLinker().refreshTable();
            }
        });
    }


}

