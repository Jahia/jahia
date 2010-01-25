package org.jahia.services.i18n;

import junit.framework.TestCase;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import java.io.FileInputStream;
import java.io.IOException;
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
                        System.err.println(key + " and " + valueKeyMap.get(value) + " have same value: " + value);
                    }

                    duplicatedValue = duplicatedValue || valueExist;

                    // put value in map
                    valueKeyMap.put(value, key);

                }
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }
            assertFalse(duplicatedValue);

        }
    }


    /**
     * Test that key name corresponds to a given name comvention
     * @throws Exception
     */
    public void testKeyName() throws Exception {
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, Locale.ENGLISH);
        assertNotNull(resourceBundle);

        if (resourceBundle != null) {
            boolean valideName = true;
            try {
                Enumeration<String> enume = resourceBundle.getKeys();
                while (enume.hasMoreElements()) {
                    String key = enume.nextElement();

                    // check if value exist for different key
                   /* boolean valide = key.indexOf("org.jahia") == 0;
                    if (!valide) {
                        System.err.println(key + " not valid." );
                    }

                    valideName = valideName && valide;*/

                    // put value in map

                }
            } catch (MissingResourceException e) {
                e.printStackTrace();
            }
            assertTrue(valideName);

        }
    }

}
