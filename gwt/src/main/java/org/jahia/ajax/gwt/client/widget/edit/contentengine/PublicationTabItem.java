package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jan 13, 2010
 * Time: 11:06:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationTabItem extends PropertiesTabItem {
    public PublicationTabItem(NodeHolder engine) {
        super(Messages.get("label.publication", "Publication"), engine, GWTJahiaItemDefinition.CACHE);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabPublication());
    }
}
