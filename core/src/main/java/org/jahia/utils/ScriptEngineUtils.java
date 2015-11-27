/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */
package org.jahia.utils;

import org.jahia.services.render.scripting.bundle.BundleScriptEngineManager;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
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

    private ScriptEngineUtils() {
        super();
        scriptEngineByExtensionCache = new ConcurrentHashMap<>(7);
        scriptEngineByNameCache = new ConcurrentHashMap<>(7);
        try {
            BundleScriptEngineManager.getInstance().getEngineByExtension("groovy").eval("true");
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
        return getScriptEngineFrom(name, false);
    }

    /**
     * Returns an instance of a {@link ScriptEngine} by its file extension.
     * @param extension the extension of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine scriptEngine(String extension) throws ScriptException {
        return getScriptEngineFrom(extension, true);
    }

    private ScriptEngine getScriptEngineFrom(String nameOrExtension, boolean fromExtension) throws ScriptException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final Map<ClassLoader, Map<String, ScriptEngine>> cache = fromExtension ? scriptEngineByExtensionCache : scriptEngineByNameCache;

        Map<String, ScriptEngine> stringScriptEngineMap = cache.get(contextClassLoader);

        if (stringScriptEngineMap == null) {
            stringScriptEngineMap = new ConcurrentHashMap<>();
            cache.put(contextClassLoader, stringScriptEngineMap);
        }

        ScriptEngine scriptEngine = stringScriptEngineMap.get(nameOrExtension);

        if (scriptEngine == null) {
            scriptEngine = fromExtension ?
                    BundleScriptEngineManager.getInstance().getEngineByExtension(nameOrExtension) :
                    BundleScriptEngineManager.getInstance().getEngineByName(nameOrExtension);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for " + (fromExtension ? "extension: " : "name: ") + nameOrExtension);
            }
            initEngine(scriptEngine);
            stringScriptEngineMap.put(nameOrExtension, scriptEngine);
        }
        return scriptEngine;
    }

    private void initEngine(ScriptEngine engine) {
        if (engine.getFactory().getNames().contains("velocity")) {
            final Properties velocityProperties = new Properties();
            final String key = "runtime.log.logsystem.log4j.logger";
            final String log4jLoggerProp = System.getProperty(key);
            if (log4jLoggerProp != null) {
                velocityProperties.setProperty(key, log4jLoggerProp);
            } else {
                velocityProperties.setProperty(key, "root");
            }
            engine.getContext().setAttribute("com.sun.script.velocity.properties", velocityProperties, ScriptContext.GLOBAL_SCOPE);
        }
    }
}
