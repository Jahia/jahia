/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
