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
package org.jahia.bundles.extender.jahiamodules.jsp;

import org.apache.jasper.compiler.TldCache;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.osgi.BundleUtils;
import org.ops4j.pax.web.jsp.JasperInitializer;
import org.ops4j.pax.web.jsp.TldScanner;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;

public class JahiaJasperInitializer extends JasperInitializer {
    private static final Logger logger = LoggerFactory.getLogger(JahiaJasperInitializer.class);

    private final ServletContext context;
    private final BundleAwareTldCache tldCache;

    private final boolean validateTld;
    private final boolean blockExternalTld;

    public JahiaJasperInitializer() throws ServletException {
        System.setProperty("org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "true");
        this.context = new ServletContextWrapper(JahiaContextLoaderListener.getServletContext());

        this.validateTld = Boolean.parseBoolean(context.getInitParameter("org.apache.jasper.XML_VALIDATE_TLD"));
        String blockExternalString = context.getInitParameter("org.apache.jasper.XML_BLOCK_EXTERNAL");
        this.blockExternalTld = (blockExternalString == null) || Boolean.parseBoolean(blockExternalString);

        this.onStartup(null, context);

        this.tldCache = new BundleAwareTldCache(context, TldCache.getInstance(context));
        this.context.setAttribute(TldCache.SERVLET_CONTEXT_ATTRIBUTE_NAME, tldCache);
    }

    public void onBundleAdded(Bundle bundle) {
        try {
            BundleTldScanner scanner = newBundleTldScanner(context, validateTld, blockExternalTld);
            scanner.scanBundle(bundle);
            tldCache.add(bundle, scanner.getUriTldResourcePathMap(), scanner.getTldResourcePathTaglibXmlMap());
        } catch (IOException e) {
            logger.error("Could not scan TLDs from bundle " +  BundleUtils.getDisplayName(bundle), e);
        }
    }

    public void onBundleRemoved(Bundle bundle) {
        tldCache.remove(bundle);
    }

    @Override
    protected TldScanner newTldScanner(ServletContext context, boolean namespaceAware, boolean validate, boolean blockExternal) {
        return newBundleTldScanner(context, validate, blockExternal);
    }

    private BundleTldScanner newBundleTldScanner(ServletContext context, boolean validate, boolean blockExternal) {
        return new BundleTldScanner(context, validate, blockExternal);
    }

}
