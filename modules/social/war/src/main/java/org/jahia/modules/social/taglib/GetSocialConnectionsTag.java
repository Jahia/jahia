package org.jahia.modules.social.taglib;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.modules.social.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Set;

/**
 * A tag that retrieves the list of paths corresponding to the user's connections.
 *
 * @author loom
 *         Date: Jul 1, 2010
 *         Time: 1:56:19 PM
 */
public class GetSocialConnectionsTag extends AbstractJCRTag {

    private static final long serialVersionUID = -2967779565265433297L;
    
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String path;
    private boolean includeSelf = true;
    private long limit;
    private long offset;
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
        includeSelf = true;
        limit = 0;
        offset = 0;
        super.resetState();
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setIncludeSelf(boolean includeSelf) {
        this.includeSelf = includeSelf;
    }

    private SocialService getSocialService() {
        if (socialService == null) {
            socialService = (SocialService) SpringContextSingleton.getModuleBean("socialService");
        }
        return socialService;
    }

    private Set<String> getConnections() throws RepositoryException {
        return getSocialService().getUserConnections(path, includeSelf);
    }
}
