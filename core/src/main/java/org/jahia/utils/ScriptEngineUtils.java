/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ScriptEngine provider class.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 27/01/11
 */
public class ScriptEngineUtils {

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ScriptEngineUtils INSTANCE = new ScriptEngineUtils();
    }

    public static ScriptEngineUtils getInstance() {
        return Holder.INSTANCE;
    }

    private Map<ClassLoader, Map<String, ScriptEngine>> scriptEngineByExtensionCache;
    private Map<ClassLoader, Map<String, ScriptEngine>> scriptEngineByNameCache;

    private ScriptEngineManager scriptEngineManager;

    private ScriptEngineUtils() {
        super();
        scriptEngineManager = new ScriptEngineManager();
        scriptEngineByExtensionCache = new ConcurrentHashMap<ClassLoader, Map<String, ScriptEngine>>(3);
        scriptEngineByNameCache = new ConcurrentHashMap<ClassLoader, Map<String, ScriptEngine>>(3);
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

        Map<String, ScriptEngine> stringScriptEngineMap = scriptEngineByNameCache.get(contextClassLoader);

        if (stringScriptEngineMap == null) {
            stringScriptEngineMap = new ConcurrentHashMap<String, ScriptEngine>();
            scriptEngineByNameCache.put(contextClassLoader, stringScriptEngineMap);
        }

        ScriptEngine scriptEngine = stringScriptEngineMap.get(name);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByName(name);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for name :" + name);
            }
            initEngine(scriptEngine);
            stringScriptEngineMap.put(name, scriptEngine);
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

        Map<String, ScriptEngine> stringScriptEngineMap = scriptEngineByExtensionCache.get(contextClassLoader);

        if (stringScriptEngineMap == null) {
            stringScriptEngineMap = new ConcurrentHashMap<String, ScriptEngine>();
            scriptEngineByExtensionCache.put(contextClassLoader, stringScriptEngineMap);
        }

        ScriptEngine scriptEngine = stringScriptEngineMap.get(extension);

        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByExtension(extension);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for extension: " + extension);
            }
            initEngine(scriptEngine);
            stringScriptEngineMap.put(extension, scriptEngine);
        }
        return scriptEngine;
    }
}
