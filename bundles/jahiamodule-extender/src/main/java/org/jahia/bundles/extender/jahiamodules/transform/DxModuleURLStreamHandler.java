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
package org.jahia.bundles.extender.jahiamodules.transform;

import org.jahia.bin.Jahia;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.PersistentBundle;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * URL stream handler that transforms the bundle URL by reading it from a persistence storage and applying additionally the module
 * dependency transformation using OSGi capabilities (see {@link ModuleUtils}}.
 */
public class DxModuleURLStreamHandler extends AbstractURLStreamHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(DxModuleURLStreamHandler.class);

    @Override
    public URLConnection openConnection(URL url) throws IOException {

        return new URLConnection(url) {

            @Override
            public void connect() throws IOException {
                // Do nothing
            }

            @Override
            public InputStream getInputStream() throws IOException {
                Thread current = Thread.currentThread();
                ClassLoader contextCL = current.getContextClassLoader();

                String bundleKey = url.getFile();
                try {
                    current.setContextClassLoader(Jahia.class.getClassLoader());
                    PersistentBundle bundle = ModuleUtils.loadPersistentBundle(bundleKey);
                    return bundle.isTransformationRequired() ? ModuleUtils.addModuleDependencies(bundle.getResource().getInputStream())
                            : bundle.getResource().getInputStream();
                } catch (ModuleManagementException e) {
                    logger.warn("Couldn't resolve the {}: protocol path for: {}", Constants.URL_PROTOCOL_DX, bundleKey);
                    throw new IOException(e);
                } finally {
                    current.setContextClassLoader(contextCL);
                }
            }

            @Override
            public long getLastModified() {
                PersistentBundle info;
                String bundleKey = url.getFile();
                try {
                    info = ModuleUtils.loadPersistentBundle(bundleKey);
                } catch (ModuleManagementException e) {
                    logger.warn("Unable to get persistent bundle for bundle key {}; cause: {}", bundleKey, e);
                    return 0;
                }
                if (info == null) {
                    logger.warn("Unable to find persistent bundle for bundle key {}", bundleKey);
                    return 0;
                }
                return info.getLastModified();
            }
        };
    }
}