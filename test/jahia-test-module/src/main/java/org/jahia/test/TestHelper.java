/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.test;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.version.VersionException;
import java.io.*;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * User: toto
 * Date: Feb 12, 2009
 * Time: 4:49:40 PM
 * 
 */
public class TestHelper {

    static Logger logger = LoggerFactory.getLogger(TestHelper.class);
    public static final String TCK_TEMPLATES = "Jahia Test";
    public static final String WEB_TEMPLATES = "templates-web";
    public static final String WEB_BLUE_TEMPLATES = "templates-web-blue";
    public static final String INTRANET_TEMPLATES = "templates-intranet";

    public static JahiaSite createSite(String name) throws Exception {
        return createSite(name, "localhost" + System.currentTimeMillis(), WEB_TEMPLATES, null, null, null);
    }

    public static JahiaSite createSite(String name, Set<String> languages, Set<String> mandatoryLanguages, boolean mixLanguagesActive) throws Exception {
        JahiaSite site = createSite(name, "localhost" + System.currentTimeMillis(), WEB_TEMPLATES, null, null, null);
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        if (!CollectionUtils.isEmpty(languages) && !languages.equals(site.getLanguages())) {
            site.setLanguages(languages);
        }
        if (!CollectionUtils.isEmpty(mandatoryLanguages) && !mandatoryLanguages.equals(site.getMandatoryLanguages())) {
            site.setMandatoryLanguages(mandatoryLanguages);
        }
        if (mixLanguagesActive != site.isMixLanguagesActive()) {
            site.setMixLanguagesActive(mixLanguagesActive);
        }
        service.updateSite(site);
        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet, String[] modulesToDeploy) throws Exception {
        return createSite(name, serverName, templateSet, null, null,modulesToDeploy);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet) throws Exception {
        return createSite(name, serverName, templateSet, null, null,null);
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName, String[] modulesToDeploy) throws Exception {
        modulesToDeploy = (modulesToDeploy == null) ? new String[0] : modulesToDeploy;

        ProcessingContext ctx = Jahia.getThreadParamBean();
        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);

        if (site != null) {
            service.removeSite(site);
        }
        File siteZIPFile = null;
        File sharedZIPFile = null;
        try {
            if (!StringUtils.isEmpty(prepackedZIPFile)) {
                ZipInputStream zis = null;
                OutputStream os = null;
                try {
                    zis = new ZipInputStream(new FileInputStream(new File(prepackedZIPFile)));
                    ZipEntry z = null;
                    while ((z = zis.getNextEntry()) != null) {
                        if (siteZIPName.equalsIgnoreCase(z.getName())
                                || "shared.zip".equals(z.getName())) {
                            File zipFile = File.createTempFile("import", ".zip");
                            os = new FileOutputStream(zipFile);
                            byte[] buf = new byte[4096];
                            int r;
                            while ((r = zis.read(buf)) > 0) {
                                os.write(buf, 0, r);
                            }
                            os.close();
                            if ("shared.zip".equals(z.getName())) {
                                sharedZIPFile = zipFile;
                            } else {
                                siteZIPFile = zipFile;
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (os != null) {
                        try {
                            os.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    if (zis != null) {
                        try {
                            zis.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (sharedZIPFile != null) {
                try {
                    ImportExportBaseService.getInstance().importSiteZip(sharedZIPFile, null, null);
                } catch (RepositoryException e) {
                    logger.warn("shared.zip could not be imported", e);
                }
            }
            site = service.addSite(admin, name, serverName, name, name, ctx.getLocale(),
                    templateSet, modulesToDeploy, siteZIPFile == null ? "noImport" : "fileImport", siteZIPFile,
                    null, false, false, null);
            ctx.setSite(site);
        } finally {
            if (sharedZIPFile != null) {
                sharedZIPFile.delete();
            }
            if (siteZIPFile != null) {
                siteZIPFile.delete();
            }
        }

        return site;
    }

    public static JahiaSite createSite(String name, String serverName, String templateSet,
                                       String prepackedZIPFile, String siteZIPName) throws Exception {
            return createSite(name, serverName, templateSet, prepackedZIPFile, siteZIPName, null);
    }

    public static void removeAllSites(JahiaSitesService service) throws JahiaException {
        final Iterator<JahiaSite> sites = service.getSites();
        while (sites.hasNext()) {
            JahiaSite jahiaSite = sites.next();
            service.removeSite(jahiaSite);
        }
    }

    public static void deleteSite(String name) throws Exception {
        JahiaSitesService service = ServicesRegistry.getInstance().getJahiaSitesService();
        JahiaSite site = service.getSiteByKey(name);
        if (site != null)
            service.removeSite(site);
    }

    public static int createSubPages(Node currentNode, int level, int nbChildren) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
       return createSubPages(currentNode, level, nbChildren, null);
    }
    
    public static int createSubPages(Node currentNode, int level, int nbChildren, String titlePrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        int pagesCreated = 0;
        if (level <= 0) return pagesCreated;
        if (!currentNode.isCheckedOut()) {
            currentNode.checkout();
        }
        for (int i = 0; i < nbChildren; i++) {
            Node newSubPage = currentNode.addNode("child" + Integer.toString(i), "jnt:page");
            if (titlePrefix != null) {
                newSubPage.setProperty("jcr:title",
                        titlePrefix + Integer.toString(i));
            }
            pagesCreated++;
            pagesCreated += createSubPages(newSubPage, level - 1, nbChildren, titlePrefix);
        }
        return pagesCreated;
    }

    /**
     * Little utility method to easily create lists of content.
     *
     * @param parentNode
     * @param listName
     * @param elementCount
     * @param textPrefix
     * @throws RepositoryException
     * @throws LockException
     * @throws ConstraintViolationException
     * @throws NoSuchNodeTypeException
     * @throws ItemExistsException
     * @throws VersionException
     */
    public static JCRNodeWrapper createList(JCRNodeWrapper parentNode, String listName, int elementCount, String textPrefix) throws RepositoryException, LockException, ConstraintViolationException, NoSuchNodeTypeException, ItemExistsException, VersionException {
        JCRNodeWrapper contentList = parentNode.addNode(listName, "jnt:contentList");

        for (int i = 0; i < elementCount; i++) {
            JCRNodeWrapper textNode = contentList.addNode(listName + "_text" + Integer.toString(i), "jnt:mainContent");
            textNode.setProperty("jcr:title", textPrefix + Integer.toString(i));
            textNode.setProperty("body", textPrefix + Integer.toString(i));
        }
        return contentList;
    }

    /**
     * Utility method to dump a part of a content tree into a String.
     *
     * @param stringBuilder
     * @param startNode
     * @param depth         usually 0 when called initially, it is incremented to mark the current depth in the tree.
     * @param logAsError
     * @return
     * @throws RepositoryException
     */
    public static StringBuilder dumpTree(StringBuilder stringBuilder, Node startNode, int depth, boolean logAsError) throws RepositoryException {
        for (int i = 0; i < depth; i++) {
            if (i == 0) {
                stringBuilder.append("+-");
            } else {
                stringBuilder.append("--");
            }
        }
        stringBuilder.append(startNode.getName());
        stringBuilder.append(" = ");
        stringBuilder.append(startNode.getIdentifier());
        stringBuilder.append("\n");
        NodeIterator childNodeIter = startNode.getNodes();
        while (childNodeIter.hasNext()) {
            Node currentChild = childNodeIter.nextNode();
            stringBuilder = dumpTree(stringBuilder, currentChild, depth + 1, logAsError);
        }
        return stringBuilder;
    }


}
