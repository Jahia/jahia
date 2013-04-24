package org.jahia.data.templates;

public class ModuleState {
    public static enum State {
        UNINSTALLED, UNRESOLVED, RESOLVED, WAITING_TO_BE_PARSED, PARSED, INSTALLED, UPDATED, STOPPED, STOPPING, STARTING, WAITING_TO_BE_STARTED, ERROR_DURING_START, STARTED;
    }

    private State state;

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
