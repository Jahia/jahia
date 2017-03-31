/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.j2ee;

import org.apache.jackrabbit.server.SessionProvider;
import org.apache.jackrabbit.server.SessionProviderImpl;
import org.apache.jackrabbit.server.jcr.JCRWebdavServer;
import org.apache.jackrabbit.webdav.*;

import javax.jcr.Repository;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * User: toto
 * Date: 27 déc. 2007
 * Time: 18:44:08
 * 
 */
public class JahiaJCRWebdavServerServlet extends JCRWebdavServerServlet {

    private static final long serialVersionUID = -2235958694965528476L;

    /**
     * the repository session provider
     */
    private transient SessionProvider sessionProvider;

    private transient JCRWebdavServer server;

    /**
     * Returns the <code>DavSessionProvider</code>
     *
     * @return server
     * @see org.apache.jackrabbit.webdav.server.AbstractWebdavServlet#getDavSessionProvider()
     */
    public DavSessionProvider getDavSessionProvider() {
        if (server == null) {
            Repository repository = RepositoryAccessServlet.getRepository(getServletContext());
            server = new JCRWebdavServer(repository, getSessionProvider());
        }
        return server;
    }

    public synchronized SessionProvider getSessionProvider() {
        if (sessionProvider == null) {
            sessionProvider = new SessionProviderImpl(new JahiaSessionCredentials(""));
        }
        return sessionProvider;
    }

    protected boolean execute(WebdavRequest request, WebdavResponse response,
                              int method, DavResource resource)
            throws ServletException, IOException, DavException {
        if (method != DavMethods.DAV_GET && Boolean.TRUE.equals(request.getAttribute("isGuest"))) {
            throw new DavException(HttpServletResponse.SC_UNAUTHORIZED);
        }

        return super.execute(request, response, method, resource);
    }



}
