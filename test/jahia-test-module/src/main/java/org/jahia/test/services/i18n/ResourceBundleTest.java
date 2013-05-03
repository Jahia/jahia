/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for the resource bundle loading. 
 * User: ktlili
 * Date: Jan 25, 2010
 * Time: 12:35:43 PM
 */
public class ResourceBundleTest {
    private static Logger logger = LoggerFactory.getLogger(ResourceBundleTest.class);
    
    @Test
    public void lookupBundleTest() {
        JahiaTemplatesPackage pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackageByFileName("templates-web-space");
        String primary = pkg.getResourceBundleName();
        // Lookup a key that is present directly in the JahiaWebTemplatesSpace.properties
        testResource(primary, pkg, "jmix_skinnable.j_skin.skins.acmebox3", Locale.ENGLISH, "ACME Box 3 Plain ");
        // Lookup a key which is not present in the JahiaWebTemplatesSpace.properties but is present in one of the RBs of dependent modules
        testResource(primary, pkg, "jmix_skinnable.j_skin.skins.box2", Locale.ENGLISH,
                "Border, light title, light content");
        // Lookup a key that is only present in the DefaultJahiaTemplates.properties
        testResource(primary, pkg, "jnt_displayMetadata.categories", Locale.ENGLISH, "Display the categories");
        // Lookup a key that is only present in the JahiaTypesResources.properties
        testResource(primary, pkg, "jmix_contentmetadata.j_lastPublishingDate", Locale.ENGLISH, "Last publication");
        // Lookup a key that is only present in the JahiaInternalResources.properties
        testResource(primary, pkg, "column.modifiedBy.label", Locale.ENGLISH, "Modified by");
        // Lookup a key that is not present anywhere
        testResource(primary, pkg, "dummy.column.modifiedBy.label", Locale.ENGLISH, "notFound");
        // another locale, Lookup a key which is not present in the JahiaWebTemplatesSpace.properties but is present in one of the RBs of
        // dependent modules
        testResource(primary, pkg, "jmix_skinnable.j_skin.skins.box2", Locale.FRENCH,
                "Avec cadre, fond de titre clair, fond du corps clair");
        // test resource overriding from a templatesSet (dummy3 is the templatesSet)
        testResource(
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName("dummy3")
                        .getResourceBundleName(), ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                        .getTemplatePackageByFileName("dummy2"), "test", Locale.ENGLISH, "test from dummy3");

    }

    /**
     * Unit test for resource resolution with bundle hierarchy
     */
    @Test
    public void lookupBundleTestLegacy() {
        String lookupModuleName= "Jahia Web Templates Space";
        String siteTemplatesPackageName= "Jahia Web Templates Space";
        // Lookup a key that is present directly in the JahiaWebTemplatesSpace.properties
        testResourceLegacy("jmix_skinnable.j_skin.skins.acmebox3","ACME Box 3 Plain ",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // Lookup a key which is not present in the JahiaWebTemplatesSpace.properties but is present in one of the RBs of dependent modules
        testResourceLegacy("jmix_skinnable.j_skin.skins.box2","Border, light title, light content",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // Lookup a key that is only present in the DefaultJahiaTemplates.properties
        testResourceLegacy("jnt_displayMetadata.categories","Display the categories",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // Lookup a key that is only present in the JahiaTypesResources.properties
        testResourceLegacy("jmix_contentmetadata.j_lastPublishingDate","Last publication",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // Lookup a key that is only present in the JahiaInternalResources.properties
        testResourceLegacy("column.modifiedBy.label","Modified by",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // Lookup a key that is not present anywhere
        testResourceLegacy("dummy.column.modifiedBy.label","notFound",lookupModuleName,Locale.ENGLISH,siteTemplatesPackageName);
        // another locale, Lookup a key which is not present in the JahiaWebTemplatesSpace.properties but is present in one of the RBs of dependent modules
        testResourceLegacy("jmix_skinnable.j_skin.skins.box2","Avec cadre, fond de titre clair, fond du corps clair",lookupModuleName,Locale.FRENCH,siteTemplatesPackageName);
        // test resource overriding from a templatesSet (dummy3 is the templatesSet)
        testResourceLegacy("test","test from dummy3","dummy2",Locale.ENGLISH,"dummy3");

    }

    private void testResource(String primaryBundleName, JahiaTemplatesPackage pkg, String searchedKey, Locale locale,
            String expectedResult) {
        String result = Messages.get(primaryBundleName, pkg, searchedKey, locale, "notFound");
        assertEquals("looking for \"" + searchedKey + "\" in Jahia Web Templates but found \"" + result
                + "\" instead of \"" + expectedResult + "\"", expectedResult, result);
    }

    private void testResourceLegacy(String searchedKey, String expectedResult, String modulePackageName, Locale locale, String siteTemplatesPackageName) {
        String notFound = "notFound";
        JahiaResourceBundle moduleResource = new JahiaResourceBundle(locale, modulePackageName, siteTemplatesPackageName);
        String result = moduleResource.get(searchedKey,notFound);
        assertEquals("looking for \""+ searchedKey + "\" in Jahia Web Templates (" + modulePackageName +") but found \""+ result +"\" instead of \"" + expectedResult + "\"",expectedResult,result);
    }

    /**
     * Test that the value is unique and doesn't correspond to several keys
     *
     * @throws Exception
     */

    @Test
    public void testUniqueValue() throws Exception {
        final ResourceBundle resourceBundle = ResourceBundles.getInternal(Locale.ENGLISH);
        assertNotNull(resourceBundle);

        if (resourceBundle != null) {
            boolean duplicatedValue = false;
            final Map<String, String> valueKeyMap = new HashMap<String, String>();
            Enumeration<String> enume = resourceBundle.getKeys();
            while (enume.hasMoreElements()) {
                String key = enume.nextElement();
                String value = resourceBundle.getString(key);

                // check if value exist for different key
                boolean valueExist = valueKeyMap.containsKey(value);
                if (valueExist) {
                    logger.error("Duplicated value found in JahiaInternalResources_en.properties: "
                            + key
                            + " = "
                            + value
                            + " and "
                            + valueKeyMap.get(value) + " = " + value);
                } else {
                    // put value in map
                    valueKeyMap.put(value, key);
                }

                duplicatedValue = duplicatedValue || valueExist;
            }
            assertFalse(duplicatedValue);
        }
    }

}
