/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.scripting;

import org.apache.commons.io.IOUtils;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.*;

import javax.script.*;
import java.io.*;
import java.util.Enumeration;

/**
 * JSR 223 ScriptEngine dispatcher.
 *
 * @author loom
 *         Date: Jan 15, 2010
 *         Time: 11:14:06 AM
 */
public class JSR223Script implements Script {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JSR223Script.class);

    private Template template;

    /**
     * Builds the script object
     *
     */
    public JSR223Script(Template template) {
        this.template = template;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     *
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        ScriptEngine scriptEngine = null;
        try {
            scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(template.getFileExtension());
        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        }
        if (scriptEngine != null) {
            ScriptContext scriptContext = scriptEngine.getContext();
            final Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
            Enumeration attrNamesEnum = context.getRequest().getAttributeNames();
            while (attrNamesEnum.hasMoreElements()) {
                String currentAttributeName = (String) attrNamesEnum.nextElement();
                if (!"".equals(currentAttributeName)) {
                    bindings.put(currentAttributeName, context.getRequest().getAttribute(currentAttributeName));
                }
            }
            InputStream scriptInputStream = JahiaContextLoaderListener.getServletContext().getResourceAsStream(template.getPath());
            if (scriptInputStream != null) {
                Reader scriptContent = null;
                try {
                    scriptContent = new InputStreamReader(scriptInputStream);
                    scriptContext.setWriter(new StringWriter());
                    // The following binding is necessary for Javascript, which doesn't offer a console by default.
                    bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                    Object result = scriptEngine.eval(scriptContent, bindings);
                    StringWriter writer = (StringWriter) scriptContext.getWriter();
                    return writer.toString();
                } catch (ScriptException e) {
                    throw new RenderException("Error while executing script " + template.getPath(), e);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Provides access to the template associated with this script
     *
     * @return the Template instance that will be executed
     */
    public Template getTemplate() {
        return template;
    }

}
