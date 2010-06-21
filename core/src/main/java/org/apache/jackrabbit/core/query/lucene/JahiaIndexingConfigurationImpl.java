package org.apache.jackrabbit.core.query.lucene;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.nodetype.xml.AdditionalNamespaceResolver;
import org.apache.jackrabbit.core.query.QueryHandlerContext;
import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.spi.commons.conversion.NameResolver;
import org.apache.jackrabbit.spi.commons.conversion.ParsingNameResolver;
import org.apache.jackrabbit.spi.commons.name.NameFactoryImpl;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class JahiaIndexingConfigurationImpl extends IndexingConfigurationImpl {
    /**
     * The logger instance for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(JahiaIndexingConfigurationImpl.class);    
    
    private Set<Name> excludesFromI18NCopy = new HashSet<Name>();
    
    public JahiaIndexingConfigurationImpl() {
        super();
    }
    
    private final Analyzer facetAnalyzer = new KeywordAnalyzer();
    
    private static final String FACET_EXPRESSION = ":" + JahiaNodeIndexer.FACET_PREFIX;
    
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
        super.init(config, context, nsMappings);
        NamespaceResolver nsResolver = new AdditionalNamespaceResolver(getNamespaces(config));        
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

    public Set<Name> getExcludesFromI18NCopy() {
        return excludesFromI18NCopy;
    }

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
}
