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

import javax.script.ScriptEngineFactory;
import java.util.Collections;
import java.util.Map;

/**
 * Contextual information resulting from the loading of {@link ScriptEngineFactory} from OSGi bundles.
 */
class BundleScriptingContext {
    private final ClassLoader classLoader;
    private final Map<String, Integer> extensionPriorities;

    BundleScriptingContext(ClassLoader classLoader, Map<String, Integer> extensionsPrioritiesMap) {
        this.classLoader = classLoader;
        this.extensionPriorities = extensionsPrioritiesMap;
    }

    ClassLoader getClassLoader() {
        return classLoader;
    }

    int getPriority(String extension, int defaultPriority) {
        if (extensionPriorities != null) {
            final Integer priority = extensionPriorities.get(extension);
            return priority == null ? defaultPriority : Math.abs(priority);
        } else {
            return defaultPriority;
        }
    }

    boolean specifiesExtensionPriorities() {
        return extensionPriorities != null && !extensionPriorities.isEmpty();
    }

    Map<String, Integer> getExtensionPriorities() {
        return extensionPriorities != null ? extensionPriorities : Collections.<String, Integer>emptyMap();
    }
}
