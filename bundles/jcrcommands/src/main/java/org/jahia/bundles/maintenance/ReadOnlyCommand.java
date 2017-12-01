package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.bin.Jahia;

@Command(scope = "dx", name = "read-only")
@Service
public class ReadOnlyCommand extends AbstractMaintenanceCommand {

    @Override
    protected String getMaintenanceStatus() {
        return (Jahia.getSettings().isReadOnlyMode() ? "ON" : "OFF");
    }

    @Override
    protected void setMaintenanceStatus(boolean enable) {
        Jahia.getSettings().setReadOnlyMode(enable);
    }
}
