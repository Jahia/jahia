package org.jahia.bundles.maintenance;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.settings.readonlymode.ReadOnlyModeController;

@Command(scope = "dx", name = "full-read-only")
@Service
public class FullReadOnlyCommand extends AbstractMaintenanceCommand {

    @Override
    protected String getMaintenanceStatus() {
        return ReadOnlyModeController.getInstance().getReadOnlyStatus().name();
    }

    @Override
    protected void setMaintenanceStatus(boolean enable) {
        ReadOnlyModeController.getInstance().switchReadOnlyMode(enable);
    }
}
