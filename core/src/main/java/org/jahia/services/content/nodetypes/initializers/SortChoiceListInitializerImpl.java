package org.jahia.services.content.nodetypes.initializers;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 11/7/11
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SortChoiceListInitializerImpl implements ChoiceListInitializer {
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        Collections.sort(values);

        return values;
    }
}
