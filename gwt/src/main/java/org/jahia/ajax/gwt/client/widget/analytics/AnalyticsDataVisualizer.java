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

package org.jahia.ajax.gwt.client.widget.analytics;

import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.store.GroupingStore;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.grid.*;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsProfile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 2:49:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsDataVisualizer extends ContentPanel {
    private List<GWTJahiaAnalyticsData> dataList;
    private LayoutContainer mainContainer;
    public static final int LABEL_VIEW = 0;
    public static final int GOOGLE_MAP_VIEW = 1;

    public static final int ANNOTATED_TIME_LINE = 2;
    private int view = ANNOTATED_TIME_LINE;


    public AnalyticsDataVisualizer() {
        init();
    }

    public AnalyticsDataVisualizer(List<GWTJahiaAnalyticsData> datas) {
        this.dataList = datas;
        init();
        refreshUI();
    }

    /**
     * init method
     */
    private void init() {
        setLayout(new FillLayout());
        setHeaderVisible(false);
        setBodyBorder(false);
        setBorders(false);
        mainContainer = new LayoutContainer();

        add(mainContainer);
        // start date / end date
        final DateField startDate = new DateField();
        final DateField endDate = new DateField();

        startDate.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent p_event) {
                oneDateChanged(startDate.getValue(), endDate.getValue());
            }
        });

        endDate.addListener(Events.Change, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent p_event) {
                oneDateChanged(startDate.getValue(), endDate.getValue());
            }
        });

        // views
        final Button textButton = new Button("Text");
        final Button worldMapButton = new Button("Geo map");
        final Button annotatedTimeLineButton = new Button("Annotated Time Line");

        annotatedTimeLineButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = ANNOTATED_TIME_LINE;
                oneAnnotatedTimeLineSelected();
            }
        });
        textButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = LABEL_VIEW;
                onTextButtonSelected();
            }
        });
        worldMapButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = GOOGLE_MAP_VIEW;
                oneGeoMapSelected();
            }
        });

        // create toolbar
        final ToolBar toolbar = new ToolBar();
        toolbar.add(new Label(" Start date: "));
        toolbar.add(startDate);
        toolbar.add(new Label(" End date"));
        toolbar.add(endDate);
        toolbar.add(annotatedTimeLineButton);
        toolbar.add(textButton);
        toolbar.add(worldMapButton);

        displayDataNotAvailble(null);
        setTopComponent(toolbar);
    }

    /**
     * display "notAvailable message"
     *
     * @param message
     */
    public void displayDataNotAvailble(String message) {
        if (message != null) {
            message = "Data not available: " + message;
        } else {
            message = "Data not available";
        }
        Html html = new Html(message);
        mainContainer.add(html);
        mainContainer.layout();
    }


    /**
     * refresh ui
     */
    public void refreshUI() {
        mainContainer.removeAll();
        switch (view) {
            case LABEL_VIEW: {
                displayTextView();
                break;
            }
            case GOOGLE_MAP_VIEW: {
                mainContainer.setLayout(new FillLayout());
                displayGeoMap(mainContainer.getElement(), dataList.size());
                break;
            }
            case ANNOTATED_TIME_LINE: {
                mainContainer.setLayout(new FillLayout());
                displayAnnotatedTimeLine(mainContainer.getElement(), dataList.size());
                break;
            }
            default: {
                displayTextView();
                break;
            }
        }
        mainContainer.layout();
        layout();
    }

    /**
     * Display as text view
     */
    private void displayTextView() {
        mainContainer.setLayout(new FillLayout());

        GroupingStore<GWTJahiaAnalyticsData> store = new GroupingStore<GWTJahiaAnalyticsData>();
        store.add(dataList);
        List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        for (Map.Entry entry : dataList.get(0).getProperties().entrySet()) {
            ColumnConfig  column = new ColumnConfig();
            column.setId((String) entry.getKey());
            column.setHeader((String) entry.getKey());
            column.setWidth(100);
            configs.add(column);
        }
        // Grouping view
        final Grid<GWTJahiaAnalyticsData> grid = new Grid<GWTJahiaAnalyticsData>(store, new ColumnModel(configs));
        mainContainer.add(grid);
        mainContainer.layout();
    }

    /**
     * set data list
     *
     * @param dataList
     */
    public void setDataList(List<GWTJahiaAnalyticsData> dataList) {
        this.dataList = dataList;
    }

    /**
     * Called on profile changed
     * @param profile
     */
    public void oneProfileChanged(GWTJahiaAnalyticsProfile profile) {
    }

    /**
     * Call on date changes
     *
     * @param newStartDate
     * @param newEndDate
     */
    public void oneDateChanged(Date newStartDate, Date newEndDate) {
    }

    /**
     * before switching to geo map
     */
    public void oneGeoMapSelected() {
    }

    /**
     * before switching to text
     */
    public void onTextButtonSelected() {
    }

    /**
     * before switching to chart
     */
    public void oneAnnotatedTimeLineSelected() {
    }

    /**
     * Get data country at the specified index
     *
     * @param dataIndex
     * @return
     */
    public String getCountry(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return data.get("country");
    }

    /**
     * Get value at the specified index
     *
     * @param dataIndex
     * @return
     */
    public int getVisits(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return Integer.parseInt((String) data.get("visits"));
    }

    public int getPageviews(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return Integer.parseInt((String) data.get("pageviews"));
    }


    /**
     * Get data index
     *
     * @param dataIndex
     * @return
     */
    public String getDate(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return data.get("date");
    }

    /**
     * Display geo map
     */
    private native void displayGeoMap(Element element, int size)
        /*-{
            if(!$wnd.google && !$wnd.google.visualisation) {
               return;
           }
           var data = new $wnd.google.visualization.DataTable();
          data.addRows(size);
          data.addColumn('string', 'Country');
          data.addColumn('number', 'Views');

          for(var i=0; i < size ; i++)
          {

              var country = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getCountry(I)(i);
              data.setValue(i, 0, country);

              var value = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getVisits(I)(i);
              data.setValue(i, 1, value);
          }

          var options = {};
          options['dataMode'] = 'regions';
          options['wmode'] = 'transparent';
          var geomap = new $wnd.google.visualization.GeoMap(element);
          geomap.draw(data, options);
        }-*/;

    /*
   * Native method used to display an annotated time line
   * */

    public native void displayAnnotatedTimeLine(Element element, int size)
        /*-{

        if(!$wnd.google && !$wnd.google.visualisation)
           {
               return;
           }
        var data  = new $wnd.google.visualization.DataTable();

        data.addColumn('date', 'Date');
        data.addColumn('number', 'Views');

        data.addRows(size);
        for(var i=0; i < size ; i++)
        {
            var date = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getDate(I)(i);
            data.setCell(
                    i,
                    0,
                    new $wnd.Date(date.substr(0,4),date.substr(4,2) - 1,date.substr(6,2)));
            var value = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getPageviews(I)(i);
            data.setCell(i, 1, value);
        }

         var options={};
         options['displayAnnotations']= true;
         options['annotationsWidth'] = 5;
         options['scaleType'] = 'maximize';
         options['wmode'] = 'transparent';
         var chart = new $wnd.google.visualization.AnnotatedTimeLine(element);
         chart.draw(data, options);

        }-*/;
}
