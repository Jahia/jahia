/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.sites.jcr;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.settings.SettingsBean;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 5, 2010
 * Time: 11:48:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class JCRSitesProvider {
    private static Logger logger = Logger.getLogger(JCRSitesProvider.class);
    private JCRTemplate jcrTemplate;

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    public List<JahiaSite> getSites() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<List<JahiaSite>>() {
                public List<JahiaSite> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    final List<JahiaSite> list = new ArrayList<JahiaSite>();
                    NodeIterator ni = session.getNode("/sites").getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        if (nodeWrapper.isNodeType("jnt:virtualsite")) {
                            try {
                                list.add(getSite(nodeWrapper));
                            } catch (RepositoryException e) {
                                logger.error("Cannot get site", e);
                            }
                        }
                    }
                    return list;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot get sites", e);
        }
        return null;
    }

    public JahiaSite getSiteById(final int id) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:siteId]=" + id, Query.JCR_SQL2);
                    NodeIterator ni = q.execute().getNodes();
                    if (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        return getSite(nodeWrapper);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public JahiaSite getSiteByKey(final String key) {
        try {
            if (StringUtils.isEmpty(key)) {
                return null;
            }
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper n = session.getNode("/sites/" + key);
                    return getSite(n);
                }
            });
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public JahiaSite getSiteByName(final String name) {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:virtualsite] as s where s.[j:serverName]='" + name + "'", Query.JCR_SQL2);
                    NodeIterator ni = q.execute().getNodes();
                    if (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.next();
                        return getSite(nodeWrapper);
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null; //
    }

    public int getNbSites() {
        return getSites().size();
    }

    public JahiaSite getDefaultSite() {
        try {
            return jcrTemplate.doExecuteWithSystemSession(new JCRCallback<JahiaSite>() {
                public JahiaSite doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNode("/sites");
                    if (node.hasProperty("j:defaultSite")) {
                        return getSite((JCRNodeWrapper) node.getProperty("j:defaultSite").getNode());
                    } else {
                        return null;
                    }
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot get site", e);
        }
        return null;
    }

    public void setDefaultSite(final JahiaSite site) {
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper node = session.getNode("/sites");
                    if (!node.isCheckedOut()) {
                        session.checkout(node);
                    }
                    if (site != null) {
                        JCRNodeWrapper s = node.getNode(site.getSiteKey());
                        node.setProperty("j:defaultSite", s);
                        session.save();
                    } else if (node.hasProperty("j:defaultSite")) {
                        node.getProperty("j:defaultSite").remove();
                        session.save();
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("cannot set default site", e);
        }
    }


    public void addSite(final JahiaSite site, final String siteKey, final String templatePackage, final String title,
                        final String descr, final String serverName, final String defaultLanguage, final boolean mixLanguagesActive, final Set<String> languages,
                        final Set<String> mandatoryLanguages) {
        try {
            int id = 1;
            List<JahiaSite> sites = getSites();
            for (JahiaSite jahiaSite : sites) {
                if (id <= jahiaSite.getID()) {
                    id = jahiaSite.getID() + 1;
                }
            }
            final int siteId = id;
            site.setID(id);

            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Query q = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [jnt:virtualsitesFolder]", Query.JCR_SQL2);
                        QueryResult qr = q.execute();
                        NodeIterator ni = qr.getNodes();
                        try {
                            while (ni.hasNext()) {
                                Node sitesFolder = ni.nextNode();
                                String options = "";
                                if (sitesFolder.hasProperty("j:virtualsitesFolderConfig")) {
                                    options = sitesFolder.getProperty("j:virtualsitesFolderConfig").getString();
                                }

                                Node f = JCRContentUtils.getPathFolder(sitesFolder, siteKey, options);
                                try {
                                    f.getNode(siteKey);
                                } catch (PathNotFoundException e) {
//                                    session.getWorkspace().getVersionManager().checkout(f.getPath());

                                    JCRNodeWrapper defaultSite = session.getNode("/templateSets/" + templatePackage);
                                    defaultSite.copy(session.getNode("/sites"), siteKey, false);

                                    if (sitesFolder.hasProperty("j:virtualsitesFolderSkeleton")) {
                                        String[] skeletons = sitesFolder.getProperty("j:virtualsitesFolderSkeleton").getString().split(",");
                                        for (int i = 0; i < skeletons.length; i++) {
                                            String skeleton = skeletons[i].trim();
                                            File path = null;
                                            if (skeleton.startsWith("/")) {
                                                path = new File(SettingsBean.getInstance().getJahiaJspDiskPath(), skeleton);
                                            } else {
                                                path = new File(SettingsBean.getInstance().getJahiaEtcDiskPath(), "/repository/" + skeleton);
                                            }
                                            if (path.exists()) {
                                                InputStream is = null;
                                                try {
                                                    is = new FileInputStream(path);
                                                    session.importXML(f.getPath() + "/" + siteKey, is, ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW, true);
                                                } finally {
                                                    IOUtils.closeQuietly(is);
                                                }
                                            }
                                        }
                                    }

                                    Node siteNode = f.getNode(siteKey);
                                    siteNode.setProperty("j:title", title);
                                    siteNode.setProperty("j:description", descr);
                                    siteNode.setProperty("j:serverName", serverName);
                                    siteNode.setProperty("j:siteId", siteId);
                                    siteNode.setProperty("j:defaultLanguage", defaultLanguage);
                                    siteNode.setProperty("j:mixLanguage", mixLanguagesActive);
                                    siteNode.setProperty("j:languages", languages.toArray(new String[languages.size()]));
                                    siteNode.setProperty("j:mandatoryLanguages", mandatoryLanguages.toArray(new String[mandatoryLanguages
                                            .size()]));
                                    siteNode.setProperty("j:templatesSet", templatePackage);
                                    session.save();
//                                    JCRPublicationService.getInstance().publish(siteNode.getPath(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
//                                            false);
                                }
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }

                        session.save();
                    } catch (PathNotFoundException e) {
                        logger.warn(e.getMessage(), e);
                    }
                    return true;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteSite(final String siteKey) {
        try {
            JCRCallback<Boolean> deleteCacllback = new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode("/sites");
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper site = sites.getNode(siteKey);
                    if (sites.hasProperty("j:defaultSite")) {
                        final JCRPropertyWrapper defaultSite = sites.getProperty("j:defaultSite");
                        if (defaultSite.getValue().getString().equals(site.getIdentifier())) {
                            defaultSite.remove();
                        }
                    }
                    site.remove();
                    session.save();
                    return true;
                }
            };
            JCRTemplate.getInstance().doExecuteWithSystemSession(deleteCacllback);
            // Now let's delete the live workspace site.
            JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, deleteCacllback);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void updateSite(final JahiaSite site) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode("/sites");
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper siteNode = sites.getNode(site.getSiteKey());
                    if (!siteNode.isCheckedOut()) {
                        session.checkout(siteNode);
                    }
                    siteNode.setProperty("j:title", site.getTitle());
                    siteNode.setProperty("j:description", site.getDescr());
                    siteNode.setProperty("j:serverName", site.getServerName());
//                    siteNode.setProperty("j:installedModules", new String[]{site.getTemplatePackageName()});
                    String defaultLanguage = site.getDefaultLanguage();
                    if (defaultLanguage != null)
                        siteNode.setProperty("j:defaultLanguage", defaultLanguage);
                    siteNode.setProperty("j:mixLanguage", site.isMixLanguagesActive());
                    siteNode.setProperty("j:languages", site.getLanguages().toArray(
                            new String[site.getLanguages().size()]));
                    siteNode.setProperty("j:mandatoryLanguages", site.getMandatoryLanguages().toArray(
                            new String[site.getMandatoryLanguages().size()]));

                    // add google analytics
                    GoogleAnalyticsProfile googleAnalyticsProfile = site.getGoogleAnalytics();
                    if (googleAnalyticsProfile != null && googleAnalyticsProfile.isEnabled()) {
                        siteNode.setProperty("j:gaAccount", googleAnalyticsProfile.getAccount());
                        siteNode.setProperty("j:gaLogin", googleAnalyticsProfile.getLogin());
                        siteNode.setProperty("j:gaPassword", googleAnalyticsProfile.getPassword());
                        siteNode.setProperty("j:gaTypeUrl", googleAnalyticsProfile.getTypeUrl());
                        siteNode.setProperty("j:gaProfile", googleAnalyticsProfile.getProfile());
                    }
                    if (googleAnalyticsProfile != null && googleAnalyticsProfile.isToDelete()) {
                        siteNode.getProperty("j:gaAccount").remove();
                        siteNode.getProperty("j:gaLogin").remove();
                        siteNode.getProperty("j:gaPassword").remove();
                        siteNode.getProperty("j:gaTypeUrl").remove();
                        siteNode.getProperty("j:gaProfile").remove();
                    }
                    session.save();
                    JCRPublicationService.getInstance().publish(siteNode.getIdentifier(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null,
                            false, new ArrayList<String>());
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private JahiaSite getSite(JCRNodeWrapper node) throws RepositoryException {
        int siteId = (int) node.getProperty("j:siteId").getLong();

        Properties props = new Properties();

        JahiaSite site = new JahiaSite(siteId, node.getProperty("j:title").getString(), node.getProperty("j:serverName").getString(),
                node.getName(), node.getProperty("j:description").getString(), props, node.getPath());
        Value[] s = node.getProperty("j:installedModules").getValues();
        final JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageByFileName(s[0].getString());
        if (aPackage != null) {
            site.setTemplatePackageName(aPackage.getName());
        }
        site.setMixLanguagesActive(node.getProperty("j:mixLanguage").getBoolean());
        site.setDefaultLanguage(node.getProperty("j:defaultLanguage").getString());
        Value[] languages = node.getProperty("j:languages").getValues();
        Set<String> languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setLanguages(languagesList);
        languages = node.getProperty("j:mandatoryLanguages").getValues();
        languagesList = new LinkedHashSet<String>();
        for (Value language : languages) {
            languagesList.add(language.getString());
        }
        site.setMandatoryLanguages(languagesList);
        String account = node.getPropertyAsString("j:gaAccount");
        String login = node.getPropertyAsString("j:gaLogin");
        String password = node.getPropertyAsString("j:gaPassword");
        String profile = node.getPropertyAsString("j:gaProfile");
        String typeUrl = node.getPropertyAsString("j:gaTypeUrl");
        boolean enabled = true;
        site.setGoogleAnalyticsProfile(typeUrl, enabled, password, login, profile, account);

        site.setUuid(node.getIdentifier());

        return site;
    }

}
