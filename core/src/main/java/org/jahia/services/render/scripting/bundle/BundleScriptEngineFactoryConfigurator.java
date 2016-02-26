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
package org.jahia.services.render.scripting.bundle;

import org.osgi.framework.Bundle;

import javax.script.ScriptEngineFactory;

/**
 * A configuration helper so that {@link ScriptEngineFactory} providers from modules can have the opportunity to perform set up or clean up operations when the bundle is started
 * and/or stopped.
 *
 * @param <T> the type parameter
 * @author Christophe Laprun
 */
public interface BundleScriptEngineFactoryConfigurator<T extends ScriptEngineFactory> {
    /**
     * Configures the specified {@link ScriptEngineFactory} when the bundle it came from is started.
     *
     * @param factory     the factory to configure
     * @param bundle      the bundle the factory was loaded from
     * @param classLoader the class loader the factory was loaded from
     */
    void configure(final T factory, final Bundle bundle, final ClassLoader classLoader);

    /**
     * Performs any clean up operations when the bundle the factory came from is stopped.
     *
     * @param factory the factory for which we want to perform clean up
     */
    void destroy(final T factory);

    /**
     * Configures the specified {@link ScriptEngineFactory} if needed right before a {@link javax.script.ScriptEngine} instance is created. This is useful when some
     * configuration details are not yet available when the bundle that declared the {@link ScriptEngineFactory} is started (e.g. when the configuration is in a Spring context
     * that is not yet available during module startup).
     * <p/>
     * Note that this method is called each time a {@link javax.script.ScriptEngine} instance is retrieved so it needs to be efficient, be thread-safe and guard against multiple
     * calls when the configuration needs to happen only once.
     *
     * @param scriptEngineFactory the factory to configure
     */
    void configurePreScriptEngineCreation(T scriptEngineFactory);
}
