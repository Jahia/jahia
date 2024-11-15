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
package org.jahia.bundles.jaas;

import org.apache.karaf.jaas.boot.ProxyLoginModule;
import org.apache.karaf.jaas.config.JaasRealm;
import org.jahia.bin.Jahia;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.HashMap;
import java.util.Map;

/**
 * JAAS Realm definition for DX.
 */
@Component(name = "org.jahia.bundles.jaas", service = JaasRealm.class, property = {
        Constants.SERVICE_DESCRIPTION + "=DX JAAS Realm Service",
        Constants.SERVICE_VENDOR + "=" + Jahia.VENDOR_NAME }, immediate = true)
public class JahiaJaasRealmService implements JaasRealm {

    public static final String REALM_NAME = "jahia";

    private BundleContext bundleContext;

    @Activate
    protected void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Deactivate
    protected void deactivate() {
        bundleContext = null;
    }

    @Override
    public AppConfigurationEntry[] getEntries() {

        Map<String, Object> options = new HashMap<>();
        options.put(ProxyLoginModule.PROPERTY_BUNDLE, Long.toString(bundleContext.getBundle().getBundleId()));
        options.put(ProxyLoginModule.PROPERTY_MODULE, JahiaLoginModule.class.getName());
        options.put(BundleContext.class.getName(), bundleContext);

        AppConfigurationEntry[] configEntries = new AppConfigurationEntry[] { new AppConfigurationEntry(ProxyLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, options) };

        return configEntries;
    }

    @Override
    public String getName() {
        return REALM_NAME;
    }

    @Override
    public int getRank() {
        return 0;
    }
}
