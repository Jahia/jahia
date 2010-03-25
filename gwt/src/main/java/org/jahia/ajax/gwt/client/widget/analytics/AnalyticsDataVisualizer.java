package org.jahia.ajax.gwt.client.widget.analytics;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;

import java.util.Date;
import java.util.List;

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
    private int view = LABEL_VIEW;


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
        mainContainer = new LayoutContainer();

        add(mainContainer);
        final ToolBar toolbar = new ToolBar();
        final DateField startDate = new DateField();
        final DateField endDate = new DateField();

        final Button textButton = new Button("Text");
        final Button worldMapButton = new Button("Geo map");
        final Button annotatedTimeLineButton = new Button("Annotated Time Line");

        textButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = LABEL_VIEW;
                oneGeoMapSelected();
            }
        });
        worldMapButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = GOOGLE_MAP_VIEW;
                oneGeoMapSelected();
            }
        });
        annotatedTimeLineButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                view = ANNOTATED_TIME_LINE;
                oneAnnotatedTimeLineSelected();
            }
        });
        toolbar.add(new Label("Start date: "));
        toolbar.add(startDate);
        toolbar.add(new Label("End date"));
        toolbar.add(endDate);
        toolbar.add(textButton);
        toolbar.add(worldMapButton);
        toolbar.add(annotatedTimeLineButton);

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
        mainContainer.setLayout(new RowLayout());
        for (GWTJahiaAnalyticsData data : dataList) {
            Html html = new Html("County: " + data.getCountry() + ", Number of views: " + data.getValue());
            mainContainer.add(html);
        }
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
        return data.getCountry();
    }

    /**
     * Get value at the specified index
     *
     * @param dataIndex
     * @return
     */
    public double getValue(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return data.getValue();
    }

    /**
     * Get data index
     *
     * @param dataIndex
     * @return
     */
    public String getDate(int dataIndex) {
        GWTJahiaAnalyticsData data = dataList.get(dataIndex);
        return data.getDate();
    }

    /**
     * Display geo map
     */
    private native void displayGeoMap(Element element, int size)
        /*-{
            if(!$wnd.google && !wnd.google.visualisation) {
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

              var value = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getValue(I)(i);
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

        if(!$wnd.google && !wnd.google.visualisation)
           {
               return;
           }
        var data     = new $wnd.google.visualization.DataTable();

        data.addColumn('date', 'Date');
        data.addColumn('number', "Views");
        data.addRows(size);
        for(var i=0; i < size ; i++)
        {

            var date = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getDate(I)(i);


            data.setValue(i, 0, new Date(date));

             var value = this.@org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer::getValue(I)(date);
             data.setValue(i, 1, value);


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
