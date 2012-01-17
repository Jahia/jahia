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

package org.apache.jackrabbit.core.query.lucene;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.NamespaceRegistryImpl;
import org.apache.jackrabbit.core.nodetype.xml.AdditionalNamespaceResolver;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.state.ItemStateManager;
import org.apache.jackrabbit.core.state.NodeState;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.conversion.ParsingNameResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.jahia.api.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Jahia specific {@link IndexingConfiguration} implementation. 
 */
public class JahiaIndexingConfigurationImpl extends IndexingConfigurationImpl {
    private static final String FACET_EXPRESSION = ":" + JahiaNodeIndexer.FACET_PREFIX;    
    private static final Name TRANSLATION_TYPE = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "translation");
    
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaIndexingConfigurationImpl.class);
    
    /**
     * The item state manager to retrieve additional item states.
     */
    private ItemStateManager ism;
    
    /**
     * @param node a node.
     * @return the text content of the <code>node</code>.
     */
    private static String getTextContent(Node node) {
        StringBuffer content = new StringBuffer();
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                content.append(((CharacterData) n).getData());
            }
        }
        return content.toString();
    }
    
    private Set<Name> excludesFromI18NCopy = new HashSet<Name>();
    
    private final Analyzer facetAnalyzer = new KeywordAnalyzer();
    
    public JahiaIndexingConfigurationImpl() {
        super();
    }

    public Set<Name> getExcludesFromI18NCopy() {
        return excludesFromI18NCopy;
    }

    /**
     * Returns the namespaces declared on the <code>node</code>.
     *
     * @param node a DOM node.
     * @return the namespaces
     */
    private Properties getNamespaces(Node node) {
        Properties namespaces = new Properties();
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attribute = (Attr) attributes.item(i);
            if (attribute.getName().startsWith("xmlns:")) {
                namespaces.setProperty(
                        attribute.getName().substring(6), attribute.getValue());
            }
        }
        return namespaces;
    }

    /**
     * Returns the analyzer configured for the property with this fieldName
     * (the string representation ,JCR-style name, of the given <code>Name</code>
     * prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>)),
     * and <code>null</code> if none is configured, or the configured analyzer
     * cannot be found. If <code>null</code> is returned, the default Analyzer
     * is used.
     *
     * @param fieldName the string representation ,JCR-style name, of the given <code>Name</code>
     * prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>))
     * @return the <code>analyzer</code> to use for indexing this property
     */
    public Analyzer getPropertyAnalyzer(String fieldName) {
        Analyzer analyzer = StringUtils.contains(fieldName, FACET_EXPRESSION) ? facetAnalyzer
                : super.getPropertyAnalyzer(fieldName); 
        return analyzer;
    }

    @Override
    public void init(Element config, QueryHandlerContext context, NamespaceMappings nsMappings) throws Exception {
        ism = context.getItemStateManager();
        Properties customNamespaces = getNamespaces(config);
        registerCustomNamespaces(context.getNamespaceRegistry(), customNamespaces);
        super.init(config, context, nsMappings);
        NamespaceResolver nsResolver = new AdditionalNamespaceResolver(customNamespaces);        
        NameResolver resolver = new ParsingNameResolver(NameFactoryImpl.getInstance(), nsResolver);
        
        NodeList indexingConfigs = config.getChildNodes();
        for (int i = 0; i < indexingConfigs.getLength(); i++) {
            Node configNode = indexingConfigs.item(i);
            if (configNode.getNodeName().equals("i18ncopy")) {
                NodeList childNodes = configNode.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node excludePropNode = childNodes.item(j);
                    if (excludePropNode.getNodeName().equals("exclude-property")) {
                        String propertyName = getTextContent(excludePropNode);
                        try {
                            getExcludesFromI18NCopy().add(resolver.getQName(propertyName));
                        } catch (Exception e) {
                            logger.warn("Cannot resolve configured property name to be excluded from i18ncopy: "
                                    + propertyName, e);
                        }
                    }
                }
            }
        }
    }
    
    private void registerCustomNamespaces(NamespaceRegistryImpl namespaceRegistry,
            Properties customNamespaces) {
        for (Object key : customNamespaces.keySet()) {
            String prefix = (String) key;
            try {
                namespaceRegistry.getURI(prefix);
            } catch (NamespaceException e) {
                String uri = customNamespaces.getProperty(prefix);
                try {
                    namespaceRegistry.registerNamespace(prefix, uri);
                    logger.info("Registered custom namespace with prefix '{}' and URI '{}'",
                            prefix, uri);
                } catch (RepositoryException e1) {
                    logger.warn("Unable to register custom namespace with prefix '" + prefix
                            + "' and URI '" + uri + "'");
                }
            }
        }
    }

    @Override
    public boolean isIndexed(NodeState state, Name propertyName) {
        return super.isIndexed(getUntranslatedNode(state), propertyName);
    }

    @Override
    public float getPropertyBoost(NodeState state, Name propertyName) {
        return super.getPropertyBoost(getUntranslatedNode(state), propertyName);
    }

    @Override
    public float getNodeBoost(NodeState state) {
        return super.getNodeBoost(getUntranslatedNode(state));
    }

    @Override
    public boolean isIncludedInNodeScopeIndex(NodeState state, Name propertyName) {
        return super.isIncludedInNodeScopeIndex(getUntranslatedNode(state), propertyName);
    }

    @Override
    public boolean useInExcerpt(NodeState state, Name propertyName) {
        return super.useInExcerpt(getUntranslatedNode(state), propertyName);
    }    
    
    private NodeState getUntranslatedNode(NodeState state) {
        if (TRANSLATION_TYPE.equals(state.getNodeTypeName())) {
            try {
                state = (NodeState) ism.getItemState(state.getParentId());
            } catch (ItemStateException e) {
            }
        }
        return state;
    }
}
