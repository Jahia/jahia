package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 20, 2009
 * Time: 11:19:36 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Module {

    String getPath();
    
    GWTJahiaNode getNode();
}
