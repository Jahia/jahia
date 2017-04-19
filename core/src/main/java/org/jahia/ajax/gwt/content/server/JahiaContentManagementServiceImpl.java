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
package org.jahia.ajax.gwt.content.server;

import com.extjs.gxt.ui.client.data.*;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;
import net.htmlparser.jericho.StartTag;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaPrivilegeRegistry;
import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.jahia.ajax.gwt.client.data.*;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.*;
import org.jahia.ajax.gwt.client.data.job.GWTJahiaJobDetail;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.wcag.WCAGValidationResult;
import org.jahia.ajax.gwt.client.data.wcag.WCAGViolation;
import org.jahia.ajax.gwt.client.data.workflow.*;
import org.jahia.ajax.gwt.client.data.workflow.history.GWTJahiaWorkflowHistoryItem;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.SessionValidationResult;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.helper.*;
import org.jahia.api.Constants;
import org.jahia.bin.Export;
import org.jahia.bin.Jahia;
import org.jahia.bin.Login;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.valves.LoginConfig;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.htmlvalidator.Result;
import org.jahia.services.htmlvalidator.ValidatorResults;
import org.jahia.services.htmlvalidator.WAIValidator;
import org.jahia.services.notification.ToolbarWarningsService;
import org.jahia.services.seo.jcr.NonUniqueUrlMappingException;
import org.jahia.services.tags.TaggingService;
import org.jahia.services.translation.TranslationException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.visibility.VisibilityConditionRule;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.security.Privilege;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.ConstraintViolationException;
import java.net.MalformedURLException;
import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * GWT server code implementation for the DMS repository services.
 *
 * @author rfelden
 */
public class JahiaContentManagementServiceImpl extends JahiaRemoteService implements JahiaContentManagementService {
    private static final transient Logger logger = LoggerFactory.getLogger(JahiaContentManagementServiceImpl.class);

    private static final Pattern VERSION_AT_PATTERN = Pattern.compile("_at_");

    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;
    private SearchHelper search;
    private PublicationHelper publication;
    private WorkflowHelper workflow;
    private VersioningHelper versioning;
    private PortletHelper portlet;
    private ContentDefinitionHelper contentDefinition;
    private ContentHubHelper contentHub;
    private PropertiesHelper properties;
    private LanguageHelper languages;
    private TemplateHelper template;
    private ImageHelper image;
    private ZipHelper zip;
    private ACLHelper aclHelper;
    private DiffHelper diff;
    private SeoHelper seo;
    private int sessionPollingFrequency;
    private CacheHelper cacheHelper;
    private SchedulerHelper schedulerHelper;
    private UIConfigHelper uiConfigHelper;
    private JCRContentUtils jcrContentUtils;
    private ChannelHelper channelHelper;
    private TranslationHelper translationHelper;
    private StubHelper stubHelper;
    private ModuleHelper moduleHelper;
    private TaggingService taggingService;
    private ToolbarWarningsService toolbarWarningsService;

