/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

                    return scriptNames.contains(scriptFactory.getEngineName().trim().toLowerCase()) ||
                            scriptNames.contains(scriptFactory.getLanguageName().trim().toLowerCase());
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
