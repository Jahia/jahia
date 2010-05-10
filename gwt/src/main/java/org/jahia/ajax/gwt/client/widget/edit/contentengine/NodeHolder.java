package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 4:51:14 PM
 * To change this template use File | Settings | File Templates.
 */
public interface NodeHolder {
    List<GWTJahiaNodeType> getNodeTypes();

    List<GWTJahiaNodeType> getMixin();

    GWTJahiaNode getNode();

    List<GWTJahiaNode> getNodes();

    GWTJahiaNode getParentNode();

    boolean isExistingNode();

    boolean isMultipleSelection();

    Map<String, GWTJahiaNodeProperty> getProperties();
}
