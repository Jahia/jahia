package org.jahia.services.content.nodetypes.initializers;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Feb 9, 2010
 * Time: 3:21:32 PM
 * ChoiceListInitializer specific interface for modules.
 */
public interface ModuleChoiceListInitializer extends ChoiceListInitializer {
    void setKey(String key);
    String getKey();
}
