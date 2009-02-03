package org.jahia.admin;

import javax.portlet.Portlet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Feb 2, 2009
 * Time: 9:46:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class AdministrationModulesRegistry {

    private List<AdministrationModule> serverModules = new ArrayList<AdministrationModule>();
    private List<AdministrationModule> siteModules = new ArrayList<AdministrationModule>();
    private Map<String, AdministrationModule> serverModulesByUrlKey = new HashMap<String, AdministrationModule>();
    private Map<String, AdministrationModule> siteModulesByUrlKey = new HashMap<String, AdministrationModule>();

    public AdministrationModulesRegistry() {

    }

    public List<AdministrationModule> getServerModules() {
        return serverModules;
    }

    public void setServerModules(List<AdministrationModule> serverModules) {
        this.serverModules = serverModules;
        for (AdministrationModule currentModule : serverModules) {
            currentModule.setServerModule(true);
            serverModulesByUrlKey.put(currentModule.getUrlKey(), currentModule);
        }
    }

    public List<AdministrationModule> getSiteModules() {
        return siteModules;
    }

    public void setSiteModules(List<AdministrationModule> siteModules) {
        this.siteModules = siteModules;
        for (AdministrationModule currentModule : siteModules) {
            currentModule.setServerModule(false);
            siteModulesByUrlKey.put(currentModule.getUrlKey(), currentModule);
        }
    }

    public AdministrationModule getServerAdministrationModule(String moduleKey) {
        return serverModulesByUrlKey.get(moduleKey);
    }

    public AdministrationModule getSiteAdministrationModule(String moduleKey) {
        return siteModulesByUrlKey.get(moduleKey);
    }

}
