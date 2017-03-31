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
