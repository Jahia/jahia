package org.jahia.taglibs.jcr.node;

import org.jahia.services.content.JCRNodeWrapper;
import org.apache.taglibs.standard.tag.common.core.Util;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 3, 2009
 * Time: 7:11:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRNodeVersionTag extends TagSupport {

    private JCRNodeWrapper node;
    private String versionName;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }


    @Override
    public int doStartTag() throws JspException {
        try {
            JCRNodeWrapper version = (JCRNodeWrapper) node.getVersionHistory().getVersion(versionName);
            pageContext.setAttribute(var, version, scope);            
        } catch (RepositoryException e) {
            throw new JspException(e);
        }
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        node = null;
        versionName = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }
}
