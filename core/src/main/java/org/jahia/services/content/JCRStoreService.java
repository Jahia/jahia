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
package org.jahia.services.content;

import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.webdav.UsageEntry;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.ServletContext;
import java.util.*;

/**
 * User: toto
 * Date: 15 nov. 2007 - 15:18:34
 */
public class JCRStoreService extends JahiaService implements ServletContextAware {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JCRStoreService.class);


    private JahiaFieldXRefManager fieldXRefManager = null;
    private String servletContextAttributeName;
    private ServletContext servletContext;

    private Map<String, String> decorators = new HashMap<String, String>();

    static private JCRStoreService instance = null;
    private JCRSessionFactory sessionFactory;

    protected JCRStoreService() {
    }

    public synchronized static JCRStoreService getInstance() {
        if (instance == null) {
            instance = new JCRStoreService();
        }
        return instance;
    }

    public void setFieldXRefManager(JahiaFieldXRefManager fieldXRefManager) {
        this.fieldXRefManager = fieldXRefManager;
    }

    public void setServletContextAttributeName(String servletContextAttributeName) {
        this.servletContextAttributeName = servletContextAttributeName;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void start() throws JahiaInitializationException {
        try {
            NodeTypeRegistry.getInstance();
        } catch (Exception e) {
            logger.error("Repository init error", e);
        }

        if ((servletContextAttributeName != null) &&
                (servletContext != null)) {
            servletContext.setAttribute(servletContextAttributeName, this);
        }
    }

    public Map<String, String> getDecorators() {
        return decorators;
    }

    public void setDecorators(Map<String, String> decorators) {
        this.decorators = decorators;
    }

    public void stop() throws JahiaException {
    }



    public void deployDefinitions(String systemId) {
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if (provider.canRegisterCustomNodeTypes()) {
                provider.deployDefinitions(systemId);
            }
        }
    }

    public void deployNewSite(JahiaSite site, JahiaUser user) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployNewSite(site, user);
    }

    public void deployExternalUser(String username, String providerName) throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getMountPoints().get("/");
        provider.deployExternalUser(username, providerName);
    }

    public void export(String path, ContentHandler ch, JahiaUser user) {
        sessionFactory.getProvider(path).export(path, ch, user);
    }

    public List<JCRNodeWrapper> getUserFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getUserFolders(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getImportDropBoxes(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getImportDropBoxes(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<JCRNodeWrapper> getSiteFolders(String site, JahiaUser user) {
        List<JCRNodeWrapper> r = new ArrayList<JCRNodeWrapper>();
        for (JCRStoreProvider storeProvider : sessionFactory.getMountPoints().values()) {
            try {
                r.addAll(storeProvider.getSiteFolders(site, user));
            } catch (RepositoryException e) {
                logger.warn("Error when querying repository", e);
            }
        }
        return r;
    }

    public List<UsageEntry> findUsages(String sourceUri, boolean onlyLockedUsages) {
        return findUsages(sourceUri, Jahia.getThreadParamBean(), onlyLockedUsages);
    }

    public List<UsageEntry> findUsages(String sourceUri, ProcessingContext jParams,
                                       boolean onlyLockedUsages) {
        return findUsages(sourceUri, Jahia.getThreadParamBean(), onlyLockedUsages, null);
    }

    public List<UsageEntry> findUsages(String sourceUri, ProcessingContext jParams,
                                       boolean onlyLockedUsages, String versionName) {
        List<UsageEntry> res = new ArrayList<UsageEntry>();
        if (fieldXRefManager == null) {
            fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        }

        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.FILE + sourceUri);

        for (Iterator<JahiaFieldXRef> iterator = c.iterator(); iterator.hasNext();) {
            JahiaFieldXRef jahiaFieldXRef = iterator.next();
            try {
                if (!onlyLockedUsages || jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    int version = 0;
                    if (jahiaFieldXRef.getComp_id().getWorkflow() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                        version = ContentField.getField(jahiaFieldXRef.getComp_id().getFieldId()).getActiveVersionID();
                    }
                    UsageEntry entry = new UsageEntry(jahiaFieldXRef.getComp_id().getFieldId(), version, jahiaFieldXRef.getComp_id().getWorkflow(), jahiaFieldXRef.getComp_id().getLanguage(), jahiaFieldXRef.getComp_id().getTarget().substring(JahiaFieldXRefManager.FILE.length()), jParams);
                    if (versionName != null) {
                        entry.setVersionName(versionName);
                    }
                    res.add(entry);
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return res;
    }

    public static String removeDiacritics(String name) {
        if (name == null) return null;
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= '\u0080') {
                if (c >= '\u00C0' && c < '\u00C6') sb.append('A');
                else if (c == '\u00C6') sb.append("AE");
                else if (c == '\u00C7') sb.append('C');
                else if (c >= '\u00C8' && c < '\u00CC') sb.append('E');
                else if (c >= '\u00CC' && c < '\u00D0') sb.append('I');
                else if (c == '\u00D0') sb.append('D');
                else if (c == '\u00D1') sb.append('N');
                else if (c >= '\u00D2' && c < '\u00D7') sb.append('O');
                else if (c == '\u00D7') sb.append('x');
                else if (c == '\u00D8') sb.append('O');
                else if (c >= '\u00D9' && c < '\u00DD') sb.append('U');
                else if (c == '\u00DD') sb.append('Y');
                else if (c == '\u00DF') sb.append("SS");
                else if (c >= '\u00E0' && c < '\u00E6') sb.append('a');
                else if (c == '\u00E6') sb.append("ae");
                else if (c == '\u00E7') sb.append('c');
                else if (c >= '\u00E8' && c < '\u00EC') sb.append('e');
                else if (c >= '\u00EC' && c < '\u00F0') sb.append('i');
                else if (c == '\u00F0') sb.append('d');
                else if (c == '\u00F1') sb.append('n');
                else if (c >= '\u00F2' && c < '\u00FF') sb.append('o');
                else if (c == '\u00F7') sb.append('/');
                else if (c == '\u00F8') sb.append('o');
                else if (c >= '\u00F9' && c < '\u00FF') sb.append('u');
                else if (c == '\u00FD') sb.append('y');
                else if (c == '\u00FF') sb.append("y");
                else if (c == '\u0152') sb.append("OE");
                else if (c == '\u0153') sb.append("oe");
                else sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public JCRNodeWrapper decorate(JCRNodeWrapper w) {
        try {
            for (String type : decorators.keySet()) {
                if (w.isNodeType(type)) {
                    String className = decorators.get(type);
                    try {
                        return (JCRNodeWrapper) Class.forName(className).getConstructor(JCRNodeWrapper.class).newInstance(w);
                    } catch (Exception e) {
                        logger.error("Cannot decorate node", e);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return w;
    }

    public JCRNodeWrapper getNodeByUUID(String uuid, JahiaUser user) throws ItemNotFoundException, RepositoryException {
        return sessionFactory.getThreadSession(user).getNodeByUUID(uuid);
    }

    public JCRNodeWrapper getNodeByUUID(String providerKey, String uuid, JahiaUser user) throws ItemNotFoundException, RepositoryException {
        return sessionFactory.getThreadSession(user).getNodeByUUID(providerKey, uuid);
    }

    public QueryManager getQueryManager(JahiaUser user) {
        try {
            return sessionFactory.getThreadSession(user).getWorkspace().getQueryManager();
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return null;
    }

    public QueryManager getQueryManager(JahiaUser user, ProcessingContext context) {
        try {
            return sessionFactory.getThreadSession(user).getWorkspace().getQueryManager(context);
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return null;
    }

    public QueryManager getQueryManager(JahiaUser user, ProcessingContext context, Properties properties) {
        try {
            return sessionFactory.getThreadSession(user).getWorkspace().getQueryManager(context, properties);
        } catch (RepositoryException e) {
            logger.error(e);
        }
        return null;
    }

    /**
     * @param queryObjectModel
     * @param user
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public QueryResult execute(QueryObjectModel queryObjectModel, JahiaUser user) throws InvalidQueryException,
            RepositoryException {
        return sessionFactory.getThreadSession(user).getWorkspace().execute(queryObjectModel);
    }

    /**
     * Check existence of a given path in the repository.
     *
     * @param path the path to check
     * @param user the current user
     * @return the node if it exists, null otherwise
     * @throws javax.jcr.RepositoryException an exception occured while retrieving the node
     */
    public JCRNodeWrapper checkExistence(String path, JahiaUser user) throws RepositoryException {
        try {
            JCRNodeWrapper node = sessionFactory.getThreadSession(user).getNode(path);
            if (node != null && node.isValid()) {
                return node;
            }
        } catch (RepositoryException e) {
            if (!(e instanceof PathNotFoundException)) {
                throw e;
            }
        }
        return null;
    }
    
    /**
     * @deprecated Use getThreadSession().getNode()
     */
    public JCRNodeWrapper getFileNode(String path, JahiaUser user) {
        // Todo Suppress this method
        if (path != null) {
            if (path.startsWith("/")) {
                for (Iterator<String> iterator = sessionFactory.getDynamicMountPoints().keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (path.startsWith(mp + "/")) {
                        String localPath = path.substring(mp.length());
                        JCRStoreProvider provider = sessionFactory.getDynamicMountPoints().get(mp);
                        return provider.getNodeWrapper(provider.getRelativeRoot() + localPath, user);
                    }
                }
                for (Iterator<String> iterator = sessionFactory.getMountPoints().keySet().iterator(); iterator.hasNext();) {
                    String mp = iterator.next();
                    if (mp.equals("/") || path.equals(mp) || path.startsWith(mp + "/")) {
                        String localPath = path;
                        if (!mp.equals("/")) {
                            localPath = path.substring(mp.length());
                        }
                        JCRStoreProvider provider = sessionFactory.getMountPoints().get(mp);
                        if (localPath.equals("")) {
                            localPath = "/";
                        }
                        return provider.getNodeWrapper(provider.getRelativeRoot() + localPath, user);
                    }
                }
                return null;
            } else if (path.length() > 0 && path.contains(":")) {
                int index = path.indexOf(":");
                String key = path.substring(0, index);
                String localPath = path.substring(index + 1);
                JCRStoreProvider provider = sessionFactory.getProviders().get(key);
                if (provider != null) {
                    return provider.getNodeWrapper(provider.getRelativeRoot() + localPath, user);
                }
            }
        }
        return new JCRNodeWrapperImpl("?", null, null);
    }
}
