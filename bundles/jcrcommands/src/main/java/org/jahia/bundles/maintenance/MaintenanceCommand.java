package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.bin.Jahia;

@Command(scope = "dx", name = "maintenance")
@Service
public class MaintenanceCommand extends AbstractMaintenanceCommand {

    @Override
    protected String getMaintenanceStatus() {
        return (Jahia.isMaintenance() ? "ON" : "OFF");
    }

    @Override
    protected void setMaintenanceStatus(boolean enable) {
        Jahia.setMaintenance(enable);
    }
}
