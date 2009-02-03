/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
