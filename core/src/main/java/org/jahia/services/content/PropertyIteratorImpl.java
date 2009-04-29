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
package org.jahia.services.content;

import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 17 d�c. 2007
 * Time: 10:09:23
 * To change this template use File | Settings | File Templates.
 */
public class PropertyIteratorImpl extends RangeIteratorImpl implements PropertyIterator {

    public PropertyIteratorImpl(Iterator iterator, long size) {
        super(iterator, size);
    }

    public Property nextProperty() {
        return (Property) next();
    }
}
