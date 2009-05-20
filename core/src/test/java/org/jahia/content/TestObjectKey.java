/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
