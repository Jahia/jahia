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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.io.IOUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.Patterns;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Script-based choice list initializer implementation.
 * Evaluates the specified script to get the list of values.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class ScriptChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(ScriptChoiceListInitializerImpl.class);
    private ScriptEngineUtils scriptEngineUtils;

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (param != null) {
            final String extension = Patterns.DOT.split(param)[1];
            ScriptEngine byName;
            try {
                byName = scriptEngineUtils.scriptEngine(extension);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
                byName = null;
            }
            if (byName != null) {
                final Set<JahiaTemplatesPackage> forModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModulesWithViewsForComponent(
                        JCRContentUtils.replaceColon(epd.getDeclaringNodeType().getName()));
                final Bindings bindings = byName.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("values", values);
                for (JahiaTemplatesPackage template : forModule) {
                    final Resource scriptPath = template.getResource(File.separator + "scripts" + File.separator + param);
                    if (scriptPath != null && scriptPath.exists()) {
                        Reader scriptContent = null;
                        try {
                            scriptContent = new InputStreamReader(scriptPath.getInputStream());
                            return (List<ChoiceListValue>) byName.eval(scriptContent, bindings);
                        } catch (ScriptException e) {
                            logger.error("Error while executing script " + scriptPath, e);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            if (scriptContent != null) {
                                IOUtils.closeQuietly(scriptContent);
                            }
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    public void setScriptEngineUtils(ScriptEngineUtils scriptEngineUtils) {
        this.scriptEngineUtils = scriptEngineUtils;
    }
}
