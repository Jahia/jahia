/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.scheduler;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ComparatorUtils;
import org.jahia.settings.SettingsBean;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Convenient Spring bean to schedule background RAM as well as persistent jobs.
 * 
 * @author Cedric Mailleux
 * @author Sergiy Shyrkov
 * @since JAHIA 6.5
 */
public class JobSchedulingBean implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(JobSchedulingBean.class);

    private boolean isRamJob;

    private JobDetail jobDetail;

    private Boolean overwriteExisting;

    private SchedulerService schedulerService;

    private SettingsBean settingsBean;

    private List<Trigger> triggers = new LinkedList<Trigger>();

    public void afterPropertiesSet() throws Exception {
        if (overwriteExisting == null) {
            overwriteExisting = settingsBean.isDevelopmentMode();
        }

        if (jobDetail == null) {
            logger.info("No JobDetail data was specified. Skip scheduling job.");
            return;
        }

        
        if (overwriteExisting) {
            scheduleJob(true);
        } else {
            JobDetail existingJobDetail = getScheduler().getJobDetail(jobDetail.getName(),
                    jobDetail.getGroup());
            if (existingJobDetail == null) {
                // job data is not present -> schedule the job as a new one
                scheduleJob(false);
            } else {
                // job data exists -> check if the triggers have changed
                if (needToRescheduleTheJob()) {
                    scheduleJob(true);
                }
            }
        }
    }

    protected Scheduler getScheduler() {
        return isRamJob ? schedulerService.getRAMScheduler() : schedulerService.getScheduler();
    }
    
    protected String getTriggerInfo(Trigger trigger) {
        return (trigger instanceof CronTrigger && ((CronTrigger) trigger).getCronExpression() != null) ? ("CronTrigger ["
                + ((CronTrigger) trigger).getCronExpression() + "]")
                : trigger.toString();
    }

    @SuppressWarnings("unchecked")
    protected boolean needToRescheduleTheJob() throws SchedulerException {
        Map<String, Trigger> existingTriggers = mapByName(getScheduler().getTriggersOfJob(
                jobDetail.getName(), jobDetail.getGroup()));

        // we have different number of triggers
        if (existingTriggers.size() != triggers.size()) {
            return true;
        }

        Map<String, Trigger> newTriggers = mapByName(triggers.toArray(new Trigger[0]));

        // the name of the triggers do not match
        if (!CollectionUtils.disjunction(existingTriggers.keySet(), newTriggers.keySet()).isEmpty()) {
            return true;
        }

        // compare triggers one by one
        for (Map.Entry<String, Trigger> existing : existingTriggers.entrySet()) {
            Trigger newTrigger = newTriggers.get(existing.getKey());
            Trigger existingTrigger = existing.getValue();
            if (!existingTrigger.getClass().getName().equals(newTrigger.getClass().getName())) {
                return true;
            }

            if (existingTrigger instanceof CronTrigger
                    && (ComparatorUtils.naturalComparator().compare(
                            ((CronTrigger) existingTrigger).getCronExpression(),
                            ((CronTrigger) newTrigger).getCronExpression()) != 0)) {
                return true;
            }
        }

        return false;
    }

    protected void scheduleJob(boolean deleteFirst) throws SchedulerException {
        if (deleteFirst) {
            logger.info("Deleting job {}", jobDetail.getFullName());
            getScheduler().deleteJob(jobDetail.getName(), jobDetail.getGroup());
        }
        if (triggers.size() == 1) {
            logger.info("Scheduling {} job {} using {}", new String[] {
                    isRamJob ? "RAM" : "persistent", jobDetail.getFullName(),
                    getTriggerInfo(triggers.get(0)) });
            getScheduler().scheduleJob(jobDetail, triggers.get(0));
        } else {
            if (triggers.size() == 0) {
                logger.info("Job has no triggers configured. Only the JobDetail data will be stored.");
            }
            getScheduler().addJob(jobDetail, true);
            for (Trigger trigger : triggers) {
                trigger.setJobName(jobDetail.getName());
                trigger.setJobGroup(jobDetail.getGroup());
                logger.info("Scheduling {} job {} using {}", new String[] {
                        isRamJob ? "RAM" : "persistent", jobDetail.getFullName(),
                        getTriggerInfo(trigger) });
                getScheduler().scheduleJob(trigger);
            }
        }
    }

    public void setJobDetail(JobDetail jobDetail) {
        this.jobDetail = jobDetail;
    }

    public void setOverwriteExisting(boolean overwriteExisting) {
        this.overwriteExisting = overwriteExisting;
    }

    public void setRamJob(boolean ramJob) {
        isRamJob = ramJob;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    public void setTrigger(Trigger trigger) {
        if (trigger != null) {
            this.triggers.add(trigger);
        }
    }

    public void setTriggers(List<Trigger> triggers) {
        if (triggers != null) {
            this.triggers.addAll(triggers);
        }
    }
    
    protected Map<String, Trigger> mapByName(Trigger[] triggers) {
        if (triggers == null || triggers.length == 0) {
            return Collections.emptyMap();
        }

        Map<String, Trigger> map = new HashMap<String, Trigger>(triggers.length);
        for (Trigger trg : triggers) {
            map.put(trg.getFullName(), trg);
        }

        return map;
    }

}
