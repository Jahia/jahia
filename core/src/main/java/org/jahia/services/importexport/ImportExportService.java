/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.importexport;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.sites.JahiaSite;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.jcr.RepositoryException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * Jahia import/export service to manipulate different types of content.
 * 
 * @author toto
 */
public interface ImportExportService {
    String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    String JAHIA_URI="http://www.jahia.org/";

    String FROM = "from";
    String TO = "to";
    String INCLUDE_TEMPLATES = "templates";
    String INCLUDE_SITE_INFOS = "siteinfos";
    String INCLUDE_ALL_FILES = "allfiles";
    String INCLUDE_DEFINITIONS = "definitions";
    String INCLUDE_LIVE_EXPORT = "includeLive";
    String INCLUDE_USERS = "includeUsers";
    String INCLUDE_ROLES = "includeRoles";
    String VIEW_CONTENT = "content";
    String VIEW_VERSION = "version";
    String VIEW_METADATA = "metadata";
    String VIEW_JAHIALINKS = "links";
    String VIEW_ACL = "acl";
    String VIEW_WORKFLOW = "wf";
    String VIEW_PID = "pid";

    String XSL_PATH = "xsl_path";
    String NO_RECURSE = "noRecurse";
    String SKIP_BINARY = "skipBinary";

    String SYSTEM_VIEW = "systemView";

    /**
     * Performs the full repository export into the provided output stream using the specified parameters.
     * 
     * @param out
     *            the output stream to write exported content into
     * @param params
     *            the export options
     * @throws JahiaException
     *             in case of processing errors
     * @throws RepositoryException
     *             for JCR-related errors
     * @throws SAXException
     *             in case of a parsing exceptions
     * @throws IOException
     *             I/O communication errors
     * @throws TransformerException
     *             XSLT transformation errors
     */
    void exportAll(OutputStream out, Map<String, Object> params) throws JahiaException, RepositoryException, SAXException, IOException, TransformerException;

    /**
     * Export complete sites
     *
     * @param outputStream
     * @param params
     * @param sites
     * @throws JahiaException
     * @throws RepositoryException
     * @throws IOException
     * @throws SAXException
     */
    void exportSites(OutputStream outputStream, Map<String, Object> params, List<JCRSiteNode> sites)
            throws RepositoryException, IOException, SAXException, TransformerException;

