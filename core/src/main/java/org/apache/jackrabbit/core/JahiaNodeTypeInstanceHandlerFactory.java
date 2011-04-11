package org.apache.jackrabbit.core;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 4/11/11
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class JahiaNodeTypeInstanceHandlerFactory implements NodeTypeInstanceHandlerFactory {
    public NodeTypeInstanceHandler getNodeTypeInstanceHandler(String userID) throws RepositoryException {
        return new JahiaNodeTypeInstanceHandler(userID);
    }
}
