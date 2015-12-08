package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import javax.jcr.*;

/**
 * @author : faissah
 * Choicelist initializer that allows to you to add/remove mixins depending on the selected value.
 *
 * In its Spring configuration, this choicelist initializer is configured with a list of CustomChoiceListInitializerItem
 * where you where you set the choicelist values and the mixins associated.
 *
 */
public class CustomChoiceListInitializer implements ModuleChoiceListInitializer {
    private static final Logger logger = LoggerFactory.getLogger(CustomChoiceListInitializer.class);

    private String key;
    private List<CustomChoiceListInitializerItem> items = new ArrayList<CustomChoiceListInitializerItem>();

    /**
     * {@inheritDoc}
     */
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values,
                                                     Locale locale, Map<String, Object> context) {

        final List<ChoiceListValue> myChoiceList = new ArrayList<ChoiceListValue>();

        if (context == null) {
            return myChoiceList;
        }

        HashMap<String, Object> myPropertiesMap = null;

        for (CustomChoiceListInitializerItem item : items) {
            myPropertiesMap = new HashMap<String, Object>();
            if (item.getMixin()!=null)
                myPropertiesMap.put("addMixin", item.getMixin());
            myChoiceList.add(new ChoiceListValue(item.getDisplayName(), myPropertiesMap, new ValueImpl(item.getValue(), PropertyType.STRING, false)));
        }

        //Return the list
        return myChoiceList;
    }

    /**
     * {@inheritDoc}
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * {@inheritDoc}
     */
    public String getKey() {
        return key;
    }

    public void setItems(List items) {
        this.items = items;
    }

    public List getItems() {
        return items;
    }
}

