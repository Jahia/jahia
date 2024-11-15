/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification;

import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

/**
 * Notification service, based on the Apache Camel framework, for sending different kinds of notifications.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 28 juin 2010
 */
public class CamelNotificationService implements CamelContextAware, DisposableBean, Runnable {

    public class CamelMessage {
        private String target;
        private Object body;
        private Map<String, Object> headers;

        public CamelMessage(String target, Object body, Map<String, Object> headers) {
            this.target = target;
            this.body = body;
            this.headers = headers;
        }

        public String getTarget() {
            return target;
        }

        public Object getBody() {
            return body;
        }

        public Map<String, Object> getHeaders() {
            return headers;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(CamelNotificationService.class);

    private CamelContext camelContext;
    private ProducerTemplate template;
    private Thread queueProcessor;
    private String queueProcessorThreadName = "notificationQueueProcessor";
    private long queueProcessorFrequency = 5000;
    private ConcurrentLinkedQueue<CamelMessage> queue = new ConcurrentLinkedQueue<CamelMessage>();
    private AtomicBoolean queueProcessorRunning = new AtomicBoolean(true);

    public void setQueueProcessorThreadName(String queueProcessorThreadName) {
        this.queueProcessorThreadName = queueProcessorThreadName;
    }

    public void setQueueProcessorFrequency(long queueProcessorFrequency) {
        this.queueProcessorFrequency = queueProcessorFrequency;
    }

    public void setCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
        template = camelContext.createProducerTemplate();
        queueProcessor = new Thread(this, queueProcessorThreadName);
        queueProcessor.setDaemon(true);
        queueProcessor.start();
    }

    public void sendMessagesWithBodyAndHeaders(String target, Object body, Map<String, Object> headers) {
        if (!camelContext.getStatus().isStarted()) {
            return;
        }
        if (headers != null) {
            template.sendBodyAndHeaders(target, body, headers);
        } else {
            template.sendBody(target, body);
        }
    }

    public void queueMessagesWithBodyAndHeaders(String target, Object body, Map<String, Object> headers) {
        queue.add(new CamelMessage(target, body, headers));
    }

    public void registerRoute(RoutesBuilder routesBuilder) throws Exception {
        camelContext.addRoutes(routesBuilder);
    }

    public CamelContext getCamelContext() {
        return camelContext;
    }

	public void destroy() throws Exception {
		if (template != null) {
			template.stop();
		}
        if (queueProcessor != null) {
            queueProcessorRunning.set(false);
        }
    }

    public void run() {
        try {
          while (queueProcessorRunning.get()) {

            int size = queue.size();
            for (int i=0; i < size; i++) {
                CamelMessage message = queue.poll();
                if (message != null) {
                    sendMessagesWithBodyAndHeaders(message.getTarget(), message.getBody(), message.getHeaders());
                } else {
                    break;
                }
            }
            Thread.sleep(queueProcessorFrequency);
          }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
