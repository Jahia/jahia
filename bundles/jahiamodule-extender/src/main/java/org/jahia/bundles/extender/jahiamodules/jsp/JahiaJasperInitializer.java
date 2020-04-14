/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
