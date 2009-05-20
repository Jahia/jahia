/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.hibernate.cache;

import org.hibernate.cache.TreeCache;
import org.hibernate.cache.CacheException;
import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.transaction.TransactionManagerLookupFactory;
import org.jboss.cache.PropertyConfigurator;

import javax.transaction.TransactionManager;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 23 janv. 2006
 * Time: 12:24:02
 * To change this template use File | Settings | File Templates.
 */
public class TreeCacheProvider implements org.hibernate.cache.CacheProvider {

    private static org.jboss.cache.TreeCache cache;
    private TransactionManager transactionManager;

    /**
     * Construct and configure the Cache representation of a named cache region.
     *
     * @param regionName the name of the cache region
     * @param properties configuration settings
     * @return The Cache representation of the named cache region.
     * @throws CacheException Indicates an error building the cache region.
     */
    public org.hibernate.cache.Cache buildCache(String regionName, Properties properties) throws CacheException {
        return new TreeCache(cache, regionName, transactionManager);
    }

    public long nextTimestamp() {
        return System.currentTimeMillis() / 100;
    }

    /**
     * Prepare the underlying JBossCache TreeCache instance.
     *
     * @param properties All current config settings.
     *
     * @throws CacheException Indicates a problem preparing cache for use.
     */
    public void start(Properties properties) {
        try {
            cache = new org.jboss.cache.TreeCache();
            PropertyConfigurator config = new PropertyConfigurator();
            config.configure(cache, "cache.xml");
            TransactionManagerLookup transactionManagerLookup = TransactionManagerLookupFactory.getTransactionManagerLookup(properties);
            if (transactionManagerLookup!=null) {
                cache.setTransactionManagerLookup( new TransactionManagerLookupAdaptor(transactionManagerLookup, properties) );
                transactionManager = transactionManagerLookup.getTransactionManager(properties);
            }
            cache.start();
        }
        catch (Exception e) {
            throw new CacheException(e);
        }
    }

    public void stop() {
        if (cache!=null) {
            cache.stop();
            cache.destroy();
            cache=null;
        }
    }

    public static org.jboss.cache.TreeCache getCache() {
        return cache;
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    static final class TransactionManagerLookupAdaptor implements org.jboss.cache.TransactionManagerLookup {
        private final TransactionManagerLookup tml;
        private final Properties props;
        TransactionManagerLookupAdaptor(TransactionManagerLookup tml, Properties props) {
            this.tml=tml;
            this.props=props;
        }
        public TransactionManager getTransactionManager() throws Exception {
            return tml.getTransactionManager(props);
        }
    }
}
