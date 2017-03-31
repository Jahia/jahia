/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.scripting;

import org.apache.commons.io.IOUtils;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.Resource;
import org.jahia.services.render.View;
import org.jahia.utils.ScriptEngineUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.*;
import java.util.Enumeration;

/**
 * JSR 223 ScriptEngine dispatcher.
 *
 * @author Serge Huber
 */
public class JSR223Script implements Script {

    private static final Logger logger = LoggerFactory.getLogger(JSR223Script.class);

    private View view;

    /**
     * Builds the script object
     */
    public JSR223Script(View view) {
        this.view = view;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        ScriptEngine scriptEngine = null;

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(view.getModule().getChainedClassLoader());

        try {
            scriptEngine = ScriptEngineUtils.getInstance().scriptEngine(view.getFileExtension());

            if (scriptEngine != null) {
                ScriptContext scriptContext = new SimpleScriptContext();
                final Bindings bindings = new SimpleBindings();
                Enumeration<?> attrNamesEnum = context.getRequest().getAttributeNames();
                while (attrNamesEnum.hasMoreElements()) {
                    String currentAttributeName = (String) attrNamesEnum.nextElement();
                    if (!"".equals(currentAttributeName)) {
                        bindings.put(currentAttributeName, context.getRequest().getAttribute(currentAttributeName));
                    }
                }
                bindings.put("params", context.getRequest().getParameterMap());
                Reader scriptContent = null;
                try {
                    InputStream scriptInputStream = getViewInputStream();
                    if (scriptInputStream != null) {
                        scriptContent = new InputStreamReader(scriptInputStream);
                        scriptContext.setWriter(new StringWriter());
                        scriptContext.setErrorWriter(new StringWriter());
                        // The following binding is necessary for Javascript, which doesn't offer a console by default.
                        bindings.put("out", new PrintWriter(scriptContext.getWriter()));
                        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                        scriptContext.setBindings(scriptEngine.getContext().getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);

                        scriptEngine.eval(scriptContent, scriptContext);

                        StringWriter writer = (StringWriter) scriptContext.getWriter();
                        return writer.toString().trim();
                    } else {
                        throw new RenderException("Error while retrieving input stream for the resource " + view.getPath());
                    }
                } catch (ScriptException e) {
                    throw new RenderException("Error while executing script " + view.getPath(), e);
                } catch (IOException e) {
                    throw new RenderException("Error while retrieving input stream for the resource " + view.getPath(), e);
                } finally {
                    if (scriptContent != null) {
                        IOUtils.closeQuietly(scriptContent);
                    }
                }
            }

        } catch (ScriptException e) {
            logger.error(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }

        return null;
    }

    /**
     * Provides access to the view associated with this script
     *
     * @return the View instance that will be executed
     */
    public View getView() {
        return view;
    }

    /**
     * Returns an {@link InputStream} object that serves the content of the view script.
     *
     * @return an {@link InputStream} object that serves the content of the view script
     * @throws IOException in case of an error retrieving the resource stream
     */
    protected InputStream getViewInputStream() throws IOException {
        return JahiaContextLoaderListener.getServletContext().getResourceAsStream(view.getPath());
    }

}
