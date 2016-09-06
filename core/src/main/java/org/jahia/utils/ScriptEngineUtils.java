/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p>
 * http://www.jahia.com
 * <p>
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 * <p>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p>
 * 1/ GPL
 * ==================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.utils;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.scripting.bundle.BundleScriptEngineManager;
import org.jahia.services.render.scripting.bundle.BundleScriptingConfigurationConstants;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Properties;

/**
 * ScriptEngine provider class.
 *
 * @author rincevent
 * @since JAHIA 6.5 Created : 27/01/11
 */
public class ScriptEngineUtils {
    private final BundleScriptEngineManager enginesManager;


    /**
     * Determines whether the {@link ScriptEngineFactory} associated with the specified extension supports at least one
     * of the script names declared by the given OSGi headers assuming they provide a value for the
     * {@link BundleScriptingConfigurationConstants#JAHIA_MODULE_SCRIPTING_VIEWS} header.
     *
     * @param extension the extension associated with the factory whose support for views defined in the specified
     *                  bundle is to be determined
     * @param headers   the OSGi headers to be checked if declared view technologies defined by the {@link
     *                  BundleScriptingConfigurationConstants#JAHIA_MODULE_SCRIPTING_VIEWS} OSGI header are supported by
     *                  the specified ScriptEngineFactory
     * @return {@code true} if the specified ScriptEngineFactory can handle at least one of the specified script names,
     * {@code false} otherwise. Note that if the headers contain the {@link BundleScriptingConfigurationConstants#JAHIA_MODULE_HAS_VIEWS}
     * header with a "{@code no}" value, we won't look at other headers since the module has indicated that it shouldn't
     * contain any views so the factory shouldn't attempt to process any it might find.
     */
    public static boolean canFactoryForExtensionProcessViews(String extension, Dictionary<String, String> headers) {
        ScriptEngineFactory engineFactory = getInstance().enginesManager.getFactoryForExtension(extension);
        return canFactoryProcessViews(engineFactory, headers);
    }

    /**
     * Determines whether the specified {@link ScriptEngineFactory} supports at least one of the script names
     * declared by the given OSGi headers assuming they provide a value for the
     * {@link BundleScriptingConfigurationConstants#JAHIA_MODULE_SCRIPTING_VIEWS} header.
     *
     * @param scriptFactory the ScriptEngineFactory whose support for views defined in the specified bundle is to be
     *                      determined
     * @param headers       the OSGi headers to be checked if declared view technologies defined by the {@link
     *                      BundleScriptingConfigurationConstants#JAHIA_MODULE_SCRIPTING_VIEWS} OSGI header are
     *                      supported by the specified ScriptEngineFactory
     * @return {@code true} if the specified ScriptEngineFactory can handle at least one of the specified script names,
     * {@code false} otherwise. Note that if the headers contain the {@link BundleScriptingConfigurationConstants#JAHIA_MODULE_HAS_VIEWS}
     * header with a "{@code no}" value, we won't look at other headers since the module has indicated that it shouldn't
     * contain any views so the factory shouldn't attempt to process any it might find.
     */
    public static boolean canFactoryProcessViews(ScriptEngineFactory scriptFactory, Dictionary<String, String> headers) {
        if (scriptFactory == null) {
            throw new IllegalArgumentException("ScriptEngineFactory is null");
        }

        if (headers != null) {

            final String hasViews = headers.get(BundleScriptingConfigurationConstants.JAHIA_MODULE_HAS_VIEWS);
            if ("no".equalsIgnoreCase(StringUtils.trim(hasViews))) {
                // if the bundle indicated that it doesn't provide views, the factory shouldn't process it regardless
                // of other configuration
                return false;
            } else {
                final String commaSeparatedScriptNames = headers.get(BundleScriptingConfigurationConstants.JAHIA_MODULE_SCRIPTING_VIEWS);
                // check if the bundle provided a list of of comma-separated scripting language names for the views it provides
                // the bundle should only be scanned if it defined the header and the header contains the name or language of the factory associated with the extension
                final String[] split = StringUtils.split(commaSeparatedScriptNames, ',');
                if (split != null) {
                    // check extensions
                    final List<String> extensions = scriptFactory.getExtensions();
                    List<String> scriptNames = new ArrayList<>(split.length);
                    for (String scriptName : split) {
                        String script = scriptName.trim().toLowerCase();
                        scriptNames.add(script);
                        if (extensions.contains(script)) {
                            return true;
                        }
                    }

                    return scriptNames.contains(scriptFactory.getEngineName().toLowerCase()) ||
                            scriptNames.contains(scriptFactory.getLanguageName().toLowerCase());
                }
            }
        }
        return false;
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ScriptEngineUtils INSTANCE = new ScriptEngineUtils();
    }

    public static ScriptEngineUtils getInstance() {
        return Holder.INSTANCE;
    }

    private ScriptEngineUtils() {
        super();
        enginesManager = BundleScriptEngineManager.getInstance();
        try {
            enginesManager.getEngineByExtension("groovy").eval("true");
        } catch (ScriptException e) {
            // Ignore
        }
    }

    /**
     * Returns an instance of a {@link ScriptEngine} by its name.
     *
     * @param name the name of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine getEngineByName(String name) throws ScriptException {
        return getScriptEngineFrom(name, false);
    }

    /**
     * Returns an instance of a {@link ScriptEngine} by its file extension.
     *
     * @param extension the extension of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine scriptEngine(String extension) throws ScriptException {
        return getScriptEngineFrom(extension, true);
    }

    private ScriptEngine getScriptEngineFrom(String nameOrExtension, boolean fromExtension) throws ScriptException {
        ScriptEngine scriptEngine = fromExtension ? enginesManager.getEngineByExtension(nameOrExtension) : enginesManager.getEngineByName(nameOrExtension);

        if (scriptEngine == null) {
            throw new ScriptException("Script engine not found for " + (fromExtension ? "extension: " : "name: ") + nameOrExtension);
        }
        initEngine(scriptEngine);
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
