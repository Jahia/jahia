/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager.spi.impl;

import org.jahia.osgi.BundleUtils;

import java.io.Serializable;
import java.util.concurrent.Callable;

/**
 * Serializable callable proxy
 *
 * @param <T> the result type of method call
 */
public class CallableProxy<T> implements Callable<T>, Serializable {
    private static final long serialVersionUID = 1;

    private final String className;
    private final String bundleKey;
    private final Serializable data;

    /**
     * Build a proxy to the specified classname, with data as the constructor argument
     *
     * @param className Class name
     * @param bundleKey Bundle where the class is defined
     * @param data constructor argument
     */
    public CallableProxy(String className, String bundleKey, Serializable data) {
        this.className = className;
        this.bundleKey = bundleKey;
        this.data = data;
    }

    @Override
    public T call() throws Exception {
        ClassLoader classLoader = BundleUtils.createBundleClassLoader(BundleUtils.getBundleBySymbolicName(bundleKey, null));
        if (data != null) {
            Callable<T> c = (Callable<T>) classLoader.loadClass(className).getConstructors()[0].newInstance(data);
            return c.call();
        } else {
            Callable<T> c = (Callable<T>) classLoader.loadClass(className).newInstance();
            return c.call();
        }
    }
}
