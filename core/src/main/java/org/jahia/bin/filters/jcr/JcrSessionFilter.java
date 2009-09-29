/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.bin.filters.jcr;

import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.params.ProcessingContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 25 févr. 2008
 * Time: 12:59:04
 * To change this template use File | Settings | File Templates.
 */
public class JcrSessionFilter implements Filter {

    private static ThreadLocal<JahiaUser> currentUser = new ThreadLocal<JahiaUser>();

    public void init(FilterConfig filterConfig) throws ServletException {

    }

    public static JahiaUser getCurrentUser() {
        return currentUser.get();
    }

    public static void setCurrentUser(JahiaUser user) {
        currentUser.set(user);
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            if (Jahia.isInitiated()) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
                if (httpServletRequest.getSession(false) != null) {
                    currentUser.set((JahiaUser) httpServletRequest.getSession().getAttribute(ProcessingContext.SESSION_USER));
                }
            }
            filterChain.doFilter (servletRequest, servletResponse );
        } finally {
            if (Jahia.isInitiated()) {
                currentUser.set(null);
                JCRSessionFactory.getInstance().closeAllSessions();
            }
        }
    }

    public void destroy() {

    }
}
