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
