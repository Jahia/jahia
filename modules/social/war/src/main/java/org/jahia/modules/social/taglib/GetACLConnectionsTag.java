package org.jahia.modules.social.taglib;

import org.apache.log4j.Logger;
import org.jahia.modules.social.SocialService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * A tag to retrieve all the user's in an ACL.
 */
public class GetACLConnectionsTag extends AbstractJCRTag {

    private static final Logger logger = Logger.getLogger(GetSocialActivitiesTag.class);
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String path;
    private SocialService socialService;

    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(var, getConnections(), scope);
        } catch (RepositoryException e) {
            throw new JspException("Error while retrieving the "+path+" connections!", e);
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        scope = PageContext.PAGE_SCOPE;
        var = null;
        path = null;
        super.resetState();
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public void setVar(String var) {
        this.var = var;
    }

    private SocialService getSocialService() {
        if (socialService == null) {
            socialService = (SocialService) ((ApplicationContext)pageContext.getServletContext().getAttribute(WebApplicationContext.class.getName() + ".jahiaTemplates")).getBean("socialService");
        }
        return socialService;
    }

    private Set<String> getConnections() throws RepositoryException {
        JCRSessionWrapper session = getJCRSession();
        JCRNodeWrapper targetNode = session.getNode(path);
        Set<String> aclConnections = getSocialService().getACLConnections(session, targetNode);
        return aclConnections;
    }

}
