package org.jahia.modules.social.taglib;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.modules.social.SocialService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Set;
import java.util.SortedSet;

/**
 * TODO Comment me
 *
 * @author loom
 *         Date: Jul 1, 2010
 *         Time: 1:56:41 PM
 */
public class GetSocialActivitiesTag extends AbstractJCRTag {

    private static final long serialVersionUID = 815042079517998908L;
    
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private long limit = 100;
    private long offset = 0;
    private String pathFilter = null;
    private Set<String> sourcePaths;
    private SocialService socialService;

    public int doEndTag() throws JspException {
        try {
            pageContext.setAttribute(var, getActivities(), scope);
        } catch (RepositoryException e) {
            throw new JspException("Error while retrieving the activities!", e);
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        scope = PageContext.PAGE_SCOPE;
        var = null;
        limit = 100;
        offset = 0;
        pathFilter = null;
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

    public void setSourcePaths(Set<String> sourcePaths) {
        this.sourcePaths = sourcePaths;
    }

    public void setPathFilter(String pathFilter) {
        this.pathFilter = pathFilter;
    }

    private SocialService getSocialService() {
        if (socialService == null) {
            socialService = (SocialService) SpringContextSingleton.getModuleBean("socialService");
        }
        return socialService;
    }

    private SortedSet<JCRNodeWrapper> getActivities() throws RepositoryException {
        JCRSessionWrapper session = getJCRSession();
        return getSocialService().getActivities(session, sourcePaths, limit, offset, pathFilter);
    }

}
