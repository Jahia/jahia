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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.xml.SystemViewExporter;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.services.content.decorator.JCRNodeDecorator;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.decorator.validation.AdvancedGroup;
import org.jahia.services.content.decorator.validation.AdvancedSkipOnImportGroup;
import org.jahia.services.content.decorator.validation.DefaultSkipOnImportGroup;
import org.jahia.services.content.decorator.validation.JCRNodeValidator;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.DocumentViewExporter;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.xml.JahiaSAXParserFactory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.retention.RetentionManager;
import javax.jcr.security.AccessControlManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import javax.validation.ConstraintViolation;
import javax.validation.groups.Default;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessControlException;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Jahia specific wrapper around <code>javax.jcr.Session</code> to be able to inject
 * Jahia specific actions and to manage sessions to multiple repository providers in
 * the backend.
 * <p/>
 * Jahia services should use this wrapper rather than the original session interface to
 * ensure that we manipulate wrapped nodes and not the ones from the underlying
 * implementation.
 *
 * @author toto
 */
public class JCRSessionWrapper implements Session {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRSessionWrapper.class);
    public static final String DEREF_SEPARATOR = "@/";

    private JCRSessionFactory sessionFactory;
    private JahiaUser user;
    private Credentials credentials;
    private JCRWorkspaceWrapper workspace;
    private boolean isLive = true;
    private Locale locale;
    private List<String> tokens = new ArrayList<String>();

    private Map<JCRStoreProvider, Session> sessions = new HashMap<JCRStoreProvider, Session>();

    private Map<String, JCRNodeWrapper> sessionCacheByPath = new HashMap<String, JCRNodeWrapper>();
    private Map<String, JCRNodeWrapper> sessionCacheByIdentifier = new HashMap<String, JCRNodeWrapper>();
    private Map<String, JCRNodeWrapper> newNodes = new HashMap<String, JCRNodeWrapper>();
    private Map<String, JCRNodeWrapper> changedNodes = new HashMap<String, JCRNodeWrapper>();

    private Map<String, String> nsToPrefix = new HashMap<String, String>();
    private Map<String, String> prefixToNs = new HashMap<String, String>();

    private Map<String, String> uuidMapping = new HashMap<String, String>();
    private Map<String, String> pathMapping = new LinkedHashMap<String, String>();

    private Map<String,Object> resolvedReferences = new HashMap<String, Object>();

    private boolean isSystem;
    private boolean skipValidation;
    private boolean isCurrentUserSession = false;
    private Date versionDate;

    private Locale fallbackLocale;
    private String versionLabel;

    private static AtomicLong activeSessions = new AtomicLong(0L);

    private Exception thisSessionTrace;
    protected UUID uuid;
    private static Map<UUID, JCRSessionWrapper> activeSessionsObjects = new ConcurrentSkipListMap<UUID, JCRSessionWrapper>();

    public JCRSessionWrapper(JahiaUser user, Credentials credentials, boolean isSystem, String workspace, Locale locale,
                             JCRSessionFactory sessionFactory, Locale fallbackLocale) {
        uuid = UUID.randomUUID();
        this.user = user;
        this.credentials = credentials;
        this.isSystem = isSystem;
        this.versionDate = null;
        this.versionLabel = null;
        if (workspace == null) {
            this.workspace = new JCRWorkspaceWrapper("default", this, sessionFactory);
        } else {
            this.workspace = new JCRWorkspaceWrapper(workspace, this, sessionFactory);
        }
        this.locale = locale;
        this.fallbackLocale = fallbackLocale;
        this.sessionFactory = sessionFactory;
        if(!isSystem) {
            activeSessions.incrementAndGet();
        }
        if(SettingsBean.getInstance().isDevelopmentMode()) {
            thisSessionTrace = new Exception((isSystem?"System ":"")+"Session: " + uuid + " Thread: " + Thread.currentThread().getName() + "_" + Thread.currentThread().getId() + " created " + new DateTime().toString());
        } else {
            thisSessionTrace = new Exception((isSystem?"System ":"")+"Session: " + uuid);
        }
        activeSessionsObjects.put(uuid, this);
    }


    public JCRNodeWrapper getRootNode() throws RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProvider("/");
        return provider.getNodeWrapper(getProviderSession(provider).getRootNode(), "/", null, this);
    }

    public Repository getRepository() {
        return sessionFactory;
    }

    public String getUserID() {
        return ((SimpleCredentials) credentials).getUserID();
    }

    public boolean isSystem() {
        return isSystem;
    }

    public boolean isSkipValidation() {
        return skipValidation;
    }

    public void setSkipValidation(boolean skipValidation) {
        this.skipValidation = skipValidation;
    }

    public Object getAttribute(String s) {
        return null;
    }

    public String[] getAttributeNames() {
        return new String[0];
    }

    public JCRWorkspaceWrapper getWorkspace() {
        return workspace;
    }

    public Locale getLocale() {
        return locale;
    }

    //    public void setInterceptorsEnabled(boolean interceptorsEnabled) {
