/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.monitors;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ParamBean;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Process Display Action.
 * this ajax action class return a count of processes waiting in the process pool<br>
 * and an indication of concurrent threads currently running to execute the jobs scheduled in the pool.<br>
 * (the better approximation we have for now of the "working charge" of process server).<br>
 * for standard users (non admin or superuser), a count of process in the pool scheduled by other users is provided.<br>
 *
 * @author joepi
 * @version $Id: PDisplayAction.java 15861 2006-12-01 13:40:46Z dpillot $
 */
abstract class AbstractPDisplayAction extends AjaxAction {

    private static final transient Logger logger = Logger.getLogger(AbstractPDisplayAction.class);
    private long cronscheduledrange=3600000;//default 1 hour
    private static String lastRequestTIme="lastrequestedTime";
    // storage of users variables
    // todo: manage multiples sessions for the same user
    //private static Map userresponses = Collections.synchronizedMap(new HashMap(256));
    //private static Map userreload = Collections.synchronizedMap(new HashMap(256));

    /**
     * constructor
     */
    public AbstractPDisplayAction() {
        super();

        //register itself as scheduler listener
        /*
        try {
            service.registerAsListener(this);
            isRegistered = true;
        } catch (SchedulerException e) {
            logger.error("error on register as listener:" + e);
        }
        */
    }

    /**
     * implentation method that will execute the AJAX Action
     *
     * @param mapping  Struts ActionMapping
     * @param form     Struts ActionForm
     * @param request  The current HttpServletRequest
     * @param response The HttpServletResponse linked to the current request
     * @return ActionForward    Struts ActionForward
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {

        try {

            final JahiaUser currentUser = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);
            final JahiaSite site = (JahiaSite) request.getSession().getAttribute(ParamBean.SESSION_SITE);
            final String sessionID = request.getSession().getId();
            final String cronlimit =request.getParameter("cl");//limit of time to display cron jobs
            if(cronlimit!=null){
                 cronscheduledrange=3600000*Integer.parseInt(cronlimit);
            }
            if (currentUser == null || site == null /*|| !currentUser.isRoot() || !currentUser.isAdminMember(site.getID())*/)
            {
                logger.debug("Unauthorized attempt to use AJAX Struts Action - Process display");
                if (isValidUser(currentUser)) {
                    throw new JahiaForbiddenAccessException("Must have 'Admin' access");
                } else {
                    throw new JahiaForbiddenAccessException("Must be logged in");
                }
            }


            String uKey = currentUser.getUserKey();

            if (uKey != null ) {
                //check the last request's time
                String lastrequestedString=(String)request.getSession().getAttribute(AbstractPDisplayAction.lastRequestTIme);
                String reloading = "false";
                if(lastrequestedString!=null){
                    //check to know if wflow jobs have been launched since this
                    long lastrequest=Long.parseLong(lastrequestedString);
                    logger.debug(" @"+lastrequest);
                    if(needToReload(lastrequest)) reloading="true";
                }
                lastrequestedString=""+System.currentTimeMillis();
                request.getSession().setAttribute(AbstractPDisplayAction.lastRequestTIme,lastrequestedString);

                /*if (!userreload.isEmpty()
                        && userreload.containsKey(uKey)
                        && ((String) userreload.get(uKey)).trim().equalsIgnoreCase("false")
                        && !userresponses.isEmpty()
                        && userresponses.containsKey(uKey)
                        ) {
                    // send the same response as previous if conditions are unchanged
                    // to avoid to poll the back-end with similar responses.
                    //logger.debug("ibid");
                    sendResponse((String) userresponses.get(uKey), response);
                } else {*/

                    int[] resp = computeResponses(currentUser);
                    //String reloading = getActualReloadFlag(uKey);
                    StringBuffer buf = new StringBuffer();
                    buf.append(XML_HEADER);
                    buf.append("<response>\n");

                    for (int n = 0; n < resp.length; n++) {
                        buf.append(buildXmlElement(INFO_KEYS[n], "" + resp[n]));
                    }
                    buf.append(buildXmlElement("reload", reloading));
                    buf.append("</response>\n");
                    sendResponse(buf.toString(), response);
                    logger.debug("SID:"+sessionID+"("+uKey+")"+buf.toString());
                    //userresponses.put(uKey, buf.toString());
                //}
            } else {
                throw new JahiaBadRequestException("user unknown");
            }

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    /**
     * internal to get the reload response status for a user
     *
     * @param ukey
     * @return a string
     */
    public String getActualReloadFlag(String ukey) {

        /*if (userreload.isEmpty()) {
            userreload.put(ukey, "true");
            return "true";
        } else if (!userreload.containsKey(ukey)) {
            userreload.put(ukey, "true");
            return "true";
        } else if (((String) userreload.get(ukey)).trim().equalsIgnoreCase("true")) {
            userreload.put(ukey, "false");
            return "false";
        } else return "false";
        */
        return "false";
    }

    /**
     * internal method to call the Scheduler service to compute current values
     * on the activity of process server.
     *
     * @param user
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    private int[] computeResponses(JahiaUser user) throws JahiaException {
        int[] responses = {0, 0, 0, 0, 0, 0};

        List alls = getActiveJobsDetails();
        List jobTypesToIgnore = getJobTypesToIgnore();
        for (java.util.Iterator iterator = alls.iterator(); iterator.hasNext();) {
            JobDetail jd = (JobDetail) iterator.next();

            JobDataMap data = jd.getJobDataMap();
            String status = data.getString(BackgroundJob.JOB_STATUS);
            String userkey = data.getString(BackgroundJob.JOB_USERKEY);
            String type = data.getString(BackgroundJob.JOB_TYPE);

            if(jobTypesToIgnore.contains(type)) {
                logger.debug("ignoring timebased jobs");
                continue;
            }
            if (status.equals(BackgroundJob.STATUS_RUNNING)) {
                responses[0]++;
                if (user.getUserKey().equals(userkey)) {
                    responses[1]++;
                } else {
                    responses[2]++;
                }
            } else if (status.equals(BackgroundJob.STATUS_WAITING)) {
                responses[0]++;
                if (user.getUserKey().equals(userkey)) {
                    responses[3]++;
                } else {
                    responses[4]++;
                }
            } else if (status.equals(BackgroundJob.STATUS_POOLED)) {
                boolean crondisplay=true;
                if(data.getString(BackgroundJob.JOB_SCHEDULED)!=null){
                    long fired=Long.parseLong(data.getString(BackgroundJob.JOB_SCHEDULED));
                    if(fired>System.currentTimeMillis()+cronscheduledrange) crondisplay=false;
                }
                if(crondisplay) responses[5]++;
            }
        }

        return responses;
    }

    protected abstract List getActiveJobsDetails() throws JahiaException;

    protected abstract List getJobTypesToIgnore();

    protected abstract boolean needToReload(long time);

    private static String[] INFO_KEYS = {"started", "running", "otherrunning", "waiting", "otherwaiting", "pooled"};

}
