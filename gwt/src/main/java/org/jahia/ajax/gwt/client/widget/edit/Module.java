package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;

/**
 * Interface defining what is a mdoule on a rendered page in edit mode
 * A module could be selected, edited, dragged etc.
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

    void setDraggable(boolean isDraggable);

    boolean isDraggable();
}
