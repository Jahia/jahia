package org.jahia.modules.serversettings.flow;

import org.apache.commons.lang.StringUtils;
import org.jahia.modules.serversettings.forge.Forge;
import org.jahia.modules.serversettings.forge.ForgeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.webflow.execution.RequestContext;

import java.io.Serializable;
import java.util.Set;

/**
 *Handler for forge settings
 */

public class ForgesSettingsHandler implements Serializable {

    private static final long serialVersionUID = 3483746034366875864L;
    static Logger logger = LoggerFactory.getLogger(ForgesSettingsHandler.class);


    private Forge forge;

    @Autowired
    private transient ForgeService forgeService;

    public void init() {
        forge = new Forge();
    }

    public void save(RequestContext context) {
        if (StringUtils.equals("add",context.getRequestParameters().get("actionType"))) {
           forgeService.addForge(forge);
        } else if (StringUtils.equals("delete",context.getRequestParameters().get("actionType"))) {
            forgeService.removeForge(forge);
        }
        forgeService.saveForges();
        forgeService.loadModules();
        forge = new Forge();
    }

    public Set<Forge> getForges() {
        return forgeService.getForges();
    }

    public Forge getForge() {
        return forge;
    }

    public void setForge(Forge forge) {
        this.forge = forge;
    }

}
