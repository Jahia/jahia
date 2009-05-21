/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache.jboss;

import org.jboss.cache.TransactionManagerLookup;
import org.jboss.cache.transaction.DummyTransactionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.transaction.TransactionManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * User: Serge Huber
 * Date: 31 oct. 2005
 * Time: 16:46:52
 * Copyright (C) Jahia Inc.
 */
public class TomcatTransactionManagerLookup implements TransactionManagerLookup {

    /**
     * our logger
     */
    private static Log log= LogFactory.getLog(TomcatTransactionManagerLookup.class);

    /**
     * lookups performed?
     */
    private static boolean lookupDone=false;

    /**
     * no lookup available?
     */
    private static boolean lookupFailed=false;

    /**
     * the (final) used TransactionManager
     */
    private static TransactionManager tm=null;

    /**
     * JNDI locations for TransactionManagers we know of
     */
    private static String[][] knownJNDIManagers={
       {"java:comp/env/UserTransaction", "Tomcat"},
       {"java:/TransactionManager", "JBoss, JRun4"},
       {"java:comp/UserTransaction", "Resin, Orion, JOnAS (JOTM)"},
       {"javax.transaction.TransactionManager", "BEA WebLogic"}
    };

    /**
     * Get the systemwide used TransactionManager
     *
     * @return TransactionManager
     */
    public TransactionManager getTransactionManager() {
       if(!lookupDone)
          doLookups();
       if(tm != null)
          return tm;
       if(lookupFailed) {
          //fall back to a dummy from JBossCache
          tm= DummyTransactionManager.getInstance();
          log.warn("Falling back to DummyTransactionManager from JBossCache");
       }
       return tm;
    }

    /**
     * Try to figure out which TransactionManager to use
     */
    private static void doLookups() {
       if(lookupFailed)
          return;
       InitialContext ctx;
       try {
          ctx=new InitialContext();
       }
       catch(NamingException e) {
          log.error("Could not create an initial JNDI context!", e);
          lookupFailed=true;
          return;
       }
       //probe jndi lookups first
       Object jndiObject=null;
       for(int i=0; i < knownJNDIManagers.length; i++) {
          try {
             if(log.isDebugEnabled()) log.debug("Trying to lookup TransactionManager for " + knownJNDIManagers[i][1]);
             jndiObject=ctx.lookup("java:comp/env/UserTransaction");
          }
          catch(NamingException e) {
             log.info("Failed to perform a lookup for [" + knownJNDIManagers[i][0] + " (" + knownJNDIManagers[i][1] + ")]");
          }
          if(jndiObject instanceof TransactionManager) {
             tm=(TransactionManager)jndiObject;
             log.info("Found TransactionManager for " + knownJNDIManagers[i][1]);
             return;
          }
       }
    }

}
