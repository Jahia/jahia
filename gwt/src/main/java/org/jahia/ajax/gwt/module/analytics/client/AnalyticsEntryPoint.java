package org.jahia.ajax.gwt.module.analytics.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.widget.node.Analytics;

/**
 * Created by IntelliJ IDEA.
 * Date: 2 mars 2009
 * Time: 10:06:06
 *
* @author Ibrahim El Ghandour
 *
 */
public class AnalyticsEntryPoint implements EntryPoint {

    public void onModuleLoad() {
        RootPanel panel = RootPanel.get("jahiaanalytics") ;
        if (panel != null) {
            panel.add(new Analytics()) ;
        }
    }
}
