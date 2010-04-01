package org.jahia.services.sites.jcr;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.content.*;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
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
                            list.add(getSite(nodeWrapper));
                        }
                    }
                    return list;
                }
            });
        } catch (RepositoryException e) {
            e.printStackTrace();
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
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback() {
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


    public void addSite(final JahiaSite site, JahiaUser user) {
        try {
            int id = 1;
            List<JahiaSite> sites = getSites();
            for (JahiaSite jahiaSite : sites) {
                if (id <= jahiaSite.getID()) {
                    id = jahiaSite.getID() + 1;
                }
            }
            site.setID(id);

            ServicesRegistry.getInstance().getJCRStoreService().deployNewSite(site, user);

            jcrTemplate.doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper defaultSite = session.getNode("/templatesSet/" + site.getTemplatePackageName() + "/defaultSite");
                        defaultSite.copy(session.getNode("/sites"), site.getSiteKey(), false);
                        session.save();
                        session.getNode("/sites/"+site.getSiteKey()).clone(session.getNode("/users"), "users");
                        session.save();
                    } catch (PathNotFoundException e) {
                    } catch (RepositoryException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteSite(final String siteKey) {
        try {
            JCRCallback deleteCacllback = new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper sites = session.getNode("/sites");
                    if (!sites.isCheckedOut()) {
                        session.checkout(sites);
                    }
                    JCRNodeWrapper site = sites.getNode(siteKey);
                    site.remove();
                    session.save();
                    return null;
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
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

                    // remove deleted profiles
                    NodeIterator gaProfileNodeIterator = siteNode.getNodes();
                    while (gaProfileNodeIterator != null && gaProfileNodeIterator.hasNext()) {
                        JCRNodeWrapper childNode = (JCRNodeWrapper) gaProfileNodeIterator.nextNode();
                        if (childNode.getNodeTypes().contains(Constants.JAHIANT_GOOGLEANALYTICS) && !site.hasProfile(childNode.getName())) {
                               childNode.remove(); 
                        }
                    }

                    // add google analytics
                    Iterator<GoogleAnalyticsProfile> it = site.getGoogleAnalyticsProfil().iterator();
                    while (it.hasNext()) {
                        GoogleAnalyticsProfile googleAnalyticsProfile = it.next();
                        JCRNodeWrapper googleAnalyticsNode;
                        if (siteNode.hasNode(googleAnalyticsProfile.getName())) {
                            googleAnalyticsNode = siteNode.getNode(googleAnalyticsProfile.getName());
                        } else {
                            googleAnalyticsNode = siteNode.addNode(googleAnalyticsProfile.getName(), Constants.JAHIANT_GOOGLEANALYTICS);
                        }
                        if(!googleAnalyticsNode.isCheckedOut()){
                           session.checkout(googleAnalyticsNode); 
                        }
                        googleAnalyticsNode.setProperty("j:account", googleAnalyticsProfile.getAccount());
                        googleAnalyticsNode.setProperty("j:login", googleAnalyticsProfile.getLogin());
                        googleAnalyticsNode.setProperty("j:password", googleAnalyticsProfile.getPassword());
                        googleAnalyticsNode.setProperty("j:profile", googleAnalyticsProfile.getProfile());
                        googleAnalyticsNode.setProperty("j:typeUrl", googleAnalyticsProfile.getTypeUrl());
                        googleAnalyticsNode.setProperty("j:enabled", googleAnalyticsProfile.isEnabled());
                    }

                    session.save();
                    return null;
                }
            });
            JCRPublicationService.getInstance().publish("/sites/"+site.getSiteKey(), Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null, true, false);
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
        site.setTemplatePackageName(s[0].getString());
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

        // load google analytics profiles
        NodeIterator gaProfileNodeIterator = node.getNodes();
        while (gaProfileNodeIterator != null && gaProfileNodeIterator.hasNext()) {
            JCRNodeWrapper childNode = (JCRNodeWrapper) gaProfileNodeIterator.nextNode();
            if (childNode.getNodeTypes().contains(Constants.JAHIANT_GOOGLEANALYTICS)) {
                String name = childNode.getName();
                String account = childNode.getPropertyAsString("j:account");
                String login = childNode.getPropertyAsString("j:login");
                String password = childNode.getPropertyAsString("j:password");
                String profile = childNode.getPropertyAsString("j:profile");
                String typeUrl = childNode.getPropertyAsString("j:typeUrl");
                boolean enabled = childNode.getProperty("j:enabled").getBoolean();

                site.addOrUpdateGoogleAnalyticsProfile(name, typeUrl, enabled, password, login, profile, account);
            }
        }

        return site;
    }

}