    public void setAcl(ACLHelper acl) {
        this.aclHelper = acl;
    }

    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    public void setContentHub(ContentHubHelper contentHub) {
        this.contentHub = contentHub;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public void setPortlet(PortletHelper portlet) {
        this.portlet = portlet;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setProperties(PropertiesHelper properties) {
        this.properties = properties;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setWorkflow(WorkflowHelper workflow) {
        this.workflow = workflow;
    }

    public void setSearch(SearchHelper search) {
        this.search = search;
    }

    public void setLanguages(LanguageHelper languages) {
        this.languages = languages;
    }

    public void setDiff(DiffHelper diff) {
        this.diff = diff;
    }

    public void setTemplate(TemplateHelper template) {
        this.template = template;
    }

    public void setImage(ImageHelper image) {
        this.image = image;
    }

    public void setVersioning(VersioningHelper versioning) {
        this.versioning = versioning;
    }

    public void setZip(ZipHelper zip) {
        this.zip = zip;
    }

    public void setSessionPollingFrequency(int sessionPollingFrequency) {
        this.sessionPollingFrequency = sessionPollingFrequency;
    }

    public void setCacheHelper(CacheHelper cacheHelper) {
        this.cacheHelper = cacheHelper;
    }

    public void setSchedulerHelper(SchedulerHelper schedulerHelper) {
        this.schedulerHelper = schedulerHelper;
    }

    public void setUiConfigHelper(UIConfigHelper uiConfigHelper) {
        this.uiConfigHelper = uiConfigHelper;
    }

    /**
     * @param jcrContentUtils the jcrContentUtils to set
     */
    public void setJCRContentUtils(JCRContentUtils jcrContentUtils) {
        this.jcrContentUtils = jcrContentUtils;
    }

    public void setChannelHelper(ChannelHelper channelHelper) {
        this.channelHelper = channelHelper;
    }

    public void setTranslationHelper(TranslationHelper translationHelper) {
        this.translationHelper = translationHelper;
    }

    /**
     * Injection of a StubHelper class to handle display of code Snippets in the code editor
     *
     * @param stubHelper instance to be injected.
     * @see StubHelper
     */
    public void setStubHelper(StubHelper stubHelper) {
        this.stubHelper = stubHelper;
    }

    public void setTaggingService(TaggingService taggingService) {
        this.taggingService = taggingService;
    }

    public void setToolbarWarningsService(ToolbarWarningsService toolbarWarningsService) {
        this.toolbarWarningsService = toolbarWarningsService;
    }

    // ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface JahiaContentManagementServiceAsync ---------------------

    /**
     * Get ManageConfiguratin
     *
     * @param name
     * @return
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTManagerConfiguration getManagerConfiguration(String name, String path) throws GWTJahiaServiceException {
        GWTManagerConfiguration config = null;
        try {
            JCRNodeWrapper context = getSite();
            if (!StringUtils.isEmpty(path)) {
                context = retrieveCurrentSession().getNode(path);
            }

            @SuppressWarnings("unchecked") Map<String, List<String>> locks = (Map<String, List<String>>) getRequest().getSession().getAttribute("engineLocks");
            if (locks == null) {
                locks = new HashMap<String, List<String>>();
                getRequest().getSession().setAttribute("engineLocks", locks);
            }

            config = uiConfigHelper.getGWTManagerConfiguration(context, getSite(), getRemoteJahiaUser(), getLocale(), getUILocale(),
                    getRequest(), name);
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        }
        return config;
    }

    /**
     * Get edit configuration
     *
     * @return
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTEditConfiguration getEditConfiguration(String path, String name, String enforcedWorkspace) throws GWTJahiaServiceException {
        GWTEditConfiguration config = null;
        try {
            JCRSessionWrapper session = retrieveCurrentSession();
            if (!session.getWorkspace().getName().equals(enforcedWorkspace)) {
                session = retrieveCurrentSession(enforcedWorkspace, session.getLocale(), true);
            }

            @SuppressWarnings("unchecked") Map<String, List<String>> locks = (Map<String, List<String>>) getRequest().getSession().getAttribute("engineLocks");
            if (locks == null) {
                locks = new HashMap<String, List<String>>();
                getRequest().getSession().setAttribute("engineLocks", locks);
            }

            config = uiConfigHelper.getGWTEditConfiguration(name, path, getRemoteJahiaUser(), getLocale(), getUILocale(),
                    getRequest(), session);
        } catch (Exception e) {
            logger.error("Cannot retrieve edit configuration " + name + " for path " + path, e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.edit.configuration", getUILocale(), name,
                    path, e.getLocalizedMessage()));
        }
        return config;
    }


    @Override
    public BasePagingLoadResult<GWTJahiaNode> lsLoad(String parentPath, List<String> nodeTypes, List<String> mimeTypes,
                                                     List<String> filters, List<String> fields, boolean checkSubChild,
                                                     int limit, int offset, boolean displayHiddenTypes, List<String> hiddenTypes,
                                                     String hiddenRegex, boolean showOnlyNodesWithTemplates, boolean useUILocale)
            throws GWTJahiaServiceException {

        Locale locale = useUILocale ? getUILocale() : getLocale();

        List<GWTJahiaNode> filteredList = new ArrayList<GWTJahiaNode>();
        for (GWTJahiaNode n : navigation
                .ls(parentPath, nodeTypes, mimeTypes, filters, fields, checkSubChild, displayHiddenTypes, hiddenTypes, hiddenRegex, retrieveCurrentSession(getWorkspace(), locale, true),
                        showOnlyNodesWithTemplates, getUILocale())) {
            if (n.isMatchFilters()) {
                filteredList.add(n);
            }
        }
        final int length = filteredList.size();
        if (offset > -1 && limit > 0) {
            if (offset >= length) {
                filteredList = new ArrayList<GWTJahiaNode>();
            } else {
                filteredList = new ArrayList<GWTJahiaNode>(filteredList.subList(offset, Math.min(length - 1, offset + limit)));
            }
        }
        return new BasePagingLoadResult<GWTJahiaNode>(filteredList, offset, length);
    }

    @Override
    public List<GWTJahiaNode> getRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes,
                                      List<String> filters, List<String> fields, List<String> selectedNodes,
                                      List<String> openPaths, boolean checkSubChild, boolean displayHiddenTypes,
                                      List<String> hiddenTypes, String hiddenRegex, boolean useUILocale) throws GWTJahiaServiceException {
        if (openPaths == null || openPaths.size() == 0) {
            openPaths = getOpenPathsForRepository(paths.toString());
        }

        if (logger.isDebugEnabled()) {
            logger.debug("retrieving open paths for " + paths + " :\n" + openPaths);
        }
        Locale locale = useUILocale ? getUILocale() : getLocale();
        List<GWTJahiaNode> result = navigation.retrieveRoot(paths, nodeTypes, mimeTypes, filters, fields, selectedNodes, openPaths, getSite(),
                retrieveCurrentSession(getWorkspace(), locale, true), locale, checkSubChild, displayHiddenTypes, hiddenTypes, hiddenRegex);
        return result;
    }

    @Override
    public List<GWTJahiaNode> getNodes(List<String> paths, List<String> fields) {
        long timer = System.currentTimeMillis();

        List<GWTJahiaNode> list = getNodesInternal(paths, fields);

        if (logger.isDebugEnabled()) {
            if (paths.size() > 3) {
                logger.debug("getNodes took {} ms for {} paths: {},...", new Object[]{
                        System.currentTimeMillis() - timer, paths.size(), StringUtils.join(paths.subList(0, 3), ", ")});
            } else {
                logger.debug("getNodes took {} ms for paths: {}", System.currentTimeMillis() - timer,
                        StringUtils.join(paths, ", "));
            }
        }
        return list;
    }

    private List<GWTJahiaNode> getNodesInternal(List<String> paths, List<String> fields) {
        List<GWTJahiaNode> list = new ArrayList<GWTJahiaNode>();
        for (String path : paths) {
            try {
                JCRNodeWrapper node = retrieveCurrentSession(getWorkspace(), getLocale(), true).getNode(path);
                GWTJahiaNode gwtJahiaNode = navigation.getGWTJahiaNode(node, fields, getUILocale());
                list.add(gwtJahiaNode);
            } catch (GWTJahiaServiceException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Missing area {}. Skipping.", path);
                }
            } catch (RepositoryException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            }
        }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, List<? extends ModelData>> getNodesAndTypes(List<ModelData> getNodesParams, List<String> types) throws GWTJahiaServiceException {
        long timer = System.currentTimeMillis();

        Map<String, List<? extends ModelData>> m = new HashMap<String, List<? extends ModelData>>();

        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        List<String> paths = new ArrayList<String>();
        for (ModelData params : getNodesParams) {
            if (logger.isDebugEnabled()) {
                paths.addAll(((List<String>) params.get("paths")));
            }
            nodes.addAll(getNodesInternal((List<String>) params.get("paths"), (List<String>) params.get("fields")));
        }
        m.put("nodes", nodes);
        for (GWTJahiaNode node : nodes) {
            if (!types.contains(node.getNodeTypes().get(0))) {
                types.add(node.getNodeTypes().get(0));
            }
        }
        m.put("types", getNodeTypes(types));

        if (logger.isDebugEnabled()) {
            if (paths.size() > 3) {
                logger.debug(
                        "getNodesAndTypes took {} ms for {} paths: {},...",
                        new Object[]{System.currentTimeMillis() - timer, paths.size(),
                                StringUtils.join(paths.subList(0, 3), ", ")}
                );
            } else {
                logger.debug("getNodesAndTypes took {} ms for paths: {}", System.currentTimeMillis() - timer,
                        StringUtils.join(paths, ", "));
            }
        }

        return m;
    }

    /**
     * Get a node by its path if existing.
     *
     * @param path path of the node you want
     * @return the founded node if existing
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException if node does not exist
     */
    public GWTJahiaNode getNode(String path) throws GWTJahiaServiceException {
        return navigation.getNode(path, null, retrieveCurrentSession(), getUILocale());
    }


    @Override
    public void saveOpenPathsForRepository(String repositoryType, List<String> paths) throws GWTJahiaServiceException {
        getSession().setAttribute(NavigationHelper.SAVED_OPEN_PATHS + repositoryType, paths);
    }

    @Override
    public BasePagingLoadResult<GWTJahiaNode> search(GWTJahiaSearchQuery searchQuery, int limit, int offset, boolean showOnlyNodesWithTemplates)
            throws GWTJahiaServiceException {
        // To do: find a better war to handle total size
        List<GWTJahiaNode> result = search.search(searchQuery, 0, 0, showOnlyNodesWithTemplates, getSite().getSiteKey().equals("systemsite") ? null : getSite(), retrieveCurrentSession());
        int size = result.size();
        result = new ArrayList<GWTJahiaNode>(result.subList(offset, Math.min(size, offset + limit)));
        return new BasePagingLoadResult<GWTJahiaNode>(result, offset, size);
    }


    @Override
    public List<GWTJahiaNode> search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes,
                                     List<String> filters) throws GWTJahiaServiceException {
        return search.search(searchString, limit, nodeTypes, mimeTypes, filters, getSite().getSiteKey().equals("systemsite") ? null : getSite(), retrieveCurrentSession());
    }

    @Override
    public PagingLoadResult<GWTJahiaNode> searchSQL(String searchString, int limit, int offset, List<String> nodeTypes,
                                                    List<String> fields, boolean sortOnDisplayName) throws GWTJahiaServiceException {
        List<GWTJahiaNode> gwtJahiaNodes = search.searchSQL(searchString, limit, offset, nodeTypes, null, null, fields, retrieveCurrentSession());
        int total = gwtJahiaNodes.size();
        if (limit >= 0) {
            try {
                Query q = retrieveCurrentSession().getWorkspace().getQueryManager().createQuery(searchString,Query.JCR_SQL2);
                total = (int) q.execute().getNodes().getSize();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (sortOnDisplayName) {
            final Collator collator = Collator.getInstance(retrieveCurrentSession().getLocale());
            Collections.sort(gwtJahiaNodes, new Comparator<GWTJahiaNode>() {
                @Override
                public int compare(GWTJahiaNode o1, GWTJahiaNode o2) {
                    return collator.compare(o1.getDisplayName(), o2.getDisplayName());
                }
            });
        }
       return new BasePagingLoadResult<GWTJahiaNode>(gwtJahiaNodes, offset, total);
    }

    @Override
    public List<GWTJahiaPortletDefinition> searchPortlets(String match) throws GWTJahiaServiceException {
        try {
            return portlet.searchPortlets(match, getLocale(), retrieveCurrentSession(), getUILocale());
        } catch (Exception e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.search.portlets", getUILocale(), e.getLocalizedMessage()));
        }
    }

    @Override
    public List<GWTJahiaNode> getSavedSearch() throws GWTJahiaServiceException {
        return search.getSavedSearch(getSite().getSiteKey().equals("systemsite") ? null : getSite(), retrieveCurrentSession());
    }

    @Override
    public void saveSearch(GWTJahiaSearchQuery searchQuery, String path, String name, boolean onTopOf)
            throws GWTJahiaServiceException {
        if (onTopOf) {
            final GWTJahiaNode parentNode = navigation.getParentNode(path, retrieveCurrentSession());
            final GWTJahiaNode jahiaNode =
                    search.saveSearch(searchQuery, parentNode.getPath(), name, retrieveCurrentSession(), getUILocale());
            try {
                contentManager.moveOnTopOf(jahiaNode.getPath(), path, retrieveCurrentSession());
            } catch (RepositoryException e) {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.store.query", getUILocale(), getLocalizedMessage(e)));
            }
        } else {
            search.saveSearch(searchQuery, path, name, retrieveCurrentSession(), getUILocale());
        }
    }

    @Override
    public void storePasswordForProvider(String providerKey, String username, String password) throws GWTJahiaServiceException  {
        try {
            final JCRSessionWrapper session = retrieveCurrentSession();
            contentHub.storePasswordForProvider(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(getUser().getLocalPath(), session), providerKey, username, password);
            session.save();
        } catch (RepositoryException e) {
            logger.error("Cannot save user properties",e);
            throw new GWTJahiaServiceException(e);
        }
    }

    @Override
    public Map<String, String> getStoredPasswordsProviders() {
        return contentHub.getStoredPasswordsProviders(getUser());
    }

    @Override
    public void setLock(List<String> paths, boolean locked) throws GWTJahiaServiceException {
        contentManager.setLock(paths, locked, retrieveCurrentSession());
    }

    @Override
    public void clearAllLocks(String path, boolean processChildNodes) throws GWTJahiaServiceException {
        contentManager.clearAllLocks(path, processChildNodes, retrieveCurrentSession(), getUILocale());
    }

    @Override
    public void markForDeletion(List<String> paths, String comment) throws GWTJahiaServiceException {
        contentManager.deletePaths(paths, false, comment, getUser(), retrieveCurrentSession(), getUILocale());
    }

    @Override
    public void deletePaths(List<String> paths) throws GWTJahiaServiceException {
        contentManager.deletePaths(paths, true, null, getUser(), retrieveCurrentSession(), getUILocale());
    }

    @Override
    public void undeletePaths(List<String> paths) throws GWTJahiaServiceException {
        contentManager.undeletePaths(paths, getUser(), retrieveCurrentSession(), getUILocale());
    }

    @Override
    public String getAbsolutePath(String path) throws GWTJahiaServiceException {
        return navigation.getAbsolutePath(path, retrieveCurrentSession(), getRequest(), getUILocale());
    }

    @Override
    public void checkWriteable(List<String> paths) throws GWTJahiaServiceException {
        contentManager.checkWriteable(paths, getUser(), retrieveCurrentSession(), getUILocale());
    }

    @Override
    public void paste(List<String> pathsToCopy, String destinationPath, String newName, boolean cut, List<String> childNodeTypesToSkip)
            throws GWTJahiaServiceException {
        contentManager
                .copy(pathsToCopy, destinationPath, newName, false, cut, false, childNodeTypesToSkip, true, retrieveCurrentSession(getLocale()), getUILocale());
    }

    @Override
    public void pasteReferences(List<String> pathsToCopy, String destinationPath, String newName)
            throws GWTJahiaServiceException {
        contentManager
                .copy(pathsToCopy, destinationPath, newName, false, false, true, null, false, retrieveCurrentSession(getLocale()), getUILocale());
    }

    @Override
    public GWTJahiaGetPropertiesResult getProperties(String path, String langCode) throws GWTJahiaServiceException {
        if (langCode == null) {
            return getProperties(path, getLocale());
        }
        return getProperties(path, LanguageCodeConverters.getLocaleFromCode(langCode));
    }


    /**
     * Get GWTJahiaGetPropertiesResult
     *
     * @param path
     * @param locale
     * @return
     * @throws GWTJahiaServiceException
     */
    private GWTJahiaGetPropertiesResult getProperties(String path, Locale locale) throws GWTJahiaServiceException {
        final GWTJahiaNode node = navigation.getNode(path, GWTJahiaNode.DEFAULT_FIELDS, retrieveCurrentSession(locale != null ? locale : retrieveCurrentSession().getLocale()), getUILocale());

        // get node type
        final List<GWTJahiaNodeType> nodeTypes = contentDefinition.getNodeTypes(node.getNodeTypes(), getUILocale());

        // get properties
        final Map<String, GWTJahiaNodeProperty> props = properties.getProperties(path, retrieveCurrentSession(locale), getUILocale());

        final GWTJahiaGetPropertiesResult result = new GWTJahiaGetPropertiesResult(nodeTypes, props);
        result.setNode(node);
        result.setAvailabledLanguages(languages.getLanguages(getSite(), getLocale()));
        result.setCurrentLocale(languages.getCurrentLang(getLocale()));
        return result;
    }

    /**
     * Save properties of for the given nodes
     *
     * @param nodes
     * @param newProps
     * @param removedTypes
     * @throws GWTJahiaServiceException
     */
    @Override
    public void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, Set<String> removedTypes)
            throws GWTJahiaServiceException {
        JCRSessionWrapper s = retrieveCurrentSession();
        try {
            properties.saveProperties(nodes, newProps, removedTypes, s, getLocale(), getSession().getId());
            retrieveCurrentSession().save();
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        }


    }

    /**
     * Save properties and acl for the given nodes
     *
     * @param nodes
     * @param acl
     * @param removedTypes
     * @throws GWTJahiaServiceException
     */
    @Override
    public void savePropertiesAndACL(List<GWTJahiaNode> nodes, GWTJahiaNodeACL acl,
                                     Map<String, List<GWTJahiaNodeProperty>> langCodeProperties,
                                     List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes) throws GWTJahiaServiceException {

        try {
            List<JCRSessionWrapper> sessions = new ArrayList<>();
            JCRSessionWrapper session = retrieveCurrentSession(null);
            sessions.add(session);

            // save shared properties
            properties.saveProperties(nodes, sharedProperties, removedTypes, session, getUILocale(), getSession().getId());


            // save properties per lang
            for (String currentLangCode : langCodeProperties.keySet()) {
                List<GWTJahiaNodeProperty> props = langCodeProperties.get(currentLangCode);
                final Locale locale = LanguageCodeConverters.languageCodeToLocale(currentLangCode);
                session = retrieveCurrentSession(locale);
                sessions.add(session);
                properties.saveProperties(nodes, props, removedTypes, session, getUILocale(), getSession().getId());
            }
            for (JCRSessionWrapper sessionWrapper : sessions) {
                sessionWrapper.validate();
            }
            if (acl != null) {
                for (GWTJahiaNode node : nodes) {
                    contentManager.setACL(node.getUUID(), acl, retrieveCurrentSession());
                }
            }
            for (JCRSessionWrapper sessionWrapper : sessions) {
                sessionWrapper.save();
            }
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            if (e instanceof CompositeConstraintViolationException) {
                properties.convertException((CompositeConstraintViolationException) e);
            }
            if (e instanceof NodeConstraintViolationException) {
                properties.convertException((NodeConstraintViolationException) e);
            }
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        } catch (LockException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        }


    }

    /**
     * Save node with properties, acl and new ordered children
     *
     * @param node
     * @param acl
     * @param langCodeProperties
     * @param sharedProperties
     * @param removedTypes
     * @throws GWTJahiaServiceException
     */
    @Override
    @SuppressWarnings("unchecked")
    public RpcMap saveNode(GWTJahiaNode node, GWTJahiaNodeACL acl,
                           Map<String, List<GWTJahiaNodeProperty>> langCodeProperties,
                           List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes) throws GWTJahiaServiceException {
        RpcMap result = new RpcMap();
        final JCRSessionWrapper jcrSessionWrapper = retrieveCurrentSession();

        try {
            JCRNodeWrapper nodeWrapper = jcrSessionWrapper.getNodeByUUID(node.getUUID());
            if (!nodeWrapper.getName().equals(JCRContentUtils.escapeLocalNodeName(node.getName()))) {
                String name = contentManager.findAvailableName(nodeWrapper.getParent(), JCRContentUtils.escapeLocalNodeName(node.getName()));
                nodeWrapper.rename(name);
                jcrSessionWrapper.save();
            }
            node.setName(nodeWrapper.getName());
            node.setPath(nodeWrapper.getPath());

//        setLock(Arrays.asList(node.getPath()), false);
            saveProperties(node, langCodeProperties, sharedProperties, removedTypes);

            if (node.get(GWTJahiaNode.INCLUDE_CHILDREN) != null) {
                List<String> removedChildrenPaths = node.getRemovedChildrenPaths();
                if (!removedChildrenPaths.isEmpty()) {
                    deletePaths(removedChildrenPaths);
                    node.clearRemovedChildrenPaths();
                }
                List<String> newNames = new ArrayList<String>();
                for (ModelData modelData : node.getChildren()) {
                    GWTJahiaNode subNode = ((GWTJahiaNode) modelData);
                    subNode.setPath(node.getPath() + "/" + subNode.getName());
                    String renamedFrom = (String) subNode.get("renamedFrom");
                    if (renamedFrom != null && nodeWrapper.hasNode(renamedFrom)) {
                        // renaming sub-node first
                        nodeWrapper.getNode(renamedFrom).rename(subNode.getName());
                    }
                    newNames.add(JCRContentUtils.escapeLocalNodeName(subNode.getName()));
                    if (subNode.get("nodeLangCodeProperties") != null && subNode.get("nodeProperties") != null) {
                        if (nodeWrapper.hasNode(JCRContentUtils.escapeLocalNodeName(subNode.getName()))) {
                            saveNode(subNode, null, (Map<String, List<GWTJahiaNodeProperty>>) subNode.get("nodeLangCodeProperties"), (List<GWTJahiaNodeProperty>) subNode.get("nodeProperties"), new HashSet<String>());
                        } else {
                            createNode(node.getPath(), subNode);
                        }
                    }
                }
                if (nodeWrapper.getPrimaryNodeType().hasOrderableChildNodes()) {
                    List<String> oldNames = new ArrayList<String>();
                    NodeIterator oldChildrenNodes = nodeWrapper.getNodes();
                    while (oldChildrenNodes.hasNext()) {
                        JCRNodeWrapper next = (JCRNodeWrapper) oldChildrenNodes.next();
                        if (newNames.contains(next.getName())) {
                            oldNames.add(next.getName());
                        }
                    }
                    if (!oldNames.equals(newNames)) {
                        for (String newName : newNames) {
                            nodeWrapper.orderBefore(newName, null);
                        }
                    }
                }
            }
            // save acl
            if (acl != null) {
                contentManager.setACL(node.getUUID(), acl, jcrSessionWrapper);
            }
            if (node.get("vanityMappings") != null) {
                saveUrlMappings(node, (Map<String, List<GWTJahiaUrlMapping>>) node.get("vanityMappings"));
            }
            if (node.get("visibilityConditions") != null) {
                List<GWTJahiaNode> visibilityConditions = (List<GWTJahiaNode>) node.get("visibilityConditions");
                contentManager.saveVisibilityConditions(node, visibilityConditions, jcrSessionWrapper, getUILocale(), getSession().getId());
                try {
                    for (GWTJahiaNode condition : visibilityConditions) {
                        if (Boolean.TRUE.equals(condition.get("node-published") != null)) {
                            publication.publish(Arrays.asList(jcrSessionWrapper.getNode(condition.getPath()).getIdentifier()));
                        }
                    }
                    if (Boolean.TRUE.equals(node.get("conditions-published"))) {
                        publication.publish(Arrays.asList(jcrSessionWrapper.getNode(node.getPath() + "/" + VisibilityService.NODE_NAME).getIdentifier()));
                    }
                } catch (RepositoryException e) {
                    logger.error("Error while saving visibility conditions for node "+node.getPath(),e);
                    throw new GWTJahiaServiceException(e);
                }
            }
            if (node.get("activeWorkflows") != null) {
                workflow.updateWorkflowRules(node,
                        (Set<GWTJahiaWorkflowDefinition>) node.get("activeWorkflows"), jcrSessionWrapper);
            }

            GWTResourceBundle rb = node.get(GWTJahiaNode.RESOURCE_BUNDLE);
            boolean needPermissionReload = false;
            if (rb != null) {
                needPermissionReload = GWTResourceBundleUtils.store(node, rb, jcrSessionWrapper);
            }

            jcrSessionWrapper.save();

            if (rb != null) {
                try {
                    result.put(GWTJahiaNode.SITE_LANGUAGES,
                            languages.getLanguages((JCRSiteNode) jcrSessionWrapper.getNodeByIdentifier(node.getSiteUUID()),
                                    getLocale())
                    );
                    if (needPermissionReload) {
                        // need to load the permissions
                        result.put(GWTJahiaNode.PERMISSIONS, getAvailablePermissions());
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (LockException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("could.not.be.accessed", getUILocale(), node.getDisplayName(), getLocalizedMessage(e)));
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            logger.debug(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        }

        closeEditEngine(node.getPath());
        return result;
    }

    private void saveProperties(GWTJahiaNode node, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNodeProperty> sharedProperties, Set<String> removedTypes) throws GWTJahiaServiceException {
        Iterator<String> langCode = langCodeProperties.keySet().iterator();

        try {
            List<JCRSessionWrapper> sessions = new ArrayList<>();
            JCRSessionWrapper session = retrieveCurrentSession(null);
            sessions.add(session);

            // save shared properties
            properties.saveProperties(Arrays.asList(node), sharedProperties, removedTypes, session, getUILocale(), getSession().getId());
            if (!removedTypes.isEmpty()) {
                for (ExtendedNodeType mixin : retrieveCurrentSession().getNodeByUUID(node.getUUID()).getMixinNodeTypes()) {
                    removedTypes.remove(mixin.getName());
                }
            }
            // save properties per lang
            while (langCode.hasNext()) {
                String currentLangCode = langCode.next();
                List<GWTJahiaNodeProperty> props = langCodeProperties.get(currentLangCode);
                session = retrieveCurrentSession(LanguageCodeConverters.languageCodeToLocale(currentLangCode));
                sessions.add(session);
                properties.saveProperties(Arrays.asList(node), props, removedTypes, session, getUILocale(), getSession().getId());
            }
            for (JCRSessionWrapper sessionWrapper : sessions) {
                sessionWrapper.validate();
            }
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            if (e instanceof CompositeConstraintViolationException) {
                properties.convertException((CompositeConstraintViolationException) e);
            }
            if (e instanceof NodeConstraintViolationException) {
                properties.convertException((NodeConstraintViolationException) e);
            }
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        } catch (LockException e) {
            logger.debug(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.save.properties", getUILocale(), getLocalizedMessage(e)));
        }
    }


    /**
     * Create node based on GWTJahiaNode with subnodes
     *
     * @param parentPath parent path
     * @param newNode    node to create
     * @return new node
     * @throws GWTJahiaServiceException
     */
    @Override
    @SuppressWarnings("unchecked")
    public GWTJahiaNode createNode(String parentPath, GWTJahiaNode newNode) throws GWTJahiaServiceException {
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        for (ModelData modelData : newNode.getChildren()) {
            nodes.add((GWTJahiaNode) modelData);
        }
        return createNode(parentPath, newNode.getName(), newNode.getNodeTypes().get(0), newNode.getNodeTypes().subList(1, newNode.getNodeTypes().size()), (GWTJahiaNodeACL) newNode.get("newAcl"),
                (List<GWTJahiaNodeProperty>) newNode.get("nodeProperties"), (Map<String, List<GWTJahiaNodeProperty>>) newNode.get("nodeLangCodeProperties"), nodes, null, true);
    }

    /**
     * Create node with multi-language
     *
     * @param parentPath
     * @param name
     * @param nodeType
     * @param mixin
     * @param acl
     * @param props
     * @param langCodeProperties
     * @param subNodes
     * @param parentNodesType
     * @param forceCreation      @return
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin,
                                   GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> props,
                                   Map<String, List<GWTJahiaNodeProperty>> langCodeProperties, List<GWTJahiaNode> subNodes, Map<String, String> parentNodesType, boolean forceCreation)
            throws GWTJahiaServiceException {
        if (name == null) {
            if (langCodeProperties != null) {
                String defaultLanguage = getLocale().toString();
                if (getSite() != null) {
                    defaultLanguage = getSite().getDefaultLanguage();
                }
                List<GWTJahiaNodeProperty> l = langCodeProperties.get(defaultLanguage);
                if (l == null && langCodeProperties.size() > 0) {
                    l = langCodeProperties.values().iterator().next();
                }
                if (l != null) {
                    name = contentManager.generateNameFromTitle(l);
                }
            }
        }
        final JCRSessionWrapper session = retrieveCurrentSession();
        GWTJahiaNode res = contentManager.createNode(parentPath, name, nodeType, mixin, props, session, getUILocale(), parentNodesType, forceCreation, getSession().getId());
        List<String> fields = Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.TAGS, GWTJahiaNode.CHILDREN_INFO, "j:view", "j:width", "j:height", GWTJahiaNode.LOCKS_INFO, GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO);
        GWTJahiaNode node;
        try {
            final JCRNodeWrapper nodeWrapper = session.getNodeByUUID(res.getUUID());
            node = navigation.getGWTJahiaNode(nodeWrapper, fields);

            if (acl != null && (!acl.getAce().isEmpty() || acl.isBreakAllInheritance())) {
                contentManager.setACL(res.getUUID(), acl, session);
            }

            if (langCodeProperties != null && !langCodeProperties.isEmpty()) {
                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
                nodes.add(node);
                ArrayList<String> locales = new ArrayList<String>(langCodeProperties.keySet());
                locales.remove(session.getLocale().toString());
                locales.add(0, session.getLocale().toString());
                Iterator<String> langCode = locales.iterator();
                // save properties per lang
                while (langCode.hasNext()) {
                    String currentLangCode = langCode.next();
                    List<GWTJahiaNodeProperty> properties = langCodeProperties.get(currentLangCode);
                    JCRSessionWrapper langSession = retrieveCurrentSession(LanguageCodeConverters.languageCodeToLocale(currentLangCode));
                    if (properties != null) {
                        this.properties.saveProperties(nodes, properties, null, langSession, getUILocale(), getSession().getId());
                        langSession.validate();
                    }
                }
            }
            session.save();
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            if (e instanceof CompositeConstraintViolationException) {
                properties.convertException((CompositeConstraintViolationException) e);
            }
            if (e instanceof NodeConstraintViolationException) {
                properties.convertException((NodeConstraintViolationException) e);
            }
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        } catch (NamespaceException e) {
            throw new GWTJahiaServiceException(e.getMessage() != null ? Messages.getInternal(
                    "label.gwt.error.jcr.namespace", getUILocale())
                    + "\n"
                    + Messages.getInternal("label.cause", getUILocale()) + ": " + e.getMessage()
                    : Messages.getInternal("label.gwt.error.jcr.namespace", getUILocale()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        }

        if (subNodes != null) {
            for (GWTJahiaNode subNode : subNodes) {
                createNode(node.getPath(), subNode);
            }
        }

        return node;
    }

    /**
     * Move and create node
     *
     * @param path
     * @param name
     * @param nodeType
     * @param mixin
     * @param acl
     * @param properties
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTJahiaNode createNodeAndMoveBefore(String path, String name, String nodeType, List<String> mixin,
                                                GWTJahiaNodeACL acl, List<GWTJahiaNodeProperty> properties,
                                                Map<String, List<GWTJahiaNodeProperty>> langCodeProperties)
            throws GWTJahiaServiceException {

        final GWTJahiaNode parentNode = navigation.getParentNode(path, retrieveCurrentSession());

        GWTJahiaNode jahiaNode =
                createNode(parentNode.getPath(), name, nodeType, mixin, acl, properties, langCodeProperties, null, null, true);

        try {
            contentManager.moveOnTopOf(jahiaNode.getPath(), path, retrieveCurrentSession());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.move.node", getUILocale(), getLocalizedMessage(e)));
        }

        try {
            retrieveCurrentSession().save();
        } catch (javax.jcr.nodetype.ConstraintViolationException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        }

        return jahiaNode;
    }

    /**
     * Create Folder
     *
     * @param parentPath
     * @param name
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTJahiaNode createFolder(String parentPath, String name) throws GWTJahiaServiceException {
        return contentManager.createFolder(parentPath, name, retrieveCurrentSession(), getUILocale(), getSession().getId());
    }

    /**
     * Create new page from page model
     * @param sourcePath path of the page model
     * @param destinationPath path of the page to be created
     * @param name name of the new page
     * @param nodeType type of the new page
     * @param properties defined properties
     * @param langCodeProperties defined i18n propreties
     * @return the create page as a GWT object
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode createPageFromPageModel(String sourcePath, String destinationPath, String name, String nodeType, List<GWTJahiaNodeProperty> properties, Map<String, List<GWTJahiaNodeProperty>> langCodeProperties) throws GWTJahiaServiceException {
        // copy node
        try {
            JCRSessionWrapper currentSession = retrieveCurrentSession(null);
            List<String> childNodeTypesToSkip = currentSession.getNode(sourcePath).hasProperty("j:copySubPages") && currentSession.getNode(sourcePath).getProperty("j:copySubPages").getBoolean() ? null : Collections.singletonList("jnt:page");
            GWTJahiaNode result = contentManager
                    .copy(Collections.singletonList(sourcePath), destinationPath, name, false, false, false, childNodeTypesToSkip, true, retrieveCurrentSession(getLocale()), getUILocale()).get(0);
            Set<String> removedTypes = new HashSet<>();
            removedTypes.add("jmix:vanityUrlMapped");
            removedTypes.add("jmix:canBeUseAsTemplateModel");
            removedTypes.add("jmix:accessControlled");
            // remove mixins from targeted Node
            for (String type : removedTypes) {
                result.getNodeTypes().remove(type);
            }

            saveProperties(result, langCodeProperties, properties, removedTypes);
            currentSession.save();
            return result;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.creation.failed.cause", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public GWTJahiaNode createPortletInstance(String path, GWTJahiaNewPortletInstance wiz)
            throws GWTJahiaServiceException {
        return portlet.createPortletInstance(path, wiz, retrieveCurrentSession(), getUILocale(), getSession().getId());
    }

    @Override
    public GWTJahiaNode createRSSPortletInstance(String path, String name, String url) throws GWTJahiaServiceException {
        return portlet.createRSSPortletInstance(path, name, url, getSite(), retrieveCurrentSession(), getUILocale(), getSession().getId());
    }

    @Override
    public GWTJahiaNode createGoogleGadgetPortletInstance(String path, String name, String script)
            throws GWTJahiaServiceException {
        return portlet.createGoogleGadgetPortletInstance(path, name, script, getSite(), retrieveCurrentSession(), getUILocale(), getSession().getId());
    }

    @Override
    public void checkExistence(String path) throws GWTJahiaServiceException {
        if (contentManager.checkExistence(path, retrieveCurrentSession(), getUILocale())) {
            throw new ExistingFileException(path);
        }
    }

    @Override
    public GWTJahiaNode rename(String path, String newName) throws GWTJahiaServiceException {
        return contentManager.rename(path, newName, retrieveCurrentSession(), getUILocale());
    }

    @Override
    public void move(List<String> sourcePaths, String targetPath) throws GWTJahiaServiceException {
        try {
            for (String sourcePath : sourcePaths) {
                contentManager.move(sourcePath, targetPath, retrieveCurrentSession());
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.move.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void moveAtEnd(List<String> sourcePaths, String targetPath) throws GWTJahiaServiceException {
        try {
            for (String sourcePath : sourcePaths) {
                contentManager.moveAtEnd(sourcePath, targetPath, retrieveCurrentSession());
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.move.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void moveOnTopOf(List<String> sourcePaths, String targetPath) throws GWTJahiaServiceException {
        try {
            // Reorder List
            for (String sourcePath : sourcePaths) {
                contentManager.moveOnTopOf(sourcePath, targetPath, retrieveCurrentSession());
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.move.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public GWTJahiaNodeACE createDefaultUsersGroupACE(List<String> permissions, boolean grand)
            throws GWTJahiaServiceException {
        return aclHelper.createUsersGroupACE(permissions, grand, getSite());
    }

    @Override
    public List<GWTJahiaNodeUsage> getUsages(List<String> paths) throws GWTJahiaServiceException {
        return navigation.getUsages(paths, retrieveCurrentSession(), getUILocale());
    }

    public List<GWTJahiaNode> getNodesByCategory(GWTJahiaNode category) throws GWTJahiaServiceException {
        return navigation.getNodesByCategory(category.getPath(), retrieveCurrentSession());
    }

    @Override
    public BasePagingLoadResult<GWTJahiaNode> getNodesByCategory(GWTJahiaNode category, int limit, int offset)
            throws GWTJahiaServiceException {
        // ToDo: handle pagination directly in the jcr
        final List<GWTJahiaNode> result = getNodesByCategory(category);
        return new BasePagingLoadResult<>(result, offset, result.size());
    }


    @Override
    public void zip(List<String> paths, String archiveName) throws GWTJahiaServiceException {
        zip.zip(paths, archiveName, retrieveCurrentSession(), getUILocale());
    }

    /**
     * Request to an online service the translations for all the values of a list of properties
     *
     * @param properties   a list of properties
     * @param definitions  the corresponding list of property definitions
     * @param srcLanguage  the source language code
     * @param destLanguage the destination language code
     * @param siteUUID     the site UUID
     * @return the properties with their values translated
     * @throws GWTJahiaServiceException
     */
    @Override
    public List<GWTJahiaNodeProperty> translate(List<GWTJahiaNodeProperty> properties, List<GWTJahiaItemDefinition> definitions, String srcLanguage, String destLanguage, String siteUUID) throws GWTJahiaServiceException {
        try {
            return translationHelper.translate(properties, definitions, srcLanguage, destLanguage, (JCRSiteNode) retrieveCurrentSession().getNodeByIdentifier(siteUUID), getUILocale());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.translate", getUILocale(), e.getLocalizedMessage()));
        }
    }

    /**
     * Request to an online service the translations for the values of a property
     *
     * @param property     a property
     * @param definition   the corresponding property definition
     * @param srcLanguage  the source language code
     * @param destLanguage the destination language code
     * @param siteUUID     the site UUID
     * @return the property with its values translated
     * @throws GWTJahiaServiceException
     */
    @Override
    public GWTJahiaNodeProperty translate(GWTJahiaNodeProperty property, GWTJahiaItemDefinition definition, String srcLanguage, String destLanguage, String siteUUID) throws GWTJahiaServiceException {
        try {
            return translationHelper.translate(property, definition, srcLanguage, destLanguage, (JCRSiteNode) retrieveCurrentSession().getNodeByIdentifier(siteUUID), getUILocale());
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.warn(e.getMessage(), e);
            } else {
                if (e instanceof TranslationException && ((TranslationException) e).getDetails() != null) {
                    logger.warn("{}: {}", e.getMessage(), ((TranslationException) e).getDetails());
                } else {
                    logger.warn(e.getMessage());
                }
            }
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.translate", getUILocale(), e.getLocalizedMessage()));
        }
    }

    @Override
    public void unzip(List<String> paths) throws GWTJahiaServiceException {
        zip.unzip(paths, false, retrieveCurrentSession(), getUILocale());
    }

    @Override
    public String getExportUrl(String path) throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper jcrSessionWrapper = JCRSessionFactory.getInstance().getCurrentUserSession();
            return Jahia.getContextPath() + Export.getExportServletPath() + "/" +
                    jcrSessionWrapper.getWorkspace().getName() + path;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.exportURL", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void cropImage(String path, String target, int top, int left, int width, int height, boolean forceReplace)
            throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        image.crop(path, target, top, left, width, height, forceReplace, session, getUILocale());
    }

    @Override
    public void resizeImage(String path, String target, int width, int height, boolean forceReplace)
            throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        image.resizeImage(path, target, width, height, forceReplace, session, getUILocale());
    }

    @Override
    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace)
            throws GWTJahiaServiceException {
        JCRSessionWrapper session = retrieveCurrentSession();
        image.rotateImage(path, target, clockwise, forceReplace, session, getUILocale());
    }

    @Override
    public void activateVersioning(List<String> path) throws GWTJahiaServiceException {
        versioning.activateVersioning(path, retrieveCurrentSession());
    }

    @Override
    public List<GWTJahiaNodeVersion> getVersions(String path) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = retrieveCurrentSession(getLocale()).getNode(path);
            List<GWTJahiaNodeVersion> versions = navigation.getVersions(node, !node.isNodeType("nt:file"));
            sortVersions(versions);
            return versions;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.versions", getUILocale(), getLocalizedMessage(e)));
        }
    }

    private void sortVersions(List<GWTJahiaNodeVersion> versions) {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Collections.sort(versions, new Comparator<GWTJahiaNodeVersion>() {
            @Override
            public int compare(GWTJahiaNodeVersion o1, GWTJahiaNodeVersion o2) {
                String[] strings1 = VERSION_AT_PATTERN.split(o1.getLabel());
                String[] strings2 = VERSION_AT_PATTERN.split(o2.getLabel());
                if (strings1.length == 2 && strings2.length == 2) {
                    try {

                        Date dateV2 = simpleDateFormat.parse(strings2[1]);
                        o2.setDate(dateV2);
                        Date dateV1 = simpleDateFormat.parse(strings1[1]);
                        o1.setDate(dateV1);
                        return dateV2.compareTo(dateV1);
                    } catch (ParseException e) {
                        return o1.getLabel().compareTo(o2.getLabel());
                    }
                } else {
                    return o1.getLabel().compareTo(o2.getLabel());
                }
            }
        });
    }

    @Override
    public BasePagingLoadResult<GWTJahiaNodeVersion> getVersions(GWTJahiaNode node, int limit, int offset)
            throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper nodeWrapper = JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace(), getLocale()).getNode(
                    node.getPath());
            List<GWTJahiaNodeVersion> result = navigation.getVersions(
                    nodeWrapper, true);
            sortVersions(result);
            // add current workspace version
            final GWTJahiaNodeVersion liveVersion = new GWTJahiaNodeVersion("live", node);
            result.add(0, liveVersion);
            liveVersion.setUrl(navigation.getNodeURL(null, nodeWrapper, null, null, "live", getLocale(), false));

            final GWTJahiaNodeVersion defaultVersion = new GWTJahiaNodeVersion("default", node);
            result.add(0, defaultVersion);
            defaultVersion.setUrl(navigation.getNodeURL(null, nodeWrapper, null, null, "default", getLocale(), false));

            // get sublist: Todo Find a better way
            int size = result.size();
            result = new ArrayList<GWTJahiaNodeVersion>(result.subList(offset, Math.min(size, offset + limit)));
            return new BasePagingLoadResult<GWTJahiaNodeVersion>(result, offset, size);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.versions", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void restoreNode(GWTJahiaNodeVersion gwtJahiaNodeVersion, boolean allSubTree)
            throws GWTJahiaServiceException {
        String nodeUuid = gwtJahiaNodeVersion.getNode().getUUID();
        // restore by label
        versioning.restoreVersionLabel(nodeUuid, gwtJahiaNodeVersion.getDate(), gwtJahiaNodeVersion.getLabel(),
                allSubTree, retrieveCurrentSession());
    }

    @Override
    public void restoreNodeByIdentifierAndDate(String identifier, Date versionDate, String versionLabel, boolean allSubTree)
            throws GWTJahiaServiceException {
        // restore by label
        versioning.restoreVersionLabel(identifier, versionDate, versionLabel, allSubTree, retrieveCurrentSession());
    }

    @Override
    public void uploadedFile(List<String[]> uploadeds) throws GWTJahiaServiceException {
        for (String[] uploaded : uploadeds) {
            contentManager.uploadedFile(uploaded[0], uploaded[1], Integer.parseInt(uploaded[2]), uploaded[3], retrieveCurrentSession(), getUILocale(), getSession().getId());
        }
    }

    public GWTRenderResult getRenderedContent(String path, String workspace, String locale, String template,
                                              String configuration, final Map<String, List<String>> contextParams,
                                              boolean editMode, String configName, String channelIdentifier,
                                              String channelVariant) throws GWTJahiaServiceException {
        Locale localValue = getLocale();
        if (locale != null) {
            localValue = LanguageCodeConverters.languageCodeToLocale(locale);
            if (!getLocale().equals(localValue)) {
                getSession().setAttribute(Constants.SESSION_LOCALE, localValue);
            }
        }
        return this.template
                .getRenderedContent(path, template, configuration, contextParams, editMode, configName, getRequest(),
                        getResponse(), retrieveCurrentSession(workspace, localValue, true), getUILocale(), channelIdentifier,
                        channelVariant);
    }

    @Override
    public String getNodeURL(String servlet, String path, Date versionDate, String versionLabel, String workspace,
                             String locale, boolean findDisplayable) throws GWTJahiaServiceException {
        final JCRSessionWrapper session = retrieveCurrentSession(workspace != null ? workspace : getWorkspace(),
                locale != null ? LanguageCodeConverters.languageCodeToLocale(locale) : getLocale(), false);
        try {
            JCRNodeWrapper node = session.getNode(path);

            String nodeURL = this.navigation.getNodeURL(servlet, node, versionDate, versionLabel, session.getWorkspace().getName(),
                    session.getLocale(), findDisplayable);

            if ("live".equals(workspace) && !SettingsBean.getInstance().isUrlRewriteSeoRulesEnabled()) {
                nodeURL = Url.appendServerNameIfNeeded(node, nodeURL, getRequest());
            }

            return getResponse().encodeURL(nodeURL);
        } catch (MalformedURLException e) {
            throw new GWTJahiaServiceException(e);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e);
        }
    }