//        this.interceptorsEnabled = interceptorsEnabled;
//    }

    public Session impersonate(Credentials credentials) throws LoginException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public JCRNodeWrapper getNodeByUUID(String uuid) throws ItemNotFoundException, RepositoryException {
        return getNodeByUUID(uuid, true);
    }

    public JCRNodeWrapper getNodeByUUID(final String uuid, final boolean checkVersion)
            throws ItemNotFoundException, RepositoryException {
        if (StringUtils.isEmpty(uuid)) {
            throw new RepositoryException("invalid identifier: " + uuid);
        }
        if (sessionCacheByIdentifier.containsKey(uuid)) {
            return sessionCacheByIdentifier.get(uuid);
        }
        RepositoryException originalEx = null;
        for (JCRStoreProvider provider : sessionFactory.getProviderList()) {
            if (!provider.isInitialized()) {
                logger.debug("Provider " + provider.getKey() + " / " + provider.getClass().getName() +
                        " is not yet initialized, skipping...");
                continue;
            }

            try {
                Session session = getProviderSession(provider);
                boolean isAliased = sessionFactory.checkAliasedStatusAndToggleSessionIfNeeded(session, getUser());
                Node n = session.getNodeByIdentifier(uuid);
                JCRNodeWrapper wrapper = null;
                if (checkVersion && (versionDate != null || versionLabel != null)) {
                    JCRNodeWrapper frozen = getFrozenVersionAsRegular(n, provider, false);
                    if (frozen != null) {
                        wrapper = frozen;
                    }
                }
                if (wrapper == null) {
                    wrapper = provider.getNodeWrapper(n, this);
                }
                if (!isAliased) {
                    sessionCacheByIdentifier.put(uuid, wrapper);
                    sessionCacheByPath.put(wrapper.getPath(), wrapper);
                }

                return wrapper;
            } catch (ItemNotFoundException ee) {
                // All good
                if (originalEx == null) {
                    originalEx = ee;
                }
            } catch (UnsupportedRepositoryOperationException uso) {
                logger.debug("getNodeByUUID unsupported by: {} / {}", provider.getKey(), provider.getClass().getName());
                if (originalEx == null) {
                    originalEx = uso;
                }
            } catch (RepositoryException ex) {
                if (originalEx == null) {
                    originalEx = ex;
                }
                logger.warn(
                        "repository exception : " + provider.getKey() + " / " + provider.getClass().getName() + " : " +
                                ex.getMessage()
                );
            }
        }
        if (originalEx != null) {
            if (originalEx instanceof ItemNotFoundException) {
                throw originalEx;
            } else {
                throw new ItemNotFoundException(uuid, originalEx);
            }
        }

        throw new ItemNotFoundException(uuid);
    }

    public JCRNodeWrapper getNodeByUUID(String providerKey, String uuid)
            throws ItemNotFoundException, RepositoryException {
        JCRStoreProvider provider = sessionFactory.getProviders().get(providerKey);
        if (provider == null) {
            throw new ItemNotFoundException(uuid);
        }
        Session session = getProviderSession(provider);
        Node n = session.getNodeByIdentifier(uuid);
        return provider.getNodeWrapper(n, this);
    }

    public JCRItemWrapper getItem(String path) throws PathNotFoundException, RepositoryException {
        return getItem(path, true);
    }

    public JCRItemWrapper getItem(String path, final boolean checkVersion)
            throws PathNotFoundException, RepositoryException {
        if (sessionCacheByPath.containsKey(path)) {
            return sessionCacheByPath.get(path);
        }
        if (path.contains(DEREF_SEPARATOR)) {
            JCRNodeWrapper parent = (JCRNodeWrapper) getItem(StringUtils.substringBeforeLast(path, DEREF_SEPARATOR), checkVersion);
            return dereference(parent, StringUtils.substringAfterLast(path, DEREF_SEPARATOR));
        }
        for (Map.Entry<String, JCRStoreProvider> mp : sessionFactory.getMountPoints().entrySet()) {
            String key = mp.getKey();
            JCRStoreProvider provider = mp.getValue();
            if (provider.isDefault() || path.equals(key) || path.startsWith(key + "/")) {
                String localPath = path;
                if (!key.equals("/")) {
                    localPath = localPath.substring(key.length());
                }
                if (localPath.equals("")) {
                    localPath = "/";
                }
//                Item item = getProviderSession(provider).getItem(localPath);
                Session session = getProviderSession(provider);
                boolean isAliased = sessionFactory.checkAliasedStatusAndToggleSessionIfNeeded(session, getUser());
                Item item = session.getItem(provider.getRelativeRoot() + localPath);
                if (item.isNode()) {
                    final Node node = (Node) item;
                    JCRNodeWrapper wrapper = null;
                    if (checkVersion && (versionDate != null || versionLabel != null) && node.isNodeType("mix:versionable")) {
                        JCRNodeWrapper frozen = getFrozenVersionAsRegular(node, provider, false);
                        if (frozen != null) {
                            wrapper = frozen;
                        }
                    }
                    if (wrapper == null) {
                        wrapper = provider.getNodeWrapper(node, localPath, null, this);
                    }
                    
                    if (!isAliased) {
                        sessionCacheByPath.put(path, wrapper);
                        sessionCacheByIdentifier.put(wrapper.getIdentifier(), wrapper);
                    }

                    return wrapper;
                } else {
                    // because of https://jira.jahia.org/browse/QA-6810, we retrieve the property from the parent
                    // node to make sure that we go through any filtering that is implemented at a node decorator level,
                    // as it is the case for the JCRUserNode. A more complete solution would involve implementing
                    // the same decorator system around properties but this is much more complex and risky to do
                    // than this (simple) method.
                    JCRPropertyWrapper jcrPropertyWrapper = provider.getPropertyWrapper((Property) item, this);
                    return jcrPropertyWrapper.getParent().getProperty(jcrPropertyWrapper.getName());
                }
            }
        }
        throw new PathNotFoundException(path);
    }

    private JCRNodeWrapper dereference(JCRNodeWrapper parent, String refPath) throws RepositoryException {
        JCRNodeWrapper wrapper;
        JCRNodeWrapper referencedNode = ((JCRNodeWrapper) parent.getProperty(Constants.NODE).getNode());
        Node realReferencedNode = referencedNode.getRealNode();
        String fullPath = parent.getPath() + DEREF_SEPARATOR + refPath;
//        if (parent.getPath().startsWith(referencedNode.getPath()+ "/")) {
//            throw new PathNotFoundException(fullPath);
//        }
        String refRootName = StringUtils.substringBefore(refPath, "/");
        if (!realReferencedNode.getName().equals(refRootName)) {
            throw new PathNotFoundException(fullPath);
        }
        refPath = StringUtils.substringAfter(refPath, "/");
        if (refPath.equals("")) {
            wrapper = referencedNode.getProvider().getNodeWrapper(realReferencedNode, fullPath, parent, this);
        } else {
            Node node = realReferencedNode.getNode(refPath);
            fullPath = parent.getPath() + DEREF_SEPARATOR + refRootName + node.getPath().substring(realReferencedNode.getPath().length());
            wrapper = referencedNode.getProvider().getNodeWrapper(node, fullPath, null, this);
        }
        sessionCacheByPath.put(fullPath, wrapper);
        return wrapper;
    }

    public JCRNodeWrapper getNode(String path) throws PathNotFoundException, RepositoryException {
        return getNode(path, true);
    }

    public JCRNodeWrapper getNode(String path, boolean checkVersion) throws PathNotFoundException, RepositoryException {
        JCRItemWrapper item = getItem(path, checkVersion);
        if (item.isNode()) {
            return (JCRNodeWrapper) item;
        } else {
            throw new PathNotFoundException();
        }
    }

    public boolean itemExists(String path) throws RepositoryException {
        try {
            getItem(path);
            return true;
        } catch (RepositoryException e) {
            return false;
        }
    }

    public void move(String source, String dest)
            throws ItemExistsException, PathNotFoundException, VersionException, ConstraintViolationException,
            LockException, RepositoryException {
        getWorkspace().move(source, dest, true);
        updatePathInCache(source, dest, sessionCacheByPath);
        updatePathInCache(source, dest, newNodes);
        updatePathInCache(source, dest, changedNodes);
    }

    private void updatePathInCache(String source, String dest, Map<String, JCRNodeWrapper> cacheByPath) {
        String sourcePrefix = source + "/";
        Set<String> paths = new HashSet<>(cacheByPath.keySet());
        for (String s : paths) {
            if (s.equals(source) || s.startsWith(sourcePrefix)) {
                JCRNodeWrapper n = cacheByPath.remove(s);
                if (n instanceof JCRNodeDecorator) {
                    n = ((JCRNodeDecorator) n).getDecoratedNode();
                }
                String newPath = dest;
                if (source.length() < n.getPath().length()) {
                    newPath +=  n.getPath().substring(source.length());
                }
                String localPath = newPath;
                if (n.getProvider().getMountPoint().length() > 1) {
                    localPath = newPath.substring(n.getProvider().getMountPoint().length());
                }
                ((JCRNodeWrapperImpl)n).localPath = localPath;
                ((JCRNodeWrapperImpl)n).localPathInProvider = localPath;
                cacheByPath.put(newPath, n);
            }
        }
    }

    public void save()
            throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
            VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        save(JCRObservationManager.SESSION_SAVE);
    }

    void registerNewNode(JCRNodeWrapper node) {
        newNodes.put(node.getPath(), node);
    }

    void registerChangedNode(JCRNodeWrapper node) {
        if (!newNodes.containsKey(node.getPath())) {
            changedNodes.put(node.getPath(), node);
        }
    }

    void unregisterNewNode(JCRNodeWrapper node) {
        if (!newNodes.isEmpty() || !changedNodes.isEmpty()) {
            newNodes.remove(node.getPath());
            changedNodes.remove(node.getPath());
            try {
                if (node.hasNodes()) {
                    NodeIterator it = node.getNodes();
                    while (it.hasNext()) {
                        unregisterNewNode((JCRNodeWrapper) it.next());
                    }
                }
            } catch (RepositoryException e) {
                logger.warn("Error unregistering new nodes", e);
            }
        }
    }

    public void save(final int operationType)
            throws AccessDeniedException, ItemExistsException, ConstraintViolationException, InvalidItemStateException,
            VersionException, LockException, NoSuchNodeTypeException, RepositoryException {
        validate(operationType);
        newNodes.clear();
        changedNodes.clear();

        JCRObservationManager.doWorkspaceWriteCall(this, operationType, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper thisSession) throws RepositoryException {
                for (Session session : sessions.values()) {
                    session.save();
                }
                return null;
            }
        });

        if (workspace.getName().equals("default")) {
            // If reference helper found values to update, update them in live too
            ReferencesHelper.updateReferencesInLive(getResolvedReferences());
        }
    }

    public void validate() throws ConstraintViolationException, RepositoryException {
        validate(JCRObservationManager.SESSION_SAVE);
    }

    protected void validate(final int operationType) throws ConstraintViolationException, RepositoryException {
        if (!skipValidation) {
            CompositeConstraintViolationException exception = validateNodes(newNodes.values(), null, operationType);
            exception = validateNodes(changedNodes.values(), exception, operationType);
            if (exception != null) {
                refresh(true);
                throw exception;
            }
        }
    }

    protected CompositeConstraintViolationException validateNodes(Collection<JCRNodeWrapper> nodes, CompositeConstraintViolationException ccve, final int operationType) throws ConstraintViolationException, RepositoryException {
        boolean isImportOperation = operationType == JCRObservationManager.IMPORT;
        for (JCRNodeWrapper node : nodes) {
            try {
                for (String s : node.getNodeTypes()) {
                    Collection<ExtendedPropertyDefinition> propDefs = NodeTypeRegistry.getInstance().getNodeType(s).getPropertyDefinitionsAsMap().values();
                    for (ExtendedPropertyDefinition propertyDefinition : propDefs) {
                        String propertyName = propertyDefinition.getName();
                        if (propertyDefinition.isMandatory() &&
                                propertyDefinition.getRequiredType() != PropertyType.WEAKREFERENCE &&
                                propertyDefinition.getRequiredType() != PropertyType.REFERENCE &&
                                !propertyDefinition.isProtected() &&
                                (!propertyDefinition.isInternationalized() || getLocale() != null) &&
                                (
                                        !node.hasProperty(propertyName) ||
                                                (!propertyDefinition.isMultiple() &&
                                                        propertyDefinition.getRequiredType() != PropertyType.BINARY &&
                                                        StringUtils.isEmpty(node.getProperty(propertyName).getString()))

                                )) {

                            Locale errorLocale = null;
                            if (propertyDefinition.isInternationalized()) {
                                errorLocale = getLocale();
                            }

                            ccve = addError(ccve, new PropertyConstraintViolationException(node, Messages.getInternal("label.error.mandatoryField", LocaleContextHolder.getLocale(), "Field is mandatory"), errorLocale, propertyDefinition));
                        }
                    }
                }
            } catch (InvalidItemStateException e) {
                logger.debug("A new node can no longer be accessed to run validation checks", e);
            }

            Map<String, Constructor<?>> validators = sessionFactory.getDefaultProvider().getValidators();
            Set<ConstraintViolation<JCRNodeValidator>> constraintViolations = new LinkedHashSet<ConstraintViolation<JCRNodeValidator>>();
            for (Map.Entry<String, Constructor<?>> validatorEntry : validators.entrySet()) {
                if (node.isNodeType(validatorEntry.getKey())) {
                    try {
                        JCRNodeValidator validatorDecoratedNode = (JCRNodeValidator) validatorEntry.getValue().newInstance(node);
                        LocalValidatorFactoryBean validatorFactoryBean = sessionFactory.getValidatorFactoryBean();
                        
                        // if we are in non-import operation we enforce Default and DefaultSkipOnImportGroup;
                        // if we are in an import operation we do not enforce the DefaultSkipOnImportGroup, but rather only the Default one
                        Set<ConstraintViolation<JCRNodeValidator>> validate = !isImportOperation ? validatorFactoryBean
                                .validate(validatorDecoratedNode, Default.class, DefaultSkipOnImportGroup.class)
                                : validatorFactoryBean.validate(validatorDecoratedNode, Default.class);

                        if (validate.isEmpty()) {
                            // we enforce advanced validations only in case the default group succeeds

                            // if we are in non-import operation we enforce both AdvancedGroup and AdvancedSkipOnImportGroup;
                            // if we are in an import operation we do not enforce the AdvancedSkipOnImportGroup, but rather only the
                            // AdvancedGroup one
                            validate = !isImportOperation ? validatorFactoryBean.validate(validatorDecoratedNode,
                                    AdvancedGroup.class, AdvancedSkipOnImportGroup.class) : validatorFactoryBean
                                    .validate(validatorDecoratedNode, AdvancedGroup.class);
                        }
                        
                        constraintViolations.addAll(validate);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            for (ConstraintViolation<JCRNodeValidator> constraintViolation : constraintViolations) {
                String propertyName;
                try {
                    Method propertyNameGetter = constraintViolation.getConstraintDescriptor().getAnnotation().annotationType().getMethod(
                            "propertyName");
                    propertyName = (String) propertyNameGetter.invoke(
                            constraintViolation.getConstraintDescriptor().getAnnotation());
                } catch (Exception e) {
                    propertyName = constraintViolation.getPropertyPath().toString();
                }
                if (StringUtils.isNotBlank(propertyName)) {
                    ExtendedPropertyDefinition propertyDefinition = node.getApplicablePropertyDefinition(
                            propertyName);
                    if (propertyDefinition == null) {
                        propertyDefinition = node.getApplicablePropertyDefinition(propertyName.replaceFirst("_",":"));
                    }
                    if (propertyDefinition != null) {
                        Locale errorLocale = null;
                        if (propertyDefinition.isInternationalized()) {
                            errorLocale = getLocale();
                            if (errorLocale == null) {
                                continue;
                            }
                        }
                        ccve = addError(ccve, new PropertyConstraintViolationException(node, constraintViolation.getMessage(), errorLocale, propertyDefinition));
                    } else {
                        ccve = addError(ccve, new NodeConstraintViolationException(node, constraintViolation.getMessage(), null));
                    }
                } else {
                    ccve = addError(ccve, new NodeConstraintViolationException(node, constraintViolation.getMessage(), null));
                }
            }
        }

        return ccve;
    }

    private CompositeConstraintViolationException addError(CompositeConstraintViolationException ccve, ConstraintViolationException exception) {
        if (ccve == null) {
            ccve = new CompositeConstraintViolationException();
        }
        ccve.addException(exception);
        return ccve;
    }


    public void refresh(boolean b) throws RepositoryException {
        for (Session session : sessions.values()) {
            session.refresh(b);
        }
        if (!b) {
            newNodes.clear();
            changedNodes.clear();
            flushCaches();
        }
    }

    public boolean hasPendingChanges() throws RepositoryException {
        for (Session session : sessions.values()) {
            if (session.hasPendingChanges()) {
                return true;
            }
        }
        return false;
    }

    public ValueFactory getValueFactory() {
        return JCRValueFactoryImpl.getInstance();
    }

    /**
     * Normally determines whether this <code>Session</code> has permission to perform
     * the specified actions at the specified <code>absPath</code>.
     * This method is not supported.
     *
     * @param absPath an absolute path.
     * @param actions a comma separated list of action strings.
     * @throws UnsupportedRepositoryOperationException as long as Jahia doesn't support it
     */
    public void checkPermission(String absPath, String actions) throws AccessControlException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public ContentHandler getImportContentHandler(String s, int i)
            throws PathNotFoundException, ConstraintViolationException, VersionException, LockException,
            RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior)
            throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException,
            VersionException, InvalidSerializedDataException, LockException, RepositoryException {
        importXML(path, inputStream, uuidBehavior, DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior, int rootBehavior)
            throws IOException, InvalidSerializedDataException, RepositoryException {
        Map<String, List<String>> references = new HashMap<String, List<String>>();
        importXML(path, inputStream, uuidBehavior, rootBehavior, null, references);
        ReferencesHelper.resolveCrossReferences(this, references);
    }

    public void importXML(String path, InputStream inputStream, int uuidBehavior, int rootBehavior, Map<String, String> replacements, Map<String, List<String>> references)
            throws IOException, InvalidSerializedDataException, RepositoryException {
        JCRNodeWrapper node = getNode(path);
        try {
            if (!node.isCheckedOut()) {
                checkout(node);
            }
        } catch (UnsupportedRepositoryOperationException ex) {
            // versioning not supported
        }

        DocumentViewImportHandler documentViewImportHandler = new DocumentViewImportHandler(this, path);
        documentViewImportHandler.setRootBehavior(rootBehavior);
        documentViewImportHandler.setUuidBehavior(uuidBehavior);
        documentViewImportHandler.setReplacements(replacements);
        if (references != null) {
            documentViewImportHandler.setReferences(references);
        }
        try {
            SAXParser parser = JahiaSAXParserFactory.newInstance().newSAXParser();

            parser.parse(inputStream, documentViewImportHandler);
        } catch (SAXParseException e) {
            logger.error("Cannot import - File contains invalid XML", e);
            throw new RuntimeException("Cannot import file because it contains invalid XML", e);
        } catch (Exception e) {
            logger.error("Cannot import", e);
            throw new RuntimeException("Cannot import file", e);
        }
    }

    /**
     * Applies the namespace prefix to the appropriate sessions, including the underlying provider sessions.
     *
     * @param prefix
     * @param uri
     * @throws NamespaceException
     * @throws RepositoryException
     */
    public void setNamespacePrefix(String prefix, String uri) throws NamespaceException, RepositoryException {
        nsToPrefix.put(uri, prefix);
        prefixToNs.put(prefix, uri);
        for (Session s : sessions.values()) {
            s.setNamespacePrefix(prefix, uri);
            try {
                NamespaceRegistry nsReg = s.getWorkspace().getNamespaceRegistry();
                if (nsReg != null) {
                    nsReg.registerNamespace(prefix, uri);
                }
            } catch (RepositoryException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Prefix/uri could not be registered in workspace's registry- " + prefix + "/" + uri,
                            e);
                }
            }
        }
    }

    public String[] getNamespacePrefixes() throws RepositoryException {
        Set<String> wsPrefixes =
                new HashSet<String>(Arrays.asList(getWorkspace().getNamespaceRegistry().getPrefixes()));
        wsPrefixes.addAll(prefixToNs.keySet());
        return wsPrefixes.toArray(new String[wsPrefixes.size()]);
    }

    public String getNamespaceURI(String prefix) throws NamespaceException, RepositoryException {
        if (prefixToNs.containsKey(prefix)) {
            return prefixToNs.get(prefix);
        }
        return getWorkspace().getNamespaceRegistry().getURI(prefix);
    }

    public String getNamespacePrefix(String uri) throws NamespaceException, RepositoryException {
        if (nsToPrefix.containsKey(uri)) {
            return nsToPrefix.get(uri);
        }
        return getWorkspace().getNamespaceRegistry().getPrefix(uri);
    }

    public void logout() {
        for (Session session : sessions.values()) {
            if (session.isLive()) {
                session.logout();
            }
        }
        sessions.clear();
        if (credentials instanceof SimpleCredentials) {
            SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
            JahiaLoginModule.removeToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));
        }
        isLive = false;
        if (activeSessionsObjects.remove(uuid) == null) {
            logger.error("Could not removed session " + this + " opened here \n", thisSessionTrace);
        }
        if(!isSystem) {
            long actives = activeSessions.decrementAndGet();
            if (logger.isDebugEnabled() && actives < activeSessionsObjects.size()) {
                Map<UUID, JCRSessionWrapper> copyActives = new HashMap<UUID, JCRSessionWrapper>(activeSessionsObjects);
                logger.debug("There is " + actives + " sessions but " + copyActives.size() + " is retained");
                for (Map.Entry<UUID, JCRSessionWrapper> entry : copyActives.entrySet()) {
                    logger.debug("Active Session " + entry.getKey() + " is" + (entry.getValue().isLive() ? "" : " not") + " live", entry.getValue().getSessionTrace());
                }
            }
        }
    }

    public boolean isLive() {
        return isLive;
    }

    /**
     * Adds the specified lock token to the wrapped sessions. Holding a
     * lock token makes the <code>Session</code> the owner of the lock
     * specified by that particular lock token.
     *
     * @param token a lock token (a string).
     * @deprecated As of JCR 2.0, {@link LockManager#addLockToken(String)}
     * should be used instead.
     */
    public void addLockToken(String token) {
        tokens.add(token);
        for (Session session : sessions.values()) {
            session.addLockToken(token);
        }
    }

    public String[] getLockTokens() {
        List<String> allTokens = new ArrayList<String>(tokens);
        for (Session session : sessions.values()) {
            String[] tokens = session.getLockTokens();
            for (String token : tokens) {
                if (!allTokens.contains(token)) {
                    allTokens.add(token);
                }
            }
        }
        return allTokens.toArray(new String[allTokens.size()]);
    }

    public void removeLockToken(String token) {
        tokens.remove(token);
        for (Session session : sessions.values()) {
            session.removeLockToken(token);
        }
    }

    /**
     * Get sessions from all providers used in this wrapper.
     *
     * @return a <code>Collection</code> of <code>JCRSessionWrapper</code> objects
     */
    public Collection<Session> getAllSessions() {
        return sessions.values();
    }

    public Session getProviderSession(JCRStoreProvider provider) throws RepositoryException {
        return getProviderSession(provider, true);
    }

    public Session getProviderSession(JCRStoreProvider provider, boolean create) throws RepositoryException {
        if (sessions.get(provider) != null && !sessions.get(provider).isLive()) {
            sessions.remove(provider);
        }
        if (sessions.get(provider) == null && create) {
            Session s = null;

            if (credentials instanceof SimpleCredentials) {
                SimpleCredentials simpleCredentials = (SimpleCredentials) credentials;
                JahiaLoginModule.Token t = JahiaLoginModule
                        .getToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));
                JahiaUser user = getUser();
                String username;
                if (JahiaUserManagerService.isGuest(user)) {
                    username = JahiaLoginModule.GUEST;
                } else {
                    username = user.getUsername();
                }
                if (isCurrentUserSession() && !simpleCredentials.getUserID().startsWith(JahiaLoginModule.SYSTEM)) {
                    s = provider.getSessionFactory().findSameSession(provider, username, workspace.getName());
                }
                if (s == null) {
                    s = provider.getSession(credentials, workspace.getName());
                }
                JahiaLoginModule.removeToken(simpleCredentials.getUserID(), new String(simpleCredentials.getPassword()));
                credentials =
                        JahiaLoginModule.getCredentials(simpleCredentials.getUserID(), (String) simpleCredentials.getAttribute(JahiaLoginModule.REALM_ATTRIBUTE), t != null ? t.deniedPath : null);
            } else {
                s = provider.getSession(credentials, workspace.getName());
            }

            sessions.put(provider, s);
            for (String token : tokens) {
                s.addLockToken(token);
            }

            for (String prefix : prefixToNs.keySet()) {
                s.setNamespacePrefix(prefix, prefixToNs.get(prefix));
            }
        }
        return sessions.get(provider);
    }

    public JahiaUser getUser() {
        return user;
    }

    public JahiaUser getAliasedUser() {
        return sessionFactory.getCurrentAliasedUser();
    }

    public Calendar getPreviewDate() {
        return sessionFactory.getCurrentPreviewDate();
    }

    /**
     * Generates a document view export using a {@link org.apache.jackrabbit.commons.xml.DocumentViewExporter}
     * instance.
     *
     * @param path       of the node to be exported
     * @param handler    handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse  whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException          if the SAX event handler failed
     * @throws RepositoryException   if another error occurs
     */
    public void exportDocumentView(String path, ContentHandler handler, boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {
        DocumentViewExporter exporter = new DocumentViewExporter(this, handler, skipBinary, noRecurse);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException("XML export is not defined for properties: " + path);
        }
    }

    /**
     * Generates a system view export using a {@link org.apache.jackrabbit.commons.xml.SystemViewExporter}
     * instance.
     *
     * @param path       of the node to be exported
     * @param handler    handler for the SAX events of the export
     * @param skipBinary whether binary values should be skipped
     * @param noRecurse  whether to export just the identified node
     * @throws PathNotFoundException if a node at the given path does not exist
     * @throws SAXException          if the SAX event handler failed
     * @throws RepositoryException   if another error occurs
     */
    public void exportSystemView(String path, ContentHandler handler, boolean skipBinary, boolean noRecurse)
            throws PathNotFoundException, SAXException, RepositoryException {

        //todo implement our own system view .. ?
        SystemViewExporter exporter = new SystemViewExporter(this, handler, !noRecurse, !skipBinary);
        Item item = getItem(path);
        if (item.isNode()) {
            exporter.export((JCRNodeWrapper) item);
        } else {
            throw new PathNotFoundException("XML export is not defined for properties: " + path);
        }
    }

    /**
     * Calls {@link Session#exportDocumentView(String, ContentHandler, boolean, boolean)}
     * with the given arguments and a {@link ContentHandler} that serializes
     * SAX events to the given output stream.
     *
     * @param absPath    passed through
     * @param out        output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse  passed through
     * @throws IOException         if the SAX serialization failed
     * @throws RepositoryException if another error occurs
     */
    public void exportDocumentView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
            throws IOException, RepositoryException {
        try {
            ContentHandler handler = getExportContentHandler(out);
            exportDocumentView(absPath, handler, skipBinary, noRecurse);
        } catch (SAXException e) {
            Exception exception = e.getException();
            if (exception instanceof RepositoryException) {
                throw (RepositoryException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                throw new RepositoryException("Error serializing document view XML", e);
            }
        }
    }

    /**
     * Calls {@link Session#exportSystemView(String, ContentHandler, boolean, boolean)}
     * with the given arguments and a {@link ContentHandler} that serializes
     * SAX events to the given output stream.
     *
     * @param absPath    passed through
     * @param out        output stream to which the SAX events are serialized
     * @param skipBinary passed through
     * @param noRecurse  passed through
     * @throws IOException         if the SAX serialization failed
     * @throws RepositoryException if another error occurs
     */
    public void exportSystemView(String absPath, OutputStream out, boolean skipBinary, boolean noRecurse)
            throws IOException, RepositoryException {
        try {
            ContentHandler handler = getExportContentHandler(out);
            exportSystemView(absPath, handler, skipBinary, noRecurse);
        } catch (SAXException e) {
            Exception exception = e.getException();
            if (exception instanceof RepositoryException) {
                throw (RepositoryException) exception;
            } else if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                throw new RepositoryException("Error serializing system view XML", e);
            }
        }
    }

    /**
     * Creates a {@link ContentHandler} instance that serializes the
     * received SAX events to the given output stream.
     *
     * @param stream output stream to which the SAX events are serialized
     * @return SAX content handler
     * @throws RepositoryException if an error occurs
     */
    private ContentHandler getExportContentHandler(OutputStream stream) throws RepositoryException {
        try {
            SAXTransformerFactory stf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler handler = stf.newTransformerHandler();

            Transformer transformer = handler.getTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            handler.setResult(new StreamResult(stream));
            return handler;
        } catch (TransformerFactoryConfigurationError e) {
            throw new RepositoryException("SAX transformer implementation not available", e);
        } catch (TransformerException e) {
            throw new RepositoryException("Error creating an XML export content handler", e);
        }
    }

    public JCRNodeWrapper getNodeByIdentifier(String id) throws ItemNotFoundException, RepositoryException {
        return getNodeByUUID(id);
    }

    public Property getProperty(String absPath) throws PathNotFoundException, RepositoryException {
        return (Property) getItem(absPath);
    }

    public boolean nodeExists(String absPath) throws RepositoryException {
        return itemExists(absPath);
    }

    public boolean propertyExists(String absPath) throws RepositoryException {
        return itemExists(absPath);
    }

    public void removeItem(String absPath)
            throws VersionException, LockException, ConstraintViolationException, AccessDeniedException,
            RepositoryException {
        JCRItemWrapper item = getItem(absPath);
        boolean flushNeeded = false;
        if (item.isNode()) {
            JCRNodeWrapper node = (JCRNodeWrapper) item;
            unregisterNewNode(node);
            if (node.hasNodes()) {
                flushNeeded = true;
            }
        }
        item.remove();
        if (flushNeeded) {
            flushCaches();
        } else {
            removeFromCache(item);
        }
    }

    void removeFromCache(JCRItemWrapper item) throws RepositoryException {
        sessionCacheByPath.remove(item.getPath());
        if (item instanceof JCRNodeWrapper) {
            sessionCacheByIdentifier.remove(((JCRNodeWrapper) item).getIdentifier());
        }
    }

    void removeFromCache(String path) throws RepositoryException {
        JCRNodeWrapper node = sessionCacheByPath.remove(path);
        if (node != null) {
            sessionCacheByIdentifier.remove(node.getIdentifier());
        }
    }

    public boolean hasPermission(String absPath, String actions) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public boolean hasCapability(String s, Object o, Object[] objects) throws RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Returns the access control manager for this <code>Session</code>.
     * <p/>
     * Jahia throws an <code>UnsupportedRepositoryOperationException</code>.
     *
     * @return the access control manager for this <code>Session</code>
     * @throws UnsupportedRepositoryOperationException if access control
     *                                                 is not supported.
     * @since JCR 2.0
     */
    public AccessControlManager getAccessControlManager()
            throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public RetentionManager getRetentionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    /**
     * Performs check out of the specified node.
     *
     * @param node the node to perform the check out
     * @see VersionManager#checkout(String) for details
     */
    public void checkout(Node node) throws UnsupportedRepositoryOperationException, LockException, RepositoryException {
        while (!node.isCheckedOut()) {
            if (!node.isNodeType(Constants.MIX_VERSIONABLE) && !node.isNodeType(Constants.MIX_SIMPLEVERSIONABLE)) {
                node = node.getParent();
            } else {
                String absPath = node.getPath();
                VersionManager versionManager = getWorkspace().getVersionManager();
                if (!versionManager.isCheckedOut(absPath)) {
                    versionManager.checkout(absPath);
                }
                return;
            }
        }
    }

    public Map<String, String> getUuidMapping() {
        return uuidMapping;
    }

    public Map<String, String> getPathMapping() {
        return pathMapping;
    }

    public Map<String, Object> getResolvedReferences() {
        return resolvedReferences;
    }

    public Locale getFallbackLocale() {
        return fallbackLocale;
    }

    public void setFallbackLocale(Locale fallbackLocale) {
        this.fallbackLocale = fallbackLocale;
    }

    public Date getVersionDate() {
        return versionDate;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionDate(Date versionDate) {
        if (this.versionDate == null) {
            this.versionDate = versionDate;
        } else {
            throw new RuntimeException("Should not change versionDate on a session in same thread");
        }
    }

    public void setVersionLabel(String versionLabel) {
        if (this.versionLabel == null) {
            if (versionLabel != null && !versionLabel.startsWith(getWorkspace().getName())) {
                throw new RuntimeException("Cannot use label " + versionLabel + " in workspace " + getWorkspace().getName());
            }
            this.versionLabel = versionLabel;
        } else {
            throw new RuntimeException("Should not change versionLabel on a session in same thread");
        }
    }

    /**
     * Returns the wrapper node which corresponds to the version specified in the current session. If the corresponding version cannot be
     * found for the node a {@link PathNotFoundException} is thrown.
     *
     * @param objectNode the source object to check version node for
     * @param provider   the node provider
     * @return the wrapper node which corresponds to the version specified in the current session
     * @throws RepositoryException in case of a repository operation error
     */
    public JCRNodeWrapper getFrozenVersionAsRegular(Node objectNode, JCRStoreProvider provider) throws RepositoryException {
        return getFrozenVersionAsRegular(objectNode, provider, true);
    }

    /**
     * Returns the wrapper node which corresponds to the version specified in the current session. If the corresponding version cannot be
     * found for the node a <b>null</b> is returned in case <code>throwExeptionIfNotFound</code> is set to false. If version is not found
     * and <code>throwExeptionIfNotFound</code> is set to true - throws a {@link PathNotFoundException}.
     *
     * @param objectNode              the source object to check version node for
     * @param provider                the node provider
     * @param throwExeptionIfNotFound if <code>true</code> a {@link PathNotFoundException} is thrown in case the corresponding version cannot be found
     * @return the wrapper node which corresponds to the version specified in the current session
     * @throws RepositoryException in case of a repository operation error
     */
    protected JCRNodeWrapper getFrozenVersionAsRegular(Node objectNode, JCRStoreProvider provider,
                                                       boolean throwExeptionIfNotFound) throws RepositoryException {
        try {
            VersionHistory vh = objectNode.getSession().getWorkspace().getVersionManager()
                    .getVersionHistory(objectNode.getPath());

            Version v = null;
            if (versionLabel != null) {
                v = JCRVersionService.findVersionByLabel(vh, versionLabel);
            }
            if (v == null && versionDate != null) {
                v = JCRVersionService.findClosestVersion(vh, versionDate);
            }

            if (v == null) {
                if (throwExeptionIfNotFound) {
                    throw new PathNotFoundException();
                } else {
                    return null;
                }
            }

            Node frozen = v.getNode(Constants.JCR_FROZENNODE);

            return provider.getNodeWrapper(frozen, this);
        } catch (UnsupportedRepositoryOperationException e) {
            if (getVersionDate() == null && getVersionLabel() == null) {
                logger.error("Error while retrieving frozen version", e);
            }
        }
        return null;
    }

    public static long getActiveSessions() {
        return activeSessions.get();
    }

    public static Map<UUID, JCRSessionWrapper> getActiveSessionsObjects() {
        return Collections.unmodifiableMap(activeSessionsObjects);
    }

    protected void flushCaches() {
        sessionCacheByIdentifier.clear();
        sessionCacheByPath.clear();
    }

    protected JCRNodeWrapper getCachedNode(String uuid) {
        return sessionCacheByIdentifier.get(uuid);
    }


    public boolean isCurrentUserSession() {
        return isCurrentUserSession;
    }


    public void setCurrentUserSession(boolean isCurrentUserSession) {
        this.isCurrentUserSession = isCurrentUserSession;
    }

    public Exception getSessionTrace() {
        return thisSessionTrace;
    }

    /**
     * Get weak references of a node
     *
     * @param node         node
     * @param propertyName name of the property
     * @return an iterator
     * @throws RepositoryException
     */
    public PropertyIterator getWeakReferences(JCRNodeWrapper node, String propertyName) throws RepositoryException {
        List<PropertyIterator> propertyIterators = new ArrayList<PropertyIterator>();
        for (JCRStoreProvider provider : sessionFactory.getProviderList()) {
            Session providerSession = getProviderSession(provider);
            PropertyIterator pi = provider.getWeakReferences(node, propertyName, providerSession);
            if (pi != null) {
                propertyIterators.add(new PropertyIteratorImpl(pi, this, provider));
            }
        }
        return new MultiplePropertyIterator(propertyIterators, -1);
    }

    @Override
    public String toString() {
        return "JCRSessionWrapper (" + workspace.getName() + ", " + locale + ", " + user + " [aliased as " + getAliasedUser()
                + "]) {sessions=" + sessions + '}';
    }
    public JCRUserNode getUserNode() throws RepositoryException {
        return (JCRUserNode) getNode(user.getLocalPath());
    }
}
