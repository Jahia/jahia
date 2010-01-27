package org.jahia.services.i18n;

import junit.framework.TestCase;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import java.io.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 25, 2010
 * Time: 12:35:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResourceBundleTest extends TestCase {

    /**
     * Test that the value is unique and doest corresponds to several keys
     *
     * @throws Exception
     */
    public void testUniqueValue() throws Exception {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, Locale.ENGLISH);
        assertNotNull(resourceBundle);

        if (resourceBundle != null) {
            boolean duplicatedValue = false;
            final Map<String, String> valueKeyMap = new HashMap<String, String>();
            try {
                Enumeration<String> enume = resourceBundle.getKeys();
                while (enume.hasMoreElements()) {
                    String key = enume.nextElement();
                    String value = resourceBundle.getString(key);

                    // check if value exist for different key
                    boolean valueExist = valueKeyMap.containsKey(value);
                    if (valueExist) {
                        System.err.println(key + " = " + value);
                        System.err.println(valueKeyMap.get(value) + " = " + value);
                    } else {
                        // put value in map
                        valueKeyMap.put(value, key);
                    }

                    duplicatedValue = duplicatedValue || valueExist;


                }
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }
            assertFalse(duplicatedValue);
        }
    }

}
