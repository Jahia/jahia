package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.bean.ItemsGroup;
import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.toolbar.resolver.ItemsResolver;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.data.JahiaData;

import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 11:49:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class StoredPasswordsItemsResolver implements ItemsResolver {
    public List<Item> getItems(JahiaData jData) {
        try {
            Map<String,String> pass = JCRSessionFactory.getInstance().getThreadSession(jData.getProcessingContext().getUser()).getStoredPasswordsProviders();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return null;
    }
}
