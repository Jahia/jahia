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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.analytics;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageInfo;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaDBUser;
import org.jahia.services.usermanager.JahiaDBGroup;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaErrorEvent;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.hibernate.model.JahiaCategory;
import org.jahia.bin.errors.ErrorEventFilter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import java.sql.*;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * Date: 25 févr. 2009
 * Time: 09:41:49
 *
 * @author Ibrahim El Ghandour
 */
public class JAMonAnalyticsService extends AnalyticsService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JAMonAnalyticsService.class);
    private String prop = " default ";
    private JdbcTemplate jdbcTemplate;


    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void trackEvent(JahiaEvent je, int type, String operation) {

        String username = "";

        int siteId = je.getProcessingContext().getSiteID();
        //String siteKey = je.getProcessingContext().getSiteKey();
        //String servername = je.getProcessingContext().getServerName();

        int pid = je.getProcessingContext().getPageID();
        //String pageTitle = je.getProcessingContext().getPage().getTitle();


        int objectId = -1;
        String objectType = "";
        String uuid = "nouuid";

        //logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        switch (type) {
            case 1: { //  event on site
                //todo to be implemented
                break;
            }
            case 2: { //  event on field
                JahiaField field = (JahiaField) je.getObject();
                uuid = getUUID(je);
                username = je.getProcessingContext().getTheUser().getUsername();
                try {
                    objectType = ((field.getDefinition().getName()).split("_"))[field.getDefinition().getName().split("_").length - 1];
                    objectId = field.getID();
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
            }
            case 3: { //  event on container
                JahiaContainer container = (JahiaContainer) je.getObject();
                uuid = getUUID(je);
                username = je.getProcessingContext().getTheUser().getUsername();
                try {
                    objectType = ((container.getDefinition().getName()).split("_"))[container.getDefinition().getName().split("_").length - 1];
                    objectId = container.getID();
                } catch (JahiaException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                break;
            }
            case 4: { //  event on page
                username = je.getProcessingContext().getTheUser().getUsername();
                if (operation.equals("accepted") || operation.equals("rejected")) {
                    JahiaPageInfo pageInfo = (JahiaPageInfo) je.getObject();
                    objectId = pageInfo.getID();
                    objectType = "info";
                } else {
                    JahiaPage page = (JahiaPage) je.getObject();
                    objectId = page.getID();
                    objectType = "page";
                }
                uuid = getUUID(je);
                break;
            }
            case 5: { //  event on user
                username = je.getProcessingContext().getTheUser().getUsername();
                JahiaDBUser user = (JahiaDBUser) je.getObject();
                objectId = user.getID();
                objectType = "user";
                break;
            }
            case 6: { // event on template
                //todo to be implemented
                break;
            }
            case 7: { // event on category
                JahiaCategory category = (JahiaCategory) je.getObject();
                objectId = category.getID();
                objectType = "category";
                break;
            }
            case 8: { // event on group
                JahiaDBGroup group = (JahiaDBGroup) je.getObject();
                objectId = group.getGroupID();
                objectType = "group";
                break;
            }
            case 9: { // event on rights
                username = je.getProcessingContext().getTheUser().getUsername();
                break;
            }
            default: {
                break;
            }
        }
        //logger.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        if (objectType.equals("")) {
            objectType = "unknown";
        }
        if (username.equals("")) {
            username = "unknown";
        }
        String jamonLabel = "JahiaEvent#u_" + username + "::o_" + operation + "::ot_" + objectType + "::oid_" + objectId + "::uu_" + uuid + "::pid_" + pid + "::sid_" + siteId;
        //logger.info("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");
        //logger.info("_-_" + jamonLabel);
        //logger.info("-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_");

        Monitor eventMon = MonitorFactory.start(jamonLabel);
        eventMon.stop();


    }

    public void trackError(JahiaErrorEvent jee) {
    
        logger.info("===============================================");
        logger.info("====================ERROR======================");
        StackTraceElement[] ste = jee.getException().getStackTrace();
        String jamonLabel = "JahiaError#Code_" + jee.getErrorCode() + "::Message_" + jee.getException().getMessage()+"::"+ste[0];
        Monitor eventMon = MonitorFactory.start(jamonLabel);
        eventMon.stop();
        logger.info("===============================================");
    }

    public Map<String, String> getLastNSystemErrors(int N) {
        //logger.info("getLastNSystemErrors");
        Map<String, String> result = new HashMap<String, String>();
         String query = "select * from counterdata cross join counters where name like '%JahiaError%' and counter = counters.id order by ts DESC limit 0," + N;
        List counterdata = jdbcTemplate.query(query, new DataRowMapper());
        int i = 1;
        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData data = (JAMonCounterData) it.next();
            result.put("time" + i, String.valueOf(data.getTimestamp()));
            result.put("code" + i, String.valueOf(data.getErrorCode()));
            result.put("msg" + i, String.valueOf(data.getErrorMessage()));
            result.put("src" + i, String.valueOf(data.getErrorSource()));
            i++;
        }
        result.put("size", String.valueOf(i));
        return result;
    }

    @Override
    public Map<String, String> getLastNactivities(int N) {

        /* get the last N events */
        Map<String, String> result = new HashMap<String, String>();
        
        String query = "select * from counterdata cross join counters where name like '%JahiaEvent%' and counter = counters.id order by ts DESC limit 0," + N;
        List counterdata = jdbcTemplate.query(query, new DataRowMapper());
        ////logger.info("**********************************************************************");
        ////logger.info("");
        int i = 1;
        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData data = (JAMonCounterData) it.next();

            result.put("id" + i, String.valueOf(data.getOid()));
            result.put("time" + i, String.valueOf(data.getTimestamp()));
            result.put("avgtime" + i, String.valueOf(data.getAvgtime()));
            result.put("hits" + i, String.valueOf(data.getHits()));
            result.put("maxtime" + i, String.valueOf(data.getMaxtime()));
            result.put("name" + i, String.valueOf(data.getName()));
            result.put("user" + i, data.getUser());
            result.put("objectid" + i, data.getObjectId());
            result.put("type" + i, data.getObjectType());
            result.put("operation" + i, data.getOperation());
            result.put("siteid" + i, data.getSiteId());
            result.put("pid" + i, String.valueOf(data.getPid()));
            result.put("uuid" + i, data.getUuid());
            result.put("hits" + i, String.valueOf(data.getHits()));
            i++;
            ////logger.info(data.getId() + "__" + data.getTimestamp() + "__" + data.getAvgtime() + "__" + data.getHits() + "__" + data.getMaxtime());
        }
        result.put("size", String.valueOf(i));
        ////logger.info("");
        ////logger.info("**********************************************************************");

        return result;
    }


    public Map<String, String> getMostNactiveUsers(int N) {
        //logger.info("%%%getMostNactiveUsers%%%");
        Map<String, String> result = new HashMap<String, String>();// username,totalop_lastaccess
        Map<String, String> resultCopy = new HashMap<String, String>();
        String query = "select * from counterdata cross join counters where name like '%JahiaEvent%' and counter = counters.id";
        List counterdata = jdbcTemplate.query(query, new DataRowMapper());

        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData jmd = (JAMonCounterData) (it.next());
            String username = jmd.getUser();//.split("_"))[1];
            if (result.containsKey(username)) {
                //logger.info("--> " + username);
                double newvalue = Double.parseDouble(result.get(username)) + jmd.getHits();
                result.put(username, String.valueOf(newvalue));
            } else {
                result.put(username, String.valueOf(jmd.getHits()));
            }
        }
        // get the most active N users 
        Map<String, String> mostActiveUsers = new HashMap<String, String>();
        for (int i = 1; i <= N; i++) {
            String mostActive = "";
            resultCopy = result;
            double max = 0;
            for (Iterator it = resultCopy.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                max = Double.parseDouble(resultCopy.get(key));
                mostActive = key;

                for (Iterator it2 = result.keySet().iterator(); it2.hasNext();) {
                    String current = (String) it2.next();
                    if (Double.parseDouble(resultCopy.get(current)) > max) {
                        max = Double.parseDouble(resultCopy.get(current));
                        mostActive = current;
                    }
                }
            }
            if(!mostActiveUsers.equals("")){
                mostActiveUsers.put(mostActive, String.valueOf(max));
            }
            result.remove(mostActive);
        }
        return mostActiveUsers;
    }

    public Map<String, String> getLeastNactiveUsers(int N) {


        Map<String, String> result = new HashMap<String, String>();// username,totalop_lastaccess
        Map<String, String> resultCopy = new HashMap<String, String>();
        String query = "select * from counterdata cross join counters where name like '%JahiaEvent%' and counter = counters.id";
        List counterdata = jdbcTemplate.query(query, new DataRowMapper());

        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData jmd = (JAMonCounterData) (it.next());
            String username = jmd.getUser();
            if (result.containsKey(username)) {
                double newvalue = Double.parseDouble(result.get(username)) + jmd.getHits();
                result.put(username, String.valueOf(newvalue));
            } else {
                result.put(username, String.valueOf(jmd.getHits()));
            }
        }
        // get the most active N users

        for (int i = 1; i <= N; i++) {

            String mostActive = "";
            resultCopy = result;
            double max = 0;
            for (Iterator it = resultCopy.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                max = Double.parseDouble(resultCopy.get(key));
                mostActive = key;
                for (Iterator it2 = result.keySet().iterator(); it2.hasNext();) {
                    String current = (String) it2.next();
                    if (Double.parseDouble(resultCopy.get(current)) > max) {
                        mostActive = current;
                    }
                }
            }
            result.remove(mostActive);
        }
        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> executeQuery(String user, String op, String type, int objId, String uuid, int pageId, int siteId, int N) {

        Map<String, String> result = new HashMap<String, String>();

        String params = "";
        String oidParam = "", sidParam = "", pidParam = "";
        if (objId != -1) {
            oidParam = "oid_" + objId + "%";
        }
        if (siteId != -1) {
            sidParam = "sid_" + siteId + "%";
        }
        if (pageId != -1) {
            pidParam = "pid_" + pageId + "%";
        }

        params = "u_" + user + "%o_" + op + "%ot_" + type + "%" + oidParam + "uu_" + uuid + "%" + pidParam + sidParam;

        String query = "select * from counterdata cross join counters where name like '%JahiaEvent%" + params + "' and counter = counters.id order by ts DESC limit 0," + N;


        List counterdata = jdbcTemplate.query(query, new DataRowMapper());
        /*
        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData jcd = (JAMonCounterData) it.next();
            //logger.info(jcd.getName());
        }
        */
        int i = 1;
        for (Iterator it = counterdata.iterator(); it.hasNext();) {
            JAMonCounterData data = (JAMonCounterData) it.next();

            result.put("id" + i, String.valueOf(data.getOid()));
            result.put("time" + i, String.valueOf(data.getTimestamp()));
            result.put("avgtime" + i, String.valueOf(data.getAvgtime()));
            result.put("hits" + i, String.valueOf(data.getHits()));
            result.put("maxtime" + i, String.valueOf(data.getMaxtime()));
            result.put("name" + i, String.valueOf(data.getName()));
            result.put("user" + i, data.getUser());
            result.put("objectid" + i, data.getObjectId());
            result.put("type" + i, data.getObjectType());
            result.put("operation" + i, data.getOperation());
            result.put("siteid" + i, data.getSiteId());
            result.put("pid" + i, String.valueOf(data.getPid()));
            result.put("uuid" + i, data.getUuid());
            result.put("hits" + i, String.valueOf(data.getHits()));


            i++;
            // //logger.info(data.getTimestamp() + "__" + data.getAvgtime() + "__" + data.getHits() + "__" + data.getMaxtime());
        }
        result.put("size", String.valueOf(i));


        return result;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> executeQuery(GWTanalyticsQuery query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getAllUsers() {
        List v = ServicesRegistry.getInstance().getJahiaUserManagerService().getUsernameList();
        Map<String, String> users = new HashMap<String, String>();
        for (Iterator it = v.iterator(); it.hasNext();) {
            String name = (String) it.next();
            users.put(name, "");
        }
        return users;
    }

    public void flushDatabase() {

        String query = "delete from counterdata";
        jdbcTemplate.execute(query);
        //logger.info(" table counterdata flushed");
        query = "delete from counters";
        jdbcTemplate.execute(query);
        //logger.info(" table counters flushed");
    }

    private String getUUID(JahiaEvent je) {
        String uuid = "nouuid";
        org.jahia.services.importexport.ImportExportService ies = ServicesRegistry.getInstance().getImportExportService();
        try {
            uuid = ies.getUuid(ContentPage.getPage(je.getProcessingContext().getPageID()));
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return uuid;
    }

    public void flushDatabaseOldest() {
    }


    public Map<String, String> getActivitiesPerUser(String user) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getActivitiesPerObjectId(int id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getActivitiesPerPageUUID(String uuid) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, String> getActivitiesPerSiteKey(int id) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public void setProp(String prop) {
        this.prop = prop;
    }

    public void start() throws JahiaInitializationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() throws JahiaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
