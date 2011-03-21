package org.jahia.modules.defaultmodule.actions;

import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * An advanced action that will add the specified principals to the specified roles for the resource specified in the
 * URL. Of course proper permissions must be given to execute this action.
 */
public class AddPrincipalsInRolesAction extends Action {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddPrincipalsInRolesAction.class);

    private JahiaGroupManagerService jahiaGroupManagerService;
    private JahiaUserManagerService jahiaUserManagerService;
    private JahiaSitesService jahiaSitesService;

    public void setJahiaGroupManagerService(JahiaGroupManagerService jahiaGroupManagerService) {
        this.jahiaGroupManagerService = jahiaGroupManagerService;
    }

    public void setJahiaUserManagerService(JahiaUserManagerService jahiaUserManagerService) {
        this.jahiaUserManagerService = jahiaUserManagerService;
    }

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {

        if ((parameters.get("principals") != null) &&
            (parameters.get("roles") != null)) {
            List<String> principals = parameters.get("principals");
            List<String> roles = parameters.get("roles");
            for (String principalKey : principals) {
                if (principalKey.startsWith("u:")) {
                    String userKey = principalKey.substring("u:".length());
                    JahiaUser jahiaUser = jahiaUserManagerService.lookupUserByKey(userKey);
                    if (jahiaUser == null) {
                        logger.warn("User " + userKey + " could not be found, will not add to roles");
                        return ActionResult.BAD_REQUEST;
                    }
                    resource.getNode().grantRoles(principalKey, new HashSet(roles));
                    session.save();
                } else if (principalKey.startsWith("g:")) {
                    String groupKey = principalKey.substring("g:".length());
                    JahiaGroup jahiaGroup = jahiaGroupManagerService.lookupGroup(groupKey);
                    if (jahiaGroup == null) {
                        logger.warn("Group " + groupKey + " could not be found, will not add to roles");
                        return ActionResult.BAD_REQUEST;
                    }
                    resource.getNode().grantRoles(principalKey, new HashSet(roles));
                    session.save();
                }
            }
        } else {
            return ActionResult.BAD_REQUEST;
        }
        return ActionResult.OK_JSON;
    }
}
