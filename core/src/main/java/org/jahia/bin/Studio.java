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
 * @see org.jahia.bin.Render
 */
public class Studio extends Render {
    private static Logger logger = Logger.getLogger(Studio.class);

    public static final String STUDIO_MODE = "studiomode";

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setEditMode(true);
        context.setEditModeConfigName(STUDIO_MODE);
        context.setServletPath(getStudioServletPath());
        return context;
    }

	public static String getStudioServletPath() {
	    // TODO move this into configuration
	    return "/cms/studio";
    }

    protected boolean hasAccess(JahiaUser user, String site) {
        return user != null && user.isPermitted(new PermissionIdentity("global/studio-mode"));
    }
}