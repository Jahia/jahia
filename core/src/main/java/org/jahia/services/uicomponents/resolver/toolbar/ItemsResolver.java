package org.jahia.services.uicomponents.resolver.toolbar;

import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.usermanager.JahiaUser;

import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 11:52:01 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ItemsResolver {
    public List<Item> getItems(JCRSiteNode site, JahiaUser user, Locale locale);
}
