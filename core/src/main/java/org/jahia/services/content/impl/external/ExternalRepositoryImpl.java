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

package org.jahia.services.content.impl.external;


import org.jahia.services.content.JCRStoreProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.util.*;

/**
 * Implementation of the {@link javax.jcr.Repository} for the {@link org.jahia.services.content.impl.external.ExternalData}.
 * @author toto
 * Date: Apr 23, 2008
 * Time: 11:45:50 AM
 * 
 */
public class ExternalRepositoryImpl implements Repository {

    private static final transient Logger logger = LoggerFactory.getLogger(ExternalRepositoryImpl.class);

    private ExternalContentStoreProvider storeProvider;
    private ExternalDataSource dataSource;

    private Map<String, Object> repositoryDescriptors = new HashMap<String, Object>();

    private ExternalAccessControlManager accessControlManager;

    private String providerKey;

    private static final Set<String> STANDARD_KEYS = new HashSet<String>() {{
        add(Repository.QUERY_FULL_TEXT_SEARCH_SUPPORTED);
        add(Repository.QUERY_JOINS);
        add(Repository.QUERY_LANGUAGES);
        add(Repository.QUERY_STORED_QUERIES_SUPPORTED);
        add(Repository.QUERY_XPATH_DOC_ORDER);
        add(Repository.QUERY_XPATH_POS_INDEX);
        add(Repository.REP_NAME_DESC);
        add(Repository.REP_VENDOR_DESC);
        add(Repository.REP_VENDOR_URL_DESC);
        add(Repository.SPEC_NAME_DESC);
        add(Repository.SPEC_VERSION_DESC);
        add(Repository.WRITE_SUPPORTED);
        add(Repository.IDENTIFIER_STABILITY);
        add(Repository.LEVEL_1_SUPPORTED);
        add(Repository.LEVEL_2_SUPPORTED);

        add(Repository.OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_AUTOCREATED_DEFINITIONS_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE);
        add(Repository.NODE_TYPE_MANAGEMENT_MULTIPLE_BINARY_PROPERTIES_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_MULTIVALUED_PROPERTIES_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_ORDERABLE_CHILD_NODES_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_OVERRIDES_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_PRIMARY_ITEM_NAME_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_PROPERTY_TYPES);
        add(Repository.NODE_TYPE_MANAGEMENT_RESIDUAL_DEFINITIONS_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_VALUE_CONSTRAINTS_SUPPORTED);
        add(Repository.NODE_TYPE_MANAGEMENT_UPDATE_IN_USE_SUPORTED);
        add(Repository.OPTION_ACCESS_CONTROL_SUPPORTED);
        add(Repository.OPTION_JOURNALED_OBSERVATION_SUPPORTED);
        add(Repository.OPTION_LIFECYCLE_SUPPORTED);
        add(Repository.OPTION_LOCKING_SUPPORTED);
        add(Repository.OPTION_OBSERVATION_SUPPORTED);
        add(Repository.OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED);
        add(Repository.OPTION_QUERY_SQL_SUPPORTED);
        add(Repository.OPTION_RETENTION_SUPPORTED);
        add(Repository.OPTION_SHAREABLE_NODES_SUPPORTED);
        add(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED);
        add(Repository.OPTION_TRANSACTIONS_SUPPORTED);
        add(Repository.OPTION_UNFILED_CONTENT_SUPPORTED);
        add(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED);
        add(Repository.OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED);
        add(Repository.OPTION_VERSIONING_SUPPORTED);
        add(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED);
        add(Repository.OPTION_XML_EXPORT_SUPPORTED);
        add(Repository.OPTION_XML_IMPORT_SUPPORTED);
        add(Repository.OPTION_ACTIVITIES_SUPPORTED);
        add(Repository.OPTION_BASELINES_SUPPORTED);

    }};

    public boolean isStandardDescriptor(String key) {
        return STANDARD_KEYS.contains(key);
    }

