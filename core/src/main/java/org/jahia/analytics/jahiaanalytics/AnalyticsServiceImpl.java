package org.jahia.analytics.jahiaanalytics;

import org.jahia.ajax.gwt.client.service.analytics.GWTAnalyticsService;
import org.jahia.ajax.gwt.commons.server.AbstractJahiaGWTServiceImpl;
import org.jahia.services.analytics.AnalyticsService;
import org.jahia.registries.ServicesRegistry;

import java.util.Map;


/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 4 mars 2009
 * Time: 10:21:08
 *
 * @author Ibrahim El Ghandour
 * 
 */
public class AnalyticsServiceImpl extends AbstractJahiaGWTServiceImpl implements GWTAnalyticsService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AnalyticsServiceImpl.class);
    public Map<String, String> getLastNActivities(int N) {
        logger.info(" getLastNActivities ");
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getLastNactivities(N);
    }


  
    public Map<String, String> getMostNactiveUsers(int N) {
         AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getMostNactiveUsers(N);
    }

    public Map<String, String> getLeastNactiveUsers(int N) {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getLeastNactiveUsers(N);
    }

    public Map<String, String> getActivitiesPerUser(String user) {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getActivitiesPerUser(user);
    }

    public Map<String, String> getActivitiesPerObjectId(int id) {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getActivitiesPerObjectId(id);
    }

    public Map<String, String> getActivitiesPerPageUUID(String uuid) {
         AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getActivitiesPerPageUUID(uuid);
    }

    public Map<String, String> getActivitiesPerSiteKey(int id) {
       AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getActivitiesPerSiteKey(id);
    }

    public Map<String, String> executeQuery(String user, String op, String type, int objId, String uuid, int pid, int siteId,int N) {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return as.executeQuery(user,op,type,objId,uuid,pid,siteId,N);  //To change body of implemented methods use File | Settings | File Templates.
    }

  /*  public Map<String, String> executeQuery(String user, String op, String type, int objId, String uuid, int siteId) {
         AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  null;//as.executeQuery(user, op, type,  objId,  uuid,  siteId);
    }*/

    public Map<String, String> getAllUsers() {
         AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        return  as.getAllUsers();
    }

    public void flushDatabase() {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        as.flushDatabase();
    }

    public void flushDatabaseOldest() {
        AnalyticsService as = ServicesRegistry.getInstance().getAnalyticsService();
        as.flushDatabaseOldest();
    }


}
