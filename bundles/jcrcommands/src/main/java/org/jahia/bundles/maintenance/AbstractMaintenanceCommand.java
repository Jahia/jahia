package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.support.completers.StringsCompleter;

public abstract class AbstractMaintenanceCommand implements Action {

    @Argument(description = "on/off")
    @Completion(value = StringsCompleter.class , values = {"ON", "OFF"})
    private String enable;

    @Override
    public Object execute() throws Exception {
        try {
            if (enable != null) {
                if (!enable.equalsIgnoreCase("on") && !enable.equalsIgnoreCase("off")) {
                    System.out.println("Please choose ON or OFF");
                } else {
                    setMaintenanceStatus(enable.equalsIgnoreCase("on"));
                }
            }
        } finally {
            System.out.println("Current status: " + getMaintenanceStatus());
        }
        return null;
    }

    abstract protected String getMaintenanceStatus();

    abstract protected void setMaintenanceStatus(boolean enable);
}
