package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsData;
import org.jahia.ajax.gwt.client.data.analytics.GWTJahiaAnalyticsQuery;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.analytics.AnalyticsDataVisualizer;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 24, 2010
 * Time: 11:19:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnalyticsTabItem extends EditEngineTabItem {
    private static JahiaContentManagementServiceAsync service = JahiaContentManagementService.App.getInstance();
    private AnalyticsDataVisualizer dataVisualizer;
    private GWTJahiaAnalyticsQuery lastQuery;

    public AnalyticsTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_analytics", "Analytics"), engine);
    }

    public AnalyticsTabItem(String title, AbstractContentEngine engine) {
        super(title, engine);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() == null) {
            return;
        } else {
            lastQuery = new GWTJahiaAnalyticsQuery();
            lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:country");
            lastQuery.setNode(engine.getNode());
            display(locale);
        }
        layout();
    }

    /**
     * render chart
     *
     * @param locale
     */
    private void display(GWTJahiaLanguage locale) {
        // init data visulaiser
        if (dataVisualizer == null) {
            dataVisualizer = new AnalyticsDataVisualizer() {
                public void oneDateChanged(Date newStartDate, Date newEndDate) {
                    lastQuery.setStartDate(newStartDate);
                    lastQuery.setStartDate(newEndDate);
                    loadData(lastQuery);
                }

                @Override
                public void oneGeoMapSelected() {
                    lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:country");
                    loadData(lastQuery);
                }

                @Override
                public void oneAnnotatedTimeLineSelected() {
                    lastQuery.setDimensions("ga:pageTitle,ga:pagePath,ga:date");
                    loadData(lastQuery);
                }
            };
            add(dataVisualizer);
        }

        // load date
        loadData(lastQuery);

    }

    /**
     * load data
     */
    private void loadData(GWTJahiaAnalyticsQuery query) {
        // get data
        service.getAnalyticsData(query, new AsyncCallback<List<GWTJahiaAnalyticsData>>() {
            public void onSuccess(List<GWTJahiaAnalyticsData> dataList) {
                dataVisualizer.setDataList(dataList);
                dataVisualizer.refreshUI();
            }

            public void onFailure(Throwable throwable) {
                Log.error("Error while getting analytics data", throwable);
            }
        });
    }


}
