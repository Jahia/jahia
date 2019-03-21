package org.jahia.tools.patches;

import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;

import static org.jahia.tools.patches.Patcher.FAILED;
import static org.jahia.tools.patches.Patcher.INSTALLED;

/**
 * Execute graphql mutations in files ending with .graphql
 */
public class GraphqlPatcher implements PatchExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GraphqlPatcher.class);

    @Override
    public boolean canExecute(String name, String lifecyclePhase) {
        return name.endsWith(lifecyclePhase + ".graphql");
    }

    @Override
    public String executeScript(String name, String scriptContent) {
        try {
            JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
            Servlet servlet = BundleUtils.getOsgiService(Servlet.class, "(component.name=graphql.servlet.OsgiGraphQLServlet)");
            String json = (String) servlet.getClass().getMethod("executeQuery", String.class).invoke(servlet, scriptContent);
            logger.info("Graphql execution result : {}", json);
            JSONObject object = new JSONObject(json);
            if (object.has("errors") && object.getJSONArray("errors").length() > 0) {
                return FAILED;
            }
            return INSTALLED;
        } catch (Exception e) {
            logger.error("Execution of script failed with error: " + e.getMessage(), e);
            return FAILED;
        } finally {
            JCRSessionFactory.getInstance().setCurrentUser(null);
        }
    }
}
