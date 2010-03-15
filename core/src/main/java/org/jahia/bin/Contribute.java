package org.jahia.bin;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Rendering controller for the edit mode.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 * @see org.jahia.bin.Render
 */
public class Contribute extends Render {

    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setContributionMode(true);
        return context;
    }

	public static String getContributeServletPath() {
	    // TODO move this into configuration
	    return "/cms/contribute";
    }

}