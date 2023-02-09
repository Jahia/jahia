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
package org.jahia.services.render.scripting.bundle;

import javax.script.*;
import java.io.Reader;

/**
* A specific {@link ScriptEngine} so that {@link #getFactory()} returns our wrapped {@link BundleScriptEngineFactory}.
*/
public class BundleScriptEngine implements ScriptEngine {

    private ScriptEngine engine;
    private BundleScriptEngineFactory factory;

    public BundleScriptEngine(ScriptEngine engine, BundleScriptEngineFactory factory) {
        this.engine = engine;
        this.factory = factory;
    }

    public Bindings createBindings() {
        return engine.createBindings();
    }

    public Object eval(Reader reader, Bindings n) throws ScriptException {
        return engine.eval(reader, n);
    }

    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return engine.eval(reader, context);
    }

    public Object eval(Reader reader) throws ScriptException {
        return engine.eval(reader);
    }

    public Object eval(String script, Bindings n) throws ScriptException {
        return engine.eval(script, n);
    }

    public Object eval(String script, ScriptContext context) throws ScriptException {
        return engine.eval(script, context);
    }

    public Object eval(String script) throws ScriptException {
        return engine.eval(script);
    }

    public Object get(String key) {
        return engine.get(key);
    }

    public Bindings getBindings(int scope) {
        return engine.getBindings(scope);
    }

    public ScriptContext getContext() {
        return engine.getContext();
    }

    public ScriptEngineFactory getFactory() {
        return factory;
    }

    public void put(String key, Object value) {
        engine.put(key, value);
    }

    public void setBindings(Bindings bindings, int scope) {
        engine.setBindings(bindings, scope);
    }

    public void setContext(ScriptContext context) {
        engine.setContext(context);
    }

 }
