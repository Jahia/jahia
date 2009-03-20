package org.jahia.services.content;

import javax.jcr.RepositoryException;
import javax.jcr.Node;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 18 mars 2009
 * Time: 16:39:18
 * To change this template use File | Settings | File Templates.
 */
public class JCRLayoutItemNode extends JCRNodeDecorator {

    public JCRLayoutItemNode(JCRNodeWrapper node) {
        super(node);
    }

    public Node getPortlet() throws RepositoryException {
        return getProperty("j:portlet").getNode();
    }

    public void setPortlet(JCRNodeWrapper portletNode) throws RepositoryException {
        setProperty("j:portlet", portletNode);
    }

    public int getColumnIndex() throws RepositoryException {
        return (int) getProperty("j:columnIndex").getLong();
    }

    public void setColumnIndex(int columnIndex) throws RepositoryException {
        setProperty("j:columnIndex", columnIndex);
    }

    public int getRowIndex() throws RepositoryException {
        return (int) getProperty("j:rowIndex").getLong();
    }

    public void setRowIndex(int rowIndex) throws RepositoryException {
        setProperty("j:rowIndex", rowIndex);
    }

    public String getStatus() throws RepositoryException {
        return getProperty("j:status").getString();
    }

    public void setStatus(String status) throws RepositoryException {
        setProperty("j:status", status);
    }
}
