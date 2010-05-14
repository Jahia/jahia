package org.jahia.modules.latestContent.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: Apr 19, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChoiceListLatestContentInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ChoiceListLatestContentInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, ExtendedNodeType realNodeType, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        final Set<ChoiceListValue> listValues = new HashSet<ChoiceListValue>();
        String displayName,value;
        ChoiceListValue myChoiceList;
        boolean editorialContent = true;
        NodeTypeIterator iterator = NodeTypeRegistry.getInstance().getAllNodeTypes();
        while(iterator.hasNext())
        {
            ExtendedNodeType node = (ExtendedNodeType) iterator.next();

            editorialContent = node.isNodeType("jmix:editorialContent");

            if(editorialContent)
            {
                displayName = value = node.getName();
                myChoiceList = new ChoiceListValue(displayName, new HashMap<String, Object>(), new ValueImpl(value, PropertyType.STRING, false));
                listValues.add(myChoiceList);
             }
        }

        return new ArrayList<ChoiceListValue>(listValues);
    }
}
