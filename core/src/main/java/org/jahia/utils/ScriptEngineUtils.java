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
package org.jahia.utils;

import org.apache.log4j.Logger;
import org.jahia.services.content.rules.RulesNotificationService;
import org.springframework.beans.factory.InitializingBean;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 27/01/11
 */
public class ScriptEngineUtils implements InitializingBean {
    private transient static Logger logger = Logger.getLogger(ScriptEngineUtils.class);


    private ScriptEngineManager scriptEngineManager;
    private Map<String, ScriptEngine> scriptEngineByExtensionCache;
    private Map<String, ScriptEngine> scriptEngineByNameCache;
    private static ScriptEngineUtils instance;

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        scriptEngineManager = new ScriptEngineManager();
        scriptEngineByExtensionCache = new LinkedHashMap<String, ScriptEngine>();
        scriptEngineByNameCache = new LinkedHashMap<String, ScriptEngine>();
    }

    public ScriptEngine getEngineByExtension(String extension) throws ScriptException {
        ScriptEngine scriptEngine = scriptEngineByExtensionCache.get(extension);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByExtension(extension);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for extension:" + extension);
            }
            scriptEngineByExtensionCache.put(extension, scriptEngine);
        }
        return scriptEngine;
    }

    public ScriptEngine getEngineByName(String name) throws ScriptException {
        ScriptEngine scriptEngine = scriptEngineByNameCache.get(name);
        if (scriptEngine == null) {
            scriptEngine = scriptEngineManager.getEngineByName(name);
            if (scriptEngine == null) {
                throw new ScriptException("Script engine not found for name :" + name);
            }
            scriptEngineByExtensionCache.put(name, scriptEngine);
        }
        return scriptEngine;
    }

    public static synchronized ScriptEngineUtils getInstance() {
        if (instance == null) {
            instance = new ScriptEngineUtils();
        }
        return instance;
    }
}
