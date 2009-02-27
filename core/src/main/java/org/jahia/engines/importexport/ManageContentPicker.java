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

 package org.jahia.engines.importexport;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.content.ContainerDefinitionKey;
import org.jahia.content.ContentObject;
import org.jahia.content.StructuralRelationship;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.search.JahiaContainerSearchHit;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchHitInterface;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.fields.ContentSmallTextField;
import org.jahia.services.importexport.CopyJob;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.*;
import org.jahia.services.search.lucene.JahiaHitCollector;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.TextHtml;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.*;

/**
 * this class manage the main engine load and handle methods and delegate search to search service
 *
 * @author Thomas Draier, joe Pillot
 * @version $Id$
 */
public class ManageContentPicker {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageContentPicker.class);

    private static ManageContentPicker instance = null;
    private static final String JSP = "/engines/importexport/contentpick.jsp";
    private static ImportExportService ie;
    private static JahiaSearchService searchService;
    private static JahiaSitesService siteService;
    private static JahiaPageService pgService;

    private ManageContentPicker() {
    }

    /**
     * @return a single instance of the object
     */
    public static synchronized ManageContentPicker getInstance() {
        if (instance == null) {
            instance = new ManageContentPicker();

            // reference the useful services
            ie = ServicesRegistry.getInstance().getImportExportService();
            searchService = ServicesRegistry .getInstance().getJahiaSearchService();
            siteService = ServicesRegistry.getInstance().getJahiaSitesService();
            pgService =ServicesRegistry.getInstance().getJahiaPageService();
            logger.debug("creating instance");
        }
        return instance;
    }

    public boolean handleActions(ProcessingContext jParams, int mode, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {
        switch (mode) {
            case (JahiaEngine.LOAD_MODE) :

                return load(jParams, engineMap, object);
            case (JahiaEngine.SAVE_MODE) :

                return save(jParams, engineMap, object);
        }
        return false;
    }

    /**
     * @param processingContext
     * @param engineMap
     * @param object
     * @return
     * @throws JahiaException
     * @throws JahiaSessionExpirationException
     *
     */
    public boolean load(ProcessingContext processingContext, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {
        //JahiaUser user = processingContext.getUser ();
        logger.debug("loading mode");
        engineMap.put("fieldsEditCallingEngineName", "contentpick_engine");
        // type of object
        ContainerDefinitionKey k = (ContainerDefinitionKey) object.getDefinitionKey(null);
        try {
            JahiaContainerDefinition def = (JahiaContainerDefinition) ContentObject.getInstance(k);
            engineMap.put("objecttype", def.getName());
        } catch (ClassNotFoundException e) {
            logger.debug(e);
        }


        //check if we just need a sort on previous results
        String o = processingContext.getParameter("orderby");
        String asc = processingContext.getParameter("asc");
        String sizeresults = processingContext.getParameter("size");
        String sitesearch = processingContext.getParameter("searchSite");
        if (sitesearch == null) sitesearch = "all";
        if (asc == null) asc = "1";
        logger.debug("asc order=" + asc);
        if (sizeresults == null) sizeresults = "10";

        //o not null nor empty,lasto exist, o!=lasto, results not null

        if (o != null
                && !o.equalsIgnoreCase("")
                && engineMap.containsKey("orderby")
                && engineMap.get("fullResults") != null
                ) {
            logger.debug("o=" + o + " lasto=" + engineMap.get("orderby"));

            List full = (List) engineMap.get("fullResults");
            JahiaSearchResult myResults = (JahiaSearchResult) engineMap.get("searchResults");
            myResults.setResult(full);

            orderBy(o, engineMap, myResults, asc.equalsIgnoreCase("1"), Integer.parseInt(sizeresults), processingContext);
            engineMap.put("orderby", o);
            engineMap.put("asc", asc);
            engineMap.put("size", sizeresults);
            engineMap.put("searchSite", sitesearch);
            engineMap.put("contentpick_engine.fieldForm", ServicesRegistry.getInstance().
                    getJahiaFetcherService().fetchServlet((ParamBean) processingContext, JSP));
            return true;
        }

        //go to search
        try {
            doSearch(object, processingContext, engineMap);
        } catch (Exception e) {
            logger.error("error:", e);
            throw new JahiaException("", "", 0, 0, e);
        }

        engineMap.put("contentpick_engine.fieldForm", ServicesRegistry.getInstance().
                getJahiaFetcherService().fetchServlet((ParamBean) processingContext, JSP));

        return true;
    }


    public boolean update(ProcessingContext jParams, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {
        logger.debug("update mode");
        return true;
    }


    private boolean save(ProcessingContext jParams, Map engineMap, ContentObject object)
            throws JahiaException {
        String op = jParams.getParameter("contentPickOp");
        logger.debug("saving mode:pickop=" + op);

        try {

            if (op != null && !op.equalsIgnoreCase("")) {
                String copytype = op.substring(0, op.indexOf('_'));
                String sourceId = op.substring(op.indexOf('_') + 1);
                ContentObject source = ContentContainer.getContainer(Integer.parseInt(sourceId));


                Class jobClass = CopyJob.class;

                String skey = source.getObjectKey().toString();
                String dkey = object.getObjectKey().toString();

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Copy " + skey + " to " + dkey, jobClass, jParams);

                Set locks = new HashSet();

                // transfer lock
                LockKey lock = LockKey.composeLockKey(LockKey.ADD_CONTAINER_TYPE, object.getID(), object.getID());
                locks.add(lock);
                if (!LockRegistry.getInstance().acquire(lock, jParams.getUser(), jobDetail.getName(), BackgroundJob.getMaxExecutionTime()))
                {
                    logger.info("Cannot acquire lock, do not copy");
                    return false;
                }

                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();
                jobDataMap.put(CopyJob.SOURCE, skey);
                jobDataMap.put(CopyJob.DEST, dkey);
                jobDataMap.put(CopyJob.SITESOURCE, siteService.getSite(source.getSiteID()).getSiteKey());
                jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, siteService.getSite(object.getSiteID()).getSiteKey());
                jobDataMap.put(CopyJob.VERSION, CopyJob.VERSION_CURRENT);
                jobDataMap.put(BackgroundJob.JOB_LOCKS, locks);

                if ("actlink".equals(copytype)) {
                    jobDataMap.put(CopyJob.LINK, StructuralRelationship.ACTIVATION_PICKER_LINK);
                } else if ("chlink".equals(copytype)) {
                    jobDataMap.put(CopyJob.LINK, StructuralRelationship.CHANGE_PICKER_LINK);
                    jobDataMap.put(CopyJob.VERSION, CopyJob.VERSION_COMPLETE);
                }
                jobDataMap.put(BackgroundJob.JOB_TYPE, CopyJob.PICKERCOPY_TYPE);
                SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                schedulerServ.scheduleJobNow(jobDetail);

                jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
                logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");
                return true;
            }
        } catch (Exception e) {
            throw new JahiaException("", "", 0, 0, e);
        }
        return false;
    }

    /**
     * internal method to do the search
     *
     * @param object the initial destination object
     * @param processingContext the processing context
     * @param engineMap  the enginemap
     * @throws Exception exception
     */
    private void doSearch(ContentObject object, ProcessingContext processingContext, Map engineMap)
            throws Exception {
        long start=System.currentTimeMillis();
        logger.debug("searching mode");
        boolean isPID = false;

        //domain scope
        Iterator sites = siteService.getSites();
        String scope = processingContext.getParameter("searchSite");
        boolean multisite=false;
        if (scope == null && siteService.getNbSites() > 1) {
            scope=""+processingContext.getSiteID();// default site search scope is current site
            logger.debug("scoping on default unique siteid:" + scope);
        }
        else if (siteService.getNbSites() == 1) {
            int siteid = ((JahiaSite) siteService.getSites().next()).getID();
            scope = String.valueOf(siteid);
            logger.debug("scoping on unique siteid:" + siteid);
        } else if(scope.equals("all")){
            logger.debug("scoping on all sites");
            multisite=true;
        }

        //get all parameters
        // main query
        String searchString = processingContext.getParameter("query");
        String smode = processingContext.getParameter("condition1");
        String query_concat_mode = processingContext.getParameter("concat");
        String size = processingContext.getParameter("size");
        String asc = processingContext.getParameter("asc");

        // checking NPE & emptyness of important params
        if (size == null) size = "10";
        if (asc == null) asc = "1";
        if (searchString == null) searchString = "";
        String query1 = searchString;

        // to remap a numeric input in PIDs
        if (!searchString.equalsIgnoreCase("") && StringUtils.isNumeric(searchString)) {
            //used to get a pid directly from the main request assuming number is pid number or content
            String number= NumberPadding.pad(searchString);
            searchString = "jahia.content:" + number + " OR jahia.page_id:"+number;
            isPID = true;

            logger.debug("assuming number is jahia PID page or content? search is:->"+searchString);
        }

        if (smode == null || smode.equalsIgnoreCase("")) smode = "or";
        if (query_concat_mode == null || query_concat_mode.equalsIgnoreCase("")) query_concat_mode = "and";

        //get alt queries
        //query2
        //String query2=(String) processingContext.getParameter("query2");
        String query21 = processingContext.getParameter("query21");
        String query22 = processingContext.getParameter("query22");
        String query23 = processingContext.getParameter("query23");
        String query24 = processingContext.getParameter("condition24");
        String query244 = processingContext.getParameter("condition244");
        String smode2 = processingContext.getParameter("condition2");
        //query3
        //String query3=(String) processingContext.getParameter("query3");
        String query31 = processingContext.getParameter("query31");
        String query32 = processingContext.getParameter("query32");
        String query33 = processingContext.getParameter("query33");
        String query34 = processingContext.getParameter("condition34");
        String query344 = processingContext.getParameter("condition344");
        String smode3 = processingContext.getParameter("condition3");
        //query4
        //String query4=(String) processingContext.getParameter("query4");
        String query41 = processingContext.getParameter("query41");
        String query42 = processingContext.getParameter("query42");
        String query43 = processingContext.getParameter("query43");
        String query44 = processingContext.getParameter("condition44");
        String query444 = processingContext.getParameter("condition444");
        String smode4 = processingContext.getParameter("condition4");

        //check NPE on alt queries and alt modes
        //if(query2==null) query2="";
        if (query21 == null) query21 = "";
        if (query22 == null) query22 = "";
        if (query23 == null) query23 = "";
        if (query244 == null) query244 = "-";

        //if(query3==null) query3="";
        if (query31 == null) query31 = "";
        if (query32 == null) query32 = "";
        if (query33 == null) query33 = "";
        if (query344 == null) query344 = "-";

        //if(query4==null) query4="";
        if (query41 == null) query41 = "";
        if (query42 == null) query42 = "";
        if (query43 == null) query43 = "";
        if (query444 == null) query444 = "-";

        //conditions
        if (smode2 == null) smode2 = "creator";//default
        if (smode3 == null) smode3 = "creator";
        if (smode4 == null) smode4 = "creator";
        engineMap.put("smode2", smode2);
        engineMap.put("smode3", smode3);
        engineMap.put("smode4", smode4);

        //order
        String orderBy = processingContext.getParameter("orderby");//orderby parameter
        if (orderBy == null || orderBy.equalsIgnoreCase("")) {
            orderBy = "score";
            logger.debug("order set on default score");
        }

        //check on emptyness of queries
        if (searchString.equalsIgnoreCase("")
                && (query21.equalsIgnoreCase("") || query22.equalsIgnoreCase("") || query23.equalsIgnoreCase("") || query244.equalsIgnoreCase("-"))
                && (query31.equalsIgnoreCase("") || query32.equalsIgnoreCase("") || query33.equalsIgnoreCase("") || query344.equalsIgnoreCase("-"))
                && (query41.equalsIgnoreCase("") || query42.equalsIgnoreCase("") || query43.equalsIgnoreCase("") || query444.equalsIgnoreCase("-"))
                ) {

            logger.debug("initialization of primary parameters to default values");
            engineMap.put("searchString", "");
            engineMap.put("smode", "or");
            engineMap.put("concat", "or");
            engineMap.put("orderby", "score");

            //return;
        }


        //store alt queries non-null in engineMap
        //if(!query2.equalsIgnoreCase("")) engineMap.put("query2",query2);
        String query2 = "";
        String query3 = "";
        String query4 = "";
        long now = System.currentTimeMillis();
        long day = 3600 * 1000 * 24;
        long aweek = now - (day * 7);
        long amonth = now - (day * 30);
        long amonths = now - (day * 180);
        String[] scopedate = {"jahia.metadata_lastpublishingdate:", "jahia.metadata_lastmodified:", "jahia.metadata_created:"};

        //query2s
        engineMap.put("query21", query21);
        if (!query21.equalsIgnoreCase("")) {

            String subcondition = processingContext.getParameter("condition21");
            if (subcondition != null && subcondition.equalsIgnoreCase("creator")) {
                query2 = "jahia.metadata_createdby:" + query21;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("contributor")) {
                query2 = "jahia.metadata_lastmodifiedby:" + query21;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("all")) {
                query2 = "(jahia.metadata_createdby:" + query21 + " OR jahia.metadata_lastmodifiedby:" + query21 + ")";
            }
        }
        engineMap.put("query22", query22);
        if (!query22.equalsIgnoreCase("")) {

            query2 = "jahia.metadata_keywords:" + query22;
        }
        engineMap.put("query23", query23);
        if (!query23.equalsIgnoreCase("")) {
            String p="";
            if(!multisite) p=getPagePID(query23,Integer.parseInt(scope));
            else p=getPagePID(query23,sites);
            if(!p.equals("")){
                query23=p;
                query2 = "jahia.page_id:" + NumberPadding.pad(query23);
                isPID = true;
            } else {
                logger.debug("q23 not found:ignoring");
            }
        }
        engineMap.put("condition24", query24);
        engineMap.put("condition244", query244);
        if (!query244.equalsIgnoreCase("-")) {


            int valscope = 0;
            if (query24.equalsIgnoreCase("pub")) valscope = 0;
            if (query24.equalsIgnoreCase("mod")) valscope = 1;
            if (query24.equalsIgnoreCase("cre")) valscope = 2;
            if (query244.equalsIgnoreCase("week")) {
                query2 = scopedate[valscope] + "[" + NumberPadding.pad(aweek) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query244.equalsIgnoreCase("month")) {
                query2 = scopedate[valscope] + "[" + NumberPadding.pad(amonth) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query244.equalsIgnoreCase("months")) {
                query2 = scopedate[valscope] + "[" + NumberPadding.pad(amonths) + " TO " + NumberPadding.pad(now) + "]";
            }

        }

        //query3s
        engineMap.put("query31", query31);
        if (!query31.equalsIgnoreCase("")) {

            String subcondition = processingContext.getParameter("condition31");
            if (subcondition != null && subcondition.equalsIgnoreCase("creator")) {
                query3 = "jahia.metadata_createdby:" + query31;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("contributor")) {
                query3 = "jahia.metadata_lastmodifiedby:" + query31;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("all")) {
                query3 = "(jahia.metadata_createdby:" + query31 + " OR jahia.metadata_lastmodifiedby:" + query31 + ")";
            }
        }
        engineMap.put("query32", query32);
        if (!query32.equalsIgnoreCase("")) {
            query3 = "jahia.metadata_keywords:" + query32;
        }
        engineMap.put("query33", query33);
        if (!query33.equalsIgnoreCase("")) {
            String p="";
            if(!multisite) p=getPagePID(query33,Integer.parseInt(scope));
            else p=getPagePID(query33,sites);
            if(!p.equals("")){
                query33=p;
                query3 = "jahia.page_id:" + NumberPadding.pad(query33);
                isPID = true;
            } else{
               logger.debug("q33 not found:ignoring");
            }
        }
        engineMap.put("condition34", query34);
        engineMap.put("condition344", query344);
        if (!query344.equalsIgnoreCase("-")) {

            int valscope = 0;
            if (query34.equalsIgnoreCase("pub")) valscope = 0;
            if (query34.equalsIgnoreCase("mod")) valscope = 1;
            if (query34.equalsIgnoreCase("cre")) valscope = 2;
            if (query344.equalsIgnoreCase("week")) {
                query3 = scopedate[valscope] + "[" + NumberPadding.pad(aweek) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query344.equalsIgnoreCase("month")) {
                query3 = scopedate[valscope] + "[" + NumberPadding.pad(amonth) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query344.equalsIgnoreCase("months")) {
                query3 = scopedate[valscope] + "[" + NumberPadding.pad(amonths) + " TO " + NumberPadding.pad(now) + "]";
            }

        }
        //query4s
        engineMap.put("query41", query41);
        if (!query41.equalsIgnoreCase("")) {

            String subcondition = processingContext.getParameter("condition41");
            if (subcondition != null && subcondition.equalsIgnoreCase("creator")) {
                query4 = "jahia.metadata_createdby:" + query41;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("contributor")) {
                query4 = "jahia.metadata_lastmodifiedby:" + query41;
            } else if (subcondition != null && subcondition.equalsIgnoreCase("all")) {
                query4 = "(jahia.metadata_createdby:" + query41 + " OR jahia.metadata_lastmodifiedby:" + query41 + ")";
            }
        }
        engineMap.put("query42", query42);
        if (!query42.equalsIgnoreCase("")) {

            query4 = "jahia.metadata_keywords:" + query42;
        }
        engineMap.put("query43", query43);
        if (!query43.equalsIgnoreCase("")) {
            String p="";
            if(!multisite) p=getPagePID(query43,Integer.parseInt(scope));
            else p=getPagePID(query43,sites);
            if(!p.equals("")){
                query43=p;
                query4 = "jahia.page_id:" + NumberPadding.pad(query43);
                isPID = true;
            } else{
               logger.debug("q43 not found:ignoring");
            }
        }
        engineMap.put("condition44", query44);
        engineMap.put("condition444", query444);
        if (!query444.equalsIgnoreCase("-")) {

            int valscope = 0;
            if (query44.equalsIgnoreCase("pub")) valscope = 0;
            if (query44.equalsIgnoreCase("mod")) valscope = 1;
            if (query44.equalsIgnoreCase("cre")) valscope = 2;
            if (query444.equalsIgnoreCase("week")) {
                query4 = scopedate[valscope] + "[" + NumberPadding.pad(aweek) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query444.equalsIgnoreCase("month")) {
                query4 = scopedate[valscope] + "[" + NumberPadding.pad(amonth) + " TO " + NumberPadding.pad(now) + "]";
            } else if (query444.equalsIgnoreCase("months")) {
                query4 = scopedate[valscope] + "[" + NumberPadding.pad(amonths) + " TO " + NumberPadding.pad(now) + "]";
            }

        }

        //summary of alt queries
        logger.debug("query2:" + query2 + " cond2:" + smode2);
        logger.debug("query3:" + query3 + " cond3:" + smode3);
        logger.debug("query4:" + query4 + " cond4:" + smode4);



        // transform main query(only if not empty)
        searchString = searchString.trim();
        if (smode.equalsIgnoreCase("exact") && !searchString.equalsIgnoreCase("")) {
            searchString = "\"" + searchString + "\"";
        } else if (smode.equalsIgnoreCase("and") && !searchString.equalsIgnoreCase("")) {
            searchString = searchString.trim().replaceAll("\\s", " AND ");
        } else if ( !searchString.equals("") ){
            searchString = " (" + searchString + ") ";
        }

        //adding advanced queries
        String advplus = " OR ";
        if (query_concat_mode.equalsIgnoreCase("and")) advplus = " AND ";

        // constructing final BIG LUCENE query
        String altqueries = "";

        if (!query2.equalsIgnoreCase("")) altqueries = altqueries + query2 + advplus;
        if (!query3.equalsIgnoreCase("")) altqueries = altqueries + query3 + advplus;
        if (!query4.equalsIgnoreCase("")) altqueries = altqueries + query4 + advplus;

        StringBuffer contentPickingFilterBuffer = new StringBuffer(256);
        contentPickingFilterBuffer.append(" AND NOT (");
        contentPickingFilterBuffer.append(JahiaSearchConstant.CONTENT_PICKING);
        contentPickingFilterBuffer.append(":true) ");

        if (!searchString.equalsIgnoreCase("") && !altqueries.equalsIgnoreCase("")) {
            searchString =altqueries+ "(" + searchString + ")" + contentPickingFilterBuffer.toString();
        } else if (searchString.equalsIgnoreCase("") && !altqueries.equalsIgnoreCase("")) {
            int i1= altqueries.lastIndexOf(" AND ");
            int i2= altqueries.lastIndexOf(" OR ");
            if(i1!=-1) {
                searchString = altqueries.substring(0,i1) + contentPickingFilterBuffer.toString();
            } else {
                searchString = altqueries.substring(0,i2) + contentPickingFilterBuffer.toString();
            }

        } else if ( !"".equals(searchString.trim()) ){
            searchString += contentPickingFilterBuffer.toString();
        }
        //summary
        if (logger.isDebugEnabled()) {
            logger.debug("[init parameters from request: "
                + " searchSite:" + scope
                + " ordered by:" + orderBy
                + "[-------  searchstring:" + searchString + " ---------------]");
        }

        //store all the params for subsequent requests
        engineMap.put("searchString", searchString);    //main query
        engineMap.put("query1", query1);                 //main query copy(used to display)
        engineMap.put("smode", smode);                  //string addition mode
        engineMap.put("concat", query_concat_mode);     //mode of add criteria
        engineMap.put("orderby", orderBy);              //sort order
        engineMap.put("searchSite", scope);             //virtual site scope
        engineMap.put("size", size);                    // size of display results
        engineMap.put("asc", asc);                    // size of display results
        //previous results (maybe we can store the history of results?)
        JahiaSearchResult searchResults;
//        JahiaSearchResult searchResultsStaged;

        // we search only if we have smtg to search
        if (!"".equals(searchString)) {
            logger.debug("entering real search");
            JahiaSearchResultBuilder resultBuilder = new ContainerSearchResultBuilderImpl();
            searchString = searchString.trim();
            // we set the hitcollector with max hits
            int maxhit=Integer.parseInt(searchService.getConfig().getProperty("searchMaxHits"));
            resultBuilder.setHitCollector(new JahiaHitCollector(false,-1, maxhit, -1, -1, false, true)); //we limit the results
            resultBuilder.getHitCollector().setSearchResultBuilder(resultBuilder);
            //languages
            List languageCodes = new ArrayList();

            // languages available on current site
            List langs = processingContext.getSite().getLanguageSettings(true);
            logger.debug("sitelangages number:" + langs.size());
            for (final Iterator i = langs.iterator(); i.hasNext();) {
                logger.debug("" + ((SiteLanguageSettings) i.next()).getCode());
            }

            //adding the lang of the request if needed
            String currlangcode = processingContext.getCurrentLocale().toString();
            if (currlangcode != null && !languageCodes.contains(currlangcode)) languageCodes.add(currlangcode);

            JahiaSite site;
            if (scope.equalsIgnoreCase("all")) {
                //multidomain search
                List handlers = new ArrayList(50);


                while (sites.hasNext()) {
                    site = (JahiaSite) sites.next();
                    if (isPID) {

                        // to add all languages available in site in the case of pid present in queries
                        List v = site.getLanguageSettings(true);
                        for (Iterator i = v.iterator(); i.hasNext();) {
                            Object o = i.next();
                            String langCode = ((SiteLanguageSettings) o).getCode();
                            if (langCode != null && !languageCodes.contains(langCode)) languageCodes.add(langCode);

                        }
                    }

                    SearchHandler h = searchService.getSearchHandler(site.getID());
                    //defensive code
                    if (h != null) {
                        handlers.add(h.getName());
                    }
                }

                String[] strings = (String[]) handlers.toArray(new String[handlers.size()]);
                logger.debug(strings);

                processingContext.setEntryLoadRequest(EntryLoadRequest.CURRENT);
                processingContext.setOperationMode(ParamBean.NORMAL);
                searchResults = searchService.search(strings, searchString, processingContext, languageCodes, resultBuilder);
                processingContext.setOperationMode(ParamBean.EDIT);
                processingContext.setEntryLoadRequest(EntryLoadRequest.STAGED);
//                searchResultsStaged = searchService.search(strings, searchString, processingContext, languageCodes, resultBuilder);
            } else {
                //just one domain search

                // we get the languagecode from this site
                //todo these could be set in user settings?
                final int searchSiteID;
                if (scope == null || scope.length() == 0) {
                    searchSiteID = processingContext.getSiteID();
                } else {
                    searchSiteID = Integer.parseInt(scope);
                }
                logger.debug("search with site scope constraint on site:"+searchSiteID);
                site = siteService.getSite(searchSiteID);

                // to add all languages available in this site
                List v = site.getLanguageSettings(true);
                for (final Iterator i = v.iterator(); i.hasNext();) {
                    Object o = i.next();
                    String langCode = ((SiteLanguageSettings) o).getCode();
                    if (langCode != null && !languageCodes.contains(langCode)) languageCodes.add(langCode);

                }
                processingContext.setEntryLoadRequest(EntryLoadRequest.CURRENT);
                processingContext.setOperationMode(ParamBean.NORMAL);
                searchResults = searchService.search(searchSiteID, searchString, processingContext, languageCodes, resultBuilder);
                processingContext.setOperationMode(ParamBean.EDIT);
                processingContext.setEntryLoadRequest(EntryLoadRequest.STAGED);
            }
            logger.debug("searching in what languages:" + languageCodes);
            engineMap.put("searchLanguageCodes", languageCodes);
            logger.debug("# of hits in raw searchresults:" + searchResults.getHitCount() + " " + searchResults);

            // subset of results
            JahiaSearchResult myResults = new JahiaSearchResult(resultBuilder);
            Map results = new HashMap();

            // destination object cdk & cdfn
            ContainerDefinitionKey cdk = (ContainerDefinitionKey) (object).getDefinitionKey(null);
            JahiaContainerDefinition thisJcd = (JahiaContainerDefinition) ContentObject.getInstance(cdk);
            logger.debug("destination cdk:" + cdk + " cdfn:" + thisJcd.getName());
            logger.debug("LUCENE "+searchResults.getHitCount()+" results returned in "+(System.currentTimeMillis()-start)+"ms,... processing compatible");
            start=System.currentTimeMillis();
            // big loop to test the compatibility of container type
            for (final Iterator iterator = searchResults.results().iterator(); iterator.hasNext();) {
                JahiaSearchHit jahiaSearchHit = (JahiaSearchHit) iterator.next();
                processHit(jahiaSearchHit, processingContext, thisJcd, object, languageCodes, results, false);
            }
            logger.debug("all hits processed in "+(System.currentTimeMillis()-start)+"ms");
            for (Iterator iterator = results.values().iterator(); iterator.hasNext();) {
                myResults.addHit((JahiaSearchHit) iterator.next());
            }

            //sorting
            orderBy(orderBy, engineMap, myResults, asc.equalsIgnoreCase("1"), Integer.parseInt(size), processingContext);
            return;
        }
        //release results
        engineMap.put("searchResults", null);
        engineMap.put("fullResults", null);
    }

    /**
     * internal method to check if the hit is compatible and extract some information for the GUI
     * @param jahiaSearchHit
     * @param processingContext
     * @param thisJcd
     * @param object
     * @param languageCodes
     * @param results
     * @param stagedResult
     * @throws JahiaException
     * @throws ClassNotFoundException
     */
        private void processHit(JahiaSearchHit jahiaSearchHit, ProcessingContext processingContext, JahiaContainerDefinition thisJcd, ContentObject object, List languageCodes, Map results, boolean stagedResult) throws JahiaException, ClassNotFoundException {
        EntryLoadRequest lr;
        if (stagedResult) {
            lr = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, processingContext.getLocaleList());
        } else {
            lr = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, processingContext.getLocaleList());
        }

        ContentObject current = ContentContainer.getContainer(jahiaSearchHit.getIntegerID());
        float score = jahiaSearchHit.getScore();
        boolean checkContentPicking = false;
        //the big loop
        while (current != null) {
            // check against rights 1st
            JahiaUser user=processingContext.getUser();
            if (!current.checkReadAccess(user) ||
                    (!current.checkWriteAccess(user) && stagedResult) ||
                    (current.hasActiveEntries() && stagedResult)) {
                break;
            }
            //check against picked status
            if (checkContentPicking && current.getPickedObject() != null) {
                // this object is already a copy, we ignore it
                break;
            }
            checkContentPicking = true;

            //check type of container found
            if (current instanceof ContentContainer) {
                ContentContainer contentContainer = ((ContentContainer) current);
                ContainerDefinitionKey currentCdk = (ContainerDefinitionKey) contentContainer.getDefinitionKey(null);
                JahiaContainerDefinition currentJcd = (JahiaContainerDefinition) ContentObject.getInstance(currentCdk);
                logger.debug("Hit-id:" + jahiaSearchHit.getId() + " [score:" + score + "] cdk:" + currentCdk + " cdfn:" + currentJcd+"object dest:"+object);

                //is compatible
                if (ie.isCompatible(thisJcd, contentContainer, processingContext)) {
                    ContentObject dest = object;
                    while (dest != null) {
                        logger.debug("going up in the genealogy of destination object"+dest);
                        if (dest.getObjectKey().equals(current.getObjectKey())) {
                            break;
                        }
                        dest = dest.getParent(null);
                    }
                    if (dest == null && current.hasActiveOrStagingEntries()) {
                        String key = Integer.toString(current.getID());
                        int pageID = contentContainer.getPageID();
                        //used to try to create an url
                        int siteID=contentContainer.getSiteID();
                        int type = JahiaSearchHitInterface.UNDEFINED_TYPE;
                        logger.debug("found compatible?" + key);
                        String t = key;// default object found ID
                        List l = contentContainer.getChilds(null, lr, JahiaContainerStructure.JAHIA_FIELD);

                        boolean descriptionFieldFound = false;

                        //looping list of childs to check page type?
                        for (final Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                            ContentField contentField = (ContentField) iterator1.next();
                            if (contentField instanceof ContentPageField) {
                                ContentPage contentPage = ((ContentPageField) contentField).getContentPage(lr);

                                if (contentPage != null && contentPage.getPageType(lr) == JahiaPage.TYPE_DIRECT) {
                                    //try to obtain a title
                                    t = contentPage.getTitle(lr);
                                    // check for some title in some languages
                                    if (t == null) for (Iterator it = languageCodes.iterator(); it.hasNext();) {
                                        t = (String) contentPage.getTitles(false).get(it.next());
                                        if (t != null) break;
                                    }

                                    pageID = contentPage.getID();
                                    siteID = contentPage.getSiteID();
                                    type = JahiaSearchHitInterface.PAGE_TYPE;
                                    logger.debug("page:" + t + " pageID" + pageID + "on site" + siteID);
                                    descriptionFieldFound = true;
                                    break;
                                } else if (contentPage != null && contentPage.getPageType(lr) == JahiaPage.TYPE_LINK) {
                                    t = contentPage.getTitle(lr);
                                    if(t==null) t="NA";
                                    pageID = contentPage.getPageLinkID(lr);
                                    siteID = contentPage.getSiteID();
                                    type = JahiaSearchHitInterface.PAGE_TYPE;
                                    logger.debug("link internal page:" + t + " pageID" + pageID + "on site" + siteID);
                                    descriptionFieldFound = true;
                                    break;
                                } else {
                                    logger.debug("contentpage is null!!!!!");
                                }
                            }
                        }

                        if (!descriptionFieldFound){
                            // case the content object is text
                            for (final Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                                ContentField contentField = (ContentField) iterator1.next();
                                if (contentField instanceof ContentSmallTextField) {
                                    String value = contentField.getValue(processingContext, lr);
                                    if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                                        t = TextHtml.html2text(value);
                                        pageID = contentField.getPageID();
                                        siteID = contentField.getSiteID();
                                        type = JahiaSearchHitInterface.FIELD_TYPE;
                                        logger.debug("smalltext:" + t + " pageID" + pageID + "on site" + siteID);
                                        descriptionFieldFound = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (!descriptionFieldFound ){
                            for (final Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                                ContentField contentField = (ContentField) iterator1.next();
                                String value = contentField.getValue(processingContext, lr);
                                if (value != null && !value.trim().equals("") && !value.equals("<empty>")) {
                                    t = TextHtml.html2text(value);

                                    //if (value.length() > 12) t = value.substring(0, 12) + " (...)";
                                    logger.debug("tkey=" + t);
                                    break;
                                }
                            }
                        }

                        if (jahiaSearchHit.getParsedObject() != null) {
                            //logger.debug("fields of searchhit" + jahiaSearchHit.getParsedObject().getFields());
                        }

                        if (results.containsKey(key)) {
                            //results hold already this hit
                            JahiaContainerSearchHit hit = (JahiaContainerSearchHit) results.get(key);
                            hit.setScore(score + hit.getScore());
                            logger.debug("improving score :" + key + " new score:" + hit.getScore());
                            break;
                        } else {
                            // new Hit
                            JahiaContainerSearchHit hit = new JahiaContainerSearchHit(jahiaSearchHit.getParsedObject());
                            hit.setType(type);
                            hit.setIntegerID(current.getID());
                            EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE,0 ,processingContext.getLocales());
                            JahiaPage page = contentContainer.getPage().getPage(loadRequest, ParamBean.NORMAL, processingContext.getUser());//                                    contentContainer.getPage().getPage(processingContext);
                            hit.setPage(page);
                            hit.setPageId(pageID);

                            // an absolute url
                            String fullurl=constructPageLink(siteID,pageID,processingContext);
                            // relative url "homemade"
                            String hiturl = Jahia.getContextPath() + Jahia.getServletPath() + "/site/"+siteService.getSite(siteID).getSiteKey()+"/pid/" + pageID;
                            //hit.setURL(processingContext.composePageUrl(pageID));
                            logger.debug("URL:---->" + fullurl);
                            //logger.debug("COMP URL:---->" + processingContext.composePageUrl(pageID));
                            hit.setURL(hiturl);
                            hit.setTeaser(t);
                            hit.setScore(score);
                            /*
                            Map fields = hit.getParsedObject().getFields();
//                            fillSearchHitWithMetadataValues(fields, contentContainer, "creator", new String[]{""}, processingContext);
//                            fillSearchHitWithMetadataValues(fields, contentContainer, "lastContributor", new String[]{""}, processingContext);
//                            fillSearchHitWithMetadataValues(fields, contentContainer, "creationDate", new String[]{""}, processingContext);
//                            fillSearchHitWithMetadataValues(fields, contentContainer, "lastPublishingDate", new String[]{""}, processingContext);

                            while (current != null) {
                                current = current.getParent(null);
                                if (current != null && current.getPickedObject() != null) {
                                    break;
                                }
                            }
                            */

                            results.put(key, hit);
                            break;
                        }
                    } else {
                        logger.debug("loop detected");
                    }
                } else {
                    logger.debug("no-compatible");
                }
                // end of compatible
            }
            //next parent object : this child one is not a contentcontainer
            current = current.getParent(null);
            score /= 1.5;
        }//end big loop
    }

    /**
     * internal method to manage cross-site links.
     * todo ned to be port in context.getSiteURL()?
     * @param siteid
     * @param pid
     * @param context
     * @return
     * @throws JahiaException
     */
    private String constructPageLink(int siteid,int pid,ProcessingContext context) throws JahiaException {
         // we save old value of pcontext
                            int _sid=context.getSiteID();
                            JahiaSite _site=context.getSite();
                            String _skey=context.getSiteKey();
                            boolean siteparams=false;
        if(siteid!=_sid) siteparams=true;
        JahiaSite oursite=siteService.getSite(siteid);
        String oursitekey=oursite.getSiteKey();

        if(siteparams){
            // cross site, need to replace s_parameters in pcontext
            context.setSite(oursite);
            context.setSiteID(siteid);
            context.setSiteKey(oursitekey);
        }
        String url= context.getSiteURL( oursite,pid,false,false, false);
        if(siteparams){
            //restore old values
            context.setSite(_site);
            context.setSiteID(_sid);
            context.setSiteKey(_skey);
        }
        return url;
    }

    private void fillSearchHitsWithMetadataValues(JahiaSearchResult results, String metadataName, ProcessingContext ctx) throws JahiaException {
        for (JahiaSearchHit hit : results.results()) {
            Map<String, String[]> fields = hit.getParsedObject().getFields();
            fillSearchHitWithMetadataValues(fields, ContentContainer.getContainer(hit.getIntegerID()), metadataName, new String[]{""}, ctx);
        }
    }

    /**
     * @param fields
     * @param container
     * @param metadataName
     * @param defaultValues the default values if metadata is null or returns empty values
     * @param context
     * @throws JahiaException
     */
    private void fillSearchHitWithMetadataValues(Map<String, String[]> fields,
                                                 ContentContainer container,
                                                 String metadataName,
                                                 String[] defaultValues,
                                                 ProcessingContext context)
            throws JahiaException {
        if (fields.get(metadataName) != null) {
            return;
        }
        ContentField metadata = container.getMetadata(metadataName);
        if (metadata != null) {
            String value = metadata.getValue(context);
            if (value == null) {
                fields.put(metadataName.toLowerCase(), defaultValues);
            } else {
                fields.put(metadataName.toLowerCase(), new String[]{value});
            }
        }
    }

    /**
     * internal method to sort by a key
     *
     * @param key
     * @param engineMap
     * @param myResults
     * @param order
     * @param size
     * @param ctx
     */
    public void orderBy(final String key, Map engineMap, JahiaSearchResult myResults, final boolean order, int size, ProcessingContext ctx)
            throws JahiaException {

        logger.debug("sort by" + key);
        if (myResults == null) {
            logger.debug("myResults is null:SHOUD NOT");
            return;
        }

//        fillSearchHitsWithMetadataValues(myResults, key, ctx);

        // create a comparator to compare from key and score
        Comparator comp = new Comparator() {

            /**
             * compare implementation
             *
             * @param o1
             * @param o2
             * @return int value
             */
            public int compare(Object o1, Object o2) {
                String metadata1 = ((JahiaSearchHit) o1).getParsedObject().getValue(key);
                float sc1 = ((JahiaSearchHit) o1).getScore();
                String metadata2 = ((JahiaSearchHit) o2).getParsedObject().getValue(key);
                float sc2 = ((JahiaSearchHit) o2).getScore();
                int o = 1;
                if (!order) o = -1;
                // possibly metadata could be null
                if (metadata1 == null && metadata2 != null) return (-1 * o);
                if (metadata2 == null) {
                    //equality case (use the score)
                    if (sc1 <= sc2)
                        return o;// second object is less than 1st
                    else
                        return (-1 * o); //opposite
                }

                //comparaison
                if (metadata1.equalsIgnoreCase(metadata2)) {
                    //equality case (use the score)
                    if (sc1 <= sc2)
                        return o;// second object is less than 1st
                    else
                        return (-1 * o); //opposite
                } else if (metadata1.compareTo(metadata2) < 0)
                    return o;// second object is less than 1st
                else
                    return (-1 * o);  //opposite
            }
        };

        for (int i = 0; i < metadata.length; i++) {
            String s = metadata[i];
            if (s.equals(key)) {
                fillSearchHitsWithMetadataValues(myResults, metadata[i], ctx);
            }
        }

        if (myResults.results() != null && myResults.results().size() > 1) {
            Collections.sort(myResults.results(), comp);
            logger.debug("sorted myresults");
        } else {
            if (myResults.results() == null) logger.debug("results null!");
            if (myResults.results() != null && myResults.results().isEmpty()) {
                logger.debug("no results");
                engineMap.put("searchResults", myResults);
                return;

            }
            if (myResults.results() != null && myResults.results().size() == 1) logger.debug("result too small");
        }
        if (myResults.results() != null && !myResults.results().isEmpty()) {
            engineMap.put("fullResults",myResults.results());
            if (myResults.results().size()>size) {
                List croppedResults = new ArrayList(myResults.results().subList(0,size));
                myResults.setResult(croppedResults);
            }
            engineMap.put("searchResults", myResults);

            for (int i = 0; i < metadata.length; i++) {
                fillSearchHitsWithMetadataValues(myResults, metadata[i], ctx);
            }
        }
    }


      /**
     * Checks if a supplied string contains only digits. <br>
     * A character is a digit if it is not in the range '\u2000' <= Car <= '\u2FFF' and its
     * Unicode name contains the word "DIGIT".
     *
     * @param checkString The String to be checked
     * @return true if the String contains only digits, false otherwise
     */
    private boolean isStringAllDigits(String checkString) {
        for (int i = 0; i < checkString.length(); i++) {
            boolean check = Character.isDigit(checkString.charAt(i));
            if (!check) return false;
        }
        return true;
    }
      /**
     * check the urlkey and obtain a pid if needed
     * @param query
     * @param siteid
     * @return the query
     * @throws JahiaException
     */
    private String getPagePID(String query,int siteid) throws JahiaException {
        if(!isStringAllDigits(query)){
               // check the numericity of the input
               //should be an urlkey-> looking for page id
               logger.debug("searching for an urlkey:"+query+ "on site "+siteid);
                List thepagesprops = pgService.getPagePropertiesByValueAndSiteID(query,siteid);
                Iterator itp=thepagesprops.iterator();
                while(itp.hasNext()) {
                    PageProperty thepageprop = (PageProperty)itp.next();
                    if(thepageprop.getName().equals("pageURLKey")){
                        query=""+thepageprop.getPageID();
                        logger.debug("found urlkey");
                        return query;
                    }
                }
            logger.debug("urlkey not found!!");
            return "";
            }
        logger.debug("this query is numeric assuming: PID");
        return query;
    }

    private String getPagePID(String query, Iterator sites) throws JahiaException {
        String r;
        while(sites.hasNext()){
            JahiaSite thesite=(JahiaSite) sites.next();
            r=getPagePID(query,thesite.getID());
            if(!r.equals("")) return r;
        }
        return "";
    }
    private static String[] metadata = { "createdBy", "lastModifiedBy", "created", "lastPublishingDate" };
}