    private void initDescriptors() {

        repositoryDescriptors.put(Repository.SPEC_VERSION_DESC, new ValueImpl("2.0"));
        repositoryDescriptors.put(Repository.SPEC_NAME_DESC, new ValueImpl("Content Repository for Java Technology API"));
        repositoryDescriptors.put(Repository.REP_VENDOR_DESC, new ValueImpl("Jahia"));
        repositoryDescriptors.put(Repository.REP_VENDOR_URL_DESC, new ValueImpl("http://www.jahia.org"));
        repositoryDescriptors.put(Repository.REP_NAME_DESC, new ValueImpl("The Web Integration Software"));
        repositoryDescriptors.put(Repository.REP_VERSION_DESC, new ValueImpl("1.0"));
        repositoryDescriptors.put(Repository.WRITE_SUPPORTED, new ValueImpl(true));
        repositoryDescriptors.put(Repository.IDENTIFIER_STABILITY, new ValueImpl(Repository.IDENTIFIER_STABILITY_SESSION_DURATION));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE, new ValueImpl(Repository.NODE_TYPE_MANAGEMENT_INHERITANCE_MINIMAL));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_OVERRIDES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_PRIMARY_ITEM_NAME_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_ORDERABLE_CHILD_NODES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_RESIDUAL_DEFINITIONS_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_AUTOCREATED_DEFINITIONS_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_SAME_NAME_SIBLINGS_SUPPORTED, new ValueImpl(false));
        List<ValueImpl> propertyTypes = new ArrayList<ValueImpl>();
        propertyTypes.add(new ValueImpl(PropertyType.BINARY));
        propertyTypes.add(new ValueImpl(PropertyType.NAME));
        propertyTypes.add(new ValueImpl(PropertyType.PATH));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_PROPERTY_TYPES, propertyTypes.toArray(new ValueImpl[propertyTypes.size()]));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_MULTIVALUED_PROPERTIES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_MULTIPLE_BINARY_PROPERTIES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_VALUE_CONSTRAINTS_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.NODE_TYPE_MANAGEMENT_UPDATE_IN_USE_SUPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_LANGUAGES, new ValueImpl[0]);
        repositoryDescriptors.put(Repository.QUERY_STORED_QUERIES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_FULL_TEXT_SEARCH_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_JOINS, new ValueImpl(false));
        repositoryDescriptors.put(Repository.LEVEL_1_SUPPORTED, new ValueImpl(true));
        repositoryDescriptors.put(Repository.LEVEL_2_SUPPORTED, new ValueImpl(true));
        repositoryDescriptors.put(Repository.QUERY_XPATH_POS_INDEX, new ValueImpl(false));
        repositoryDescriptors.put(Repository.QUERY_XPATH_DOC_ORDER, new ValueImpl(false));

        repositoryDescriptors.put(Repository.OPTION_ACCESS_CONTROL_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_ACTIVITIES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_BASELINES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_JOURNALED_OBSERVATION_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LIFECYCLE_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_LOCKING_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_AND_PROPERTY_WITH_SAME_NAME_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_NODE_TYPE_MANAGEMENT_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_OBSERVATION_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_QUERY_SQL_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_RETENTION_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SHAREABLE_NODES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_SIMPLE_VERSIONING_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_TRANSACTIONS_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UNFILED_CONTENT_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_MIXIN_NODE_TYPES_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_UPDATE_PRIMARY_NODE_TYPE_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_VERSIONING_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_EXPORT_SUPPORTED, new ValueImpl(false));
        repositoryDescriptors.put(Repository.OPTION_XML_IMPORT_SUPPORTED, new ValueImpl(false));
    }

    public ExternalRepositoryImpl(ExternalContentStoreProvider storeProvider, ExternalDataSource dataSource, ExternalAccessControlManager accessControlManager) {
        initDescriptors();
        this.storeProvider = storeProvider;
        this.dataSource = dataSource;
        this.accessControlManager = accessControlManager;

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
        return new ExternalSessionImpl(this, credentials);
    }

    public Session login(Credentials credentials) throws LoginException, RepositoryException {
        return new ExternalSessionImpl(this, credentials);
    }

    public Session login(String s) throws LoginException, NoSuchWorkspaceException, RepositoryException {
        return new ExternalSessionImpl(this, null);
    }

    public Session login() throws LoginException, RepositoryException {
        return new ExternalSessionImpl(this, null);
    }

    public boolean isSingleValueDescriptor(String key) {
        Object repositoryDescriptor = repositoryDescriptors.get(key);
        if (repositoryDescriptor instanceof Value) {
            return true;
        } else {
            return false;
        }
    }

    public Value getDescriptorValue(String key) {
        return (Value) repositoryDescriptors.get(key);
    }

    public Value[] getDescriptorValues(String key) {
        return (Value[]) repositoryDescriptors.get(key);
    }

    protected ExternalAccessControlManager getAccessControlManager() {
        return accessControlManager;
    }

    public ExternalContentStoreProvider getStoreProvider() {
        return storeProvider;
    }

    public ExternalDataSource getDataSource() {
        return dataSource;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public void setProviderKey(String providerKey) {
        this.providerKey = providerKey;
    }
}
