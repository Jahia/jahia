package org.jahia.services.uicomponents.resolver.toolbar.impl;

import org.jahia.ajax.gwt.client.widget.toolbar.action.RedirectWindowActionItem;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.uicomponents.bean.toolbar.Property;
import org.jahia.services.uicomponents.resolver.toolbar.ItemsResolver;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:22:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DefaultItemsResolver implements ItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DefaultItemsResolver.class);

    protected Item createJsRedirectItem(String itemTitle, String jsParamName) throws JahiaException {
        // to do resolve the node url
        if (jsParamName == null) {
            jsParamName = "http://www.jahia.org";
        }
        String title = itemTitle;


        // create the toolitem
        Item item = new Item();
        item.setTitle(title);
        item.setActionItem(new RedirectWindowActionItem());
        item.setDisplayTitle(true);

        // add url property
        Property property = new Property();
        property.setName("js.url");
        property.setValue(jsParamName);
        item.addProperty(property);




        return item;

    }


}
