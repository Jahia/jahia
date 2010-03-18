package org.jahia.bin;

import org.apache.log4j.Logger;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.render.RenderContext;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Rendering controller for the edit mode.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 * @see Render
 */
public class Edit extends Render {
    private static Logger logger = Logger.getLogger(Edit.class);
    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setEditMode(true);
        return context;
    }

	public static String getEditServletPath() {
	    // TODO move this into configuration
	    return "/cms/edit";
    }

    protected boolean hasAccess(JahiaUser user, String site) {
        if (user == null) {
            return false;
        }

        if (site == null) {
            logger.error("Site key is null.");
        }
        return user.isPermitted(new PermissionIdentity("edit-mode","actions", site));
    }
}
