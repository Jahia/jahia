package org.jahia.ajax.gwt.client.service.analytics;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 4 mars 2009
 * Time: 10:29:39
 *
 * @author Ibrahim El Ghandour
 */
public interface GWTAnalyticsServiceAsync {
    public void getLastNSystemErrors(int N, AsyncCallback<Map<String, String>> async);

    public void getLastNActivities(int N, AsyncCallback<Map<String, String>> async);

    public void getMostNactiveUsers(int N,AsyncCallback<Map<String, String>> asyncCallback);

    public void getLeastNactiveUsers(int N,AsyncCallback<Map<String, String>> asyncCallback);

    public void getActivitiesPerUser(String user, AsyncCallback<Map<String, String>> asyncCallback);

    public void getActivitiesPerObjectId(int id, AsyncCallback<Map<String, String>> asyncCallback);

    public void getActivitiesPerPageUUID(String uuid, AsyncCallback<Map<String, String>> asyncCallback);

    public void getActivitiesPerSiteKey(int id, AsyncCallback<Map<String, String>> asyncCallback);

    public void executeQuery(String user, String op, String type, int objId, String uuid,int pid, int siteId,int N,AsyncCallback<Map<String, String>> asyncCallback);

    public void getAllUsers(AsyncCallback<Map<String, String>> asyncCallback);

    public void flushDatabase(AsyncCallback<Void> asyncCallback);

    public void flushDatabaseOldest(AsyncCallback<Void> asyncCallback);


}
