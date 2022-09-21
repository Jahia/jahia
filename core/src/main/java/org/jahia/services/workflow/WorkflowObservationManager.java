/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.workflow;

import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.hazelcast.HazelcastTopic;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

public class WorkflowObservationManager implements HazelcastTopic.MessageListener<Map<String,Object>> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(WorkflowObservationManager.class);
    public static final String WORKFLOW_TOPIC = "org.jahia.broadcaster.wf";

    private WorkflowService service;
    private HazelcastTopic hazelcastTopic;
    private String listenerId;
    private List<WorkflowListener> listeners = new ArrayList<WorkflowListener>();

    public WorkflowObservationManager(WorkflowService service) {
        this.service = service;
    }

    public void initAfterAllServicesAreStarted() {
        ServiceTracker<HazelcastTopic, HazelcastTopic> st = new ServiceTracker<>(FrameworkService.getBundleContext(), HazelcastTopic.class, new ServiceTrackerCustomizer<HazelcastTopic, HazelcastTopic>() {
            @Override
            public HazelcastTopic addingService(ServiceReference<HazelcastTopic> serviceReference) {
                HazelcastTopic service = FrameworkService.getBundleContext().getService(serviceReference);
                WorkflowObservationManager.this.hazelcastTopic = service;
                WorkflowObservationManager.this.listenerId = service.addListener("workflowEvents", WorkflowObservationManager.this);
                return service;
            }

            @Override
            public void modifiedService(ServiceReference<HazelcastTopic> serviceReference, HazelcastTopic hazelcastTopic) {
                //kllkj
            }

            @Override
            public void removedService(ServiceReference<HazelcastTopic> serviceReference, HazelcastTopic hazelcastTopic) {
                hazelcastTopic.removeListener("workflowEvents", listenerId);
                WorkflowObservationManager.this.hazelcastTopic = null;
            }
        });
        st.open();
    }

    public void notifyWorkflowStarted(String provider, String workflowId) {
        Workflow wf = service.getWorkflow(provider, workflowId, null);
        notifyWorkflowStarted(wf);
        sendRemote("notifyWorkflowStarted", wf);
    }

    private void notifyWorkflowStarted(Workflow wf) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.workflowStarted(wf);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void notifyWorkflowEnded(String provider, String workflowId) {
        HistoryWorkflow wf = service.getHistoryWorkflow(workflowId, provider, null);
        notifyWorkflowEnded(wf);
        sendRemote("notifyWorkflowEnded", wf);
    }

    private void notifyWorkflowEnded(HistoryWorkflow wf) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.workflowEnded(wf);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void notifyNewTask(String provider, String taskId) {
        WorkflowTask task = service.getWorkflowTask(taskId, provider,null);
        notifyNewTask(task);
        sendRemote("notifyNewTask", task);
    }

    private void notifyNewTask(WorkflowTask task) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.newTaskCreated(task);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void notifyTaskEnded(String provider, String taskId) {
        WorkflowTask task = service.getWorkflowTask(taskId, provider,null);
        notifyTaskEnded(task);
        sendRemote("notifyTaskEnded", task);
    }

    private void notifyTaskEnded(WorkflowTask task) {
        for (WorkflowListener listener : listeners) {
            try {
                listener.taskEnded(task);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void addWorkflowListener(WorkflowListener listener) {
        listeners.add(listener);
    }

    public void sendRemote(String type, Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (hazelcastTopic != null) {
            Map<String, Object> m = new HashMap<>();
            m.put("type", type);
            try (ObjectOutputStream oos = new ObjectOutputStream(out)) {
                oos.writeObject(obj);
                m.put("data", out.toByteArray());
                m.put("source", SettingsBean.getInstance().getPropertyValue("cluster.node.serverId"));
                JobDetail messageJob = BackgroundJob.createJahiaJob("WorkflowMessageJob", MessageJob.class);
                messageJob.getJobDataMap().put("message", m);
                messageJob.getJobDataMap().put("hazelcastTopic",hazelcastTopic);
                ServicesRegistry.getInstance().getSchedulerService().getRAMScheduler().scheduleJob(messageJob,new SimpleTrigger(messageJob.getName() + "_Trigger", new Date(System.currentTimeMillis() + 2000)));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class MessageJob extends BackgroundJob {

        public MessageJob() {
        }

        @Override
        public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
            JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
            Map<String, Object> m = (Map<String, Object>) jobDataMap.get("message");
            HazelcastTopic hazelcastTopic = (HazelcastTopic) jobDataMap.get("hazelcastTopic");
            hazelcastTopic.send("workflowEvents", m);
        }
    }

    @Override
    public void onMessage(Map<String,Object> m) {
        if (!SettingsBean.getInstance().getPropertyValue("cluster.node.serverId").equals(m.get("source"))) {
            Object obj;
            try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream((byte[]) m.get("data")))) {
                obj = is.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (m.get("type").equals("notifyWorkflowStarted")) {
                notifyWorkflowStarted((Workflow) obj);
            } else if (m.get("type").equals("notifyWorkflowEnded")) {
                notifyWorkflowEnded((HistoryWorkflow) obj);
            } else if (m.get("type").equals("notifyNewTask")) {
                notifyNewTask((WorkflowTask) obj);
            } else if (m.get("type").equals("notifyTaskEnded")) {
                notifyTaskEnded((WorkflowTask) obj);
            }
        }
    }
}
