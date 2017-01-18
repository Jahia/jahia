/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.query.lucene;

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
import org.apache.lucene.util.Version;
import org.jahia.api.Constants;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Jahia specific {@link IndexingConfiguration} implementation.
 */
public class JahiaIndexingConfigurationImpl extends IndexingConfigurationImpl {
    private static final String FACET_EXPRESSION = ":" + JahiaNodeIndexer.FACET_PREFIX;

    private static final String SPELLCHECK_EXPRESSION = FieldNames.FULLTEXT + LuceneUtils.DASH;

    private static final Name TRANSLATION_TYPE = NameFactoryImpl.getInstance().create(Constants.JAHIANT_NS, "translation");

    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaIndexingConfigurationImpl.class);

    /**
     * Should match value used in indexing_configuration.xml
     */
    public static final String FULL_SPELLCHECK_FIELD_NAME = "0:FULL:SPELLCHECK";

    /**
     * The item state manager to retrieve additional item states.
     */
    private ItemStateManager ism;

    /**
     * Sets of property names which are included in spellchecking.
     */
    private Set<String> includedInSpellchecking = null;

    /**
     * The configured {@code AnalyzerRegistry} if any.
     */
    private final LanguageCustomizingAnalyzerRegistry analyzerRegistry = new LanguageCustomizingAnalyzerRegistry(this);

    /**
     * @param node a node.
     * @return the text content of the <code>node</code>.
     */
    private static String getTextContent(Node node) {
        StringBuilder content = new StringBuilder();
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeType() == Node.TEXT_NODE) {
                content.append(((CharacterData) n).getData());
            }
        }
        return content.toString();
    }

    private Set<Name> excludesFromI18NCopy = Collections.emptySet();

    private Set<Name> hierarchicalNodetypes = Collections.emptySet();

    private Set<ExcluedType> excludesTypesByPath = Collections.emptySet();

    private final Analyzer keywordAnalyzer = new KeywordAnalyzer();

    public JahiaIndexingConfigurationImpl() {
        super();
    }

    public Set<Name> getExcludesFromI18NCopy() {
        return excludesFromI18NCopy;
    }

    public Set<ExcluedType> getExcludesTypesByPath() {
        return excludesTypesByPath;
    }

    public Set<Name> getHierarchicalNodetypes() {
        return hierarchicalNodetypes;
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
     *                  prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>))
     * @return the <code>analyzer</code> to use for indexing this property
     */
    public Analyzer getPropertyAnalyzer(String fieldName) {
        Analyzer analyzer = StringUtils.contains(fieldName, FACET_EXPRESSION) ? keywordAnalyzer
                : super.getPropertyAnalyzer(StringUtils.startsWith(fieldName, SPELLCHECK_EXPRESSION) ? "0:FULL:SPELLCHECK" : fieldName);
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
            final String nodeName = configNode.getNodeName();
            if (nodeName.equals("i18ncopy")) {
                excludesFromI18NCopy = initPropertyCollectionFrom(configNode, "exclude-property", resolver);
            } else if (nodeName.equals("hierarchical")) {
                hierarchicalNodetypes = initPropertyCollectionFrom(configNode, "nodetype", resolver);
            } else if (nodeName.equals("spellchecker")) {
                includedInSpellchecking = initPropertyCollectionFrom(configNode, "include-property", null);
            } else if (nodeName.equals("analyzer-registry")) {
                processAnalyzerRegistryConfiguration(configNode);
            } else if (nodeName.equals("exclude") && configNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) configNode;
                String nodeType = element.getAttribute("nodetype");
                if (StringUtils.isNotEmpty(nodeType)) {
                    if (excludesTypesByPath.isEmpty()) {
                        excludesTypesByPath = new HashSet<>();
                    }
                    String path = element.getAttribute("path");
                    Boolean isRegexp = Boolean.valueOf(element.getAttribute("isRegexp"));
                    excludesTypesByPath.add(new ExcluedType(resolver.getQName(nodeType), path, isRegexp));
                }
            }
        }
    }

    private void processAnalyzerRegistryConfiguration(Node configNode) {
        try {
            // each node should be a language code to which an analyzer and its optional customization is
            // associated
            final NodeList childNodes = configNode.getChildNodes();
            final int length = childNodes.getLength();

            // iterate over children
            for (int j = 0; j < length; j++) {
                final Node child = childNodes.item(j);

                // only process elements
                if (Node.ELEMENT_NODE == child.getNodeType()) {
                    // language code
                    final String lang = child.getNodeName();

                    // Analyzer class name
                    final String className = getClassAttribute(child);
                    if (className != null) {

                        Analyzer analyzer = null;
                        final Class<?> analyzerClass = Class.forName(className);
                        try {
                            // instantiate Analyzer
                            final Constructor<?> constructor = analyzerClass.getConstructor(Version.class);
                            analyzer = (Analyzer) constructor.newInstance(Version.LUCENE_30);
                        } catch (Exception e) {
                            // attempt to use a default constructor if it exists
                            try {
                                analyzer = (Analyzer) analyzerClass.newInstance();
                            } catch (Exception e1) {
                                logger.warn("Couldn't instantiate Analyzer class: " + className
                                        + ". It must provide a constructor with a org.apache.lucene.util.Version " +
                                        "argument or a no-arg constructor.", e1);
                            }
                        }
                        if (analyzer != null) {
                            Boolean useASCIIFoldingFilter = null;
                            try {
                                useASCIIFoldingFilter = Boolean.parseBoolean(child.getAttributes().getNamedItem("useASCIIFoldingFilter").getNodeValue().trim());
                            } catch (Exception e) {
                                // nothing to do
                            }

                            // instantiate and initialize AnalyzerCustomizer
                            final NodeList potentialCustomizers = child.getChildNodes();
                            final int potentialCustomizersNb = potentialCustomizers.getLength();
                            for (int customizerIndex = 0; customizerIndex < potentialCustomizersNb; customizerIndex++) {

                                final Node customizerNode = potentialCustomizers.item(customizerIndex);
                                if (Node.ELEMENT_NODE == customizerNode.getNodeType()) {
                                    final String customizerClassName = getClassAttribute(customizerNode);
                                    if (customizerClassName != null) {
                                        try {
                                            // todo: how should these be packaged? Class-loading this way might not
                                            // work in an OSGi environment
                                            final Class<?> customizerClass = Class.forName(customizerClassName);
                                            final AnalyzerCustomizer customizer = (AnalyzerCustomizer) customizerClass.newInstance();

                                            // now that we have our customizer, initialize it
                                            final NodeList keys = customizerNode.getChildNodes();
                                            final int keyNb = keys.getLength();
                                            final Map<String, List<String>> props = new HashMap<String, List<String>>(keyNb);
                                            for (int keyIndex = 0; keyIndex < keyNb; keyIndex++) {
                                                final Node item = keys.item(keyIndex);

                                                // only process element nodes
                                                if (Node.ELEMENT_NODE == item.getNodeType()) {
                                                    final String key = item.getNodeName();
                                                    final String value = getTextContent(item).trim();

                                                    List<String> values = props.get(key);
                                                    if (values == null) {
                                                        values = new ArrayList<String>();
                                                        props.put(key, values);
                                                    }

                                                    values.add(value);
                                                }
                                            }
                                            customizer.initFrom(props);

                                            // and finally, customize the Analyzer
                                            analyzer = customizer.customize(analyzer);

                                            // we only allow one customizer so stop here
                                            break;
                                        } catch (Exception e) {
                                            // we don't break out of the loop here in an attempt to check if maybe
                                            // another customizer has been configured
                                            logger.warn("Couldn't instantiate AnalyzerCustomizer class: " + customizerClassName, e);
                                        }
                                    }
                                }
                            }

                            // and finally add the Analyzer to the registry if we managed to instantiate it
                            analyzerRegistry.addAnalyzer(lang, analyzer, useASCIIFoldingFilter);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Couldn't process AnalyzerRegistry configuration", e);
        }
    }

    private String getClassAttribute(Node node) {
        try {
            return node.getAttributes().getNamedItem("class").getNodeValue().trim();
        } catch (Exception e) {
            return null;
        }
    }

    private Set initPropertyCollectionFrom(Node configNode, String childNameToAdd, NameResolver resolver) {
        final NodeList childNodes = configNode.getChildNodes();
        final int length = childNodes.getLength();

        // init collection if needed
        Set configurationCollection = new HashSet(length);

        for (int j = 0; j < length; j++) {
            final Node childNode = childNodes.item(j);
            if (childNode.getNodeName().equals(childNameToAdd)) {
                String propertyName = getTextContent(childNode);
                try {
                    // if we didn't pass a resolver, add the property name as-is, otherwise convert it to a proper QName
                    final Object name = resolver != null ? resolver.getQName(propertyName) : propertyName;
                    configurationCollection.add(name);
                } catch (Exception e) {
                    logger.warn("Cannot resolve configured property name : " + propertyName, e);
                }
            }
        }

        return configurationCollection;
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

    public boolean shouldPropertyBeSpellchecked(String propertyName) {
        // if includedInSpellchecking is null, we consider that no setup of spellchecking has been done and all
        // properties should therefore be included
        return includedInSpellchecking == null || includedInSpellchecking.contains(propertyName);
    }

    public LanguageCustomizingAnalyzerRegistry getAnalyzerRegistry() {
        return analyzerRegistry;
    }

    /**
     * inner object to handle exlued types in index configuration
     */
    class ExcluedType {
        private Name type;
        private String path;
        private boolean regexp;

        ExcluedType(Name type, String path, boolean regexp) {
            this.type = type;
            this.path = path;
            this.regexp = regexp;
        }

        boolean matchNode(Name nodeTypeToCheck) throws RepositoryException {
            return type.equals(nodeTypeToCheck);
        }

        boolean matchPath(String pathToCheck) {
            if (path == null) {
                return true;
            }
            if (regexp) {
                return pathToCheck.matches(path);
            } else {
                return StringUtils.startsWith(pathToCheck, path);
            }
        }
    }
}
