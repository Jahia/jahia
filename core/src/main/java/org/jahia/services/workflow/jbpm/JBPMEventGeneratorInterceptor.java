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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm;

import org.drools.core.command.impl.AbstractInterceptor;
import org.kie.api.command.Command;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to generate back-end events for jBPM's internal commands so that we can do things such as invalidate
 * caches, etc...
 */
public class JBPMEventGeneratorInterceptor extends AbstractInterceptor {

    public interface JBPMEventListener {
        public <T> boolean canProcess(Command<T> command);

        public <T> void beforeCommand(Command<T> command);

        public <T> void afterCommand(Command<T> command);
    }

    private static List<JBPMEventListener> listeners = new ArrayList<JBPMEventListener>();

    public static boolean registerListener(JBPMEventListener listener) {
        if (listeners.contains(listener)) {
            return false;
        }
        return listeners.add(listener);
    }

    public static boolean unregisterListener(JBPMEventListener listener) {
        if (!listeners.contains(listener)) {
            return false;
        }
        return listeners.remove(listener);
    }

    public JBPMEventGeneratorInterceptor() {

    }

    public <T> T execute(Command<T> command) {
        for (JBPMEventListener listener : listeners) {
            if (listener.canProcess(command)) {
                listener.beforeCommand(command);
            }
        }
        T result = getNext().execute(command);
        for (JBPMEventListener listener : listeners) {
            if (listener.canProcess(command)) {
                listener.afterCommand(command);
            }
        }
        return result;
    }
}
