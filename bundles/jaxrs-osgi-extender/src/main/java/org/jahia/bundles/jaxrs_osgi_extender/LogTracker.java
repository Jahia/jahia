/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.jaxrs_osgi_extender;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.osgi.service.log.Logger;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Log tracker
 */
public class LogTracker extends ServiceTracker<LogService, LogService> implements LogService {
    /**
     * New LogTracker
     *
     * @param context context
     */
    public LogTracker(BundleContext context) {
        super(context, LogService.class.getName(), null);
    }

    public void log(int level, String message) {
        log(null, level, message, null);
    }

    public void log(int level, String message, Throwable exception) {
        log(null, level, message, exception);
    }

    public void log(ServiceReference sr, int level, String message) {
        log(sr, level, message, null);
    }

    public void log(ServiceReference sr, int level, String message,
                    Throwable exception) {
        LogService log = getService();
        if (log != null) {
            log.log(sr, level, message, exception);
        }
    }

    public Logger getLogger(String s) {
        LogService log = getService();
        if (log != null) {
            return log.getLogger(s);
        }
        return null;
    }

    public Logger getLogger(Class<?> aClass) {
        LogService log = getService();
        if (log != null) {
            return log.getLogger(aClass);
        }
        return null;
    }

    public <L extends Logger> L getLogger(String s, Class<L> aClass) {
        LogService log = getService();
        if (log != null) {
            return log.getLogger(s, aClass);
        }
        return null;
    }

    public <L extends Logger> L getLogger(Class<?> aClass, Class<L> aClass1) {
        LogService log = getService();
        if (log != null) {
            return log.getLogger(aClass, aClass1);
        }
        return null;
    }

    public <L extends Logger> L getLogger(Bundle bundle, String s, Class<L> aClass) {
        LogService log = getService();
        if (log != null) {
            return log.getLogger(bundle, s, aClass);
        }
        return null;
    }
}
