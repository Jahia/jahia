package org.jahia.bin;

import org.apache.log4j.Logger;
import org.jahia.params.ParamBean;
import org.jahia.services.rbac.Permission;
import org.jahia.services.rbac.PermissionIdentity;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.web.servlet.ModelAndView;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Rendering controller for the edit mode.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 4:15:21 PM
 *
 * @see org.jahia.bin.Render
 */
public class Contribute extends Render {
    private static Logger logger = Logger.getLogger(Contribute.class);

    
    protected RenderContext createRenderContext(HttpServletRequest req, HttpServletResponse resp, JahiaUser user) {
        RenderContext context = super.createRenderContext(req, resp, user);
        context.setContributionMode(true);
        return context;
    }

    public static String getContributeServletPath() {
        // TODO move this into configuration
        return "/cms/contribute";
    }

    protected boolean hasAccess(JahiaUser user, String site) {
        if (user == null) {
            return false;
        }

        if (site == null) {
            logger.error("Site key is null.");
        }
        return user.isPermitted(new PermissionIdentity("contribute-mode","actions", site));
    }

}