    /**
     * Export JCR node as xml
     *
     * @param node node to export
     * @param exportRoot
     *@param out outputstream
     * @param params   @throws JahiaException
     * @throws RepositoryException
     * @throws SAXException
     * @throws IOException
     */
    void exportNode(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws RepositoryException, SAXException, IOException, TransformerException;

    /**
     * Export JCR content along with binaries into a zip
     *
     * @param node node to export
     * @param exportRoot
     *@param out outputstream
     * @param params   @throws JahiaException
     * @throws RepositoryException
     * @throws SAXException
     * @throws IOException
     */
    void exportZip(JCRNodeWrapper node, JCRNodeWrapper exportRoot, OutputStream out, Map<String, Object> params) throws RepositoryException, SAXException, IOException, TransformerException;

    // Import

    /**
     * Performs an import of the XML content, detecting its type: users,
     * categories or general JCR content.
     *
     * @param parentNodePath
     *            the path of the parent node, where the content should be
     *            imported
     * @param content
     *            the XML content stream
     * @param rootBehavior Ignore root xml element - can be used to import multiple nodes in the same node, using one single
     *          import
     * @throws IOException
     *             in case of read/write errors
     * @throws RepositoryException
     *             in case of repository operation errors
     * @throws JahiaException
     *             in case of errors during categories import
     */
    void importXML(String parentNodePath, InputStream content, int rootBehavior) throws IOException, RepositoryException,
            JahiaException;

    /**
     * Performs an import of the ZIP file. The format of XML files will be detected, as if they were imported with
     * importXML(String, InputStream) method. Binary content will be
     *
     * @param parentNodePath
     * @param file
     * @param rootBehavior Ignore root xml element - can be used to import multiple nodes in the same node, using one single
     *          import
     * @throws IOException
     * @throws RepositoryException
     * @throws JahiaException
     */
    void importZip(String parentNodePath, Resource file, int rootBehavior) throws IOException, RepositoryException;

    /**
     * Performs an import of the ZIP file. The format of XML files will be detected, as if they were imported with
     * importXML(String, InputStream) method. Binary content will be
     *
     * @param parentNodePath
     * @param file
     * @param rootBehavior Ignore root xml element - can be used to import multiple nodes in the same node, using one single
     *          import
     * @throws IOException
     * @throws RepositoryException
     * @throws JahiaException
     */
    void importZip(String parentNodePath, Resource file, int rootBehavior, JCRSessionWrapper session) throws IOException, RepositoryException;

    /**
     * Validates a JCR content import file in document format and returns expected failures.
     *
     *
     * @param session
     *            current JCR session instance
     * @param is
     *            the input stream with a JCR content in document format
     * @param contentType the content type for the content
     * @param installedModules the list of installed modules, where the first element is a template set name
     * @return the validation result
     * @since Jahia 6.6
     */
    ValidationResults validateImportFile(JCRSessionWrapper session, InputStream is, String contentType, List<String> installedModules);

    /**
     * Import the size from the specified file node.
     * 
     * @param nodeWrapper
     *            the file node to read content of the imported site from
     * @throws RepositoryException
     *             in case of a JCR error
     * @throws IOException
     *             in case of an I/O exception
     * @throws JahiaException
     *             if a processing error happens
     */
    void importSiteZip(JCRNodeWrapper nodeWrapper) throws RepositoryException, IOException, JahiaException;

    /**
     * Import the size from the specified resource.
     * 
     * @param resource
     *            the resource to read content of the imported site from
     * @throws RepositoryException
     *             in case of a JCR error
     * @throws IOException
     *             in case of an I/O exception
     * @throws JahiaException
     *             if a processing error happens
     */
    void importSiteZip(Resource resource) throws RepositoryException, IOException, JahiaException;

    /**
     * Import the size from the specified resource using the provided JCR session.
     * 
     * @param resource
     *            the resource to read content of the imported site from
     * @param session
     *            current JCR session to use for the import
     * @throws RepositoryException
     *             in case of a JCR error
     * @throws IOException
     *             in case of an I/O exception
     * @throws JahiaException
     *             if a processing error happens
     */
    void importSiteZip(Resource resource, JCRSessionWrapper session) throws RepositoryException, IOException, JahiaException;

    /**
     * Import a full site zip into a newly created site.
     *
     * zip file can contain all kind of legacy jahia import files or jcr import format.
     *
     * @param file Zip file
     * @param site The new site where to import
     * @param infos site infos
     * @throws RepositoryException
     * @throws IOException
     */
    void importSiteZip(Resource file, JahiaSite site, Map<Object, Object> infos) throws RepositoryException, IOException;

    /**
     * Import a full site zip into a newly created site.
     *
     * zip file can contain all kind of legacy jahia import files or jcr import format.
     *
     * @param file Zip file
     * @param site The new site where to import
     * @param infos site infos
     * @param legacyMappingFilePath path to the legacy mappings
     * @param legacyDefinitionsFilePath path for the legacy definitions
     * @throws RepositoryException
     * @throws IOException
     */
    void importSiteZip(Resource file, JahiaSite site, Map<Object, Object> infos, String legacyMappingFilePath, String legacyDefinitionsFilePath) throws RepositoryException, IOException;

    /**
     * Performs the import of categories from the provided import stream into the specified root category.
     * 
     * @param rootCategory
     *            the root category to use
     * @param is
     *            the input stream to read import content from
     */
    void importCategories(Category rootCategory, InputStream is);

    /**
     * Performs a batch import of users from the provided file
     * @param file a file to read user data from
     * @return a list of tuples &lt;username, password, homePage&gt; for the imported users 
     * @throws IOException in case of a reading/parsing error
     */
    List<String[]> importUsers(File file) throws IOException;

    /**
     * Performs a batch import of users from the provided file
     * 
     * @param file
     *            a file to read user data from
     * @param site
     *            site to use for group resolution
     * @return a list of tuples &lt;username, password, homePage&gt; for the imported users
     * @throws IOException
     *             in case of a reading/parsing error
     */
    List<String[]> importUsersFromZip(File file, JahiaSite site) throws IOException ;
}
