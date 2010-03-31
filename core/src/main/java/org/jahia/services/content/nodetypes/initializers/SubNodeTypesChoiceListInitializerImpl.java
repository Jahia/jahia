package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
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
public class SubNodeTypesChoiceListInitializerImpl implements ChoiceListInitializer {
    
    private transient static Logger logger = Logger.getLogger(SubNodeTypesChoiceListInitializerImpl.class);

    @SuppressWarnings("unchecked")
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final SortedSet<ChoiceListValue> listValues = new TreeSet<ChoiceListValue>();
        if (StringUtils.isEmpty(param)) {
            param = "jmix:structuredContent";
        }
        try {
            String includedTypes = StringUtils.substringBefore(param, ";");

            Set<String> excludedTypes = new HashSet<String>();
            String exclusion = StringUtils.substringAfter(param, ";");
            if (StringUtils.isNotBlank(exclusion)) {
                excludedTypes.addAll(CollectionUtils.collect(Arrays.asList(StringUtils.substringAfter(param, ";").split(",")), new Transformer() {
                    public Object transform(Object input) {
                        return ((String) input).trim();
                    }
                }));
            }
            
            for (String nodeTypeName : includedTypes.split(",")) {
                nodeTypeName = nodeTypeName.trim();
                ExtendedNodeType nodeType = NodeTypeRegistry.getInstance()
                        .getNodeType(nodeTypeName);
                if (!isExcludedType(nodeType, excludedTypes)) {
                    listValues.add(new ChoiceListValue(nodeType
                            .getLabel(locale), new HashMap<String, Object>(),
                            new ValueImpl(nodeType.getName(),
                                    PropertyType.STRING, false)));
                }
                NodeTypeIterator nti = nodeType.getSubtypes();
                while (nti.hasNext()) {
                    ExtendedNodeType type = (ExtendedNodeType) nti.next();
                    if (!isExcludedType(type, excludedTypes)) {
                        listValues.add(new ChoiceListValue(type
                                .getLabel(locale),
                                new HashMap<String, Object>(), new ValueImpl(
                                        type.getName(), PropertyType.STRING,
                                        false)));
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            logger.error("Cannot get type", e);
        }
       
        return new ArrayList<ChoiceListValue>(listValues);
    }
    private boolean isExcludedType(ExtendedNodeType nodeType, Set<String> excludedTypes) {
        boolean isExcluded = false;
        for (String excludedType : excludedTypes) {
            if (nodeType.isNodeType(excludedType)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }
    
}
