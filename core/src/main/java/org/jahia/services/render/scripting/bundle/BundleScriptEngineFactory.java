/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import org.osgi.framework.Bundle;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

/**
 * A {@link ScriptEngineFactory} implementation that makes sure that {@link ScriptEngine} are created using the
 * appropriate class loader as some implementations have this requirement in order to work properly in an OSGi
 * environment.
 */
class BundleScriptEngineFactory implements Configurable, ScriptEngineFactory {

    private final ScriptEngineFactory factory;
    private final BundleScriptingContext context;
    private final String wrappedFactoryClassName;
    private final boolean isConfigurable;

    BundleScriptEngineFactory(ScriptEngineFactory factory, BundleScriptingContext context) {
        this.factory = factory;
        isConfigurable = factory instanceof Configurable;
        wrappedFactoryClassName = factory.getClass().getCanonicalName();
        this.context = context;
    }

    public String getEngineName() {
        return factory.getEngineName();
    }

    public String getEngineVersion() {
        return factory.getEngineVersion();
    }

    public List<String> getExtensions() {
        return factory.getExtensions();
    }

    public String getLanguageName() {
        return factory.getLanguageName();
    }

    public String getLanguageVersion() {
        return factory.getLanguageVersion();
    }

    public String getMethodCallSyntax(String obj, String m, String... args) {
        return factory.getMethodCallSyntax(obj, m, args);
    }

    public List<String> getMimeTypes() {
        return factory.getMimeTypes();
    }

    public List<String> getNames() {
        return factory.getNames();
    }

    public String getOutputStatement(String toDisplay) {
        return factory.getOutputStatement(toDisplay);
    }

    public Object getParameter(String key) {
        return factory.getParameter(key);
    }

    public String getProgram(String... statements) {
        return factory.getProgram(statements);
    }

    /**
     * Returns an instance of a {@link ScriptEngine} loaded using the class loader that was used to load this
     * {@link ScriptEngineFactory} so that proper class visibility is established.
     *
     * @see ScriptEngineFactory#getScriptEngine()
     */
    public ScriptEngine getScriptEngine() {
        final ClassLoader contextClassLoader = context.getClassLoader();
        ScriptEngine engine;
        if (contextClassLoader != null) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            engine = new BundleScriptEngine(factory.getScriptEngine(), this);
            Thread.currentThread().setContextClassLoader(old);
        } else {
            engine = factory.getScriptEngine();
        }
        return engine;
    }

    @Override
    public String toString() {
        return "BundleScriptEngineFactory wrapping " + wrappedFactoryClassName;
    }

    BundleScriptingContext getContext() {
        return context;
    }

    String getWrappedFactoryClassName() {
        return wrappedFactoryClassName;
    }

    private Configurable getWrappedAsConfigurable() {
        return (Configurable) factory;
    }

    public void configurePreRegistration(Bundle bundle) {
        if (isConfigurable) {
            getWrappedAsConfigurable().configurePreRegistration(bundle);
        }
    }

    public void destroy(Bundle bundle) {
        if (isConfigurable) {
            getWrappedAsConfigurable().destroy(bundle);
        }
    }

    public void configurePreScriptEngineCreation() {
        if (isConfigurable) {
            getWrappedAsConfigurable().configurePreScriptEngineCreation();
        }
    }
}
