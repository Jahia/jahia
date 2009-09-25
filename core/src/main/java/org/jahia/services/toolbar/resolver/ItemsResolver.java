package org.jahia.services.toolbar.resolver;

import org.jahia.services.toolbar.bean.Item;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 11:52:01 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ItemsResolver {
    public List<Item> getItems(org.jahia.data.JahiaData jData);
}
