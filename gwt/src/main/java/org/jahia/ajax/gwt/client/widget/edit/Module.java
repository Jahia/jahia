package org.jahia.ajax.gwt.client.widget.edit;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Container;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 20, 2009
 * Time: 11:19:36 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Module {

    void onParsed();

    HTML getHtml();

    LayoutContainer getContainer();

    String getModuleId();

    String getPath();
    
    GWTJahiaNode getNode();

    Module getParentModule();

    void setParentModule(Module module);

    void setNode(GWTJahiaNode node);

    String getTemplate();
}
