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

package org.jahia.services.events;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Jahia groovy script engine
 *
 */
public class JahiaGroovyEngine extends GroovyScriptEngine {

   private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaGroovyEngine.class);

     private static class ScriptCacheEntry {
        private Class scriptClass;
        private long lastModified;
        private Map dependencies = new HashMap();
    }

    //private URL[] roots;
    private Map scriptCache = Collections.synchronizedMap(new HashMap());
    private ResourceConnector rc;

    /**
     * The groovy script engine will run groovy scripts and reload them and
     * their dependencies when they are modified. This is useful for embedding
     * groovy in other containers like games and application servers.
     *
     * @param roots This an array of URLs where Groovy scripts will be stored. They should
     *              be layed out using their package structure like Java classes
     */
    public JahiaGroovyEngine(URL[] roots) {
        super(roots);
        this.rc=this;
        logger.debug("length URLS:"+roots.length);
        for(int i=0;i<roots.length;i++){
            logger.debug(i+" "+roots[i].getFile());
        }

    }

     /**
     * Run a script identified by name with dependencies control.
     *
     * @param scriptName name of the script to run
     * @param binding binding to pass to the script
     * @param DependenciesCheck boolean to control dependency check
     * @return an object
     * @throws groovy.util.ResourceException resource issue
     * @throws groovy.util.ScriptException script parsing issue
     */
    public Object run(String scriptName, Binding binding,boolean DependenciesCheck) throws ResourceException, ScriptException {
        ScriptCacheEntry entry = updateCacheEntry(scriptName, getParentClassLoader(),DependenciesCheck);
        Script scriptObject = InvokerHelper.createScript(entry.scriptClass, binding);
        return scriptObject.run();
    }

    /**
     * Locate the class and reload it or any of its dependencies
     * (from private method of parent class)
     * @param scriptName
     * @param parentClassLoader
     * @return the scriptName cache entry
     * @throws ResourceException
     * @throws ScriptException
     */
    private ScriptCacheEntry updateCacheEntry(String scriptName, final ClassLoader parentClassLoader, boolean dependenciesCheck)
            throws ResourceException, ScriptException
    {
        ScriptCacheEntry entry;

        scriptName = scriptName.intern();
        synchronized (scriptName) {

            URLConnection groovyScriptConn = rc.getResourceConnection(scriptName);

            // URL last modified
            long lastModified = groovyScriptConn.getLastModified();
            // Check the cache for the scriptName
            entry = (ScriptCacheEntry) scriptCache.get(scriptName);
            // If the entry isn't null check all the dependencies

            boolean dependencyOutOfDate = false;

            //checks only if dependenciesCheck is requested by caller or script is modified
            if ((entry != null && dependenciesCheck)||(entry != null && entry.lastModified < lastModified)) {

                logger.debug("checking "+entry.dependencies.keySet().size()+" dependencies");
                for (Iterator i = entry.dependencies.keySet().iterator(); i.hasNext();) {
                    URLConnection urlc = null;
                    URL url = (URL) i.next();
                    try {
                        urlc = url.openConnection();
                        urlc.setDoInput(false);
                        urlc.setDoOutput(false);
                        long dependentLastModified = urlc.getLastModified();
                        if (dependentLastModified > ((Long) entry.dependencies.get(url)).longValue()) {
                            dependencyOutOfDate = true;
                            break;
                        }
                    } catch (IOException ioe) {
                        dependencyOutOfDate = true;
                        break;
                    }
                }
                
            }

            if (entry == null || entry.lastModified < lastModified || dependencyOutOfDate) {
                String loggermessage="updating cache entry because ";
                if(entry == null)
                     loggermessage+="entry is null, creating...";
                if(entry != null && entry.lastModified < lastModified)
                     loggermessage+="entry is modified, updating...";
                if(dependencyOutOfDate)
                    loggermessage+="dependencies need to be updated";
                logger.debug(loggermessage);
                // Make a new entry
                entry = new ScriptCacheEntry();

                // Closure variable
                final ScriptCacheEntry finalEntry = entry;

                // Compile the scriptName into an object
                GroovyClassLoader groovyLoader =
                        (GroovyClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return new GroovyClassLoader(parentClassLoader) {
                                    protected Class findClass(String className) throws ClassNotFoundException {
                                        String filename = className.replace('.', File.separatorChar) + ".groovy";
                                        URLConnection dependentScriptConn = null;
                                        try {
                                            dependentScriptConn = rc.getResourceConnection(filename);
                                            finalEntry.dependencies.put(
                                                    dependentScriptConn.getURL(),
                                                    new Long(dependentScriptConn.getLastModified()));
                                        } catch (ResourceException e1) {
                                            throw new ClassNotFoundException("Could not read " + className + ": " + e1);
                                        }
                                        try {
                                            return parseClass(dependentScriptConn.getInputStream(), filename);
                                        } catch (CompilationFailedException e2) {
                                            throw new ClassNotFoundException("Syntax error in " + className + ": " + e2);
                                        } catch (IOException e2) {
                                            throw new ClassNotFoundException("Problem reading " + className + ": " + e2);
                                        }
                                    }
                                };
                            }
                        });

                try {
                    entry.scriptClass = groovyLoader.parseClass(groovyScriptConn.getInputStream(), scriptName);
                } catch (Exception e) {
                    throw new ScriptException("Could not parse scriptName: " + scriptName, e);
                }
                entry.lastModified = lastModified;
                scriptCache.put(scriptName, entry);
            }
        }
        return entry;
    }
}
