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
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.ChineseAnalyzer;
import org.apache.lucene.analysis.cz.CzechAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.fa.PersianAnalyzer;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.nl.DutchAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.th.ThaiAnalyzer;
import org.apache.lucene.util.Version;
import org.jahia.api.Constants;
import org.jahia.services.search.analyzer.ASCIIFoldingAnalyzerWrapper;
import org.jahia.utils.LuceneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * Language to Analyzer map.
     */
    private static final Map<String, Analyzer> languageToAnalyzer = new ConcurrentHashMap<String, Analyzer>();

    static {
        languageToAnalyzer.put("ar", new ASCIIFoldingAnalyzerWrapper(new ArabicAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("br", new ASCIIFoldingAnalyzerWrapper(new BrazilianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("cjk", new ASCIIFoldingAnalyzerWrapper(new CJKAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("cn", new ASCIIFoldingAnalyzerWrapper(new ChineseAnalyzer()));
        languageToAnalyzer.put("cz", new ASCIIFoldingAnalyzerWrapper(new CzechAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("de", new ASCIIFoldingAnalyzerWrapper(new GermanAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("el", new ASCIIFoldingAnalyzerWrapper(new GreekAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("en", new ASCIIFoldingAnalyzerWrapper(new org.apache.lucene.analysis.standard.StandardAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("fa", new ASCIIFoldingAnalyzerWrapper(new PersianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("fr", new ASCIIFoldingAnalyzerWrapper(new FrenchAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("nl", new ASCIIFoldingAnalyzerWrapper(new DutchAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("ru", new ASCIIFoldingAnalyzerWrapper(new RussianAnalyzer(Version.LUCENE_30)));
        languageToAnalyzer.put("th", new ASCIIFoldingAnalyzerWrapper(new ThaiAnalyzer(Version.LUCENE_30)));
    }

    private Set<String> includedInSpellchecking = null;

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

    private final Analyzer keywordAnalyzer = new KeywordAnalyzer();

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
     *                  prefixed with <code>FieldNames.FULLTEXT_PREFIX</code>))
     * @return the <code>analyzer</code> to use for indexing this property
     */
    public Analyzer getPropertyAnalyzer(String fieldName) {
        if (StringUtils.contains(fieldName, FACET_EXPRESSION)) {
            return keywordAnalyzer;
        } else {
            Analyzer analyzer = null;

            // first attempt to find a language specific analyzer
            final String language = LuceneUtils.extractLanguageOrNullFrom(fieldName);
            if (language != null) {
                analyzer = languageToAnalyzer.get(language);
            }

            // if we didn't find an analyzer yet, get one from super
            if (analyzer == null) {
                analyzer = super.getPropertyAnalyzer(fieldName);
            }

            return analyzer;
        }
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
            } else if (nodeName.equals("spellchecker")) {
                includedInSpellchecking = initPropertyCollectionFrom(configNode, "include-property", null);
            }
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
}
