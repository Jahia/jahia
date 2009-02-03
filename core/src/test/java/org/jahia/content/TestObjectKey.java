/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.content;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 23 mars 2007
 * Time: 09:23:32
 * To change this template use File | Settings | File Templates.
 */
public class TestObjectKey extends TestCase {
    private static final int INSTANCE_LOOP_COUNT = 100000;

    public void testGetInstanceSpeed() {
        // we do the following to register the classes before calling ObjectKey
        ContentPageKey pageKey = new ContentPageKey(1);
        ContentContainerListKey containerListKey = new ContentContainerListKey(1);
        ContentContainerKey containerKey = new ContentContainerKey(1);
        ContentFieldKey fieldKey = new ContentFieldKey(1);
        ContentMetadataKey metadataKey = new ContentMetadataKey(1);

        CategoryKey categoryKey = new CategoryKey(1);

        FieldDefinitionKey fieldDefKey = new FieldDefinitionKey(1);
        ContainerDefinitionKey containerDefKey = new ContainerDefinitionKey(1);
        PageDefinitionKey pageDefKey = new PageDefinitionKey(1);

        // now let's run the speed test.
        long startTime = System.currentTimeMillis();
        for (int i=0; i < INSTANCE_LOOP_COUNT; i++) {
            try {
                ObjectKey.getInstance(ContentPageKey.PAGE_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(ContentContainerListKey.CONTAINERLIST_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(ContentContainerKey.CONTAINER_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(ContentFieldKey.FIELD_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(ContentMetadataKey.METADATA_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i) );

                ObjectKey.getInstance(CategoryKey.CATEGORY_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));

                ObjectKey.getInstance(FieldDefinitionKey.FIELD_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(ContainerDefinitionKey.CONTAINER_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
                ObjectKey.getInstance(PageDefinitionKey.PAGE_TYPE + ObjectKey.KEY_SEPARATOR + Integer.toString(i));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        long stopTime = System.currentTimeMillis();
        long totalTime = stopTime - startTime;
        System.out.println("Generated " + INSTANCE_LOOP_COUNT*9 + " ObjectKey instances in " + totalTime + "ms");
    }

    public void testConverters() throws ClassNotFoundException {
        ContentPageKey pageKey = new ContentPageKey(1);
        assertEquals(ObjectKey.getInstance(pageKey.getKey()).getKey(), ObjectKey.toObjectKeyString(pageKey.getType(), pageKey.getIDInType()));
        assertEquals(ContentPageKey.toObjectKeyString(pageKey.getIDInType()), ObjectKey.toObjectKeyString(pageKey.getType(), pageKey.getIDInType()));
    }

    public void testChildInstance() {
        // compatibility check, to see if deprecated API still works properly.
        ObjectKey fieldKey = ContentFieldKey.getChildInstance("1");
        assertEquals(fieldKey.getIDInType(), "1");
        ObjectKey containerKey = ContentContainerKey.getChildInstance("1");
        assertEquals(containerKey.getIDInType(), "1");
        ObjectKey containerListKey = ContentContainerListKey.getChildInstance("1");
        assertEquals(containerListKey.getIDInType(), "1");
        ObjectKey pageKey = ContentPageKey.getChildInstance("1");
        assertEquals(pageKey.getIDInType(), "1");        
        ObjectKey metadataKey = ContentMetadataKey.getChildInstance("1");
        assertEquals(metadataKey.getIDInType(), "1");

        ObjectKey categoryKey = CategoryKey.getChildInstance("1");
        assertEquals(categoryKey.getIDInType(), "1");

        ObjectKey fieldDefinitionKey = FieldDefinitionKey.getChildInstance("1");
        assertEquals(fieldDefinitionKey.getIDInType(), "1");
        ObjectKey containerDefinitionKey = ContainerDefinitionKey.getChildInstance("1");
        assertEquals(containerDefinitionKey.getIDInType(), "1");
        ObjectKey pageDefinitionKey = PageDefinitionKey.getChildInstance("1");
        assertEquals(pageDefinitionKey.getIDInType(), "1");
    }
}
