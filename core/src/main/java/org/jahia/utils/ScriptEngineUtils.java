/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ScriptEngine provider class.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 27/01/11
 */
public class ScriptEngineUtils {

    private static ScriptEngineUtils instance;
    
    public static ScriptEngineUtils getInstance() {
        if (instance == null) {
            synchronized (ScriptEngineUtils.class) {
                if (instance == null) {
                    instance = new ScriptEngineUtils();
                }
            }
        }
        return instance;
    }
    
    private Map<ClassLoader, Map<String, ScriptEngine>> scriptEngineByExtensionCache;
    private Map<ClassLoader, Map<String, ScriptEngine>> scriptEngineByNameCache;

    private ScriptEngineManager scriptEngineManager;
    
    public ScriptEngineUtils() {
        super();
        scriptEngineManager = new ScriptEngineManager();
        scriptEngineByExtensionCache = new LinkedHashMap<ClassLoader, Map<String, ScriptEngine>>(3);
        scriptEngineByNameCache = new LinkedHashMap<ClassLoader, Map<String, ScriptEngine>>(3);
        try {
            scriptEngineManager.getEngineByExtension("groovy").eval("true");
        } catch (ScriptException e) {
            // Ignore
        }
    }

    /**
     * Returns an instance of a {@link ScriptEngine} by its name.
     * @param name the name of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine getEngineByName(String name) throws ScriptException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (scriptEngineByNameCache.get(contextClassLoader) == null) {
            scriptEngineByNameCache.put(contextClassLoader, new HashMap<String, ScriptEngine>());
        }
        ScriptEngine scriptEngine = scriptEngineByNameCache.get(contextClassLoader).get(name);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByName(name);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for name :" + name);
            }
            initEngine(scriptEngine);
            scriptEngineByNameCache.get(contextClassLoader).put(name, scriptEngine);
        }
        return scriptEngine;
    }

    private void initEngine(ScriptEngine engine) {
        if (engine.getFactory().getNames().contains("velocity")) {
            Properties velocityProperties = new Properties();
            if(System.getProperty("runtime.log.logsystem.log4j.logger")!=null)  {
            	velocityProperties.setProperty("runtime.log.logsystem.log4j.logger", System.getProperty("runtime.log.logsystem.log4j.logger")); 
            } else {
              velocityProperties.setProperty("runtime.log.logsystem.log4j.logger", "root");
            }
            engine.getContext().setAttribute("com.sun.script.velocity.properties", velocityProperties, ScriptContext.GLOBAL_SCOPE);
        }
    }
    
    /**
     * Returns an instance of a {@link ScriptEngine} by its file extension.
     * @param extension the extension of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine scriptEngine(String extension) throws ScriptException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (scriptEngineByExtensionCache.get(contextClassLoader) == null) {
            scriptEngineByExtensionCache.put(contextClassLoader, new HashMap<String, ScriptEngine>());
        }
        ScriptEngine scriptEngine = scriptEngineByExtensionCache.get(contextClassLoader).get(extension);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByExtension(extension);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for extension: " + extension);
            }
            initEngine(scriptEngine);
            scriptEngineByExtensionCache.get(contextClassLoader).put(extension, scriptEngine);
        }
        return scriptEngine;
    }
}
