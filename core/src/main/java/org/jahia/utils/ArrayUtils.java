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
package org.jahia.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Convenient array utility methods.
 * 
 * @author Benjamin Papez
 */
public final class ArrayUtils {

    public static <T> T[] join(T[]... arrays) {
        // calculate size of target array
        int size = 0;
        for (T[] array : arrays) {
            size += array.length;
        }

        // create list of appropriate size
        List<T> list = new ArrayList<T>(size);
        Class<?> componentType = null;
        // add arrays
        for (T[] array : arrays) {
            Collections.addAll(list, array);
            if (componentType == null) {
                componentType = array.getClass().getComponentType();
            }
        }

        // create and return final array
        return list.toArray((T[])Array.newInstance(componentType, size));
    }

}
