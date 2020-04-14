/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow.jbpm;

import org.drools.core.time.*;
import org.drools.core.time.Job;
import org.drools.core.time.Trigger;
import org.drools.core.time.impl.TimerJobInstance;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.process.core.timer.NamedJobContext;
import org.jbpm.process.core.timer.SchedulerServiceInterceptor;
import org.jbpm.process.core.timer.impl.DelegateSchedulerServiceInterceptor;
import org.jbpm.process.core.timer.impl.GlobalTimerService;
import org.jbpm.process.core.timer.impl.QuartzSchedulerService;
import org.jbpm.process.core.timer.impl.QuartzSchedulerService.GlobalQuartzJobHandle;
import org.jbpm.process.core.timer.impl.QuartzSchedulerService.InmemoryTimerJobInstanceDelegate;
import org.jbpm.process.instance.timer.TimerManager.ProcessJobContext;
import org.jbpm.process.instance.timer.TimerManager.StartProcessJobContext;
import org.quartz.*;
import org.quartz.Scheduler;
import org.quartz.impl.jdbcjobstore.JobStoreCMT;
import org.quartz.impl.jdbcjobstore.JobStoreSupport;
import org.quartz.spi.JobStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.NotSerializableException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Quartz based <code>GlobalSchedulerService</code> that is configured according to Quartz rules and allows to store jobs in data base. With
 * that it survives server crashes and operates as soon as service is initialized without session being active.
 * 
 * Note, please, this is a copy of the jBPM's {@link QuartzSchedulerService} code, which uses DX scheduler instead of creating a new one.
 */
