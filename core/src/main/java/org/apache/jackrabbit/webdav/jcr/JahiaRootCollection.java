/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.webdav.jcr;

import org.apache.jackrabbit.webdav.*;
import org.apache.jackrabbit.webdav.io.InputContext;
import org.apache.jackrabbit.webdav.io.OutputContext;
import org.apache.jackrabbit.webdav.search.SearchResource;
import org.apache.jackrabbit.webdav.version.DeltaVResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * <code>JahiaRootCollection</code> represent the WebDAV root resource that does not
 * represent any repository item. A call to getMembers() returns a
 * <code>DavResourceIterator</code> containing only workspace resources
 * resources, thus revealing the names of the accessible JCR workspaces.
 */
public class JahiaRootCollection extends JahiaServerRootCollection {

    private static Logger log = LoggerFactory.getLogger(JahiaRootCollection.class);

    /**
     * Create a new <code>JahiaRootCollection</code>.
     *
     * @param locator
     * @param session
     * @param factory
     */
    public JahiaRootCollection(DavResourceLocator locator, JcrDavSession session,
                                  DavResourceFactory factory) {
        super(locator, session, factory);

        // initialize the supported locks and reports
        initLockSupport();
        initSupportedReports();
    }

    //--------------------------------------------------------< DavResource >---
    /**
     * Returns a string listing the METHODS for this resource as it
     * is required for the "Allow" response header.
     *
     * @return string listing the METHODS allowed
     * @see org.apache.jackrabbit.webdav.DavResource#getSupportedMethods()
     */
    public String getSupportedMethods() {
        StringBuilder sb = new StringBuilder(DavResource.METHODS);
        sb.append(", ");
//        sb.append(DeltaVResource.METHODS_INCL_MKWORKSPACE);
//        sb.append(", ");
        sb.append(SearchResource.METHODS);
        return sb.toString();
    }

    /**
     * Returns true
     *
     * @return true
     * @see org.apache.jackrabbit.webdav.DavResource#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * Returns true
     *
     * @return true
     * @see org.apache.jackrabbit.webdav.DavResource#isCollection()
     */
    public boolean isCollection() {
        return true;
    }

    /**
     * Returns an empty string.
     *
     * @return empty string
     * @see org.apache.jackrabbit.webdav.DavResource#getDisplayName()
     */
    public String getDisplayName() {
        return JahiaServerRootCollection.MAP_POINT;
    }

    /**
     * Always returns 'now'
     *
     * @return
     * @see org.apache.jackrabbit.webdav.DavResource#getModificationTime()
     */
    public long getModificationTime() {
        return new Date().getTime();
    }

    /**
     * Sets content lengths to '0' and retrieves the modification time.
     *
     * @param outputContext
     * @throws java.io.IOException
     * @see DavResource#spool(org.apache.jackrabbit.webdav.io.OutputContext)
     */
    public void spool(OutputContext outputContext) throws IOException {
        if (outputContext.hasStream()) {
            Session session = getRepositorySession();
            Repository rep = session.getRepository();
            String repName = rep.getDescriptor(Repository.REP_NAME_DESC);
            String repURL = rep.getDescriptor(Repository.REP_VENDOR_URL_DESC);
            String repVersion = rep.getDescriptor(Repository.REP_VERSION_DESC);
            String repostr = repName + " " + repVersion;

            StringBuilder sb = new StringBuilder();
            sb.append("<html><head><title>");
            sb.append(repostr);
            sb.append("</title></head>");
            sb.append("<body><h2>").append(repostr).append("</h2>");
            sb.append("<h3>Available Workspace Resources:</h3><ul>");

            DavResourceIterator it = getMembers();
            while (it.hasNext()) {
                DavResource res = it.nextResource();
                sb.append("<li><a href=\"");
                sb.append(res.getHref());
                sb.append("\">");
                sb.append(res.getDisplayName());
                sb.append("</a></li>");
            }
            sb.append("</ul><hr size=\"1\"><em>Powered by <a href=\"");
            sb.append(repURL).append("\">").append(repName);
            sb.append("</a> ").append(repVersion);
            sb.append("</em></body></html>");

            outputContext.setContentLength(sb.length());
            outputContext.setModificationTime(getModificationTime());
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputContext.getOutputStream(), "utf8"));
            writer.print(sb.toString());
            writer.close();
        } else {
            outputContext.setContentLength(0);
            outputContext.setModificationTime(getModificationTime());
        }
    }

    /**
     * Retrieve JahiaServerRootCollection.
     *
     * @return resource this resource is an internal member of.
     * @see org.apache.jackrabbit.webdav.DavResource#getCollection()
     */
    public DavResource getCollection() {
        try {
            return  getFactory().createResource(
                    getLocator().getFactory().createResourceLocator(getLocator().getPrefix(),"/default", "/default/"+JahiaServerRootCollection.MAP_POINT),
                    getSession());
        } catch (DavException e) {
            log.error("Error retrieving root collection", e);
            return null;
        }

    }

    /**
     * Modification workspace list not supported. This method always throw exception with 403: Forbidden code
     * @see DavResource#addMember(DavResource, org.apache.jackrabbit.webdav.io.InputContext)
     */
    public void addMember(DavResource resource, InputContext inputContext) throws DavException {
        throw new DavException(DavServletResponse.SC_FORBIDDEN);
    }

    /**
     * Returns an iterator over the member resources, which are all
     * workspace resources available.
     *
     * @return members of this collection
     * @see org.apache.jackrabbit.webdav.DavResource#getMembers()
     */
    public DavResourceIterator getMembers() {
        List<DavResource> memberList = new ArrayList();
        try {
            String[] wsNames = getRepositorySession().getWorkspace().getAccessibleWorkspaceNames();
            for (String wsName : wsNames) {
                String wspPath = "/" + wsName;
                DavResourceLocator childLoc = getLocator().getFactory().createResourceLocator(getLocator().getPrefix(), wspPath, wspPath);
                memberList.add(createResourceFromLocator(childLoc));
            }
        } catch (RepositoryException e) {
            log.error(e.getMessage());
        } catch (DavException e) {
            // should never occur
            log.error(e.getMessage());
        }
        return new DavResourceIteratorImpl(memberList);
    }

    /**
     * Modification workspace list not supported. This method always throw exception with 403: Forbidden code
     * @throws DavException
     *
     * @see DavResource#removeMember(org.apache.jackrabbit.webdav.DavResource)
     */
    public void removeMember(DavResource member) throws DavException {
        log.error("Cannot add a remove the root node.");
        throw new DavException(DavServletResponse.SC_FORBIDDEN);
    }

    //-----------------------------------------------------< DeltaVResource >---
    /**
     * @see DeltaVResource#addWorkspace(org.apache.jackrabbit.webdav.DavResource)
     */
    @Override
    public void addWorkspace(DavResource workspace) throws DavException {
        log.error("Cannot add a remove the root node.");
        throw new DavException(DavServletResponse.SC_FORBIDDEN);
    }

    //--------------------------------------------------------------------------
    /**
     * @see AbstractResource#initLockSupport()
     */
    @Override
    protected void initLockSupport() {
        // no locking supported
    }

    /**
     * Since the root resource does not represent a repository item and therefore
     * is not member of a workspace resource, this method always returns
     * <code>null</code>.
     *
     * @return <code>null</code>
     * @see AbstractResource#getWorkspaceHref()
     */
    @Override
    protected String getWorkspaceHref() {
        return null;
    }

    public String getHref() {
        return getLocator().getHref(isCollection());
    }
}
