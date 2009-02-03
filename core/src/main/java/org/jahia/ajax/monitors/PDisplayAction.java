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
