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
import org.jahia.exceptions.JahiaException;
import org.jahia.services.scheduler.SchedulerService;

import java.util.ArrayList;
import java.util.List;

/**
 * Process Display Action.
 * this ajax action class return a count of processes waiting in the process pool<br>
 * and an indication of concurrent threads currently running to execute the jobs scheduled in the pool.<br>
 * (the better approximation we have for now of the "working charge" of process server).<br>
 * for standard users (non admin or superuser), a count of process in the pool scheduled by other users is provided.<br>
 *
 * @author joepi
 * @version $Id$
 */
public class PDisplayAction extends AbstractPDisplayAction {

    private static final transient Logger logger = Logger.getLogger(PDisplayAction.class);
    private static final SchedulerService service = servicesRegistry.getSchedulerService();

    public static List jobTypesToIgnore = new ArrayList();

    static {
        jobTypesToIgnore.add("timebased");
    }
    // storage of users variables
    // todo: manage multiples sessions for the same user
    //private static Map userresponses = Collections.synchronizedMap(new HashMap(256));
    //private static Map userreload = Collections.synchronizedMap(new HashMap(256));

    /**
     * constructor
     */
    public PDisplayAction() {
        super();
        logger.debug("initialisation of Pdisplay action");

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

    public List getActiveJobsDetails() throws JahiaException {
        List jobs = service.getAllActiveJobsDetails();
        if ( jobs != null ){
            jobs.addAll(SiteIndexingJobDisplayAction.getJobsDetails(true));
        }
        return jobs;
    }

    public List getJobTypesToIgnore() {
        return jobTypesToIgnore;
    }

    protected boolean needToReload(long time) {
        return service.getLastJobCompletedTime() >= time;
    }

}