    @Override
    public List<GWTJahiaJobDetail> importContent(String parentPath, String fileKey, Boolean replaceContent)
            throws GWTJahiaServiceException {
        List<GWTJahiaJobDetail> details = schedulerHelper.getActiveJobs(getUILocale());
        contentManager.importContent(parentPath, fileKey, replaceContent, retrieveCurrentSession(), getUILocale(), getSession().getId());
        return details;
    }

    @Override
    public List<GWTJahiaChannel> getChannels() throws GWTJahiaServiceException {
        return channelHelper.getChannels();
    }

    @Override
    public Map<String, GWTJahiaWorkflowDefinition> getWorkflowDefinitions(List<String> workflowDefinitionIds) throws GWTJahiaServiceException {
        Map<String, GWTJahiaWorkflowDefinition> l = new HashMap<String, GWTJahiaWorkflowDefinition>();
        for (String wf : workflowDefinitionIds) {
            l.put(wf, workflow.getGWTJahiaWorkflowDefinition(wf, getUILocale()));
        }
        return l;
    }

    @Override
    public void startWorkflow(String path, GWTJahiaWorkflowDefinition workflowDefinition,
                              List<GWTJahiaNodeProperty> properties, List<String> comments) throws GWTJahiaServiceException {
        workflow.startWorkflow(path, workflowDefinition, retrieveCurrentSession(), properties, comments);
    }

