package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.widget.form.ComboBox;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jan 13, 2010
 * Time: 11:06:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class PublicationTabItem extends PropertiesTabItem {
    public PublicationTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_publication", "Publication"), engine, GWTJahiaItemDefinition.PUBLICATION);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabPublication());
    }
}
