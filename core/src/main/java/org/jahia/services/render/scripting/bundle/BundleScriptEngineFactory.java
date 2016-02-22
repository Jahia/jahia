package org.jahia.services.render.scripting.bundle;

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.List;

/**
 * A {@link ScriptEngineFactory} implementation that makes sure that {@link ScriptEngine} are created using the appropriate class loader as some implementations have this
 * requirement in order to work properly in an OSGi environment.
 */
class BundleScriptEngineFactory implements ScriptEngineFactory {

    private final ScriptEngineFactory factory;
    private final BundleScriptingContext context;

    BundleScriptEngineFactory(ScriptEngineFactory factory, BundleScriptingContext context) {
        this.factory = factory;
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
        return "BundleScriptEngineFactory wrapping " + factory.getClass().getName();
    }

    BundleScriptingContext getContext() {
        return context;
    }
}
