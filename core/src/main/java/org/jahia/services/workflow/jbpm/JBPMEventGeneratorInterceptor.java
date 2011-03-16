package org.jahia.services.workflow.jbpm;

import org.jbpm.api.cmd.Command;
import org.jbpm.pvm.internal.svc.Interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to generate back-end events for jBPM's internal commands so that we can do things such as invalidate
 * caches, etc...
 */
public class JBPMEventGeneratorInterceptor extends Interceptor {

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
        T result = next.execute(command);
        for (JBPMEventListener listener : listeners) {
            if (listener.canProcess(command)) {
                listener.afterCommand(command);
            }
        }
        return result;
    }
}
