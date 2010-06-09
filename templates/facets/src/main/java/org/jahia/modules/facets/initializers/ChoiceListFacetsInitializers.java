package org.jahia.modules.facets.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ComponentLinkerChoiceListInitializer;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
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
        List<ExtendedPropertyDefinition> propertyDefs = new LinkedList<ExtendedPropertyDefinition>();
        try {
            if (((JCRNodeWrapper) context.get("contextNode")).hasProperty("j:bindedComponent")) {
                JCRNodeWrapper node = (JCRNodeWrapper) ((JCRNodeWrapper) context.get("contextNode")).getProperty("j:bindedComponent").getNode();
                List<String> l = new LinkedList<String>();
                if (node.hasProperty("j:allowedTypes") && node.getProperty("j:allowedTypes").getValues().length > 0) {
                    for (Value v : node.getProperty("j:allowedTypes").getValues()) {
                        l.add(v.getString());
                    }
                } else {
                    l.add("jnt:content");
                }
                for (String str : l) {
                    ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(str);
                    ExtendedNodeType[] supertypes = type.getDeclaredSupertypes();
                    propertyDefs.addAll(Arrays.asList(type.getPropertyDefinitions()));
                    Set<ExtendedNodeType> s = new HashSet<ExtendedNodeType>(Arrays.asList(supertypes));

                    Set<ExtendedNodeType> nodeTypeSet = NodeTypeRegistry.getInstance().getMixinExtensions().get(NodeTypeRegistry.getInstance().getNodeType(str));
                    if (nodeTypeSet != null)
                        s.addAll(nodeTypeSet);
                    if (s != null) {
                        for (ExtendedNodeType nt : s) {
                            propertyDefs.addAll(Arrays.asList(nt.getPropertyDefinitions()));
                            Set<ExtendedNodeType> nts = NodeTypeRegistry.getInstance().getMixinExtensions().get(NodeTypeRegistry.getInstance().getNodeType(nt.getName()));
                            if (nts != null) {
                                for (ExtendedNodeType n : nts) {
                                    propertyDefs.addAll(Arrays.asList(n.getPropertyDefinitions()));
                                }
                            }
                        }
                    }
                }

                for (PropertyDefinition p : propertyDefs) {
                    ExtendedPropertyDefinition ep = (ExtendedPropertyDefinition) p;
                    if (ep.isFacetable()) {
                        String displayName = ep.getLabel(locale);
                        String value = p.getDeclaringNodeType().getName() + ";" + p.getName();
                        listValues.add(new ChoiceListValue(displayName, new HashMap<String, Object>(),
                                new ValueImpl(value, PropertyType.STRING, false)));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<ChoiceListValue>(listValues);
    }
}
