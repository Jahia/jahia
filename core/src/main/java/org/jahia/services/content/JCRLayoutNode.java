package org.jahia.services.content;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:38:46
 * To change this template use File | Settings | File Templates.
 */
public class JCRLayoutNode extends JCRNodeDecorator {
    public JCRLayoutNode(JCRNodeWrapper node) {
        super(node);
    }

    public List<JCRLayoutItemNode> getLayoutItems() throws RepositoryException {
        List<JCRNodeWrapper> nodeWrappers = getChildren();
        if (nodeWrappers != null) {
            List<JCRLayoutItemNode> nodes = new ArrayList<JCRLayoutItemNode>();
            for (JCRNodeWrapper n : nodeWrappers) {
                nodes.add(new JCRLayoutItemNode(n));
            }
            return nodes;
        }
        return new ArrayList<JCRLayoutItemNode>();
    }

    public boolean isLiveDraggable() throws RepositoryException {
        return getProperty("j:liveDraggable").getBoolean();
    }

    public void setLiveDraggable(boolean liveDraggable) throws RepositoryException {
        setProperty("j:liveDraggable", liveDraggable);
    }

    public boolean isLiveEditable() throws RepositoryException {
        return getProperty("j:liveEditable").getBoolean();
    }

    public void setLiveEditable(boolean liveEditable) throws RepositoryException {
        setProperty("j:liveEditable", liveEditable);
    }

    public long getNbColumns() throws RepositoryException {
        return getProperty("j:nbColumns").getLong();
    }

    public void setNbColumns(long nbColumns) throws RepositoryException {
        setProperty("j:nbColumns", nbColumns);
    }

    public String getPage() throws RepositoryException {
        return getProperty("j:page").getString();
    }

    public void setPage(String page) throws RepositoryException {
        setProperty("j:page", page);
    }

    public JCRLayoutItemNode addLayoutItem(JCRNodeWrapper portletNode, int column, int row, String status) throws RepositoryException {
        JCRNodeWrapper jcrNodeWrapper = addNode("j:item", "jnt:layoutItem");
        JCRLayoutItemNode jcrLayoutItemNode = new JCRLayoutItemNode(jcrNodeWrapper);
        jcrLayoutItemNode.setPortlet(portletNode);
        jcrLayoutItemNode.setColumnIndex(column);
        jcrLayoutItemNode.setRowIndex(row);
        jcrLayoutItemNode.setStatus(status);
        return jcrLayoutItemNode;
    }
}