    @Override
    public void startWorkflow(List<String> uuids, GWTJahiaWorkflowDefinition def,
                              List<GWTJahiaNodeProperty> properties, List<String> comments, Map<String, Object> args, String locale) throws GWTJahiaServiceException {
        workflow.startWorkflow(uuids, def, retrieveCurrentSession(locale != null ? LanguageCodeConverters.languageCodeToLocale(locale) : getLocale()), properties, comments, args);
    }

    @Override
    public void abortWorkflow(String processId, String provider) throws GWTJahiaServiceException {
        workflow.abortWorkflow(processId, provider);
    }


    @Override
    public void assignAndCompleteTask(GWTJahiaWorkflowTask task, GWTJahiaWorkflowOutcome outcome,
                                      List<GWTJahiaNodeProperty> properties) throws GWTJahiaServiceException {
        workflow.assignAndCompleteTask(task, outcome, retrieveCurrentSession(), properties);
    }

    @Override
    public List<GWTJahiaWorkflowComment> addCommentToWorkflow(GWTJahiaWorkflow wf, String comment) {
        this.workflow.addCommentToWorkflow(wf, getUser(), comment, getLocale());
        return getWorkflowComments(wf);
    }

    @Override
    public List<GWTJahiaWorkflowComment> getWorkflowComments(GWTJahiaWorkflow workflow) {
        return this.workflow.getWorkflowComments(workflow, getLocale());
    }

    @Override
    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryForUser() throws GWTJahiaServiceException {
        List<GWTJahiaWorkflowHistoryItem> res = workflow.getWorkflowHistoryForUser(getUser(), getLocale(), getUILocale());

        return res;
    }

