package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;

/**
 * Choice list initializer that looks up direct sub types from the specified type
 * If no param is specified, get the list of nodeTypes
 *
 */
public class NodeTypesChoiceListInitializerImpl implements ChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(NodeTypesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        if (StringUtils.isEmpty(param)) {
            param = "jmix:structuredContent";
        }
        try {
            ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(param);
            NodeTypeIterator nti = nodeType.getDeclaredSubtypes();
            while (nti.hasNext()) {
                ExtendedNodeType type = (ExtendedNodeType) nti.next();
                listValues.add(new ChoiceListValue(type.getLabel(locale),new HashMap<String, Object>(), new ValueImpl(
                                type.getName(), PropertyType.STRING, false)));
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Cannot get type",e);
        }
        
        Collections.sort(listValues);
        
        return listValues;
    }
}
