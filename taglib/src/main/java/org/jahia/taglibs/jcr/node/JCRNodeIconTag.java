package org.jahia.taglibs.jcr.node;

import org.apache.jackrabbit.core.nodetype.EffectiveNodeType;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/1/11
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class JCRNodeIconTag extends AbstractJCRTag {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTypeTag.class);

    private JCRNodeWrapper node;
    private ExtendedNodeType type;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setType(ExtendedNodeType type) {
        this.type = type;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public int doStartTag() throws JspException {
        try {
            if (node != null) {
                pageContext.setAttribute(var, JCRContentUtils.getIcon(node), scope);
            } else if (type != null) {
                pageContext.setAttribute(var, JCRContentUtils.getIcon(type), scope);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        resetState();
        return super.doEndTag();
    }

    @Override
    protected void resetState() {
        node = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        super.resetState();
    }

}
