package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 6, 2010
 * Time: 7:54:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataTabItem extends PropertiesTabItem {
    public MetadataTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_metadata", "Metadata"), engine, GWTJahiaItemDefinition.METADATA);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineTabMetadata());
        excludedTypes = Arrays.asList("jmix:categorized", "jmix:tagged");
    }

}
