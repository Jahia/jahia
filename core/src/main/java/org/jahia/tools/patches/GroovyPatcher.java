/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.tools.patches;

import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import groovy.lang.Closure;

import javax.script.*;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

import static org.jahia.tools.patches.Patcher.SUFFIX_FAILED;
import static org.jahia.tools.patches.Patcher.SUFFIX_INSTALLED;

/**
 * Simple patch that executes Groovy scripts
 *
 * @author Sergiy Shyrkov
 */
public class GroovyPatcher implements PatchExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GroovyPatcher.class);

    @Override
    public boolean canExecute(String name, String lifecyclePhase) {
        return name.endsWith(lifecyclePhase + ".groovy");
    }

    public String executeScript(String name, String scriptContent) {
        AtomicReference<String> res = new AtomicReference<>();
        try {
            ScriptEngine engine = getEngine();
            ScriptContext ctx = new SimpleScriptContext();
            ctx.setWriter(new StringWriter());
            Bindings bindings = engine.createBindings();
            bindings.put("log", logger);
            bindings.put("setResult", new Closure<Void>(this) {
                @Override
                public Void call(Object... args) {
                    res.set(args[0].toString());
                    return null;
                }
            });
            ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            engine.eval(scriptContent, ctx);
            return res.get() == null ? SUFFIX_INSTALLED : res.get();
        } catch (ScriptException e) {
            logger.error("Execution of script failed with error: {}", e.getMessage(), e);
            return res.get() == null ? SUFFIX_FAILED : res.get();
        }
    }

    protected ScriptEngine getEngine() throws ScriptException {
        try {
            return ScriptEngineUtils.getInstance().scriptEngine("groovy");
        } catch (ScriptException e) {
            if (e.getMessage() != null
                    && e.getMessage().startsWith("Script engine not found for extension")) {
                return null;
            } else {
                throw e;
            }
        }
    }

}
