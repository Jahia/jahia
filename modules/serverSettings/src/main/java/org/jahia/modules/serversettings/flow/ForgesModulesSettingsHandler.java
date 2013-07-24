package org.jahia.modules.serversettings.flow;

import org.jahia.modules.serversettings.forge.ForgeService;
import org.jahia.modules.serversettings.forge.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Handler for modules from forges
 */



public class ForgesModulesSettingsHandler implements Serializable {

    private static final long serialVersionUID = -8659813895109454723L;

    @Autowired
    private transient ForgeService forgeService;

    private Set<Module> modules;
    static Logger logger = LoggerFactory.getLogger(ForgesModulesSettingsHandler.class);

    public void init() {
        modules = forgeService.loadModules();
    }

    public Set<Module> getModules() {
        return modules;
    }

}
