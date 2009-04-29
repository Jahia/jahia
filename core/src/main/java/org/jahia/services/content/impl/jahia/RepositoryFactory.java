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
package org.jahia.services.content.impl.jahia;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 17 déc. 2007
 * Time: 18:36:19
 * To change this template use File | Settings | File Templates.
 */
public class RepositoryFactory implements ObjectFactory {
    public Object getObjectInstance(Object obj, Name name, Context nameCtx,
                                    Hashtable environment)
            throws Exception {

        return new RepositoryImpl();
    }


}
