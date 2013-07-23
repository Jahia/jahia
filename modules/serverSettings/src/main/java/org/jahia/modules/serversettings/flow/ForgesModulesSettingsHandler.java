package org.jahia.modules.serversettings.flow;

import org.jahia.modules.serversettings.forge.Module;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Handler for modules from forges
 */



public class ForgesModulesSettingsHandler implements Serializable {

    private static final long serialVersionUID = -8659813895109454723L;
    @Autowired
    private transient JCRTemplate template;

    @Autowired
    private transient HttpClientService httpClientService;

    private List<Module> modules;
    static Logger logger = LoggerFactory.getLogger(ForgesModulesSettingsHandler.class);

    public void init() {
        loadForges();
    }

    public List<Module> getModules() {
        return modules;
    }

    private void loadForges() {
        // fill forges
        try {
            template.doExecuteWithSystemSession(new JCRCallback<Object>() {
                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    // get All modules from the forge
                    if (session.getNode("/").hasNode("settings") && session.getNode("/settings").hasNode("forgesSettings")) {
                        NodeIterator forges = session.getNode("/settings/forgesSettings").getNodes();
                        while (forges.hasNext()) {
                            Node forge = forges.nextNode();
                            if (forge.isNodeType("jnt:forgeServerSettings")) {
                                String url = forge.getProperty("j:url").getString()+"/contents/forge-modules-repository.forgeModuleList.json";
                                String jsonModuleList = httpClientService.executeGet(url);
                                try {
                                    JSONArray modulesRoot = new JSONArray(jsonModuleList);

                                    JSONArray moduleList = modulesRoot.getJSONObject(0).getJSONArray("modules");
                                    for (int i = 0; i < moduleList.length(); i++ ) {
                                        Module module = new Module();
                                        module.setVersion(moduleList.getJSONObject(i).getString("version"));
                                        module.setTitle(moduleList.getJSONObject(i).getString("title"));
                                        module.setName(moduleList.getJSONObject(i).getString("name"));
                                        module.setDownloadUrl(moduleList.getJSONObject(i).getString("downloadUrl"));
                                        module.setUsed(session.getNode("/modules").hasNode(moduleList.getJSONObject(i).getString("name")));
                                        if (modules == null) {
                                            modules = new ArrayList<Module>();
                                        }
                                        modules.add(module);
                                    }
                                } catch (JSONException e) {
                                    logger.error("unable to parse JSON return string for " + url);
                                }
                            }
                        }
                    }
                    return null;
                }
            });

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