    /**
     * Publish the specified uuids.
     *
     * @param uuids the list of node uuids to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    @Override
    public void publish(List<String> uuids,
                        List<GWTJahiaNodeProperty> properties, List<String> comments) throws GWTJahiaServiceException {
        publication.publish(uuids, retrieveCurrentSession(), getSite(), properties, comments);
    }

    /**
     * Unpublish the specified path and its subnodes.
     *
     * @param uuids the list of node uuids to publish, will not auto publish the parents
     * @throws GWTJahiaServiceException
     */
    @Override
    public void unpublish(List<String> uuids) throws GWTJahiaServiceException {
        long l = System.currentTimeMillis();
        JCRSessionWrapper session = retrieveCurrentSession();
        publication.unpublish(uuids, Collections.singleton(session.getLocale().toString()),
                session.getUser());
        if (logger.isDebugEnabled()) {
            logger.debug("-->" + (System.currentTimeMillis() - l));
        }
    }

    /**
     * Get the publication status information for multiple nodes by their identifier.
     * Check is done against the current session locale.
     *
     * @param uuids                 uuids to get publication info from
     * @param allSubTree check on the whole subtree or no.
     * @param checkForUnpublication allow to check for element which have been unpublished
     * @return a List of GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    @Override
    public List<GWTJahiaPublicationInfo> getPublicationInfo(List<String> uuids, boolean allSubTree,
                                                            boolean checkForUnpublication)
            throws GWTJahiaServiceException {
        final JCRSessionWrapper session = retrieveCurrentSession();
        return publication.getFullPublicationInfos(uuids,
                Collections.singleton(retrieveCurrentSession().getLocale().toString()),
                session, allSubTree, checkForUnpublication);
    }

    /**
     * Get the publication status information for multiple nodes by their identifier.
     * Check is done against the set of languages provided.
     *
     * @param uuids                 uuids to get publication info from
     * @param allSubTree check on the whole subtree or no.
     * @param checkForUnpublication allow to check for element which have been unpublished
     * @param languages Set of languages from which we want information
     * @return a List of GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException
     */
    @Override
    public List<GWTJahiaPublicationInfo> getPublicationInfo(List<String> uuids, boolean allSubTree,
                                                            boolean checkForUnpublication, Set<String> languages)
            throws GWTJahiaServiceException {
        final JCRSessionWrapper session = retrieveCurrentSession();

        return publication.getFullPublicationInfos(uuids, languages, session, allSubTree, checkForUnpublication);
    }


    /**
     * Get worflow info by path
     *
     * @param path
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaWorkflowInfo getWorkflowInfo(String path) throws GWTJahiaServiceException {
        return workflow.getWorkflowInfo(path, true, retrieveCurrentSession(), getLocale(), getUILocale());
    }

    /**
     * Gwt Highlighted
     *
     * @param original
     * @param amendment
     * @return
     */
    @Override
    public String getHighlighted(String original, String amendment) {
        return diff.getHighlighted(original, amendment);
    }


    @SuppressWarnings("unchecked")
    private List<String> getOpenPathsForRepository(String repositoryType) throws GWTJahiaServiceException {
        return (List<String>) getSession().getAttribute(NavigationHelper.SAVED_OPEN_PATHS + repositoryType);
    }

    private JahiaUser getUser() {
        return getRemoteJahiaUser();
    }

    /* (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService#getUrlMappings(org.jahia.ajax.gwt.client.data.node.GWTJahiaNode, java.lang.String)
     */
    @Override
    public List<GWTJahiaUrlMapping> getUrlMappings(GWTJahiaNode node, String locale) throws GWTJahiaServiceException {
        try {
            return seo.getUrlMappings(node, locale, retrieveCurrentSession());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.urlMappings", getUILocale(), getLocalizedMessage(e)));
        }
    }

    /* (non-Javadoc)
     * @see org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService#saveUrlMappings(org.jahia.ajax.gwt.client.data.node.GWTJahiaNode, java.util.Set, java.util.List)
     */
    public void saveUrlMappings(GWTJahiaNode node, Map<String, List<GWTJahiaUrlMapping>> mappings)
            throws GWTJahiaServiceException {
        try {
            seo.saveUrlMappings(node, mappings, retrieveCurrentSession());
        } catch (NonUniqueUrlMappingException e) {
            Locale uiLocale = getUILocale();
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments(
                    "failure.duplicateVanityUrlMapping", uiLocale,
                    e.getUrlMapping(), e.getNodePath(), e.getExistingNodePath(),
                    Messages.getInternal("label." + e.getWorkspace(), uiLocale)));
        } catch (ConstraintViolationException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.save.urlMappings", getUILocale(), e.getLocalizedMessage()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.save.urlMappings", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void deployTemplates(String templatesPath, String sitePath) throws GWTJahiaServiceException {
        logger.info("Deploying templates {} to the target {}", templatesPath, sitePath);
        moduleHelper.deployModule(templatesPath, sitePath, retrieveCurrentSession());
        logger.info("...template deployment done.");
    }

    @Override
    public GWTJahiaNode createModule(String moduleName, String artifactId, String groupId, String siteType, String sources) throws GWTJahiaServiceException {
        try {
            return moduleHelper.createModule(moduleName, artifactId, groupId, siteType, sources, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot create module", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.create.module", getUILocale(), e.getLocalizedMessage()));
        }
    }

    @Override
    public GWTJahiaNode checkoutModule(String moduleId, String scmURI, String scmType, String branchOrTag, String sources) throws GWTJahiaServiceException {
        try {
            return moduleHelper.checkoutModule(moduleId, scmURI, scmType, branchOrTag, sources, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot checkout module", e);
            String message = e.getLocalizedMessage();
            if (StringUtils.isEmpty(message)) {
                message = Messages.getInternal("label.gwt.error.nomessage", getUILocale());
            }
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.checkout.module", getUILocale(), message));
        }
    }

    @Override
    public GWTJahiaNode sendToSourceControl(String moduleId, String scmURI, String scmType) throws GWTJahiaServiceException {
        try {
            return contentManager.sendToSourceControl(moduleId, scmURI, scmType, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot init remote repository", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.init.remote.repo", getUILocale(), e.getLocalizedMessage()));
        }
    }

    @Override
    public void saveModule(String moduleId, String message) throws GWTJahiaServiceException {
        boolean noChanges;
        try {
            noChanges = !moduleHelper.saveAndCommitModule(moduleId, message, retrieveCurrentSession(null));
        } catch (Exception e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.synchronize.sources", getUILocale(), "\n" + e.getLocalizedMessage()));
        }

        if (noChanges) {
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.nothing.to.commit", getUILocale()));
        }
    }

