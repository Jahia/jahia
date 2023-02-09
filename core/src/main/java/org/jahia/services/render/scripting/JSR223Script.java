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
