package org.jahia.modules.facets.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 19, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChoiceListFacetsInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ChoiceListFacetsInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType,
                                                     String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final Set<ChoiceListValue> listValues = new HashSet<ChoiceListValue>();
        try {
            JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
            if (node != null) {
                NodeIterator children = node.getNodes();
                while (children.hasNext()) {
                    JCRNodeWrapper child = (JCRNodeWrapper) children.nextNode();
                    final List<String> nodeTypesList = child.getNodeTypes();
                    for (String s : nodeTypesList) {
                        NodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                        final PropertyDefinition[] pr = nt.getPropertyDefinitions();
                        for (PropertyDefinition p : pr) {
                            ExtendedPropertyDefinition ep = (ExtendedPropertyDefinition) p;
                            if (ep.isFacetable()) {
                                String displayName = ep.getLabel(locale);
                                String value = p.getDeclaringNodeType().getName() + ";" + p.getName();
                                listValues.add(new ChoiceListValue(displayName, new HashMap<String, Object>(),
                                                                   new ValueImpl(value, PropertyType.STRING, false)));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<ChoiceListValue>(listValues);
    }
}
