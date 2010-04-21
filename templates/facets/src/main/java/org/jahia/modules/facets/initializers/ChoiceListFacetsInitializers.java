package org.jahia.modules.facets.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeType;
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
public class ChoiceListFacetsInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ChoiceListFacetsInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final ArrayList<ChoiceListValue> listValues = new ArrayList<ChoiceListValue>();
        try {
            final NodeTypeIterator ntr = NodeTypeRegistry.getInstance().getAllNodeTypes();
            while (ntr.hasNext()) {
                NodeType nt = ntr.nextNodeType();
                final PropertyDefinition[] pi = nt.getPropertyDefinitions();
                for (PropertyDefinition prop : pi) {
                    if (((ExtendedPropertyDefinition) prop).isFacetable()) {
                    String displayName = nt.getName() + ";" + prop.getName();
                    listValues.add(new ChoiceListValue(displayName, new HashMap<String, Object>(), new ValueImpl(
                            displayName, PropertyType.STRING, false)));
                    }
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return listValues;
    }
}
