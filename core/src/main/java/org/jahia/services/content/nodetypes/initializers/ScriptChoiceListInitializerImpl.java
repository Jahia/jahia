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

package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.io.IOUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
            final String extension = param.split("\\.")[1];
            ScriptEngine byName;
            try {
                byName = scriptEngineUtils.scriptEngine(extension);
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
                byName = null;
            }
            if (byName != null) {
                final Set<JahiaTemplatesPackage> forModule = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackagesForModule(
                        JCRContentUtils.replaceColon(epd.getDeclaringNodeType().getName()));
                final Bindings bindings = byName.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("values", values);
                for (JahiaTemplatesPackage template : forModule) {
                    final File scriptPath = new File(
                            template.getFilePath() + File.separator + "scripts" + File.separator + param);
                    if (scriptPath.exists()) {
                        FileReader scriptContent = null;
                        try {
                            scriptContent = new FileReader(scriptPath);
                            return (List<ChoiceListValue>) byName.eval(scriptContent, bindings);
                        } catch (ScriptException e) {
                            logger.error("Error while executing script " + scriptPath, e);
                        } catch (FileNotFoundException e) {
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
