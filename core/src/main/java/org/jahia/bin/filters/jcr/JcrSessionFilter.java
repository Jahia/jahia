/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin.filters.jcr;

import org.jahia.bin.Jahia;
import org.jahia.registries.ServicesRegistry;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 25 févr. 2008
 * Time: 12:59:04
 * To change this template use File | Settings | File Templates.
 */
public class JcrSessionFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter (servletRequest, servletResponse );
        } finally {
            if (Jahia.isInitiated()) {
                ServicesRegistry.getInstance().getJCRStoreService().closeAllSessions();
            }
        }
    }

    public void destroy() {

    }
}
