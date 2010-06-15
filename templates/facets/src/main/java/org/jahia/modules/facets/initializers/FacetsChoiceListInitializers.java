package org.jahia.modules.facets.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 19, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class FacetsChoiceListInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(FacetsChoiceListInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final Set<ChoiceListValue> listValues = new HashSet<ChoiceListValue>();
        try {
            NodeTypeIterator ntr = NodeTypeRegistry.getInstance().getAllNodeTypes();
            while (ntr.hasNext()) {
                ExtendedNodeType nt =(ExtendedNodeType) ntr.nextNodeType();
                for (PropertyDefinition def : nt.getPropertyDefinitions()) {
                    ExtendedPropertyDefinition ep = (ExtendedPropertyDefinition) def;
                    if (ep.isFacetable()) {
                        String displayName = ep.getLabel(locale);
                        displayName += nt.isMixin()?"":" (" + nt.getLabel(locale) + ")";
                        String value = ep.getDeclaringNodeType().getName() + ";" + ep.getName();
                        listValues.add(new ChoiceListValue(displayName, new HashMap<String, Object>(),
                                new ValueImpl(value, PropertyType.STRING , false)));
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<ChoiceListValue>(listValues);
    }
}
