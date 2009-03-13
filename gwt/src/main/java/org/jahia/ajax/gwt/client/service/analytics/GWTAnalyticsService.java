package org.jahia.ajax.gwt.client.service.analytics;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.core.client.GWT;

import java.util.Map;

import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.util.URL;

/**
 * Created by IntelliJ IDEA.
 * Date: 4 mars 2009
 * Time: 10:14:35
 *
 * @author Ibrahim El Ghandour
 */
public interface GWTAnalyticsService extends RemoteService {

    public static class App {
        private static GWTAnalyticsServiceAsync ourInstance = null;


        public static synchronized GWTAnalyticsServiceAsync getInstance() {
            if (ourInstance == null) {
                String relativeServiceEntryPoint = JahiaGWTParameters.getServiceEntryPoint() + "analytics/";
                String serviceEntryPoint = URL.getAbsolutleURL(relativeServiceEntryPoint);
                ourInstance = (GWTAnalyticsServiceAsync) GWT.create(GWTAnalyticsService.class);
                ((ServiceDefTarget) ourInstance).setServiceEntryPoint(serviceEntryPoint);
            }

            return ourInstance;
        }

    }

    public Map<String, String> getLastNActivities(int N);

    public Map<String,String> getMostNactiveUsers(int N);

    public Map<String,String> getLeastNactiveUsers(int N);

    public Map<String, String> getActivitiesPerUser(String user);

    public Map<String, String> getActivitiesPerObjectId(int id);

    public Map<String, String> getActivitiesPerPageUUID(String uuid);

    public Map<String, String> getActivitiesPerSiteKey(int id);

    public Map<String, String> executeQuery(String user, String op, String type, int objId,String uuid,int pid, int siteId,int N);

    public Map<String, String> getAllUsers();

    public void flushDatabase();

    public void flushDatabaseOldest();
}