public class JahiaQuartzSchedulerService implements GlobalSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(JahiaQuartzSchedulerService.class);

    private AtomicLong idCounter = new AtomicLong();
    private TimerService globalTimerService;
    private SchedulerServiceInterceptor interceptor = new DelegateSchedulerServiceInterceptor(this);

    // global data shared across all scheduler service instances
    private Scheduler scheduler;

    public JahiaQuartzSchedulerService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public JobHandle scheduleJob(Job job, JobContext ctx, Trigger trigger) {
        Long id = idCounter.getAndIncrement();
        String jobname = null;

        if (ctx instanceof ProcessJobContext) {
            ProcessJobContext processCtx = (ProcessJobContext) ctx;
            jobname = processCtx.getSessionId() + "-" + processCtx.getProcessInstanceId() + "-" + processCtx.getTimer().getId();
            if (processCtx instanceof StartProcessJobContext) {
                jobname = "StartProcess-"+((StartProcessJobContext) processCtx).getProcessId()+ "-" + processCtx.getTimer().getId();
            }
        } else if (ctx instanceof NamedJobContext) {
            jobname = ((NamedJobContext) ctx).getJobName();
        } else {
            jobname = "Timer-"+ctx.getClass().getSimpleName()+ "-" + id;

        }

        // check if this scheduler already has such job registered if so there is no need to schedule it again
        try {
            JobDetail jobDetail = scheduler.getJobDetail(jobname, "jbpm");

            if (jobDetail != null) {
                TimerJobInstance timerJobInstance = (TimerJobInstance) jobDetail.getJobDataMap().get("timerJobInstance");
                return timerJobInstance.getJobHandle();
            }
        } catch (SchedulerException e) {

        }
        GlobalQuartzJobHandle quartzJobHandle = new GlobalQuartzJobHandle(id, jobname, "jbpm");
        TimerJobInstance jobInstance = ((AcceptsTimerJobFactoryManager) globalTimerService).
                getTimerJobFactoryManager().createTimerJobInstance( job,
                ctx,
                trigger,
                quartzJobHandle,
                (InternalSchedulerService) globalTimerService );
        quartzJobHandle.setTimerJobInstance( (TimerJobInstance) jobInstance );

        interceptor.internalSchedule(jobInstance);
        return quartzJobHandle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean removeJob(JobHandle jobHandle) {
        GlobalQuartzJobHandle quartzJobHandle = (GlobalQuartzJobHandle) jobHandle;

        try {

            boolean removed =  scheduler.deleteJob(quartzJobHandle.getJobName(), quartzJobHandle.getJobGroup());
            return removed;
        } catch (SchedulerException e) {

            throw new RuntimeException("Exception while removing job", e);
        } catch (RuntimeException e) {
            SchedulerMetaData metadata;
            try {
                metadata = scheduler.getMetaData();
                if (metadata.getJobStoreClass().isAssignableFrom(JobStoreCMT.class)) {
                    return true;
                }
            } catch (SchedulerException e1) {

            }
            throw e;
        }
    }

    @Override
    public void internalSchedule(TimerJobInstance timerJobInstance) {

        GlobalQuartzJobHandle quartzJobHandle = (GlobalQuartzJobHandle) timerJobInstance.getJobHandle();
        // Define job instance
        JobDetail jobq = new JobDetail(quartzJobHandle.getJobName(), quartzJobHandle.getJobGroup(), QuartzJob.class);

        jobq.getJobDataMap().put("timerJobInstance", timerJobInstance);

        // Define a Trigger that will fire "now"
        org.quartz.Trigger triggerq = new SimpleTrigger(quartzJobHandle.getJobName()+"_trigger", quartzJobHandle.getJobGroup(), timerJobInstance.getTrigger().hasNextFireTime());

        // Schedule the job with the trigger
        try {
            if (scheduler.isShutdown()) {
                return;
            }
            ((AcceptsTimerJobFactoryManager) globalTimerService).getTimerJobFactoryManager().addTimerJobInstance( timerJobInstance );
            JobDetail jobDetail = scheduler.getJobDetail(quartzJobHandle.getJobName(), quartzJobHandle.getJobGroup());
            if (jobDetail == null) {
                scheduler.scheduleJob(jobq, triggerq);
            } else {
                // need to add the job again to replace existing especially important if jobs are persisted in db
                scheduler.addJob(jobq, true);
                triggerq.setJobName(quartzJobHandle.getJobName());
                triggerq.setJobGroup(quartzJobHandle.getJobGroup());
                scheduler.rescheduleJob(quartzJobHandle.getJobName()+"_trigger", quartzJobHandle.getJobGroup(), triggerq);
            }

        } catch (ObjectAlreadyExistsException e) {
            // in general this should not happen even in clustered environment but just in case
            // already registered jobs should be caught in scheduleJob but due to race conditions it might not
            // catch it in time - clustered deployments only
            logger.warn("Job has already been scheduled, most likely running in cluster: {}", e.getMessage());

        } catch (JobPersistenceException e) {
            if (e.getCause() instanceof NotSerializableException) {
                // in case job cannot be persisted, like rule timer then make it in memory
                internalSchedule(new InmemoryTimerJobInstanceDelegate(quartzJobHandle.getJobName(), ((GlobalTimerService) globalTimerService).getTimerServiceId()));
            } else {
                ((AcceptsTimerJobFactoryManager) globalTimerService).getTimerJobFactoryManager().removeTimerJobInstance(timerJobInstance);
                throw new RuntimeException(e);
            }
        } catch (SchedulerException e) {
            ((AcceptsTimerJobFactoryManager) globalTimerService).getTimerJobFactoryManager().removeTimerJobInstance(timerJobInstance);
            throw new RuntimeException("Exception while scheduling job", e);
        }
    }

    @Override
    public synchronized void initScheduler(TimerService timerService) {
        this.globalTimerService = timerService;
    }

    @Override
    public void shutdown() {
    }

    public void forceShutdown() {
    }

    public static class QuartzJob extends org.jbpm.process.core.timer.impl.QuartzSchedulerService.QuartzJob {

        @Override
        public void execute(JobExecutionContext quartzContext) throws JobExecutionException {
            try {
                super.execute(quartzContext);
            } finally {
                JcrSessionFilter.endRequest();
            }
        }
    }

    @Override
    public JobHandle buildJobHandleForContext(NamedJobContext ctx) {
        return new GlobalQuartzJobHandle(-1, ctx.getJobName(), "jbpm");
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isTransactional() {
        try {
            Class<JobStore> jobStoreClass = scheduler.getMetaData().getJobStoreClass();
            if (JobStoreSupport.class.isAssignableFrom(jobStoreClass)) {
                return true;
            }
        } catch (Exception e) {
            logger.warn("Unable to determine if quartz is transactional due to problems when checking job store class", e);
        }
        return false;
    }

    @Override
    public void setInterceptor(SchedulerServiceInterceptor interceptor) {
        this.interceptor = interceptor;

    }

    @Override
    public boolean retryEnabled() {
        return false;
    }
}