    @Override
    public String updateModule(String moduleId) throws GWTJahiaServiceException {
        try {
            return moduleHelper.updateModule(moduleId, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot update module", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.update.module", getUILocale(), e.getLocalizedMessage()));
        }
    }

    /**
     * Add file to source control.
     *
     * @param moduleId Module name
     * @param node     Node denoting a file from the sources folder of the module
     * @throws GWTJahiaServiceException
     */
    @Override
    public void addToSourceControl(String moduleId, GWTJahiaNode node) throws GWTJahiaServiceException {
        try {
            moduleHelper.addToSourceControl(moduleId, node, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot add to source control", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.add.to.source.control", getUILocale(), e.getLocalizedMessage()));
        }
    }

    /**
     * Call to send conflict resolved on a node to the source control
     *
     * @param moduleId : module id involved
     * @param node     : node with conflict
     * @throws GWTJahiaServiceException
     */
    @Override
    public void markConflictAsResolved(String moduleId, GWTJahiaNode node) throws GWTJahiaServiceException {
        try {
            contentManager.markConflictAsResolved(moduleId, node, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot mark conflict as resolved", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.mark.conflict.resolved", getUILocale(), e.getLocalizedMessage()));
        }
    }

    /**
     * Call to compile and deploy a module
     *
     * @param moduleId : module to compile and deploy
     * @throws GWTJahiaServiceException
     */
    @Override
    public void compileAndDeploy(String moduleId) throws GWTJahiaServiceException {
        try {
            moduleHelper.compileAndDeploy(moduleId, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Cannot compile module", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.compile.module", getUILocale(), e.getLocalizedMessage()));
        }
    }

    /**
     * Call to generate the war of the specified module and release it in Jahia.
     * This will create a war from the current sources associated with the module.
     * And upload it in Jahia and return the file to the caller.
     *
     * @param moduleId The module to compile and package as war file.
     * @return The war file generated.
     * @throws GWTJahiaServiceException if something bad happened (like impossible to compile the module)
     */
    @Override
    public GWTJahiaNode generateWar(String moduleId) throws GWTJahiaServiceException {
        try {
            return moduleHelper.releaseModule(moduleId, null, retrieveCurrentSession(null));
        } catch (Exception e) {
            logger.error("Error during generation of a package for module " + moduleId, e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.generate.war", getUILocale(), moduleId, e.getLocalizedMessage()));
        }
    }

    /**
     * This will release the module. This means we will compile, update the version number of the module and package it as war file and then
     * deploy the newly created version in Jahia.
     *
     * @param moduleId    The module to be released.
     * @param releaseInfo the release data, like next version, should we publish to Maven distribution server and Jahia Private App Store etc.
     * @return a {@link RpcMap} with the filename, the download URL for the generated module and the newly deployed module node
     * @throws GWTJahiaServiceException if something happened during compile/deploy of the module
     */
    @Override
    public RpcMap releaseModule(String moduleId, GWTModuleReleaseInfo releaseInfo) throws GWTJahiaServiceException {
        try {
            GWTJahiaNode node = moduleHelper.releaseModule(moduleId, releaseInfo, retrieveCurrentSession(null));
            RpcMap r = new RpcMap();
            r.put("newModule", navigation.getNode("/modules/" + moduleId, GWTJahiaNode.DEFAULT_SITE_FIELDS, retrieveCurrentSession(), getUILocale()));
            r.put("artifactUrl", releaseInfo.getArtifactUrl());
            r.put("catalogModulePageUrl", releaseInfo.getForgeModulePageUrl());
            if (node != null) {
                r.put("filename", node.getName());
                r.put("downloadUrl", node.getUrl());
            }

            return r;
        } catch (Exception e) {
            final String nextVersion = releaseInfo.getNextVersion();
            logger.error("Error during releasing version " + nextVersion + " of module " + moduleId, e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.release.module", getUILocale(), nextVersion,
                    moduleId, "\n" + e.getLocalizedMessage()));
        }
    }


    /**
     * @param seoHelper the seoHelper to set
     */
    public void setSeo(SeoHelper seoHelper) {
        this.seo = seoHelper;
    }

    @Override
    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryProcesses(String nodeId, String lang)
            throws GWTJahiaServiceException {
        Locale locale = LanguageCodeConverters.languageCodeToLocale(lang);
        return workflow.getWorkflowHistoryProcesses(nodeId, retrieveCurrentSession(locale), getUILocale());
    }

    @Override
    public List<GWTJahiaWorkflowHistoryItem> getWorkflowHistoryTasks(String provider, String processId)
            throws GWTJahiaServiceException {
        return workflow.getWorkflowHistoryTasks(provider, processId, getUILocale());
    }

    @Override
    public SessionValidationResult isValidSession() throws GWTJahiaServiceException {
        // >0 : schedule poll repeating for this value
        // 0 : session expire
        // <0 : polling deactivated
        final String loginUrl = getLogingUrl();
        final HttpSession session = getRequest().getSession(false);
        if (session != null) {
            Long date = (Long) session.getAttribute("lastPoll");
            long lastAccessed = session.getLastAccessedTime();
            long now = System.currentTimeMillis();
            boolean invalidated = false;
            if (date != null && (date / 1000 == lastAccessed / 1000)) {
                // last call was (probably) a poll call
                long first = (Long) session.getAttribute("firstPoll");
                if (logger.isDebugEnabled()) {
                    logger.debug("Inactive since : " + (now - first));
                }
                if (now - first < session.getMaxInactiveInterval() * 1000) {
                    session.setMaxInactiveInterval(session.getMaxInactiveInterval() - (int) ((now - first) / 1000));
                } else {
                    session.invalidate();
                    invalidated = true;
                }
            } else {
                session.setAttribute("firstPoll", now);
            }

            if (!invalidated) {
                session.setAttribute("lastPoll", now);
            }
            return new SessionValidationResult(loginUrl, sessionPollingFrequency);
        } else {
            return new SessionValidationResult(loginUrl, 0);
        }
    }

    private String getLogingUrl() {
        HttpServletRequest request = getRequest();
        String loginUrl = StringUtils.defaultIfEmpty(LoginConfig.getInstance().getCustomLoginUrl(request),
                Login.getServletPath());
        boolean isAbsolute = ImportSupport.isAbsoluteUrl(loginUrl);
        if (!isAbsolute) {
            if (request.getContextPath().length() > 0) {
                // need to prepend context path
                loginUrl = request.getContextPath() + loginUrl;
            }
            // we run non-absolute URLs vie encodeURL()
            loginUrl = getResponse().encodeURL(loginUrl);
        }

        return loginUrl;
    }

    @Override
    public GWTJahiaCreateEngineInitBean initializeCreateEngine(String typename, String parentpath, String targetName)
            throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
            JCRNodeWrapper parent = sessionWrapper.getNode(parentpath);

            GWTJahiaCreateEngineInitBean result = new GWTJahiaCreateEngineInitBean();
            result.setLanguages(languages.getLanguages(getSite(), getLocale()));
            String defaultLanguage = parent.getResolveSite().getDefaultLanguage();
            if (defaultLanguage == null) {
                defaultLanguage = sessionWrapper.getRootNode().getResolveSite().getDefaultLanguage();
            }

            result.setDefaultLanguageCode(defaultLanguage);
            result.setCurrentLocale(languages.getCurrentLang(getLocale()));
            final List<ExtendedNodeType> availableMixins = contentDefinition.getAvailableMixin(typename, parent.getResolveSite());

            List<GWTJahiaNodeType> gwtMixin = contentDefinition.getGWTNodeTypes(availableMixins, getUILocale());

            result.setMixin(gwtMixin);

            List<ExtendedNodeType> allTypes = new ArrayList<ExtendedNodeType>();
            ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(typename);
            allTypes.add(nodeType);
            allTypes.addAll(availableMixins);

            result.setChoiceListInitializersValues(contentDefinition.getAllChoiceListInitializersValues(allTypes,
                    nodeType, null, parent, getUILocale()));
            result.setDefaultValues(contentDefinition.getAllDefaultValues(allTypes, sessionWrapper.getRootNode().getResolveSite().getLanguagesAsLocales()));

            result.setAcl(contentManager.getACL(parentpath, true, sessionWrapper, getUILocale()));
            result.setDefaultName(jcrContentUtils.generateNodeName(parent, defaultLanguage, nodeType, targetName));
            return result;
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public GWTJahiaCreatePortletInitBean initializeCreatePortletEngine(String typename, String parentpath)
            throws GWTJahiaServiceException {

        GWTJahiaCreateEngineInitBean result = initializeCreateEngine(typename, parentpath, null);
        GWTJahiaCreatePortletInitBean portletInitBean = new GWTJahiaCreatePortletInitBean();
        portletInitBean.setChoiceListInitializersValues(result.getChoiceListInitializersValues());
        portletInitBean.setLanguages(result.getLanguages());
        portletInitBean.setMixin(result.getMixin());
        portletInitBean.setNodeType(contentDefinition.getNodeType(typename, getUILocale()));

        return portletInitBean;
    }

    @Override
    public GWTJahiaEditEngineInitBean initializeEditEngine(String nodepath, boolean tryToLockNode)
            throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
            JCRNodeWrapper nodeWrapper = sessionWrapper.getNode(nodepath);
            final GWTJahiaNode node = navigation.getGWTJahiaNode(nodeWrapper);
            addEngineLock(tryToLockNode && !node.isLocked(), nodeWrapper);
            // get node type
            final List<GWTJahiaNodeType> nodeTypes =
                    contentDefinition.getNodeTypes(nodeWrapper.getNodeTypes(), getUILocale());

            // get properties
            final Map<String, GWTJahiaNodeProperty> props =
                    properties.getProperties(nodepath, retrieveCurrentSession(), getUILocale());

            final GWTJahiaEditEngineInitBean result = new GWTJahiaEditEngineInitBean(nodeTypes, props);
            result.setNode(node);
            result.hasOrderableChildNodes(nodeWrapper.getPrimaryNodeType().hasOrderableChildNodes());
            result.setAvailabledLanguages(languages.getLanguages(getSite(), getLocale()));
            result.setCurrentLocale(languages.getCurrentLang(getLocale()));

            String defaultLanguage = nodeWrapper.getResolveSite().getDefaultLanguage();
            if (defaultLanguage == null) {
                defaultLanguage = sessionWrapper.getRootNode().getResolveSite().getDefaultLanguage();
            }

            result.setDefaultLanguageCode(defaultLanguage);


            final List<ExtendedNodeType> availableMixins =
                    contentDefinition.getAvailableMixin(nodeWrapper.getPrimaryNodeTypeName(), nodeWrapper.getResolveSite());

            List<GWTJahiaNodeType> gwtMixin = contentDefinition.getGWTNodeTypes(availableMixins, getUILocale());

            result.setMixin(gwtMixin);

            List<ExtendedNodeType> allTypes = new ArrayList<ExtendedNodeType>();
            allTypes.add(nodeWrapper.getPrimaryNodeType());
            allTypes.addAll(Arrays.asList(nodeWrapper.getMixinNodeTypes()));
            allTypes.addAll(availableMixins);

            JCRNodeWrapper parent = null;
            try {
                nodeWrapper.getParent();
            } catch (ItemNotFoundException ignored) {

            }
            result.setInitializersValues(
                    contentDefinition.getAllChoiceListInitializersValues(allTypes, nodeWrapper.getPrimaryNodeType(), nodeWrapper,
                            parent, getUILocale())
            );
            result.setDefaultValues(contentDefinition.getAllDefaultValues(allTypes, sessionWrapper.getRootNode().getResolveSite().getLanguagesAsLocales()));
            final GWTJahiaNodeACL gwtJahiaNodeACL = contentManager.getACL(nodepath, false, sessionWrapper, getUILocale());
            result.setAcl(gwtJahiaNodeACL);
            Map<String, Set<String>> referencesWarnings = new HashMap<String, Set<String>>();
            for (GWTJahiaNodeProperty property : props.values()) {
                try {
                    if (property.getName().equals("*") || nodeWrapper.getProperty(property.getName()).getDefinition().isProtected()) {
                        continue;
                    }
                } catch (PathNotFoundException e) {
                    // ignore
                }
                List<GWTJahiaNode> refs = new ArrayList<GWTJahiaNode>();
                for (GWTJahiaNodePropertyValue value : (property.getValues())) {
                    if ((value.getType() == GWTJahiaNodePropertyType.REFERENCE ||
                            value.getType() == GWTJahiaNodePropertyType.WEAKREFERENCE) && value.getNode() != null) {
                        refs.add(value.getNode());
                    }
                }
                if (!refs.isEmpty()) {
                    final Set<String> allDeniedUsers = compareAcl(gwtJahiaNodeACL, refs);
                    if (!allDeniedUsers.isEmpty()) {
                        referencesWarnings.put(property.getName(), allDeniedUsers);
                    }
                }
            }
            result.setReferencesWarnings(referencesWarnings);
            result.setTranslationEnabled(translationHelper.isTranslationEnabled(nodeWrapper.getResolveSite()));
            return result;
        } catch (PathNotFoundException e) {
            // the node no longer exists
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void closeEditEngine(String nodepath)
            throws GWTJahiaServiceException {
        final JCRSessionWrapper jcrSessionWrapper = retrieveCurrentSession();
        try {
            JCRNodeWrapper n = jcrSessionWrapper.getNode(nodepath);
            if (n.getProvider().isLockingAvailable() && n.isLocked() && n.getLockOwner().equals(JCRSessionFactory.getInstance().getCurrentUser().getUsername())) {
                @SuppressWarnings("unchecked") Map<String, List<String>> locks = (Map<String, List<String>>) getRequest().getSession().getAttribute("engineLocks");
                String windowId = getRequest().getParameter("windowId");
                if (windowId != null && locks != null) {
                    if (locks.get(windowId) != null) {
                        n.unlock("engine");
                        GWTResourceBundleUtils.unlock(n);
                        locks.get(windowId).remove(n.getSession().getLocale() + "/" + n.getIdentifier());
                    }
                } else if (windowId == null) {
                    logger.error("Missing windowId : " + getRequest().getRequestURI() + "?" + getRequest().getQueryString());
                    throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.unlock.node", getUILocale()));
                }
            }

            dumpLocks(n, "closeEditEngine");
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing if lock is not supported
        } catch (RepositoryException e) {
            logger.warn("Unable to unlock node " + nodepath, e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.unlock.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public Set<String> compareAcl(GWTJahiaNodeACL nodeAcl, List<GWTJahiaNode> references)
            throws GWTJahiaServiceException {
        JCRSessionWrapper sessionWrapper = retrieveCurrentSession();

        final Set<String> result = new HashSet<String>();

        for (GWTJahiaNode reference : references) {
            GWTJahiaNodeACL referenceAcl =
                    contentManager.getACL(reference.getPath(), false, sessionWrapper, getUILocale());

            final Set<String> allReadUsers = new HashSet<String>();  // All users having read rights
            for (GWTJahiaNodeACE ace : nodeAcl.getAce()) {
                if ((ace.getRoles().containsKey("jcr:read_live") &&
                        !Boolean.FALSE.equals(ace.getRoles().get("jcr:read_live"))) ||
                        (ace.getInheritedRoles().containsKey("jcr:read_live") &&
                                !Boolean.FALSE.equals(ace.getInheritedRoles().get("jcr:read_live")))) {
                    allReadUsers.add(ace.getPrincipalType() + ":" + ace.getPrincipal());
                }
            }

            final Set<String> allDeniedUsers = new HashSet<String>();  // All users having read rights
            for (GWTJahiaNodeACE ace : referenceAcl.getAce()) {
                if ((ace.getRoles().containsKey("jcr:read_live") &&
                        !Boolean.TRUE.equals(ace.getRoles().get("jcr:read_live"))) ||
                        (ace.getInheritedRoles().containsKey("jcr:read_live") &&
                                !Boolean.TRUE.equals(ace.getInheritedRoles().get("jcr:read_live")))) {
                    allDeniedUsers.add(ace.getPrincipalType() + ":" + ace.getPrincipal());
                }
            }
            allDeniedUsers.retainAll(allReadUsers);
            result.addAll(allDeniedUsers);
        }
        return result;
    }

    @Override
    public GWTJahiaEditEngineInitBean initializeEditEngine(List<String> paths, boolean tryToLockNode)
            throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper sessionWrapper = retrieveCurrentSession();

            List<GWTJahiaNodeType> nodeTypes = null;
            List<GWTJahiaNodeType> gwtMixin = null;
            List<ExtendedNodeType> allTypes = new ArrayList<ExtendedNodeType>();

            JCRNodeWrapper nodeWrapper = null;
            for (String path : paths) {
                nodeWrapper = sessionWrapper.getNode(path);
                addEngineLock(tryToLockNode && !JCRContentUtils.isLockedAndCannotBeEdited(nodeWrapper), nodeWrapper);

                // get node type
                if (nodeTypes == null) {
                    final List<GWTJahiaNodeType> theseTypes =
                            contentDefinition.getNodeTypes(nodeWrapper.getNodeTypes(), getUILocale());
                    nodeTypes = theseTypes;
                } else {
                    List<GWTJahiaNodeType> previousTypes = new ArrayList<GWTJahiaNodeType>(nodeTypes);
                    nodeTypes.clear();
                    for (GWTJahiaNodeType p : previousTypes) {
                        if (!nodeWrapper.isNodeType(p.getName())) {
                            List<String> superTypes = p.getSuperTypes();
                            for (String s : superTypes) {
                                if (nodeWrapper.isNodeType(s)) {
                                    nodeTypes.add(0, contentDefinition.getNodeType(s, getUILocale()));
                                    break;
                                }
                            }
                        } else {
                            nodeTypes.add(p);
                        }
                    }
                }

                final List<ExtendedNodeType> availableMixins =
                        contentDefinition.getAvailableMixin(nodeWrapper.getPrimaryNodeTypeName(), nodeWrapper.getResolveSite());

                List<GWTJahiaNodeType> theseMixin = contentDefinition.getGWTNodeTypes(availableMixins, getUILocale());
                if (gwtMixin == null) {
                    gwtMixin = theseMixin;
                } else {
                    gwtMixin.retainAll(theseMixin);
                }

                allTypes.add(nodeWrapper.getPrimaryNodeType());
                allTypes.addAll(Arrays.asList(nodeWrapper.getMixinNodeTypes()));
                allTypes.addAll(availableMixins);
            }

            final GWTJahiaEditEngineInitBean result =
                    new GWTJahiaEditEngineInitBean(nodeTypes, new HashMap<String, GWTJahiaNodeProperty>());
            result.setAvailabledLanguages(languages.getLanguages(getSite(), getLocale()));
            result.setCurrentLocale(languages.getCurrentLang(getLocale()));
            result.setMixin(gwtMixin);
            result.setInitializersValues(contentDefinition.getAllChoiceListInitializersValues(allTypes,
                    NodeTypeRegistry.getInstance().getNodeType("nt:base"), nodeWrapper, nodeWrapper.getParent(),
                    getUILocale()));
            result.setDefaultValues(new HashMap<String, Map<String, List<GWTJahiaNodePropertyValue>>>());
            return result;
        } catch (PathNotFoundException e) {
            // the node no longer exists
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        }

    }

    private void addEngineLock(boolean tryToLockNode, JCRNodeWrapper nodeWrapper) throws RepositoryException, GWTJahiaServiceException {
        try {
            if (tryToLockNode && nodeWrapper.getProvider().isLockingAvailable() && nodeWrapper.hasPermission(Privilege.JCR_LOCK_MANAGEMENT)) {
                nodeWrapper.lockAndStoreToken("engine");
                GWTResourceBundleUtils.lock(nodeWrapper);
                String windowId = getRequest().getParameter("windowId");
                if (windowId != null) {
                    @SuppressWarnings("unchecked") Map<String, List<String>> locks = (Map<String, List<String>>) getRequest().getSession().getAttribute("engineLocks");
                    if (locks == null) {
                        locks = new HashMap<String, List<String>>();
                    }
                    List<String> l = locks.get(windowId);
                    if (l == null) {
                        l = new ArrayList<String>();
                        locks.put(windowId, l);
                    }
                    l.add(nodeWrapper.getSession().getLocale() + "/" + nodeWrapper.getIdentifier());
                    getRequest().getSession().setAttribute("engineLocks", locks);
                } else {
                    logger.error("Missing windowId : " + getRequest().getRequestURI() + "?" + getRequest().getQueryString());
                    throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.unlock.node", getUILocale()));
                }
            }
            dumpLocks(nodeWrapper, "initializeEditEngine");
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing if lock is not supported
        }
    }

    private void dumpLocks(JCRNodeWrapper nodeWrapper, String message) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug(message + " - Locks for " + nodeWrapper.getPath() + " - " + nodeWrapper.isLocked());
            if (nodeWrapper.hasProperty("j:lockTypes")) {
                for (Value value : nodeWrapper.getProperty("j:lockTypes").getValues()) {
                    logger.debug(value.getString());
                }
            }
            NodeIterator ni = nodeWrapper.getNodes("j:translation*");
            while (ni.hasNext()) {
                JCRNodeWrapper jcrNodeWrapper = (JCRNodeWrapper) ni.next();
                logger.debug(message + " Locks for " + jcrNodeWrapper.getPath() + " - " + jcrNodeWrapper.isLocked());
                if (jcrNodeWrapper.hasProperty("j:lockTypes")) {
                    for (Value value : jcrNodeWrapper.getProperty("j:lockTypes").getValues()) {
                        logger.debug(value.getString());
                    }
                }

            }
        }
    }

    @Override
    public Map<GWTJahiaWorkflowType, List<GWTJahiaWorkflowDefinition>> getWorkflowRules(String path)
            throws GWTJahiaServiceException {
        return workflow.getWorkflowRules(path, retrieveCurrentSession(), getUILocale());
    }

    /**
     * Returns the source node that is used in the specified reference node.
     *
     * @param referenceIdentifier the current reference node UUID
     * @return the source node that is used in the specified reference node
     * @throws GWTJahiaServiceException in case of an error
     */
    public GWTJahiaNode getSource(String referenceIdentifier) throws GWTJahiaServiceException {
        GWTJahiaNode gwtSourceNode = null;

        try {
            JCRSessionWrapper session = retrieveCurrentSession();
            JCRNodeWrapper node = session.getNodeByIdentifier(referenceIdentifier);
            if (node.hasProperty(Constants.NODE)) {
                JCRNodeWrapper source = (JCRNodeWrapper) node.getProperty(Constants.NODE).getNode();
                if (source != null) {
                    gwtSourceNode = navigation.getGWTJahiaNode(source);
                }
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.source", getUILocale(), getLocalizedMessage(e)));
        }

        return gwtSourceNode;
    }

    @Override
    public void flush(String path) throws GWTJahiaServiceException {
        try {
            if (retrieveCurrentSession().getNode(path).hasPermission("adminCache")) {
                cacheHelper.flush(path, true);
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.flush", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void flushAll() throws GWTJahiaServiceException {
        try {
            if (retrieveCurrentSession().getRootNode().hasPermission("adminCache")) {
                cacheHelper.flushAll();
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.flush", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void flushSite(String siteUUID) throws GWTJahiaServiceException {
        JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
        try {
            JCRNodeWrapper nodeWrapper = sessionWrapper.getNodeByIdentifier(siteUUID);
            if (nodeWrapper.hasPermission("adminCache")) {
                cacheHelper.flush(nodeWrapper.getPath(), true);
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.flush", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public Map<String, Object> getPollData(Set<String> keys) throws GWTJahiaServiceException {
        Map<String, Object> result = new HashMap<String, Object>();
        Locale locale = getLocale();
        if (keys.contains("activeJobs")) {
            result.put("activeJobs", schedulerHelper.getActiveJobs(locale));
        }
        if (keys.contains("numberOfTasks")) {
            result.put("numberOfTasks", workflow.getNumberOfTasksForUser(getUser()));
        }
        return result;
    }

    @Override
    public BasePagingLoadResult<GWTJahiaJobDetail> getJobs(int offset, int limit, String sortField, String sortDir,
                                                           List<String> groupNames) throws GWTJahiaServiceException {
        // todo Proper pagination support would imply that we only load the job details that were requested. Also sorting is not at all supported for the moment.
        JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
        List<GWTJahiaJobDetail> jobList =
                schedulerHelper.getAllJobs(getLocale(), sessionWrapper.getUser(), new HashSet<String>(groupNames));
        int size = jobList.size();
        jobList = new ArrayList<GWTJahiaJobDetail>(jobList.subList(offset, Math.min(size, offset + limit)));
        BasePagingLoadResult<GWTJahiaJobDetail> pagingLoadResult = new BasePagingLoadResult<GWTJahiaJobDetail>(jobList, offset, size);
        return pagingLoadResult;
    }

    @Override
    public Boolean deleteJob(String jobName, String groupName) throws GWTJahiaServiceException {
        return schedulerHelper.deleteJob(jobName, groupName);
    }

    @Override
    public List<String> getAllJobGroupNames() throws GWTJahiaServiceException {
        return schedulerHelper.getAllJobGroupNames();
    }

    @Override
    public BasePagingLoadResult<GWTJahiaContentHistoryEntry> getContentHistory(String nodeIdentifier, int offset, int limit) throws GWTJahiaServiceException {
        JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
        List<GWTJahiaContentHistoryEntry> historyListJahia;
        try {
            historyListJahia = contentManager.getContentHistory(sessionWrapper, nodeIdentifier, offset, limit);
            int size = historyListJahia.size();
            historyListJahia = new ArrayList<GWTJahiaContentHistoryEntry>(historyListJahia.subList(offset, Math.min(size, offset + limit)));
            BasePagingLoadResult<GWTJahiaContentHistoryEntry> pagingLoadResult = new BasePagingLoadResult<GWTJahiaContentHistoryEntry>(historyListJahia, offset, size);
            return pagingLoadResult;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.content.history", getUILocale(), getLocalizedMessage(e)));
        }
    }

    @Override
    public void cleanReferences(String path) throws GWTJahiaServiceException {
        contentManager.deleteReferences(path, getUser(), retrieveCurrentSession(), getUILocale());
    }

    @Override
    public GWTChoiceListInitializer getFieldInitializerValues(String typeName, String propertyName, String parentPath, Map<String, List<GWTJahiaNodePropertyValue>> dependentValues)
            throws GWTJahiaServiceException {
        try {
            JCRSessionWrapper sessionWrapper = retrieveCurrentSession();
            JCRNodeWrapper parent = sessionWrapper.getNode(parentPath);

            ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(typeName);

            GWTChoiceListInitializer initializer = contentDefinition.getInitializerValues(nodeType.getPropertyDefinition(propertyName),
                    nodeType, null, parent, dependentValues, getUILocale());

            return initializer;
        } catch (RepositoryException e) {
            logger.error("Cannot get node", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", getUILocale(), getLocalizedMessage(e)));
        }
    }

    /**
     * Get the list of all registered namespaces
     *
     * @return
     */
    @Override
    public List<String> getNamespaces() {
        return new ArrayList<String>(NodeTypeRegistry.getInstance().getNamespaces().keySet());
    }

    @Override
    public List<GWTJahiaNode> getPortalNodes(String targetAreaName) {
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        try {
            Query q = retrieveCurrentSession().getWorkspace().getQueryManager().createQuery("select * from [jnt:contentFolder] as l where localname()='" + JCRContentUtils.sqlEncode(targetAreaName) + "' and isdescendantnode(l,['/sites'])", Query.JCR_SQL2);
            QueryResult queryResult = q.execute();
            NodeIterator nodeIterator = queryResult.getNodes();
            while (nodeIterator.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodeIterator.nextNode();
                nodes.add(navigation.getGWTJahiaNode(nodeWrapper));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }
        return nodes;
    }

    @Override
    public Map<String, WCAGValidationResult> validateWCAG(Map<String, String> richTexts) {
        Map<String, WCAGValidationResult> result = new HashMap<String, WCAGValidationResult>(richTexts.size());
        Locale locale;
        try {
            locale = getUILocale();
        } catch (GWTJahiaServiceException e) {
            logger.warn("Unable to get UI locale", e);
            locale = getRequest().getLocale();
        }

        for (Map.Entry<String, String> richText : richTexts.entrySet()) {
            ValidatorResults validatorResults = new WAIValidator(locale).validate(richText.getValue());
            result.put(richText.getKey(), toWCAGResult(validatorResults));
        }

        return result;
    }

    @Override
    public int getNumberOfTasksForUser() throws GWTJahiaServiceException {
        return workflow.getNumberOfTasksForUser(getUser());
    }

    private WCAGValidationResult toWCAGResult(ValidatorResults validatorResults) {
        if (validatorResults.isEmpty()) {
            return WCAGValidationResult.OK;
        }

        WCAGValidationResult wcagResult = new WCAGValidationResult();
        for (Result result : validatorResults.getErrors()) {
            wcagResult.getErrors().add(new WCAGViolation(result.getType().toString(), StringEscapeUtils.escapeHtml(result.getMessage()), result.getContext(), StringEscapeUtils.escapeXml(result.getCode()), result.getExample(), Integer.valueOf(result.getLine()), Integer.valueOf(result.getColumn())));
        }

        for (Result result : validatorResults.getWarnings()) {
            wcagResult.getWarnings().add(new WCAGViolation(result.getType().toString(), StringEscapeUtils.escapeHtml(result.getMessage()), result.getContext(), StringEscapeUtils.escapeXml(result.getCode()), result.getExample(), Integer.valueOf(result.getLine()), Integer.valueOf(result.getColumn())));
        }

        for (Result result : validatorResults.getInfos()) {
            wcagResult.getInfos().add(new WCAGViolation(result.getType().toString(), StringEscapeUtils.escapeHtml(result.getMessage()), result.getContext(), StringEscapeUtils.escapeXml(result.getCode()), result.getExample(), Integer.valueOf(result.getLine()), Integer.valueOf(result.getColumn())));
        }

        return wcagResult;
    }

    @Override
    public GWTJahiaToolbar getGWTToolbars(String toolbarGroup) throws GWTJahiaServiceException {
        return uiConfigHelper.getGWTToolbarSet(getSite(), getSite(), getRemoteJahiaUser(), getLocale(), getUILocale(), getRequest(), toolbarGroup);
    }

    @Override
    public GWTJahiaPortletOutputBean drawPortletInstanceOutput(String windowID, String entryPointIDStr, String pathInfo, String queryString) {
        GWTJahiaPortletOutputBean result = new GWTJahiaPortletOutputBean();
        try {
            int fieldId = Integer.parseInt(windowID);
            String portletOutput = ServicesRegistry.getInstance().getApplicationsDispatchService().getAppOutput(fieldId, entryPointIDStr, getRemoteJahiaUser(), getRequest(), getResponse(), getServletContext(), getWorkspace());
            try {
                JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(entryPointIDStr);
                String nodeTypeName = node.getPrimaryNodeTypeName();
                /** todo cleanup the hardcoded value here */
                if ("jnt:htmlPortlet".equals(nodeTypeName)) {
                    result.setInIFrame(false);
                }
                if ("jnt:contentPortlet".equals(nodeTypeName) || "jnt:rssPortlet".equals(nodeTypeName)) {
                    result.setInContentPortlet(true);
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
            result.setHtmlOutput(portletOutput);

            // what we need to do now is to do special processing for <script> tags, and on the client side we will
            // create them dynamically.
            Source source = new Source(portletOutput);
            source = new Source((new SourceFormatter(source)).toString());
            List<StartTag> scriptTags = source.getAllStartTags(HTMLElementName.SCRIPT);
            for (StartTag curScriptTag : scriptTags) {
                if ((curScriptTag.getAttributeValue("src") != null) &&
                        (!curScriptTag.getAttributeValue("src").equals(""))) {
                    result.getScriptsWithSrc().add(curScriptTag.getAttributeValue("src"));
                } else {
                    result.getScriptsWithCode().add(curScriptTag.getElement().getContent().toString());
                }
            }

        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    @Override
    public List<GWTJahiaSite> getAvailableSites() {
        final List<JCRSiteNode> sites;
        final List<GWTJahiaSite> returnedSites = new ArrayList<GWTJahiaSite>();
        try {
            sites = ServicesRegistry.getInstance().getJahiaSitesService().getSitesNodeList();
            for (JCRSiteNode jahiaSite : sites) {
                GWTJahiaSite gwtJahiaSite = new GWTJahiaSite();
                gwtJahiaSite.setSiteName(jahiaSite.getTitle());
                gwtJahiaSite.setSiteKey(jahiaSite.getSiteKey());
                gwtJahiaSite.setTemplateFolder(jahiaSite.getTemplateFolder());
                gwtJahiaSite.setTemplatePackageName(jahiaSite.getTemplatePackageName());
                gwtJahiaSite.setInstalledModules(jahiaSite.getInstalledModules());
                returnedSites.add(gwtJahiaSite);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return returnedSites;
    }

    @Override
    public boolean createRemotePublication(String nodeName, Map<String, String> props, boolean validateConnectionSettings)
            throws GWTJahiaServiceException {

        String theUrl = props.get("remoteUrl");
        while (theUrl.endsWith("/")) {
            theUrl = theUrl.substring(0, theUrl.length() - 1);
        }
        if (!theUrl.startsWith("http://") && !theUrl.startsWith("https://")) {
            theUrl = "http://" + theUrl;
        }
        props.put("remoteUrl", theUrl);

        String thePath = props.get("remotePath");
        while (thePath.endsWith("/")) {
            thePath = thePath.substring(0, thePath.length() - 1);
        }
        props.put("remotePath", thePath);

        if (validateConnectionSettings) {
            publication.validateConnection(props, retrieveCurrentSession(), getUILocale());
        }

        List<GWTJahiaNodeProperty> gwtProps = new ArrayList<GWTJahiaNodeProperty>();
        gwtProps.add(new GWTJahiaNodeProperty("remoteUrl", props.get("remoteUrl")));
        gwtProps.add(new GWTJahiaNodeProperty("remotePath", props.get("remotePath")));
        gwtProps.add(new GWTJahiaNodeProperty("remoteUser", props.get("remoteUser")));
        gwtProps.add(new GWTJahiaNodeProperty("remotePassword", props.get("remotePassword")));
        gwtProps.add(new GWTJahiaNodeProperty("node", props.get("node")));
        gwtProps.add(new GWTJahiaNodeProperty("schedule", props.get("schedule")));

        createNode("/remotePublications", JCRContentUtils.generateNodeName(nodeName),
                "jnt:remotePublication", null, null, gwtProps,
                new HashMap<String, List<GWTJahiaNodeProperty>>(), null, null, true);

        return true;
    }

    @Override
    public Integer deleteAllCompletedJobs() throws GWTJahiaServiceException {
        return schedulerHelper.deleteAllCompletedJobs();
    }

    @Override
    public String getNodeURLByIdentifier(String servlet, String identifier, Date versionDate, String versionLabel, String workspace,
                                         String locale) throws GWTJahiaServiceException {
        final JCRSessionWrapper session = retrieveCurrentSession(workspace != null ? workspace : getWorkspace(),
                locale != null ? LanguageCodeConverters.languageCodeToLocale(locale) : getLocale(), false);
        try {
            JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(identifier);
            if (nodeByIdentifier.isFile()) {
                String url = nodeByIdentifier.getUrl();
                if (versionDate != null) {
                    url += "?v=" + (versionDate.getTime());
                    if (versionLabel != null) {
                        url += "&l=" + versionLabel;
                    }
                }

                return url;
            } else {
                return getResponse().encodeURL(this.navigation.getNodeURL(servlet, nodeByIdentifier, versionDate, versionLabel, session.getWorkspace().getName(),
                        session.getLocale(), false));
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e);
        }
    }

    @Override
    public GWTJahiaNodeType getNodeType(String name) throws GWTJahiaServiceException {
        return contentDefinition.getNodeType(name, getUILocale());
    }

    @Override
    public List<GWTJahiaNodeType> getNodeTypes(List<String> names) throws GWTJahiaServiceException {
        List<GWTJahiaNodeType> types = contentDefinition.getNodeTypes(names, getUILocale());

        try {
            if (getSite().getPath().startsWith("/sites")) {
                for (GWTJahiaNodeType type : types) {
                    boolean perm = contentDefinition.checkPermissionForType(type.getName(), getSite());
                    type.set("canUseComponentForCreate", perm);
                    type.set("canUseComponentForEdit", perm);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get components", e);
        }
        return types;
    }

    @Override
    public List<GWTJahiaNodeType> getSubNodeTypes(List<String> names) throws GWTJahiaServiceException {
        return contentDefinition.getSubNodeTypes(names, getUILocale());
    }

    /**
     * Returns a list of node types with name and label populated that are the
     * sub-types of the specified base type.
     *
     * @param baseTypes            the node type name to find sub-types
     * @param displayStudioElement
     * @return a list of node types with name and label populated that are the
     * sub-types of the specified base type
     */
    @Override
    public Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> getContentTypes(List<String> baseTypes, boolean includeSubTypes, boolean displayStudioElement) throws GWTJahiaServiceException {
        return contentDefinition.getContentTypes(baseTypes, new HashMap<String, Object>(), getUILocale(), includeSubTypes, displayStudioElement);
    }

    /**
     * Returns a tree of node types and sub-types with name and label populated that are the
     * sub-types of the specified node types.
     *
     * @param nodeTypes
     * @param excludedNodeTypes
     * @param includeSubTypes
     * @return a tree of node types
     * @throws GWTJahiaServiceException
     */
    @Override
    public List<GWTJahiaNodeType> getContentTypesAsTree(List<String> nodeTypes, List<String> excludedNodeTypes,
                                                        boolean includeSubTypes) throws GWTJahiaServiceException {
        return contentDefinition.getContentTypesAsTree(nodeTypes, excludedNodeTypes, includeSubTypes, getSite(), getUILocale(), retrieveCurrentSession());
    }

//    private List<GWTJahiaNode> addComponentNodeTypeInfo(List<String> fields, List<GWTJahiaNode> list) throws GWTJahiaServiceException {
//        List<GWTJahiaNode> filteredList = new ArrayList<GWTJahiaNode>();
//        if (fields.contains("componentNodeType")) {
//        }
//    }
//

    @Override
    public GWTJahiaNodeType getWFFormForNodeAndNodeType(String formResourceName)
            throws GWTJahiaServiceException {
//        try {
//            JCRNodeWrapper nodeWrapper = retrieveCurrentSession().getNode(node.getPath());
//            Map<String,Object> context = new HashMap<String,Object>();
//            context.put("contextNode", nodeWrapper);
        return contentDefinition.getNodeType(formResourceName, getUILocale());
//        } catch (RepositoryException e) {
//            logger.error("Cannot get node", e);
//            throw new GWTJahiaServiceException(e.toString());
//        }
    }


    @Override
    @SuppressWarnings("unchecked")
    public ModelData getVisibilityInformation(String path) throws GWTJahiaServiceException {
        ModelData result = new BaseModelData();
        try {
            VisibilityService visibilityService = VisibilityService.getInstance();
            Map<String, VisibilityConditionRule> conditionsClasses = visibilityService.getConditions();
            List<GWTJahiaNodeType> types = new ArrayList<GWTJahiaNodeType>();
            Map<String, List<String>> requiredFields = new HashMap<String, List<String>>();
            for (Map.Entry<String, VisibilityConditionRule> entry : conditionsClasses.entrySet()) {
                GWTJahiaNodeType type = getNodeType(entry.getKey());
                type.set("xtemplate", entry.getValue().getGWTDisplayTemplate(getUILocale()));
                requiredFields.put(entry.getKey(), entry.getValue().getRequiredFieldNamesForTemplate());
                types.add(type);
            }
            result.set("types", types);

            JCRNodeWrapper node = retrieveCurrentSession().getNode(path);
            Map<JCRNodeWrapper, Boolean> conditionMatchesDetails = visibilityService.getConditionMatchesDetails(node);

            List<GWTJahiaNode> conditions = new ArrayList<GWTJahiaNode>();

            for (Map.Entry<JCRNodeWrapper, Boolean> entry : conditionMatchesDetails.entrySet()) {
                List<String> fields = new ArrayList<String>(Arrays.asList(GWTJahiaNode.PUBLICATION_INFO));
                fields.addAll(requiredFields.get(entry.getKey().getPrimaryNodeTypeName()));
                GWTJahiaNode jahiaNode = navigation.getGWTJahiaNode(entry.getKey(), fields);
                jahiaNode.set("conditionMatch", entry.getValue());
                ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(entry.getKey().getPrimaryNodeTypeName());
                ResourceBundle rb = ResourceBundles.get(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(nt.getSystemId()), getUILocale());
                for (String prop : requiredFields.get(entry.getKey().getPrimaryNodeTypeName())) {
                    Object val = jahiaNode.get(prop);
                    if (val instanceof List) {
                        List<Object> nl = new ArrayList<Object>();
                        for (Object v : (List<Object>) val) {
                            if (v instanceof String) {
                                nl.add(Messages.get(rb, nt.getPropertyDefinition(prop).getResourceBundleKey() + "." + JCRContentUtils.replaceColon((String) v), (String) v));
                            } else {
                                nl.add(v);
                            }
                        }
                        jahiaNode.set(prop, nl);
                    } else if (val instanceof String) {
                        jahiaNode.set(prop, Messages.get(rb, nt.getPropertyDefinition(prop).getResourceBundleKey() + "." + JCRContentUtils.replaceColon((String) val), (String) val));
                    }
                }
                conditions.add(jahiaNode);
            }

            result.set("conditions", conditions);
            if (node.hasNode(VisibilityService.NODE_NAME)) {
                JCRNodeWrapper conditionalVisibilityNode = node.getNode(VisibilityService.NODE_NAME);
                result.set("j:forceMatchAllConditions", conditionalVisibilityNode.getProperty("j:forceMatchAllConditions").getValue().getBoolean());
                String locale = node.getSession().getLocale().toString();
                result.set("publicationInfo", publication.getAggregatedPublicationInfosByLanguage(conditionalVisibilityNode,
                        Collections.singleton(locale), retrieveCurrentSession(), true, true).get(locale));
            } else {
                result.set("j:forceMatchAllConditions", false);
            }
            result.set("currentStatus", visibilityService.matchesConditions(node));
            try {
                JCRNodeWrapper liveNode = retrieveCurrentSession("live", null, false).getNodeByUUID(node.getIdentifier());
                result.set("liveStatus", visibilityService.matchesConditions(liveNode));
            } catch (RepositoryException e) {

            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e);
        }

        return result;
    }

    /**
     * Initialize a map with all data needed to render the code editor.
     *
     * @param path         path from where we are trying to open the code editor
     * @param isNew        is this a new file or an existing one
     * @param nodeTypeName null or the node type associated with the file
     * @param fileType     the type of file we are creating/editing
     * @return a RpcMap containing all information to display code editor
     * @throws GWTJahiaServiceException if something happened
     */

    @Override
    public RpcMap initializeCodeEditor(String path, boolean isNew, String nodeTypeName, String fileType)
            throws GWTJahiaServiceException {
        return stubHelper.initializeCodeEditor(path, isNew, nodeTypeName, fileType, getSite().getName(), getUILocale(),
                retrieveCurrentSession());
    }

    @Override
    public List<GWTJahiaValueDisplayBean> getTags(String prefix, String startPath, Long minCount, Long limit, Long offset, boolean sortByCount) throws GWTJahiaServiceException {
        List<GWTJahiaValueDisplayBean> tags = new ArrayList<GWTJahiaValueDisplayBean>();
        try {
            Map<String, Long> tagsMap = taggingService.getTagsSuggester().suggest(prefix, startPath, minCount, limit, offset, sortByCount, retrieveCurrentSession());
            for (String tagEntry : tagsMap.keySet()) {
                tags.add(new GWTJahiaValueDisplayBean(tagEntry, tagEntry));
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException("Cannot get tags", e);
        }
        return tags;
    }

    @Override
    public String convertTag(String tag) {
        return taggingService.getTagHandler().execute(tag);
    }

    @Override
    public String getToolbarWarnings() throws GWTJahiaServiceException{
        return toolbarWarningsService.getMessagesValueAsString(getUILocale());
    }

    @Override
    public GWTModuleReleaseInfo getInfoForModuleRelease(String moduleId) throws GWTJahiaServiceException {
        GWTModuleReleaseInfo result = null;
        try {
            result = moduleHelper.getModuleDistributionInfo(moduleId, retrieveCurrentSession());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
        return result;
    }

    public void setModuleHelper(ModuleHelper moduleHelper) {
        this.moduleHelper = moduleHelper;
    }

    @Override
    public GWTModuleReleaseInfo setDistributionServerForModule(String module, GWTModuleReleaseInfo info)
            throws GWTJahiaServiceException {
        GWTModuleReleaseInfo result = null;
        try {
            JCRSessionWrapper session = retrieveCurrentSession();
            if (info.getForgeUrl() != null) {
                moduleHelper.updateForgeUrlForModule(module, info.getForgeUrl(), session, info.getUsername(), info.getPassword());
            } else {
                moduleHelper.updateDistributionServerForModule(module, info.getRepositoryId(), info.getRepositoryUrl(), session);
            }
            result = moduleHelper.getModuleDistributionInfo(module, retrieveCurrentSession());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e);
        }
        return result;
    }

    private String getLocalizedMessage(RepositoryException e) throws GWTJahiaServiceException {
        if (e.getLocalizedMessage() != null) {
            return e.getLocalizedMessage();
        } else if (e.getMessage() != null) {
            return e.getMessage();
        }
        return Messages.getInternal("label.gwt.error."+e.getClass().getName(), getUILocale(), StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(e.getClass().getName()), " "));
    }

    @Override
    public List<String> getAvailablePermissions() throws GWTJahiaServiceException {
        return JahiaPrivilegeRegistry.getRegisteredPrivilegeNames();
    }

}
