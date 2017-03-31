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

import org.osgi.framework.Bundle;

import javax.script.ScriptEngineFactory;

/**
 * A configuration interface for {@link ScriptEngineFactory} implementations so that providers of new scripting
 * language from modules can have the opportunity to perform set up or clean up operations when the bundle is started
 * and/or stopped.
 *
 * @author Christophe Laprun
 */
public interface Configurable {

    /**
     * Configures the {@link ScriptEngineFactory} when its bundle is started but before it is registered with the
     * {@link BundleScriptResolver}. Note that this means that Spring context is not available at this time.
     *
     * @param bundle the bundle the factory was loaded from
     */
    void configurePreRegistration(Bundle bundle);

    /**
     * Performs any clean up operations when the bundle this factory was loaded from is stopped.
     *
     * @param bundle the bundle the factory was loaded from
     */
    void destroy(Bundle bundle);

    /**
     * Configures this {@link ScriptEngineFactory} if needed right before a {@link javax.script.ScriptEngine}
     * instance is created. This is useful when some configuration details are not yet available when the bundle that
     * declared it is started (e.g. when the configuration is in a Spring context that is not yet available during
     * module startup).
     *
     * Note that this method is called each time a {@link javax.script.ScriptEngine} instance is retrieved so it needs
     * to be efficient, be thread-safe and guard against multiple calls when the configuration needs to happen only
     * once.
     */
    void configurePreScriptEngineCreation();
}
