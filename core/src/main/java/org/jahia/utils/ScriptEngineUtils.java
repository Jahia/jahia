/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils;

import org.springframework.beans.factory.InitializingBean;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
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
public class ScriptEngineUtils implements InitializingBean {

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
    
    private Map<String, ScriptEngine> scriptEngineByExtensionCache;
    private Map<String, ScriptEngine> scriptEngineByNameCache;

    private ScriptEngineManager scriptEngineManager;

    public void afterPropertiesSet() throws Exception {
        scriptEngineManager = new ScriptEngineManager();
        scriptEngineByExtensionCache = new LinkedHashMap<String, ScriptEngine>(3);
        scriptEngineByNameCache = new LinkedHashMap<String, ScriptEngine>(3);
    }

    /**
     * Returns an instance of a {@link ScriptEngine} by its name.
     * @param name the name of the script engine to look for
     * @return an instance of a {@link ScriptEngine} by its name
     * @throws ScriptException in case of a script engine initialization error
     */
    public ScriptEngine getEngineByName(String name) throws ScriptException {
        ScriptEngine scriptEngine = scriptEngineByNameCache.get(name);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByName(name);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for name :" + name);
            }
            initEngine(scriptEngine);
            scriptEngineByNameCache.put(name, scriptEngine);
        }
        return scriptEngine;
    }

    private void initEngine(ScriptEngine engine) {
        if (engine.getFactory().getNames().contains("velocity")) {
            Properties velocityProperties = new Properties();
            velocityProperties.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
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
        ScriptEngine scriptEngine = scriptEngineByExtensionCache.get(extension);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByExtension(extension);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for extension: " + extension);
            }
            initEngine(scriptEngine);
            scriptEngineByExtensionCache.put(extension, scriptEngine);
        }
        return scriptEngine;
    }
}
