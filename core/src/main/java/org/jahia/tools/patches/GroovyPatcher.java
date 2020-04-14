/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.tools.patches;

import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.StringWriter;

import static org.jahia.tools.patches.Patcher.FAILED;
import static org.jahia.tools.patches.Patcher.INSTALLED;

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
        try {
            ScriptEngine engine = getEngine();
            ScriptContext ctx = new SimpleScriptContext();
            ctx.setWriter(new StringWriter());
            Bindings bindings = engine.createBindings();
            bindings.put("log", new LoggerWrapper(logger, logger.getName(), ctx.getWriter()));
            ctx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);

            engine.eval(scriptContent, ctx);
            String result = ((StringWriter) ctx.getWriter()).getBuffer().toString();
            logger.info(result);
            return INSTALLED;
        } catch (ScriptException e) {
            logger.error("Execution of script failed with error: " + e.getMessage(), e);
            return FAILED;
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
