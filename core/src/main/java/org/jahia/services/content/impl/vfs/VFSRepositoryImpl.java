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

package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.log4j.Logger;

import javax.jcr.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class VFSRepositoryImpl implements Repository {

    private static final transient Logger logger = Logger.getLogger(VFSRepositoryImpl.class);

    private String root;
    private String rootPath;

    private FileSystemManager manager;

    private Map<String, Object> repositoryDescriptors = new HashMap<String, Object>();

    private void initDescriptors() {

        repositoryDescriptors.put(Repository.SPEC_VERSION_DESC, new VFSValueImpl("2.0"));
        repositoryDescriptors.put(Repository.SPEC_NAME_DESC, new VFSValueImpl("Content Repository for Java Technology API"));
        repositoryDescriptors.put(Repository.REP_VENDOR_DESC, new VFSValueImpl("Jahia"));
        repositoryDescriptors.put(Repository.REP_VENDOR_URL_DESC, new VFSValueImpl("http://www.jahia.org"));
        repositoryDescriptors.put(Repository.REP_NAME_DESC, new VFSValueImpl("The Web Integration Software"));
        repositoryDescriptors.put(Repository.REP_VERSION_DESC, new VFSValueImpl("1.0"));
        repositoryDescriptors.put(Repository.WRITE_SUPPORTED, new VFSValueImpl(true));
        repositoryDescriptors.put(Repository.IDENTIFIER_STABILITY, new VFSValueImpl(Repository.IDENTIFIER_STABILITY_SESSION_DURATION));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE, new VFSValueImpl(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE_MINIMAL));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_OVERRIDES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_PRIMARY_ITEM_NAME_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_ORDERABLE_CHILD_NODES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_RESIDUAL_DEFINITIONS_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_AUTOCREATED_DEFINITIONS_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED, new VFSValueImpl(false));
        List<VFSValueImpl> propertyTypes = new ArrayList<VFSValueImpl>();
        propertyTypes.add(new VFSValueImpl(PropertyType.BINARY));
        propertyTypes.add(new VFSValueImpl(PropertyType.NAME));
        propertyTypes.add(new VFSValueImpl(PropertyType.PATH));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_PROPERTY_TYPES, propertyTypes.toArray(new VFSValueImpl[propertyTypes.size()]));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_MULTIVALUED_PROPERTIES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_MULTIPLE_BINARY_PROPERTIES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_VALUE_CONSTRAINTS_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_UPDATE_IN_USE_SUPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_LANGUAGES, new VFSValueImpl[0]);
        repositoryDescriptors.put(Repository.QUERY_STORED_QUERIES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_FULL_TEXT_SEARCH_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_JOINS, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.LEVEL_1_SUPPORTED, new VFSValueImpl(true));
        repositoryDescriptors.put(Repository.LEVEL_2_SUPPORTED, new VFSValueImpl(true));
        repositoryDescriptors.put(Repository.QUERY_XPATH_POS_INDEX, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_XPATH_DOC_ORDER, new VFSValueImpl(false));

        repositoryDescriptors.put(Repository.OPTION_ACCESS_CONTROL_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_ACTIVITIES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_BASELINES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_JOURNALED_OBSERVATION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LIFECYCLE_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LOCKING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_OBSERVATION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_QUERY_SQL_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_RETENTION_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SHAREABLE_NODES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_TRANSACTIONS_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UNFILED_CONTENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_VERSIONING_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_EXPORT_SUPPORTED, new VFSValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_IMPORT_SUPPORTED, new VFSValueImpl(false));
    }

    public VFSRepositoryImpl(String root) {
        initDescriptors();
        this.root = root;

        try {
            manager = VFS.getManager();
            rootPath = getFile("/").getName().getPath();
        } catch (FileSystemException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getRoot() {
        return root;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String[] getDescriptorKeys() {
        return repositoryDescriptors.keySet().toArray(new String[repositoryDescriptors.size()]);
    }

    public String getDescriptor(String s) {
        Object descriptorObject = repositoryDescriptors.get(s);
        if (descriptorObject == null) {
            return null;
        }
        if (descriptorObject instanceof Value) {
            return ((Value) descriptorObject).toString();
        } else {
            throw new RuntimeException("Expected single-value value but found multi-valued property instead !");
        }
    }

    public Session login(Credentials credentials, String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, credentials);
    }

    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public Session login() throws LoginException, RepositoryException {
        return new VFSSessionImpl(this, null);
    }

    public FileObject getFile(String path) throws FileSystemException {
        return manager.resolveFile(root + path);
    }

    public FileObject getFileByIdentifier(String identifier) throws FileSystemException {
        return manager.resolveFile(identifier);
    }

    public boolean isStandardDescriptor(String key) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isSingleValueDescriptor(String key) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Value getDescriptorValue(String key) {
        return (Value) repositoryDescriptors.get(key);
    }

    public Value[] getDescriptorValues(String key) {
        return (Value[]) repositoryDescriptors.get(key);
    }
}
