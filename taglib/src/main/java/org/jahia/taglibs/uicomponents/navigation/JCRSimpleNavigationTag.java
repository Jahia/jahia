package org.jahia.taglibs.uicomponents.navigation;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.services.content.JCRNodeWrapper;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Nov 6, 2009
 * Time: 3:53:53 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class JCRSimpleNavigationTag extends AbstractJahiaTag {
    // todo: add parameters needed from JCRNavigationTag or remove this tag.
    private static transient final Category logger = Logger.getLogger(JCRNavigationMenuTag.class);
    private JCRNodeWrapper node;
    private String var;

    public JCRNodeWrapper getNode() {
        return node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        Set<JCRNodeWrapper> nodeSet = new LinkedHashSet<JCRNodeWrapper>();

        try {
            final NodeIterator iterator = node.getNodes();
            while (iterator.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) iterator.nextNode();
                if (!nodeWrapper.isNodeType("jnt:page")) {
                    continue;
                }
                nodeSet.add(nodeWrapper);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        pageContext.setAttribute(var, nodeSet);
        return super.doStartTag();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int doEndTag() throws JspException {
        node = null;
        var = null;
        return super.doEndTag();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
